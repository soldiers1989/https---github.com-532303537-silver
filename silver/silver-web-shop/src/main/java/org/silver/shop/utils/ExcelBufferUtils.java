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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import net.sf.json.JSONObject;


/**
 * 主要用于Excel导入时写入缓冲数据 web层
 */
@Component
public class ExcelBufferUtils {
	@Autowired
	private OrderImplLogsTransaction orderImplLogsTransaction;

	
	private static Logger logger = LogManager.getLogger(ExcelBufferUtils.class);
	// 创建一个静态钥匙
	private static Object LOCK = new Object();// 值是任意的
	/**
	 * 总行数
	 */
	private static final String TOTAL_COUNT = "totalCount";
	/**
	 * 完成数
	 */
	private static final String COMPLETED = "completed";
	
	/**
	 * MQ发送数量
	 */
	private static final String SENDCOUNTER = "sendCounter";
	
	/**
	 * 错误数
	 */
	private static final String ERRCOUNTER = "errCounter";
	
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
		synchronized (LOCK) {
			AtomicInteger counter = (AtomicInteger) params.get("counter");
			datasMap.put(COMPLETED, counter.getAndIncrement());
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
		synchronized (LOCK) {
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
			datasMap.put(COMPLETED, counter.get());
			datasMap.put(TOTAL_COUNT, totalCount);
			// 将数据放入到缓存中
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
		}
	}

	/**
	 * 线程完成时根据标识进行不同的业务处理
	 * 
	 * @param errl
	 *            错误信息
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            序号
	 * @param name
	 *            名称
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
		synchronized (LOCK) {
			try{
				String redisInfo = JedisUtil.get(key);
				if (StringEmptyUtils.isNotEmpty(redisInfo) ) {
					updateRedis(redisInfo, key, errl, paramsMap);
				} else {
					notRedis(key, errl, paramsMap);
				}
			}catch (Exception e) {
				logger.error("----web层excel写入缓存错误--",e);
			}
		}
	}

	/**
	 * MQ版,当缓存中有数据时,取出已有信息,更新信息
	 * 
	 * @param redisByte
	 *            缓存中的数据
	 * @param key
	 *            缓存键
	 * @param errl
	 *            错误信息集合
	 * @param paramsMap
	 *            缓存参数
	 */
	private void updateRedis(String redisInfo, String key, List<Map<String, Object>> errl,
			Map<String, Object> paramsMap) {
		JSONObject redisJSON = JSONObject.fromObject(redisInfo);
		List<Map<String, Object>> reErrl = (List<Map<String, Object>>) redisJSON.get(BaseCode.ERROR.toString());
		if (reErrl != null && !reErrl.isEmpty()) {
			if (errl != null && !errl.isEmpty()) {
				reErrl.add(errl.get(0));
			}
			redisJSON.put(BaseCode.ERROR.toString(), reErrl);
		} else {
			if (errl != null ) {
				redisJSON.put(BaseCode.ERROR.toString(), errl);
			}
		}
		
		// 类型
		String type = paramsMap.get("type") + "";
		int errCounter = 0;
		// 发送至MQ队列成功数量
		int sendCounter = 0;
		switch (type) {
		case "success":
			sendCounter = Integer.parseInt(redisJSON.get(SENDCOUNTER) + "");
			sendCounter++;
			redisJSON.put(SENDCOUNTER, sendCounter);
			break;
		case "error":
			errCounter = Integer.parseInt(redisJSON.get(ERRCOUNTER) + "");
			errCounter++;
			redisJSON.put(ERRCOUNTER, errCounter);
			break;
		default:
			redisJSON.put(ERRCOUNTER, redisJSON.get(ERRCOUNTER));
			redisJSON.put(SENDCOUNTER, redisJSON.get(SENDCOUNTER));
			break;
		}
		redisJSON.put(BaseCode.STATUS.toString(), "1");
		redisJSON.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
		// 将数据放入到缓存中
		JedisUtil.set(key, 3600, redisJSON);
	}

	/**
	 * Mq版,当第一次缓存中没有数据时，第一次存放数据
	 * 
	 * @param key
	 *            键名
	 * @param errl
	 *            错误信息集合
	 * @param paramsMap
	 *            缓存参数
	 * 
	 */
	private void notRedis(String key, List<Map<String, Object>> errl, Map<String, Object> paramsMap) {
		//
		int counter = 0;
		//错误数量
		int errCounter = 0;
		// 成功发送数量
		int sendCounter = 0;
		JSONObject json = new JSONObject();
		// 类型
		String type = paramsMap.get("type") + "";
		//
		if ("success".equals(type)) {
			sendCounter++;
		} else if ("error".equals(type)) {
			errCounter++;
		}
		//
		if (errl != null && !errl.isEmpty()) {
			json.put(BaseCode.ERROR.toString(), errl);
		} else {
			json.put(BaseCode.ERROR.toString(), new ArrayList<>());
		}
		json.put(COMPLETED, counter);
		json.put(SENDCOUNTER, sendCounter);
		json.put(ERRCOUNTER, errCounter);
		json.put(BaseCode.STATUS.toString(), "1");
		json.put(TOTAL_COUNT, paramsMap.get(TOTAL_COUNT));
		// 将数据放入到缓存中
		JedisUtil.set(key, 3600, json);
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
			int counter = Integer.parseInt(datasMap.get(COMPLETED) + "");
			int errCounter = Integer.parseInt(datasMap.get(ERRCOUNTER) + "");
			if ((counter + errCounter) == totalCount) {
				datasMap.put(BaseCode.MSG.toString(), "完成!");
				datasMap.put(BaseCode.STATUS.toString(), "2");
				Map<String, Object> reMap = finishProcessing(errl, totalCount, serialNo, name);
				if ("checkGZOrderImport".equals(name) || "checkQBOrderImport".equals(name)) {
					datasMap.put("fcy", reMap.get("fcy"));
					datasMap.put("orderCount", reMap.get("orderCount"));
				}
				// 将数据放入到缓存中
				System.out.println("---线程结束后数据放入到缓存中>>"+datasMap.toString());
				JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
				String oldKey = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
				JedisUtil.set(oldKey.getBytes(), SerializeUtil.toBytes(datasMap), 3600);
			}
		}
	}

}
