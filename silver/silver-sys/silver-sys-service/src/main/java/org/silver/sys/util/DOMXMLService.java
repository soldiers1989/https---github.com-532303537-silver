package org.silver.sys.util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.silver.sys.dao.OrderRecordDao;
import org.silver.sys.model.Head;
import org.silver.sys.model.order.OrderRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * xml报文解析
 * 
 * @author zhangxin 2016/8/5
 * 
 */
public class DOMXMLService {

	@Resource
	private OrderRecordDao orderRecordDao;
	
	public static Map<String, Object> getHeadBeanList(File in) throws Exception {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(in);
			Element root = document.getDocumentElement(); // 获取根节点
			System.out.println("根元素:" + root.getNodeName());
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node headBeanElement = nodes.item(i);
				// 遍历headBean孩子节点，注意这里孩子节点包括那些空格和换行（它们是文本节点）
				if ("Head".equals(headBeanElement.getNodeName())) {
					NodeList nodeDetail = headBeanElement.getChildNodes();
					//解析回执报文头信息
					Map<String, Object> map1=xmlElementChager(nodeDetail);
					statusMap.putAll(map1);
				} else if ("Declaration".equals(headBeanElement.getNodeName())) {
					NodeList nodeDetail1 = headBeanElement.getChildNodes();
					//解析报文回执状态
					Map<String, Object> map2=xmlElementChager(nodeDetail1);
					statusMap.putAll(map2);
					for (int k = 0; k < nodeDetail1.getLength(); k++) {
						Node declarationElement = nodeDetail1.item(k);
						if ("GoodsRegRecList".equals(declarationElement.getNodeName())) {
							NodeList enodeDetail = declarationElement.getChildNodes();
							//商品备案回执状态
							Map<String, Object> map3=xmlElementChager(enodeDetail);
							statusMap.putAll(map3);
						}
					}
				}
			}
			statusMap.put("dom_status", 1);
			statusMap.put("msg", "报文解析成功 ");
		} catch (Exception e) {
			e.printStackTrace();
			statusMap.put("dom_status", -1);
			statusMap.put("msg", "报文解析失败 ");
			return statusMap;
		}
		return statusMap;

	}
	/**
	 * 将报文解析的子节点内容，放在一个MAP里
	 * @param nodeList   报文子节点List
	 * @return
	 */
	private static Map<String, Object> xmlElementChager(NodeList nodeList) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (!nodeList.item(i).getNodeName().equals("#text")) {
				System.out.println(nodeList.item(i).getNodeName() + "----->" + nodeList.item(i).getFirstChild().getNodeValue());
				resultMap.put(nodeList.item(i).getNodeName(), nodeList.item(i).getFirstChild().getNodeValue());
			}
		}
		return resultMap;
	}	
	
	private  Map<String, Object> findEntByTypeAndNo(Map<String, Object> map) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		String type =(String) map.get("OrgMessageType");
		String orgMessageID=(String) map.get("OrgMessageID");
		String respondBy=(String) map.get("RespondBy");
		params.put("OrgMessageID", orgMessageID);
		params.put("del_flag", 0);
		if(type.equals("KJ881111")){
			List<OrderRecord> list=orderRecordDao.findByProperty(params, 0, 0);
			OrderRecord orderRecord =new OrderRecord();
			if(respondBy.equals("01")){//电子口岸回执
				for (int i = 0; i < list.size(); i++) {
					orderRecord=list.get(i);
//					orderRecord.setStatus(map.get("status")+"");
					orderRecord.setRemarks(map.get("RespondNotes")+"");
					if(orderRecordDao.update(orderRecord)){
						resultMap.put("status", 1);
						resultMap.put("msg", "更新回执状态成功 ");
					}else{
						
					}
				}
			}
			if(respondBy.equals("02")){//海关回执
				for (int i = 0; i < list.size(); i++) {
					orderRecord=list.get(i);
					orderRecord.setCusStatus(map.get("status")+"");
					orderRecord.setCusNotes(map.get("RespondNotes")+"");
					if(orderRecordDao.update(orderRecord)){
						resultMap.put("status", 1);
						resultMap.put("msg", "更新回执状态成功 ");
					}
				}
			}
			if(respondBy.equals("03")){//检验检疫回执
				for (int i = 0; i < list.size(); i++) {
					orderRecord=list.get(i);
					orderRecord.setCiqStatus(map.get("status")+"");
					orderRecord.setCiqNotes(map.get("RespondNotes")+"");
					if(orderRecordDao.update(orderRecord)){
						resultMap.put("status", 1);
						resultMap.put("msg", "更新回执状态成功 ");
					}
				}
			}
		}
		return resultMap;
		
	}
	
}
