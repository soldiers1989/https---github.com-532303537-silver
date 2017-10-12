package org.silver.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;

/**
 * 商城流水号生成工具类
 */
public class SerialNoUtils {
	private SerialNoUtils() {

	}

	/**
	 * 生成流水号
	 * 流水号为:自编抬头+(当前)年+五位自增数(数据库中当前年份的数量+1)+时间戳(13位)+4位随机数
	 * @param topStr
	 *            自编抬头
	 * @param serialNo
	 *            流水号
	 * @return
	 */
	public static final String getSerialNo(String topStr, String serialNo) {
		int count = splitSerialNo(serialNo);
		String strCount = String.valueOf(count);
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
		return topStr + year + strCount + current + ramCount;
	}

	/**
	 * 截取字符串的第7位开始至第12位结束 
	 * 如：YM_2017|00001|15058114089963091 自增部分
	 * 
	 * @return Map
	 */
	public static final int splitSerialNo(String serialNo) {
		int count = 0;
		if (serialNo == null) {
			// 当查询数据库无记录时为：1
			count = 1;
		} else {
			String countId = serialNo.substring(7, 12);
			// 商品自增ID,得出的自增数上+1
			count = Integer.parseInt(countId) + 1;
		}
		return count;
	}
}
