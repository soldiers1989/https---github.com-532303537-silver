package org.silver.util;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.silver.common.BaseCode;

/**
 * 用于批量生成信息时,缓冲数据
 *
 */
public class BufferUtils {
	//完成数量
	private  static  AtomicInteger counter = new AtomicInteger(0);
	//线程完成状态次数
	private  static  AtomicInteger statusCounter = new AtomicInteger(0);
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
	 *            名称标识
	 */
	public static final void writeRedis(String status, List<Map<String, Object>> errl, int totalCount, String serialNo,
			String name) {
		
	
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		synchronized (lock) {
			if ("1".equals(status)) {
				datasMap.put("completed", counter.getAndIncrement());
				datasMap.put(BaseCode.STATUS.toString(), status);
			} else if ("2".equals(status)) {
				// 获取当前计算机CPU线程个数
				int cpuCount = Runtime.getRuntime().availableProcessors();
				if (totalCount <= cpuCount) {
					statusCounter.set(cpuCount);
				} else {
					datasMap.put("count", statusCounter.getAndIncrement());
				}
				if (statusCounter.get() == cpuCount) {// 当最后一次线程时
					datasMap.put(BaseCode.MSG.toString(), "完成!");
					datasMap.put(BaseCode.STATUS.toString(), status);
					datasMap.remove("count");
					datasMap.remove("startTime");
					//重置计数器
					counter = new AtomicInteger(0);
					statusCounter = new AtomicInteger(0);
					switch (name) {
					case "order"://只有再订单导入时才需要排序错误
						errl = SortUtil.sortList(errl);
						FileUtils.deleteFile(new File("/gadd-excel/"));
						
						break;
					default:
						break;
					}
				}
			}
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put("totalCount", totalCount);
			// 将数据放入到缓存中
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
		}
	}
}
