package org.silver.shop.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.shop.config.FenZhangConfig;


/**
 * 针对银盛分配的(银盟)1119账号进行对应的加密体检
 */
public class PaySubmitUtils {

	private static Logger logger = LogManager.getLogger(Object.class);

	/**
	 * api请求的签名工具方法，把请求参数按照字符排序拼接，然后进行RAS加密 私钥用来签名
	 * 
	 * @param request
	 * @param sParaTemp
	 * @return
	 * @see
	 */
	public static String sign1(HttpServletRequest request, Map<String, String> sParaTemp) {
		Map<String, String> sPara = FenZhangSignUtils.paraFilter(sParaTemp);
		ServletContext servletContext = request.getServletContext();
		String partnerId = sParaTemp.get("partner_id");
		String partnerCert = FenZhangConfig.PATH_PARTER_PKCS12;
		if (partnerId.endsWith(FenZhangConfig.PLATFORM_PARTNER_NO)) {
			partnerCert = FenZhangConfig.PATH_PARTER_PKCS12;
		}
		String realPath = servletContext.getRealPath("/") + "WEB-INF/classes/credentials";
		InputStream pfxCertFileInputStream = null;
		File f = new File(realPath + partnerCert);
		try {
			pfxCertFileInputStream = new FileInputStream(f);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		String signResult = "";
		try {
			signResult = FenZhangSignUtils.rsaSign(sPara, sParaTemp.get("charset"), pfxCertFileInputStream);
		} catch (Exception e) {
			throw new RuntimeException("签名失败，请检查证书文件是否存在，密码是否正确");
		}

		return signResult;
	}

	/**
	 * API验证签名工具，把签名值，请求字符编码，返回结果(json body)传递过来进行验证签名 公钥验证签名
	 * 
	 * @param request
	 * @param sign
	 * @param responseBody
	 * @param charset
	 * @return
	 * @see
	 */
	public static boolean verifyJsonSign(HttpServletRequest request, String sign, String responseBody, String charset) {
		ServletContext servletContext = request.getServletContext();
		String realPath = servletContext.getRealPath("/") + "WEB-INF/classes/credentials";
		InputStream publicCertFileInputStream = null;
		try {
			File f = new File(realPath + FenZhangConfig.PATH_YSEPAY_PUBLIC_CERT);
			publicCertFileInputStream = new FileInputStream(f);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		boolean isSign = false;
		try {
			isSign = FenZhangSignUtils.rsaCheckContent(publicCertFileInputStream, responseBody, sign, charset);
		} catch (Exception e) {
			// throw new RuntimeException("验证签名失败，请检查银盛公钥证书文件是否存在");
			logger.info("验证签名失败，请检查银盛公钥证书文件是否存在");
		}
		return isSign;
	}

	/**
	 * 拼接请求网关参数
	 * 
	 * @param request
	 * @param sParaTemp
	 * @param strMethod
	 * @param strButtonName
	 * @return
	 */
	public static String buildRequest(HttpServletRequest request, Map<String, String> sParaTemp, String strMethod,
			String strButtonName) {
		Map<String, String> sPara = buildRequestPara(request, sParaTemp);
		List<String> keys = new ArrayList<>(sPara.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id=\"ysepaysubmit\" name=\"ysepaysubmit\" action=\"" + FenZhangConfig.YSEPAY_GATEWAY_URL
				+ "\" method =\"" + strMethod + "\" style=\"display:none\">");

		for (int i = 0; i < keys.size(); i++) {
			String name = keys.get(i);
			String value = sPara.get(name);
			sbHtml.append("<input type=\"text\" name=\"" + name + "\" value=\"" + StringEscapeUtils.escapeHtml(value)
					+ "\"/><br/>");
		}

		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display;\"></form>");
		sbHtml.append("<script>document.forms['ysepaysubmit'].submit();</script>");
		logger.debug("发送报文为：" + sbHtml);
		return sbHtml.toString();
	}

	/**
	 * 拼接请求代付加急参数
	 * 
	 * @param request
	 * @param sParaTemp
	 * @return
	 */
	public static String buildRequestdf(HttpServletRequest request, Map<String, String> sParaTemp, String strMethod,
			String strButtonName) {
		Map<String, String> sPara = buildRequestPara(request, sParaTemp);
		List<String> keys = new ArrayList<String>(sPara.keySet());
		StringBuffer sbHtml = new StringBuffer();
		sbHtml.append("正在跳转。。。<br/>" + "<form id=\"ysepaysubmit\" name=\"ysepaysubmit\" action=\""
				+ FenZhangConfig.YSEPAY_GATEWAY_URL_DF + "\" method = \"" + strMethod + "\">");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);

			sbHtml.append("<input type=\"text\" name=\"" + name + "\" value=\"" + StringEscapeUtils.escapeHtml(value)
					+ "\"/><br/>");
		}

		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display;\"></form>");
		sbHtml.append("<script>document.forms['ysepaysubmit'].submit();</script>");

		return sbHtml.toString();
	}

	private static Map<String, String> buildRequestPara(HttpServletRequest request, Map<String, String> sParaTemp) {
		Map<String, String> sPara = FenZhangSignUtils.paraFilter(sParaTemp);
		ServletContext servletContext = request.getServletContext();
		String partnerId = sParaTemp.get("partner_id");
		String partnerCert = FenZhangConfig.PATH_PARTER_PKCS12;
		if (partnerId.endsWith(FenZhangConfig.PLATFORM_PARTNER_NO)) {
			partnerCert = FenZhangConfig.PATH_PARTER_PKCS12;
		}
		String realPath = servletContext.getRealPath("/") + "WEB-INF/classes/credentials";
		InputStream pfxCertFileInputStream = null;
		try {
			File f = new File(realPath + partnerCert);
			pfxCertFileInputStream = new FileInputStream(f);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// InputStream pfxCertFileInputStream =
		// servletContext.getResourceAsStream(partnerCert);
		String mysign = "";
		try {
			mysign = FenZhangSignUtils.rsaSign(sPara, sParaTemp.get("charset"), pfxCertFileInputStream);
		} catch (Exception e) {
			throw new RuntimeException("签名失败，请检查证书文件是否存在，密码是否正确");
		}

		sPara.put("sign", mysign);

		return sPara;
	}

	/**
	 * 异步验证签名
	 * 
	 * @param request
	 * @param params
	 * @return
	 */
	public static boolean verifySign(HttpServletRequest request, Map<String, String> params) {
		ServletContext servletContext = request.getServletContext();
		String realPath = servletContext.getRealPath("/") + "WEB-INF/classes/credentials";
		InputStream publicCertFileInputStream = null;
		try {
			File f = new File(realPath + FenZhangConfig.PATH_YSEPAY_PUBLIC_CERT);
			System.out.println(f.exists() + "文件存在");
			publicCertFileInputStream = new FileInputStream(f);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String sign = "";
		if (params.get("sign") != null) {
			sign = params.get("sign");
		}
		boolean isSign = false;
		try {
			isSign = FenZhangSignUtils.rsaCheckContent(publicCertFileInputStream, params, sign, FenZhangConfig.DEFAULT_CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
			// throw new RuntimeException("验证签名失败，请检查银盛公钥证书文件是否存在");
			logger.error("验证签名失败，请检查银盛公钥证书文件是否存在");
		}

		return isSign;
	}

	/**
	 * 产生订单号
	 */
	public static String produceOrderNo() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();
		String str1 = df.format(d).toString().replaceAll("-", "");
		String str2 = "";
		for (int i = 0; i < 10; i++) {
			str2 += (int) (Math.random() * 10);
		}
		String str3 = str1 + str2;
		return str3;
	}

	public static String signWithFilepath(String realPath, Map<String, String> sParaTemp) {
		Map<String, String> sPara = FenZhangSignUtils.paraFilter(sParaTemp);
		String partnerId = sParaTemp.get("partner_id");
		String partnerCert = FenZhangConfig.PATH_PARTER_PKCS12;
		if (partnerId.endsWith(FenZhangConfig.PLATFORM_PARTNER_NO)) {
			partnerCert = FenZhangConfig.PATH_PARTER_PKCS12;
		}
		InputStream pfxCertFileInputStream = null;
		File f = new File(realPath + partnerCert);
		try {
			pfxCertFileInputStream = new FileInputStream(f);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String signResult = "";
		try {
			signResult = FenZhangSignUtils.rsaSign(sPara, sParaTemp.get("charset"), pfxCertFileInputStream);
		} catch (Exception e) {
			throw new RuntimeException("签名失败，请检查证书文件是否存在，密码是否正确");
		}

		return signResult;
	}
}
