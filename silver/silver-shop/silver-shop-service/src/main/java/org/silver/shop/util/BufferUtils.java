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

	// 创建一个静态钥匙
	private static Object LOCK = "lock";// 值是任意的

	/**
	 * 总行数
	 */
	private static final String TOTAL_COUNT = "totalCount";
	
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
	public void writeRedis(List<Map<String, Object>> errl, Map<String, Object> paramsMap) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + paramsMap.get("name") + "_" + paramsMap.get("serialNo");
		synchronized (LOCK) {
			AtomicInteger counter = (AtomicInteger) paramsMap.get("counter");
			datasMap.put("completed", counter.getAndIncrement());
			datasMap.put(BaseCode.STATUS.toString(), "1");
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
			// 将数据放入到缓存中
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
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
	public void writeCompletedRedis(List<Map<String, Object>> errl, Map<String,Object> paramsMap) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String name = paramsMap.get("name")+"";
		String serialNo = paramsMap.get("serialNo")+"";
		String merchantId = paramsMap.get("merchantId")+"";
		String merchantName = paramsMap.get("merchantName")+"";
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		synchronized (LOCK) {
			AtomicInteger threadCounter = (AtomicInteger) paramsMap.get("threadCounter");
			int totalCount = Integer.parseInt(paramsMap.get(TOTAL_COUNT) + "");
			int cpuCount = getRedisCPUCount(key, totalCount);
			if (cpuCount == 1) {
				threadCounter.set(cpuCount);
			} else {
				datasMap.put("count", threadCounter.getAndIncrement());
				datasMap.put(BaseCode.STATUS.toString(), "1");
			}
			if (threadCounter.get() == cpuCount) {// 当最后一次线程时
				datasMap.put(BaseCode.MSG.toString(), "完成!");
				datasMap.put(BaseCode.STATUS.toString(), "2");
				datasMap.remove("count");
				datasMap.remove("startTime");
				errorLogsService.addErrorLogs(errl, totalCount, serialNo, merchantId, merchantName, name);
			}
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put(TOTAL_COUNT, totalCount);
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

	public static void main(String[] args) {
		AtomicInteger counter = new AtomicInteger(0);
		for(int i=0; i<100;i++){
			System.out.println(counter.getAndIncrement());
		}
	}
}
