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
 * 用于批量生成支付单、发送支付单与订单时,缓冲数据 service层使用
 */
@Component("bufferUtils")
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
	 * @param errl
	 *            错误信息
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
	 * @param errl
	 *            错误信息
	 */
	public void writeCompletedRedis(List<Map<String, Object>> errl, Map<String, Object> paramsMap) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String name = paramsMap.get("name") + "";
		String serialNo = paramsMap.get("serialNo") + "";
		String merchantId = paramsMap.get("merchantId") + "";
		String merchantName = paramsMap.get("merchantName") + "";
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

	/**
	 * MQ版,将正在执行数据更新到缓存中
	 * 
	 * @param errl
	 *            错误信息
	 */
	public void writeRedisMq(List<Map<String, Object>> errl, Map<String, Object> paramsMap) {
		int counter = 0;
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + paramsMap.get("name") + "_" + paramsMap.get("serialNo");
		Map<String, Object> datasMap = new HashMap<>();
		byte[] redisByte = JedisUtil.get(key.getBytes());
		if (redisByte != null && redisByte.length > 0) {
			Map<String, Object> redisMap = (Map<String, Object>) SerializeUtil.toObject(redisByte);
			List<Map<String, Object>> reErrl = (List<Map<String, Object>>) redisMap.get(BaseCode.ERROR.toString());
			if (reErrl !=null && !reErrl.isEmpty()) { 
				if(errl !=null && !errl.isEmpty()){
					reErrl.add(errl.get(0));
				}
				datasMap.put(BaseCode.ERROR.toString(), reErrl);
			}
			counter = Integer.parseInt(redisMap.get("completed") + "");
		} else {
			datasMap.put(BaseCode.ERROR.toString(), errl);
		}
		//
		int type = Integer.parseInt(paramsMap.get("type") + "");
		//
		if (type == 1 || type== 200) {
			counter++;
		}
		datasMap.put("completed", counter);
		datasMap.put(BaseCode.STATUS.toString(), "1");
		datasMap.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
		// 将数据放入到缓存中
		JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
	}

	/**
	 * 线程执行完成时写入缓存
	 * 
	 * @param errl
	 *            错误信息
	 */
	public void writeCompletedRedisMq(Map<String, Object> paramsMap) {
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String name = paramsMap.get("name") + "";
		String serialNo = paramsMap.get("serialNo") + "";
		String merchantId = paramsMap.get("merchantId") + "";
		String merchantName = paramsMap.get("merchantName") + "";
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		int totalCount = Integer.parseInt(paramsMap.get(TOTAL_COUNT) + "");
		byte[] redisInfo = JedisUtil.get(key.getBytes());
		if (redisInfo != null && redisInfo.length > 0) {
			Map<String, Object> datasMap = (Map<String, Object>) SerializeUtil.toObject(redisInfo);
			int counter = Integer.parseInt(datasMap.get("completed") + "");
			if (counter == totalCount) {
				datasMap.put(BaseCode.MSG.toString(), "完成!");
				datasMap.put(BaseCode.STATUS.toString(), "2");
				List<Map<String, Object>> reErrl = (List<Map<String, Object>>) datasMap.get(BaseCode.ERROR.toString());
				errorLogsService.addErrorLogs(reErrl, totalCount, serialNo, merchantId, merchantName, name);
				// 将数据放入到缓存中
				JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
			}
		}
	}
}
