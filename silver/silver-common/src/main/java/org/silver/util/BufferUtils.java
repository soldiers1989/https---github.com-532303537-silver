package org.silver.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;

/**
 * 用于批量生成信息时,缓冲数据
 *
 */
public class BufferUtils {

	private BufferUtils() {
		throw new IllegalStateException("Utility class");
	}

	// 创建一个静态钥匙
	private static Object lock = "lock";// 值是任意的

	/**
	 * 将正在导入的excel表格数据更新到缓存中
	 * 
	 * @param status
	 *            状态：1-进行中,2-已完成
	 * @param errl
	 *            错误信息
	 * @param totalCount
	 *            总数
	 * @param completed
	 *            已完成数量
	 * @param name
	 *            名称
	 */
	public static final void writeRedis(String status, List<Map<String, Object>> errl, int totalCount, String serialNo,
			String name) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		// 线程进入计数
		int count = 0;
		// 完成数量
		int completed = 0;
		long startTime = 0;
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		synchronized (lock) {
			// 读取缓存中的信息
			byte[] redisByte = JedisUtil.get(key.getBytes());
			if (redisByte != null && redisByte.length > 0) {
				datasMap = (Map<String, Object>) SerializeUtil.toObject(redisByte);
				String str = datasMap.get("completed") + "";
				if(StringEmptyUtils.isNotEmpty(str)){
					completed = Integer.parseInt(str);
				}
				String s = datasMap.get("count") + "";
				if (StringEmptyUtils.isNotEmpty(s)) {
					count = Integer.parseInt(s);
				}
			}
			if (StringEmptyUtils.isEmpty(datasMap.get("startTime") + "")) {
				// 开始时间
				startTime = System.currentTimeMillis();
				datasMap.put("startTime", startTime);
			}
			if ("1".equals(status)) {
				completed++;
				datasMap.put("completed", completed);
				datasMap.put(BaseCode.STATUS.toString(), status);
			} else if ("2".equals(status)) {
				// 获取当前计算机CPU线程个数
				int cpuCount = Runtime.getRuntime().availableProcessors();
				// int cpuCount = 1;
				if (totalCount < cpuCount) {
					count = cpuCount;
				} else {
					count++;
					datasMap.put("count", count);
				}
				if (count == cpuCount) {// 当最后一次线程时
					startTime = Long.parseLong(datasMap.get("startTime") + "");
					long endTime = System.currentTimeMillis();
					datasMap.put(BaseCode.MSG.toString(), "完成!");
					datasMap.put(BaseCode.STATUS.toString(), status);
					datasMap.put("time", "---总线程运行时间----->" + (endTime - startTime) + "ms");
					datasMap.remove("count");
					datasMap.remove("startTime");
				}
			}
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put("totalCount", totalCount);
			// 将数据放入到缓存中
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
		}
	}
}
