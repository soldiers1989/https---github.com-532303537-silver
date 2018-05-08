package org.silver.shop.util;

import java.text.SimpleDateFormat;
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
	 * 完成数
	 */
	private static final String COMPLETED = "completed";

	/**
	 * 序号
	 */
	private static final String SERIAL_NO = "serialNo";
	
	/**
	 * 将正在执行数据更新到缓存中
	 * 
	 * @param errl
	 *            错误信息
	 */
	public void writeRedis(List<Map<String, Object>> errl, Map<String, Object> redisMap) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + redisMap.get("name") + "_" + redisMap.get(SERIAL_NO);
		synchronized (LOCK) {
			AtomicInteger counter = (AtomicInteger) redisMap.get("counter");
			datasMap.put(COMPLETED, counter.getAndIncrement());
			datasMap.put(BaseCode.STATUS.toString(), "1");
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put(TOTAL_COUNT, redisMap.get(TOTAL_COUNT));
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
		String serialNo = paramsMap.get(SERIAL_NO) + "";
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
				orderImplLogsService.addErrorLogs(errl, totalCount, serialNo, merchantId, merchantName, name);
			}
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put(TOTAL_COUNT, totalCount);
			// 将数据放入到缓存中
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
		}
	}

	/**
	 * 获取缓存中CPU数量
	 * 
	 * @param key
	 *            键
	 * @param totalCount
	 *            总数
	 * @return int CPU数
	 */
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
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + paramsMap.get("name") + "_" + paramsMap.get(SERIAL_NO)
				+ "_service";
		byte[] redisByte = JedisUtil.get(key.getBytes());
		if (redisByte != null && redisByte.length > 0) {
			updateRedis(redisByte, key, errl, paramsMap);
			updateWebRedis(redisByte, paramsMap);
		} else {
			notRides(key, errl, paramsMap);
		}
	}

	/**
	 * 更新web层缓存参数,用于页面获取参数
	 * 
	 * @param redisByte
	 *            service层缓存信息
	 * @param paramsMap
	 *            缓存参数
	 */
	private void updateWebRedis(byte[] redisByte, Map<String, Object> paramsMap) {
		String webKey = "Shop_Key_ExcelIng_" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "_" + paramsMap.get("name")
				+ "_" + paramsMap.get(SERIAL_NO) + "_Web";
		byte[] webRedisByte = JedisUtil.get(webKey.getBytes());
		if (webRedisByte != null && webRedisByte.length > 0) {
			ConcurrentMap<String, Object> redisMap = (ConcurrentMap<String, Object>) SerializeUtil.toObject(redisByte);
			ConcurrentMap<String, Object> webRedisMap = (ConcurrentMap<String, Object>) SerializeUtil
					.toObject(webRedisByte);
			int errCounter = Integer.parseInt(webRedisMap.get("errCounter") + "");
			int completed = Integer.parseInt(redisMap.get(COMPLETED) + "");
			// 将web层的错误信息及service层的完成数量,更新进缓存
			webRedisMap.put(COMPLETED, errCounter + completed);
			String newKey = "Shop_Key_ExcelIng_" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "_"
					+ paramsMap.get("name") + "_" + paramsMap.get(SERIAL_NO);
			JedisUtil.set(newKey.getBytes(), SerializeUtil.toBytes(webRedisMap), 3600);
		}
	}

	/**
	 * MQ版,根据缓存键更新已在缓存中有的信息
	 * 
	 * @param redisByte
	 *            缓存信息
	 * @param key
	 *            缓存键
	 * @param errl
	 *            错误信息
	 * @param paramsMap
	 *            缓存参数
	 */
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
		} else {
			if (errl != null) {
				redisMap.put(BaseCode.ERROR.toString(), errl);
			}
		}
		int counter = Integer.parseInt(redisMap.get(COMPLETED) + "");
		// 类型
		String type = paramsMap.get("type") + "";
		// 当成功后才进行计数
		if ("success".equals(type)) {
			counter++;
		}
		redisMap.put(COMPLETED, counter);
		redisMap.put(BaseCode.STATUS.toString(), "1");
		redisMap.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
		// 将数据放入到缓存中
		JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(redisMap), 3600);
	}

	/**
	 * MQ版本,当缓存中没有信息时,将信息写入进缓存
	 * 
	 * @param key
	 *            键
	 * @param errl
	 *            错误信息
	 * @param paramsMap
	 *            参数
	 */
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
		}
		datasMap.put(COMPLETED, counter);
		datasMap.put(BaseCode.STATUS.toString(), "1");
		datasMap.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
		// 将数据放入到缓存中
		JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);

	}

	/**
	 * MQ版,线程执行完成时写入缓存
	 * 
	 * @param paramsMap
	 *            缓存参数
	 */
	public void writeCompletedRedisMq(Map<String, Object> paramsMap) {
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String name = paramsMap.get("name") + "";
		String serialNo = paramsMap.get(SERIAL_NO) + "";
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo + "_service";
		byte[] redisInfo = JedisUtil.get(key.getBytes());
		String webKey = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo + "_Web";
		byte[] webRedisInfo = JedisUtil.get(webKey.getBytes());
		if (redisInfo != null && redisInfo.length > 0 && webRedisInfo != null && webRedisInfo.length > 0) {
			updateCompletedRedis(redisInfo, webRedisInfo, paramsMap, key, webKey);
		}
	}

	private void updateCompletedRedis(byte[] redisInfo, byte[] webRedisInfo, Map<String, Object> paramsMap, String key,
			String webKey) {
		String name = paramsMap.get("name") + "";
		String serialNo = paramsMap.get(SERIAL_NO) + "";
		String merchantId = paramsMap.get("merchantId") + "";
		String merchantName = paramsMap.get("merchantName") + "";
		int totalCount = Integer.parseInt(paramsMap.get(TOTAL_COUNT) + "");
		Map<String, Object> serviceMap = (Map<String, Object>) SerializeUtil.toObject(redisInfo);
		Map<String, Object> webMap = (Map<String, Object>) SerializeUtil.toObject(webRedisInfo);
		int counter = Integer.parseInt(serviceMap.get(COMPLETED) + "");
		int sendCounter = Integer.parseInt(webMap.get("sendCounter") + "");
		int errCounter = Integer.parseInt(webMap.get("errCounter") + "");
		// Mq队列完成数=web层的发送MQ队列成功数量,并且web层发送数量+错误信息数量=总数时则表示整个表单已经读取完成,更新信息至缓存
		if (counter == sendCounter && (sendCounter + errCounter) == totalCount) {
			Map<String, Object> datasMap = new HashMap<>();
			datasMap.put(BaseCode.MSG.toString(), "完成!");
			datasMap.put(BaseCode.STATUS.toString(), "2");
			datasMap.put(TOTAL_COUNT, totalCount);
			List<Map<String, Object>> serviceErrl = (List<Map<String, Object>>) serviceMap
					.get(BaseCode.ERROR.toString());
			List<Map<String, Object>> webErrl = (List<Map<String, Object>>) webMap.get(BaseCode.ERROR.toString());
			if (serviceErrl != null) {
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
			// 更新web层使用的缓存,用于前台页面显示
			JedisUtil.set(newKey.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
		}
	}

}
