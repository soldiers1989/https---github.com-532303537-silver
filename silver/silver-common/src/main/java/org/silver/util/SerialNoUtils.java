package org.silver.util;

/**
 * 商城流水号生成工具类
 */
public class SerialNoUtils {
	private SerialNoUtils() {

	}

	/**
	 * 生成流水号 流水号为:自编抬头+(当前)年+五位增长数(当前年份下ID总数+1)+时间戳(13位)+4位随机数
	 * 
	 * @param topStr
	 *            自编抬头
	 * @param year
	 *            年份
	 * @param serialNoCount
	 *            流水号数目
	 * @return String
	 */
	public static final String getSerialNo(String topStr, int year, long serialNoCount) {
		long count = serialNoCount + 1;
		// 默认随机数为4位
		int randomNumber = 4;
		String strCount = String.valueOf(count);
		while (strCount.length() < 5) {
			strCount = "0" + strCount;
		}		
		if (strCount.length() > 5) {
			randomNumber = 3;
		}
		// 获取到当前时间戳
		Long timestamp = System.currentTimeMillis();
		// 生成随机数
		int ramCount = RandomUtils.getRandom(randomNumber);
		return topStr + year + strCount + timestamp + ramCount;
	}

	/**
	 * 生成流水号(不要时间戳) 流水号为:自编抬头+(当前)年+五位增长数(当前年份下ID总数+1)+4位随机数
	 * 
	 * @param topStr
	 *            自编抬头
	 * @param year
	 *            年份
	 * @param serialNoCount
	 *            流水号数目
	 * @return String
	 */
	public static final String getSerialNotTimestamp(String topStr, int year, long serialNoCount) {
		long count = serialNoCount + 1;
		String strCount = String.valueOf(count);
		while (strCount.length() < 5) {
			strCount = "0" + strCount;
		}
		// 随机4位数
		int ramCount = RandomUtils.getRandom(4);
		return topStr + year + strCount + ramCount;
	}

	public static void main(String[] args) {
		String s = getSerialNo("tes_", 2017, 100000);
		String s2 = getSerialNotTimestamp("tes_", 2017, 100000);
		System.out.println("------->>>>>" + s2.length());
	}
}
