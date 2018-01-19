package org.silver.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alvis
 *
 * @version 2016年9月20日 下午1:53:31
 */
public class StringUtil {

	public static String addPrefixZero(String val, Integer len) {
		if (val == null || "".equals(val) || val.length() >= len) {
			return val;
		}
		StringBuffer prefix = new StringBuffer("");
		int i = len - val.length();
		for (; i >= 1; i--) {
			prefix.append("0");
		}
		return prefix.toString() + val;
	}

	/**
	 * 根据Unicode编码完美的判断中文汉字和符号
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	/**
	 * 完整的判断中文汉字和符号
	 * 
	 * @param strName
	 * @return
	 */
	public static boolean isChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断字符串是否全是中文
	 * 
	 * @param str
	 *            待校验字符串
	 * @return 是否为中文
	 * @warn 不能校验是否为中文标点符号
	 */
	public static boolean isContainChinese(String str) {
		if (StringEmptyUtils.isEmpty(str)) {
			return false;
		}
		String reg = "[\\u4e00-\\u9fa5]+";
		return str.trim().matches(reg);
	}

	/**
	 * 判断字符串全是数字
	 * @param str 字符串
	 * @return boolean
	 */
	public static boolean isNumeric(String str) {
		if (StringEmptyUtils.isEmpty(str)) {
			return false;
		}
		str = str.trim();
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		// System.out.println(StringUtil.addPrefixZero("2356", 8));
		System.out.println(isContainChinese("鵼隻"));
		//System.out.println(isNumeric("1中"));
	}
}
