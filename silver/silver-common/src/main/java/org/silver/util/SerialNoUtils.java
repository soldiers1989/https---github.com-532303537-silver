package org.silver.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;

/**
 * 商城流水号生成工具类
 */
public class SerialNoUtils {
	// 创建一个静态钥匙
	private static Object LOCK = new Object();// 值是任意的

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

	/**
	 * 生成流水号(不要时间戳) 流水号为:自编抬头+(当前)年+五位增长数(当前年份下ID总数+1)+4位随机数
	 * 格式：自编抬头_年份_五位增长数_4位随机数
	 * 
	 * @param topStr
	 *            自编抬头
	 * @param year
	 *            年份
	 * @param serialNoCount
	 *            流水号数目
	 * @return String
	 */
	public static final String getSerialNotTimestamp2(String topStr, int year, long serialNoCount) {
		long count = serialNoCount + 1;
		String strCount = String.valueOf(count);
		while (strCount.length() < 5) {
			strCount = "0" + strCount;
		}
		// 随机4位数
		int ramCount = RandomUtils.getRandom(4);
		return topStr + "_" + year + "_" + strCount + ramCount;
	}

	/**
	 * 根据(自编抬头)抬头获取当天缓存中的自增数
	 * 
	 * @param topStr
	 *            自编抬头
	 * @return int
	 */
	public static final int getSerialNo(String topStr) {
		if (StringEmptyUtils.isEmpty(topStr)) {
			return -1;
		}
		synchronized (LOCK) {
			String dateSign = DateUtil.format(new Date(), "yyyyMMdd");
			String key = "shop_key_" + topStr + "_SerialNo_" + dateSign;
			String str = JedisUtil.get(key);
			int serial = 1;
			if (str != null && !"".equals(str.trim())) {
				try {
					serial = Integer.parseInt(str);
					serial++;
					JedisUtil.set(key, (60 * 60) * 24, serial);
					return serial;
				} catch (Exception e) {
					e.printStackTrace();
					return -1;
				}
			}
			JedisUtil.set(key, (60 * 60) * 24, 1);
			return serial;
		}
	}

	/**
	 * 生成流水号 流水号格式为:自编抬头+时间(yyyyMMdd)+五位增长数(当天缓存自增数)+4位随机数
	 * 
	 * @param topStr
	 *            自编抬头
	 * @param serialNoCount
	 *            流水号数目
	 * @return String
	 */
	public static final String getSerialNo(String topStr, long serialNoCount) {
		Date date = new Date();
		long count = serialNoCount + 1;
		String strCount = String.valueOf(count);
		int randomNumber = 4;
		while (strCount.length() < 5) {
			strCount = "0" + strCount;
		}
		String time = DateUtil.formatDate(date, "yyyyMMdd");
		// 自增数已经超过99999的时候,随机数减1位
		if (strCount.length() > 5) {
			randomNumber = 3;
		}
		// 默认随机数为4位
		int ramCount = RandomUtils.getRandom(randomNumber);
		return topStr + time + strCount + ramCount;
	}

	/**
	 * 生成流水号不要后4位随机数 流水号格式为:自编抬头+五位增长数(当天缓存自增数)
	 * 
	 * @param topStr
	 *            自编抬头
	 * @param id
	 *            流水号数目
	 * @return String
	 */
	public static String getNotRandomSerialNo(String topStr, long id) {
		// 判断数据库查询出数据如果小于0,则中断程序,告诉异常
		if (id < 0) {
			return null;
		}
		// 得出的总数上+1
		long count = id + 1;
		String strId = String.valueOf(count);
		// 当商户ID没有5位数时,前面补0
		while (strId.length() < 5) {
			strId = "0" + strId;
		}
		return topStr + strId;
	}

	public static void main(String[] args) {
		String str = "000000000000000000000000000091";
		System.out.println("---->>>"+str.length());
	}
}
