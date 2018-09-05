package org.silver.util;

import java.util.Random;
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
		if (StringEmptyUtils.isEmpty(strName)) {
			return false;
		}
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
	 * 
	 * @param str
	 *            字符串
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

	/**
	 * 通用替换多表查询字符串
	 * <li>例:{\"value\":919.04}、{\"value\":\"深圳市前海爱库\"}</li>
	 * <li>返回：深圳市前海爱库</li>
	 * 
	 * @param str
	 * @return
	 */
	public static String replace(String str) {
		if (StringEmptyUtils.isEmpty(str)) {
			return "";
		}
		//
		str = str.replace("{\"value\":", "");
		str = str.replace("\"", "");
		str = str.replace("}", "");
		return str;
	}

	/**
	 * 要求产生字符串的长度
	 * 
	 * @param length
	 *            字符串长度
	 * @return String
	 */
	public static String getRandomString(int length) {
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(62);
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String str = "{\"value\":919.04}";
		String str2 = "{\"value\":\"深圳市前海爱库\"}";

		// System.out.println(StringUtil.addPrefixZero("2356", 8));
		System.out.println("--->>>>" + isContainChinese("娜地拉·艾孜拉提".replace("·", "")));
		// System.out.println(isNumeric("1中"));

		System.out.println("-=--=->>>" + reservedInitialsReplaceOther("艾孜拉提"));
	}

	/**
	 * 保留首字母，剩余的全部替换为"*"
	 * @param string 需要替换的字符串
	 * @return String 替换后的字符串
	 */
	public static String reservedInitialsReplaceOther(String string) {
		if (StringEmptyUtils.isEmpty(string)) {
			return "";
		}
		StringBuilder sb = new StringBuilder(string.substring(1, string.length()));
		String x = "";
		for (int i = 0; i < sb.length(); i++) {
			x += "*";
		}
		return string.charAt(0) + "" + sb.replace(0, sb.length(), x);
	}
}
