package org.silver.util;

import java.util.HashMap;
import java.util.Map;

public class StringOrderUtil {
	
	/**
	 * 判断一个字符串是一个字符串打乱顺序后的字符串
	 * @param source 源字符串
	 * @param target 目标字符串
	 * @return boolean
	 */
	public static boolean isScrambledString(String source, String target) {
		if (source == null || target == null) {
			throw new IllegalArgumentException("source or target is null.");
		}

		if (source.length() != target.length()) {
			System.out.println("target string's length is not equal to source length.");
			return false;
		}

		// 目标串中每个字符都包含在原串中
		int length = source.length();
		Map<Character, Integer> targetCount = new HashMap<Character, Integer>();
		for (int i = 0; i < length; i++) {
			char c = target.charAt(i);
			// target中某个字符不在原串中，返回false
			int indexOfSource = source.indexOf(c);
			if (indexOfSource == -1) {
				return false;
			}

			// 统计该串在本串中的个数
			if (targetCount.get(c) == null) {
				targetCount.put(c, 1);
			} else {
				Integer count = targetCount.get(c);
				targetCount.put(c, 1 + count);
			}
		}

		// 统计原串中各个字符的个数
		Map<Character, Integer> sourceCount = new HashMap<Character, Integer>();
		for (int i = 0; i < length; i++) {
			char c = source.charAt(i);
			if (sourceCount.get(c) == null) {
				sourceCount.put(c, 1);
			} else {
				Integer count = sourceCount.get(c);
				sourceCount.put(c, 1 + count);
			}
		}

		// 目标串中每个字符个数跟原串中对应字符的个数一样
		for (Map.Entry<Character, Integer> entry : targetCount.entrySet()) {
			Character key = entry.getKey();
			if (entry.getValue() != sourceCount.get(key)) {
				return false;
			}
		}

		// 目标串中的每个元素都在原串中，且对应个数相同
		return true;
	}

	public static void main(String[] args) {
		String a = "美国雅培小安素儿童奶粉巧克力味";
		String b = "国美雅培小安素儿童巧克力味奶2";
		boolean result = isScrambledString(a, b);
		System.out.println(result);

		// 个数不同
		// b = "descnnn";
		// result = isScrambledString(a,b);
		// System.out.println(result);
	}
}
