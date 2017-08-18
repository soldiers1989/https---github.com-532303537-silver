package org.silver.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.UUID;

public class AppUtil {

	public static String generateAppKey() {
		UUID uuid = UUID.randomUUID();
		String appKey = uuid.toString().replace("-", "");
		return appKey.trim();
	}

	public static String generateAppSecret() {
		String a = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String appSecret = "";
		char[] rands = new char[64];
		for (int i = 0; i < rands.length; i++) {
			int rand = (int) (Math.random() * a.length());
			rands[i] = a.charAt(rand);
		}
		List<String> l = new ArrayList<String>();
		for (int i = 0; i < rands.length; i++) {
			l.add(String.valueOf(rands[i]));
		}
		String ss = l.toString();
		appSecret = ss.replace("[", "").replace("]", "").replace(",", "").replace(" ", "");
		return appSecret.trim();
	}

	
	public static String generateYMAccessToken(){
		return "Ym_"+AppUtil.generateAppSecret()+"_"+AppUtil.generateAppSecret();
	}
	
	/**
	 * 
	 * @param systemTime 当前系统时间戳
	 * @param clientTime 客户端时间戳
	 * @param max_allow  允许的最大误差时间 毫秒
	 * @return
	 */
	public static boolean checkTimestamp(long systemTime,String clientTime,long max_allow){
		if(clientTime.length()!=13){
			return false;
		}
		long time=0,sys=System.currentTimeMillis();;
		try{
			time=Long.parseLong(clientTime);
			if(!((sys-time)<=max_allow&&(time-sys)<=max_allow)){
				return false;
			}
		}catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
/*	public static Map<String, Object> sign(String appKey, String appSecret) {
		Map<String, Object> signMap = new HashMap<String, Object>();
		String timestamp = System.currentTimeMillis() + "";
		String str = appKey + appSecret + timestamp;
		String clientSign = MD5Util.encode(str, timestamp);
		signMap.put("appKey", appKey);
		signMap.put("timestamp", timestamp);
		signMap.put("clientSign", clientSign);
		return signMap;

	}*/

	public static String toUnicode(String str) {
		char[] arChar = str.toCharArray();
		int iValue = 0;
		String uStr = "";
		for (int i = 0; i < arChar.length; i++) {
			iValue = (int) str.charAt(i);
			if (iValue <= 256) {
				// uStr+="& "+Integer.toHexString(iValue)+";";
				uStr += "\\" + Integer.toHexString(iValue);
			} else {
				// uStr+="&#x"+Integer.toHexString(iValue)+";";
				uStr += "\\u" + Integer.toHexString(iValue);
			}
		}
		return uStr;
	}

	public static String unicodeToGB(String s) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(s, "\\u");
		while (st.hasMoreTokens()) {
			sb.append((char) Integer.parseInt(st.nextToken(), 16));
		}
		return sb.toString();
	}

	public static String toUnicodeString(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0 && c <= 255) {
				sb.append(c);
			} else {
				sb.append("\\u" + Integer.toHexString(c));
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		//  httpClient = new AsyncHttpClient();
	}
}
