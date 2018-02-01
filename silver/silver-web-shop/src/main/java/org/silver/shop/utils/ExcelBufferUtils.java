package org.silver.shop.utils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.controller.system.log.ErrorLogsController;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.system.log.ErrorLogInfo;
import org.silver.shop.service.system.log.ErrorLogsTransaction;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.DateUtil;
import org.silver.util.FileUtils;
import org.silver.util.HttpUtil;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.silver.util.SortUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;

/**
 * 主要用于Excel导入时写入缓冲数据
 */
@Service
public class ExcelBufferUtils {
	@Autowired
	private ErrorLogsTransaction errorLogs;

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
	public final void writeRedis(List<Map<String, Object>> errl, int totalCount, String serialNo, String name) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		synchronized (lock) {
			datasMap.put("completed", counter.getAndIncrement());
			datasMap.put(BaseCode.STATUS.toString(), "1");
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put("totalCount", totalCount);
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
	 */
	public final void writeCompletedRedis(List<Map<String, Object>> errl, int totalCount, String serialNo,
			String name) {
		Map<String, Object> datasMap = new HashMap<>();
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String key = "Shop_Key_ExcelIng_" + dateSign + "_" + name + "_" + serialNo;
		synchronized (lock) {
			int cpuCount = getRedisCPUCount(key, totalCount);
			if (cpuCount == 1) {
				statusCounter.set(cpuCount);
			} else {
				datasMap.put("count", statusCounter.getAndIncrement());
			}
			if (statusCounter.get() == cpuCount) {// 当最后一次线程时
				datasMap.put(BaseCode.MSG.toString(), "完成!");
				datasMap.put(BaseCode.STATUS.toString(), "2");
				datasMap.remove("count");
				datasMap.remove("cpuCount");
				// 重置计数器
				counter = new AtomicInteger(0);
				statusCounter = new AtomicInteger(0);

				switch (name) {
				case "orderImport":// 只有再订单导入时才需要排序错误
					errl = SortUtil.sortList(errl);
					// 删除文件夹下所有复制文件
					FileUtils.deleteFile(new File("/gadd-excel/"));
					errorLogs.addErrorLogs(errl, totalCount, serialNo, name);
					break;
				default:
					break;
				}
			}
			datasMap.put(BaseCode.ERROR.toString(), errl);
			datasMap.put("totalCount", totalCount);
			datasMap.put("cpuCount", cpuCount);
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
