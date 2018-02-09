package org.silver.shop.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.ErrorLogsService;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.DateUtil;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于批量生成支付单、发送支付单与订单时,缓冲数据
 *
 */
@Component
public class BufferUtils {

	@Autowired
	private ErrorLogsService errorLogsService;

	// 完成数量
	private static AtomicInteger counter = new AtomicInteger(0);
	// 线程完成状态次数
	private static AtomicInteger statusCounter = new AtomicInteger(0);

	// 创建一个静态钥匙
	private static Object lock = "lock";// 值是任意的

	/**
	 * 将正在执行数据更新到缓存中
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
	public void writeRedis(List<Map<String, Object>> errl, int totalCount, String serialNo, String name) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		synchronized (lock) {
			if (errl != null && totalCount > 0 && StringEmptyUtils.isNotEmpty(serialNo)
					&& StringEmptyUtils.isNotEmpty(name)) {
				datasMap.put("completed", counter.getAndIncrement());
				datasMap.put(BaseCode.STATUS.toString(), "1");
				datasMap.put(BaseCode.ERROR.toString(), errl);
				datasMap.put("totalCount", totalCount);
				// 将数据放入到缓存中
				JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
			}
		}
	}

	/**
	 * 线程执行完成时写入缓存
	 * 
	 * @param status
	 *            2-已完成
	 * @param errl
	 *            错误信息
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            批次号
	 * @param name
	 *            名称
	 * @param merchantName
	 * @param merchantId
	 */
	public void writeCompletedRedis(List<Map<String, Object>> errl, int totalCount, String serialNo, String name,
			String merchantId, String merchantName) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		synchronized (lock) {
			int cpuCount = getRedisCPUCount(key, totalCount);
			if (cpuCount == 1) {
				statusCounter.set(cpuCount);
			} else {
				datasMap.put("count", statusCounter.getAndIncrement());
				datasMap.put(BaseCode.STATUS.toString(), "1");
			}
			if (statusCounter.get() == cpuCount) {// 当最后一次线程时
				datasMap.put(BaseCode.MSG.toString(), "完成!");
				datasMap.put(BaseCode.STATUS.toString(), "2");
				datasMap.remove("count");
				datasMap.remove("startTime");
				// 重置计数器
				counter = new AtomicInteger(0);
				statusCounter = new AtomicInteger(0);
				errorLogsService.addErrorLogs(errl, totalCount, serialNo, merchantId, merchantName, name);
			}
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put("totalCount", totalCount);
			// 将数据放入到缓存中
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
		}
	}

	private final int getRedisCPUCount(String key, int totalCount) {
		byte[] redisByte = JedisUtil.get(key.getBytes(), 3600);
		if (redisByte != null) {
			Map<String, Object> datasMap = (Map<String, Object>) SerializeUtil.toObject(redisByte);
			String reCpuCount = datasMap.get("cpuCount") + "";
			if (StringEmptyUtils.isNotEmpty(reCpuCount)) {
				// 获取到缓存中已计算出来的CPU数量
				return Integer.parseInt(reCpuCount);
			}
		}
		return CalculateCpuUtils.calculateCpu(totalCount);
	}
}
