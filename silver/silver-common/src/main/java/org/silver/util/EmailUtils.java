package org.silver.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 邮箱工具类
 */
public class EmailUtils {
	
	/**
	 * 校验邮箱
	 * @param email 邮箱
	 * @return Boolean 
	 */
	public static boolean checkEmail(String email) {
		if (StringEmptyUtils.isEmpty(email)){
			return false;
		}
		String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern p;
		Matcher m;
		p = Pattern.compile(regEx1);
		m = p.matcher(email);
		return m.matches();
	}
}
