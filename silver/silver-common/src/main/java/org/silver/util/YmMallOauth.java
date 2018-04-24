package org.silver.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.silver.util.MD5;
import org.silver.util.YmHttpUtil;

import net.sf.json.JSONObject;

/**
 * 银盟商城实名认证（正式账号）
 * 
 * @author Administrator
 *
 */
public class YmMallOauth {
	private static final String terminalID = "21000441";
	private static final String key = "077536";
	private static final String factoryID = "GZYM";
	private static final String oauthURL = "http://hepay.eptok.com:9800/ysInterfaceServe/identityAuthentication.cgi";

	/**
	 * 
	 * @param termTransID
	 *            交易批次号，应用平台保持唯一
	 * @param productType
	 *            YS02 二要素 YS03 三要素 YS04四要素
	 * @param bankAccountNo
	 *            银行卡号
	 * @param bankAccountName
	 *            姓名
	 * @param idNo
	 *            身份证号
	 * @param bankMobile
	 *            手机号
	 * @param extend
	 *            补充字段 可空
	 * @return
	 */
	public Map<String, Object> oauthInfo(String termTransID, String productType, String bankAccountNo,
			String bankAccountName, String idNo, String bankMobile, String extend) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new LinkedHashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		if (bankAccountNo == null) {
			bankAccountNo = "";
		}
		if (bankMobile == null) {
			bankMobile = "";
		}
		params.put("terminalID", terminalID);
		params.put("factoryID", factoryID);
		params.put("reqDateTime", sdf.format(new Date()));
		params.put("termTransID", termTransID);
		params.put("productType", productType);
		params.put("bankAccountNo", bankAccountNo);
		params.put("bankAccountName", bankAccountName);
		params.put("idNo", idNo);
		params.put("bankMobile", bankMobile);
		params.put("extend", extend);
		params.put("key", key);
		String postStr = "", sign = "";
		try {
			postStr = YmMallOauth.getPostStr(params);
			postStr = postStr.substring(0, postStr.length() - 1);
			sign = MD5.getMD5(postStr.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
			statusMap.put("-5", "系统内部错误");
			statusMap.put("msg", e.getMessage());
			return statusMap;
		}
		params.put("sign", sign);
		String result = YmHttpUtil.HttpPost(oauthURL, params);
		if (result != null) {
			JSONObject obj = null;
			try {
				obj = JSONObject.fromObject(result);
			} catch (Exception e) {
				e.printStackTrace();
				statusMap.put("status", -5);
				statusMap.put("msg", "系统内部错误，认证出错");
				return statusMap;
			}
			if ("0000".equals((obj.get("status") + "").trim())) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("实名认证不通过，请核实身份信息!");
		}
		return ReturnInfoUtils.errorInfo("实名认证失败,网络异常!");
	}

	public static String getPostStr(Map<String, Object> params) throws Exception {
		StringBuffer sb = new StringBuffer();
		if (params != null) {
			for (Entry<String, Object> e : params.entrySet()) {
				String type = e.getValue().getClass().toString();
				if ("class java.util.ArrayList".equals(type)) {
					List templist = (List) e.getValue();
					if (templist.size() > 0) {
						for (int i = 0; i < templist.size(); i++) {
							sb.append(e.getKey());
							sb.append("=");
							sb.append(templist.get(i).toString());
							sb.append("&");
						}
					}
				} else {
					sb.append(e.getKey());
					sb.append("=");
					sb.append(e.getValue().toString());
					sb.append("&");
				}
			}
		}
		return sb.toString().trim();
	}

	public static void main(String[] args) throws Exception {
		YmMallOauth oau = new YmMallOauth();
		//System.out.println(oau.oauthInfo("0001", "YS02", "", "", "", "", ""));
	}
}
