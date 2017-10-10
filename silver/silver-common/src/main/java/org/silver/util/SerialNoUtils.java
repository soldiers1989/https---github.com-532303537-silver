package org.silver.util;

import java.util.Calendar;
import java.util.Random;

/**
 * 商城流水号生成工具类
 */
public class SerialNoUtils {
	private SerialNoUtils(){
		
	}
	/**
	 * 生成流水号
	 * @param topStr
	 *            自编抬头
	 * @param count
	 *            自增数
	 * @return
	 */
	public static final String getSerialNo(String topStr, String strCount) {

		Calendar cal = Calendar.getInstance();
		// 获取当前年份
		int year = cal.get(Calendar.YEAR);
		// 当自增数没有5位数时,前面补0
		while (strCount.length() < 5) {
			strCount = "0" + strCount;
		}
		// 获取到当前时间戳
		Long current = System.currentTimeMillis();
		// 随机4位数
		int ramCount = RandomUtils.getRandom(4);
		// 编号为 自编抬头+(当前)年+五位自增数(数据库中当前年份的数量+1)+时间戳(13位)+4位随机数
		return topStr + year + strCount + current + ramCount;
	}
}
