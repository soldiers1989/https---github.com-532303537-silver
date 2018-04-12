package org.silver.shop.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.service.system.log.OrderImplLogsTransaction;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.DateUtil;
import org.silver.util.FileUtils;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.silver.util.SortUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 主要用于Excel导入时写入缓冲数据 web层
 */
@Component
public class ExcelBufferUtils {
	@Autowired
	private OrderImplLogsTransaction orderImplLogsTransaction;

	// 创建一个静态钥匙
	private static Object lock = "lock";// 值是任意的
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
	public final void writeRedis(List<Map<String, Object>> errl, Map<String, Object> params) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + params.get("name") + "_" + params.get("serialNo");
		synchronized (lock) {
			AtomicInteger counter = (AtomicInteger) params.get("counter");
			datasMap.put("completed", counter.getAndIncrement());
			datasMap.put(BaseCode.STATUS.toString(), "1");
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put(TOTAL_COUNT, params.get(TOTAL_COUNT));
			// 将数据放入到缓存中
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
		}
	}

	/**
	 * 线程执行完成时写入缓存
	 * 
	 * @param errl
	 *            错误信息
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            批次号
	 * @param name
	 *            名称
	 */
	public final void writeCompletedRedis(List<Map<String, Object>> errl, Map<String, Object> params) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String name = params.get("name") + "";
		String serialNo = params.get("serialNo") + "";
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		synchronized (lock) {
			AtomicInteger threadCounter = (AtomicInteger) params.get("statusCounter");
			AtomicInteger counter = (AtomicInteger) params.get("counter");
			int totalCount = Integer.parseInt(params.get(TOTAL_COUNT) + "");
			int cpuCount = getRedisCPUCount(key, totalCount);
			if (cpuCount == 1) {
				threadCounter.set(cpuCount);
			} else {
				datasMap.put("count", threadCounter.getAndIncrement());
			}
			datasMap.put("cpuCount", cpuCount);
			datasMap.put(BaseCode.STATUS.toString(), "1");
			if (threadCounter.get() == cpuCount) {// 当最后一次线程时
				datasMap.put(BaseCode.MSG.toString(), "完成!");
				datasMap.put(BaseCode.STATUS.toString(), "2");
				Map<String, Object> reMap = finishProcessing(errl, totalCount, serialNo, name);
				if ("checkGZOrderImport".equals(name) || "checkQBOrderImport".equals(name)) {
					datasMap.put("fcy", reMap.get("fcy"));
					datasMap.put("orderCount", reMap.get("orderCount"));
				}
			}
			datasMap.put(BaseCode.ERROR.toString(), SortUtil.sortList(errl));
			datasMap.put("completed", counter.get());
			datasMap.put(TOTAL_COUNT, totalCount);
			// 将数据放入到缓存中
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
		}
	}

	/**
	 * 
	 * @param errl
	 * @param totalCount
	 * @param serialNo
	 * @param name
	 */
	private Map<String, Object> finishProcessing(List<Map<String, Object>> errl, int totalCount, String serialNo,
			String name) {
		Map<String, Object> statusMap = new HashMap<>();
		// 截取批次号数字
		String[] str = serialNo.split("_");
		int serial = Integer.parseInt(str[1]);
		String key = "";
		switch (name) {
		case "orderImport":
			// 删除文件夹下所有复制文件
			FileUtils.deleteFile(new File("/gadd-excel/"));
			orderImplLogsTransaction.addErrorLogs(SortUtil.sortList(errl), totalCount, serialNo, name);
			break;
		case "checkGZOrderImport":// 国宗订单导入预处理
			key = "Shop_Key_CheckGZOrder_List_" + serial;
			return checkGZOrderImport(key);
		case "checkQBOrderImport":
			key = "Shop_Key_CheckQBOrder_List_" + serial;
			return checkGZOrderImport(key);
		default:
			break;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	private Map<String, Object> checkGZOrderImport(String key) {
		Map<String, Object> statusMap = new HashMap<>();
		double fcy = 0.0;
		byte[] redisByte = JedisUtil.get(key.getBytes());
		if (redisByte != null && redisByte.length > 0) {
			// 换取缓存中的Map
			ConcurrentMap<String, Object> item = (ConcurrentMap<String, Object>) SerializeUtil.toObject(redisByte);
			for (Map.Entry<String, Object> entry : item.entrySet()) {
				List<Morder> mOrderList = (List<Morder>) entry.getValue();
				Morder order = mOrderList.get(0);
				// 总订单商品总金额
				fcy += order.getFCY();
			}
			statusMap.put("orderCount", item.size());
		}
		statusMap.put("fcy", fcy);
		return statusMap;
	}

	/**
	 * 根据键获取缓存中已计算好的CPU数量,如缓存中没有则从新计算
	 * 
	 * @param key
	 *            缓存中的键
	 * @param totalCount
	 *            总数
	 * @return int cpu数
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
	 * MQ版,将正在执行数据更新到缓存中 web层
	 * 
	 * @param errl
	 *            错误信息
	 */
	public void writeRedisMq(List<Map<String, Object>> errl, Map<String, Object> paramsMap) {
		String key = "Shop_Key_ExcelIng_" + DateUtil.formatDate(new Date(), "yyyyMMdd") + "_" + paramsMap.get("name")
				+ "_" + paramsMap.get("serialNo") + "_Web";
		synchronized (lock) {
			byte[] redisByte = JedisUtil.get(key.getBytes());
			if (redisByte != null && redisByte.length > 0) {
				updateRedis(redisByte, key, errl, paramsMap);
			} else {
				notRedis(key, errl, paramsMap);
			}
		}
	}

	private void updateRedis(byte[] redisByte, String key, List<Map<String, Object>> errl,
			Map<String, Object> paramsMap) {
		ConcurrentMap<String, Object> redisMap = (ConcurrentMap<String, Object>) SerializeUtil.toObject(redisByte);
		List<Map<String, Object>> reErrl = (List<Map<String, Object>>) redisMap.get(BaseCode.ERROR.toString());
		if (reErrl != null && !reErrl.isEmpty()) {
			if (errl != null && !errl.isEmpty()) {
				reErrl.add(errl.get(0));
			}
			redisMap.put(BaseCode.ERROR.toString(), reErrl);
		}
		// 类型
		String type = paramsMap.get("type") + "";
		int counter = 0;
		int errCounter = 0;
		// 发送至MQ队列成功数量
		int sendCounter = 0;
		switch (type) {
		case "success":
			sendCounter = Integer.parseInt(redisMap.get("sendCounter") + "");
			sendCounter++;
			redisMap.put("sendCounter", sendCounter);
			break;
		case "error":
			errCounter = Integer.parseInt(redisMap.get("errCounter") + "");
			// counter = Integer.parseInt(redisMap.get("completed") + "");
			errCounter++;
			// counter++;
			redisMap.put("errCounter", errCounter);
			// redisMap.put("completed", counter);
			break;
		default:
			redisMap.put("errCounter", redisMap.get("errCounter"));
			// redisMap.put("completed", redisMap.get("completed"));
			redisMap.put("sendCounter", redisMap.get("sendCounter"));
			break;
		}
		redisMap.put(BaseCode.STATUS.toString(), "1");
		redisMap.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
		// 将数据放入到缓存中
		JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(redisMap), 3600);
		//
		//String oldKey = key.substring(0, key.length() - 4);
		//JedisUtil.set(oldKey.getBytes(), SerializeUtil.toBytes(redisMap), 3600);
	}

	private void notRedis(String key, List<Map<String, Object>> errl, Map<String, Object> paramsMap) {
		int counter = 0;
		int errCounter = 0;
		// 成功发送数量
		int sendCounter = 0;
		ConcurrentMap<String, Object> datasMap = new ConcurrentHashMap<>();
		// 类型
		String type = paramsMap.get("type") + "";
		//
		if ("success".equals(type)) {
			sendCounter++;
		} else if ("error".equals(type)) {
			// counter++;
			errCounter++;
		}
		//
		if (errl != null && !errl.isEmpty()) {
			datasMap.put(BaseCode.ERROR.toString(), errl);
		} else {
			datasMap.put(BaseCode.ERROR.toString(), new ArrayList<>());
		}
		datasMap.put("completed", counter);
		datasMap.put("sendCounter", sendCounter);
		datasMap.put("errCounter", errCounter);
		datasMap.put(BaseCode.STATUS.toString(), "1");
		datasMap.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
		// 将数据放入到缓存中
		JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
		//String oldKey = key.substring(0, key.length() - 4);
		//JedisUtil.set(oldKey.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
	}

	/**
	 * MQ版,线程执行完成时写入缓存
	 * 
	 * @param errl
	 *            错误信息
	 * @param 参数
	 */
	public void writeCompletedRedisMq(List<Map<String, Object>> errl, Map<String, Object> paramsMap) {
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String name = paramsMap.get("name") + "";
		String serialNo = paramsMap.get("serialNo") + "";
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo + "_Web";
		int totalCount = Integer.parseInt(paramsMap.get(TOTAL_COUNT) + "");
		byte[] redisInfo = JedisUtil.get(key.getBytes());
		if (redisInfo != null && redisInfo.length > 0) {
			Map<String, Object> datasMap = (Map<String, Object>) SerializeUtil.toObject(redisInfo);
			int counter = Integer.parseInt(datasMap.get("completed") + "");
			int errCounter = Integer.parseInt(datasMap.get("errCounter") + "");
			if ((counter + errCounter) == totalCount) {
				datasMap.put(BaseCode.MSG.toString(), "完成!");
				datasMap.put(BaseCode.STATUS.toString(), "2");
				Map<String, Object> reMap = finishProcessing(errl, totalCount, serialNo, name);
				if ("checkGZOrderImport".equals(name) || "checkQBOrderImport".equals(name)) {
					datasMap.put("fcy", reMap.get("fcy"));
					datasMap.put("orderCount", reMap.get("orderCount"));
				}
				// 将数据放入到缓存中
				JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
				String oldKey = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
				JedisUtil.set(oldKey.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
			}
		}
	}

}
