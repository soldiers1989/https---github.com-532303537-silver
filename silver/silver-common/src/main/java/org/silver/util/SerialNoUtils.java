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
		String strCount = String.valueOf(count);
		while (strCount.length() < 5) {
			strCount = "0" + strCount;
		}
		// 获取到当前时间戳
		Long timestamp = System.currentTimeMillis();
		// 随机4位数
		int ramCount = RandomUtils.getRandom(4);
		return topStr + year + strCount + timestamp + ramCount;
	}
}
