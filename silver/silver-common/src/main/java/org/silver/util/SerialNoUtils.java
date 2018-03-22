package org.silver.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 商城流水号生成工具类
 */
public class SerialNoUtils {
	// 创建一个静态钥匙
	private static Object lock = "lock";// 值是任意的

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
	 * 根据不同场景,获取缓存当前日期下自增数
	 * 
	 * @param name
	 *            名称
	 * @return int
	 */
	public static final int getRedisIdCount(String name) {
		Map<String, Object> statusMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		int count = 1;
		String key = "Shop_Key_" + name + "_Int_" + dateSign;
		synchronized (lock) {// 防止多线程时,冲突
			// 读取缓存中的信息
			byte[] redisByte = JedisUtil.get(key.getBytes(), 86400);
			if (redisByte != null && redisByte.length > 0) {
				Map<String, Object> datasMap = (Map<String, Object>) SerializeUtil.toObject(redisByte);
				count = Integer.parseInt(datasMap.get("count") + "");
				count++;
			}
			statusMap.put("count", count);
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(statusMap), 86400);
			return count;
		}
	}

	/**
	 * 根据(自编抬头)抬头获取当天缓存中的自增数
	 * 
	 * @param topStr 自编抬头
	 * @return int 
	 */
	public static final int getSerialNo(String topStr) {
		String dateSign = DateUtil.format(new Date(), "yyyyMMdd");
		String str = JedisUtil.get(topStr + "_SerialNo_" + dateSign);
		int serial = 1;
		if (str != null && !"".equals(str.trim())) {
			try {
				serial = Integer.parseInt(str);
				JedisUtil.set(topStr + "_SerialNo_" + dateSign, 60 * 60 * 24, serial + 1);
				return serial + 1;
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}
		JedisUtil.set(topStr + "_SerialNo_" + dateSign, 60 * 60 * 24, 1);
		return serial;
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
		//自增数已经超过99999的时候,随机数减1位
		if (strCount.length() > 5) {
			randomNumber = 3;
		}
		// 默认随机数为4位
		int ramCount = RandomUtils.getRandom(randomNumber);
		return topStr + time + strCount + ramCount;
	}

	public static void main(String[] args) {
		String str = "YT201802228377126678";
		Map<String, Object> map = new HashMap<>();
		map.put("str", str);
		System.out.println("YM20180320022081798".length());
		System.out.println(map);
		System.out.println(map.toString());
	}
}
