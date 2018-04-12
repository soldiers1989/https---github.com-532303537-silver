package org.silver.shop.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.OrderImplLogsService;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.DateUtil;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.silver.util.SortUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于批量生成支付单、发送支付单与订单时,缓冲数据 service层使用
 */
@Component("bufferUtils")
public class BufferUtils {

	@Autowired
	private OrderImplLogsService orderImplLogsService;

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
				orderImplLogsService.addErrorLogs(errl, totalCount, serialNo, merchantId, merchantName, name);
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
	 * @param paramsMap
	 *            缓存参数
	 */
	public void writeRedisMq(List<Map<String, Object>> errl, Map<String, Object> paramsMap) {
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + paramsMap.get("name") + "_" + paramsMap.get("serialNo")
				+ "_service";

		byte[] redisByte = JedisUtil.get(key.getBytes());
		if (redisByte != null && redisByte.length > 0) {
			updateRedis(redisByte, key, errl, paramsMap);
			updateWebRedis(redisByte, paramsMap);
		} else {
			notRides(key, errl, paramsMap);
		}
	}

	private void updateWebRedis(byte[] redisByte, Map<String, Object> paramsMap) {

		String webKey = "Shop_Key_ExcelIng_" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "_" + paramsMap.get("name")
				+ "_" + paramsMap.get("serialNo") + "_Web";
		byte[] webRedisByte = JedisUtil.get(webKey.getBytes());
		if (webRedisByte != null && webRedisByte.length > 0) {
			ConcurrentMap<String, Object> redisMap = (ConcurrentMap<String, Object>) SerializeUtil.toObject(redisByte);
			ConcurrentMap<String, Object> webRedisMap = (ConcurrentMap<String, Object>) SerializeUtil
					.toObject(webRedisByte);
			int errCounter = Integer.parseInt(webRedisMap.get("errCounter") + "");
			int completed = Integer.parseInt(redisMap.get("completed") + "");
			webRedisMap.put("completed", errCounter + completed);

			String newKey = "Shop_Key_ExcelIng_" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "_"
					+ paramsMap.get("name") + "_" + paramsMap.get("serialNo");
			JedisUtil.set(newKey.getBytes(), SerializeUtil.toBytes(webRedisMap), 3600);
		}
	}

	private void updateRedis(byte[] redisByte, String key, List<Map<String, Object>> errl,
			Map<String, Object> paramsMap) {
		ConcurrentMap<String, Object> redisMap = (ConcurrentMap<String, Object>) SerializeUtil.toObject(redisByte);
		List<Map<String, Object>> reErrl = (List<Map<String, Object>>) redisMap.get(BaseCode.ERROR.toString());
		//
		if (reErrl != null && !reErrl.isEmpty()) {
			if (errl != null && !errl.isEmpty()) {
				reErrl.add(errl.get(0));
			}
			redisMap.put(BaseCode.ERROR.toString(), reErrl);
		}
		int counter = Integer.parseInt(redisMap.get("completed") + "");
		// 类型
		String type = paramsMap.get("type") + "";
		// 当成功后才进行计数
		if ("success".equals(type)) {
			counter++;
			System.out.println(Thread.currentThread().getName() + "----success-->>>" + counter);
		}
		redisMap.put("completed", counter);
		redisMap.put(BaseCode.STATUS.toString(), "1");
		redisMap.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
		// 将数据放入到缓存中
		JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(redisMap), 3600);

	}

	private void notRides(String key, List<Map<String, Object>> errl, Map<String, Object> paramsMap) {
		int counter = 0;
		ConcurrentMap<String, Object> datasMap = new ConcurrentHashMap<>();
		if (errl != null && !errl.isEmpty()) {
			datasMap.put(BaseCode.ERROR.toString(), errl);
		} else {
			datasMap.put(BaseCode.ERROR.toString(), new ArrayList<>());
		}
		// 类型
		String type = paramsMap.get("type") + "";
		// 当成功后才进行计数
		if ("success".equals(type)) {
			counter++;
			System.out.println(Thread.currentThread().getName() + "----success-->>>" + counter);
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
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo + "_service";
		int totalCount = Integer.parseInt(paramsMap.get(TOTAL_COUNT) + "");
		byte[] redisInfo = JedisUtil.get(key.getBytes());
		String webKey = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo + "_Web";
		byte[] webRedisInfo = JedisUtil.get(webKey.getBytes());
		if (redisInfo != null && redisInfo.length > 0 && webRedisInfo != null && webRedisInfo.length > 0) {
			Map<String, Object> serviceMap = (Map<String, Object>) SerializeUtil.toObject(redisInfo);
			Map<String, Object> webMap = (Map<String, Object>) SerializeUtil.toObject(webRedisInfo);
			int counter = Integer.parseInt(serviceMap.get("completed") + "");
			int sendCounter = Integer.parseInt(webMap.get("sendCounter") + "");
			int errCounter = Integer.parseInt(webMap.get("errCounter") + "");
			System.out.println("--counter--->" + counter + ";---sendCounter->>>" + sendCounter + ";----errCounter->>"
					+ errCounter);
			if (counter == sendCounter && (sendCounter + errCounter) == totalCount) {
				Map<String, Object> datasMap = new HashMap<>();
				datasMap.put(BaseCode.MSG.toString(), "完成!");
				datasMap.put(BaseCode.STATUS.toString(), "2");
				datasMap.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
				List<Map<String, Object>> serviceErrl = (List<Map<String, Object>>) serviceMap
						.get(BaseCode.ERROR.toString());
				List<Map<String, Object>> webErrl = (List<Map<String, Object>>) webMap.get(BaseCode.ERROR.toString());
				if (serviceErrl != null && !serviceErrl.isEmpty()) {
					if (webErrl != null && !webErrl.isEmpty()) {
						serviceErrl.addAll(webErrl);
					}
					datasMap.put(BaseCode.ERROR.toString(), serviceErrl);
				} else if (webErrl != null && !webErrl.isEmpty()) {
					datasMap.put(BaseCode.ERROR.toString(), webErrl);
				}
				orderImplLogsService.addErrorLogs(serviceErrl, totalCount, serialNo, merchantId, merchantName, name);
				// 将数据放入到缓存中
				JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
				String newKey = webKey.substring(0, webKey.length() - 4);
				SortUtil.sortList(webErrl);
				JedisUtil.set(newKey.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
			}
		}
	}
}
