package org.silver.util;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 发送手机验证码
 */
public class SendMsg {
	private static final String SMS_URL = "http://sms.3etone.com/sms.aspx";
	private static final String ACTION = "send";
	private static final String USERID = "850";
	private static final String ACCOUNT = "hxp";
	private static final String PASSWORD = "hxp123456";

	static String getSendTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.format(new Date());
	}

	/**
	 * 
	 * @param action
	 *            send
	 * @param userid
	 *            企业id
	 * @param account
	 *            发送账户
	 * @param password
	 *            密码
	 * @param mobile
	 *            发送给手机号，多个用逗号隔开
	 * @param content
	 *            发送内容
	 * @param sendTime
	 *            发送时间 列 2017-10-01 09:00:00
	 * @param checkContent
	 *            1检查 0不检查
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Map<String, Object> sendMsg(String mobile, String content)
			throws ParserConfigurationException, SAXException, IOException {
		Map<String, Object> params = new HashMap<>();
		params.put("action", ACTION);
		params.put("userid", USERID);
		params.put("account", ACCOUNT);
		params.put("password", PASSWORD);
		params.put("mobile", mobile);
		params.put("content", content);
		params.put("sendTime", getSendTime());
		params.put("checkcontent", 0);
		String result = YmHttpUtil.HttpPost(SMS_URL, params);
		if (result != null && !"".equals(result)) {
			return xmlElementChager(result);
		}
		return null;
	}

	private static Map<String, Object> xmlElementChager(String xmlStr)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xmlStr)));
		Element root = document.getDocumentElement();
		NodeList nodeList = root.getChildNodes();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (!nodeList.item(i).getNodeName().equals("#text")) {
				if (nodeList.item(i).hasChildNodes()) {
					NodeList nlist = nodeList.item(i).getChildNodes();
					Map<String, Object> nodeMap = new HashMap<String, Object>();
					for (int n = 0; n < nlist.getLength(); n++) {
						if (!nlist.item(n).getNodeName().equals("#text"))
							nodeMap.put(nlist.item(n).getNodeName(), nlist.item(n).getFirstChild().getNodeValue());
					}
					resultMap.put(nodeList.item(i).getNodeName(), nodeMap);
				} else {
					resultMap.put(nodeList.item(i).getNodeName(), nodeList.item(i).getFirstChild().getNodeValue());
				}
			}
		}
		return resultMap;
	}

	public static void main(String[] args) {
		try {
			System.out.println(SendMsg.sendMsg("13533527688", "【广州银盟信息科技有限公司】验证码为"));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
}
