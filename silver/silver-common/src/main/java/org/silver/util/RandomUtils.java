package org.silver.util;

/**
 * 随机数生成工具类
 *
 */
public class RandomUtils {
	private RandomUtils() {

	}

	/**
	 * 根据需要的随机数长度来生成随机数
	 * 
	 * @param length
	 *            长度
	 * @return int
	 */
	public static int getRandom(int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1D) {
			random += 0.1D;
		}
		for (int i = 0; i < length; i++) {
			num *= 10;
		}
		return (int) (random * num);
	}
}
