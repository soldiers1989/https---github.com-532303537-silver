package org.silver.util;

/**
 * @author Alvis
 *
 * @version 2016年9月20日 下午1:53:31
 */
public class StringUtil {

	public static String addPrefixZero(String val,Integer len){
		if(val == null || "".equals(val) || val.length() >= len){
			return val;
		}
		StringBuffer prefix = new StringBuffer("");
		int i = len - val.length();
		for(;i>= 1;i--){
			prefix.append("0");
		}
		return prefix.toString()+val;
	}
	
	public static void main(String[] args) {
		System.out.println(StringUtil.addPrefixZero("2356", 6));
	}
}
