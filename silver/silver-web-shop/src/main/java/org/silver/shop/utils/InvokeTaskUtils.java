package org.silver.shop.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.silver.shop.service.system.commerce.GoodsRecordTransaction;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.shop.task.GZExcelTask;
import org.silver.shop.task.NotRGExcelTask;
import org.silver.shop.task.PretreatmentQBExcelTask;
import org.silver.shop.task.QBExcelTask;
import org.silver.util.AppUtil;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.ExcelUtil;
import org.silver.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于web层
 */
@Component
public class InvokeTaskUtils {

	@Autowired
	private ManualService manualService;

	@Autowired
	private GoodsRecordTransaction goodsRecordTransaction;

	/**
	 * 启动线程标识
	 */
	private static final String FLAG = "flag";
	/**
	 * 商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 商户名称
	 */
	private static final String MERCHANT_NAME = "merchantName";
	/**
	 * 开始行数
	 */
	private static final String START_COUNT = "startCount";
	/**
	 * 结束行数
	 */
	private static final String END_COUNT = "endCount";
	/**
	 * 总行数
	 */
	private static final String TOTAL_COUNT = "totalCount";
	/**
	 * 批次号
	 */
	private static final String SERIAL_NO = "serialNo";
	/**
	 * 线程每次数量计数器
	 */
	private static final String COUNTER = "counter";
	/**
	 * 线程每次完成数量计数器
	 */
	private static final String STATUS_COUNTER = "statusCounter";

	/**
	 * 启动线程,根据启动线程数量对应CPU数
	 * 
	 * @param flag
	 *            1-企邦(手工订单导入),2-国宗(手工订单导入),3-(批量导入未)备案商品,
	 *            4-启邦(银盟统一模板)预处理手工订单导入,5-国宗(预处理手工订单导入)
	 * @param totalCount
	 *            总行数
	 * @param file
	 *            excel文件
	 * @param errl
	 *            错误List
	 * @param merchantId
	 *            商户Id
	 * @param serialNo
	 *            导入批次号
	 * @param merchantName
	 *            商户名称
	 */
	public void startTask(int flag, int totalCount, File file, String merchantId, String serialNo,
			String merchantName) {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		// 完成数量
		AtomicInteger counter = new AtomicInteger(0);
		// 线程完成状态次数
		AtomicInteger statusCounter = new AtomicInteger(0);
		// 错误信息集合
		List<Map<String, Object>> errl = new Vector();
		// 开始行数
		int startCount = 0;
		if (flag == 2 || flag == 5) {
			startCount = 1;
		} else {
			// 企邦表(银盟定义的模板)多了一行说明,所以开始行数为2
			startCount = 2;
		}
		// 结束行数
		int endCount = 0;
		int cpuCount = CalculateCpuUtils.calculateCpu(totalCount);
		if (cpuCount == 1) {// 不需要开辟多线程
			File dest = copyFile(file);
			ExcelUtil excelC = new ExcelUtil(dest);
			Map<String, Object> params = setParams(flag, merchantId, merchantName, totalCount, startCount, serialNo,
					counter, statusCounter);
			// 当是单线程时,结束行数为总行数
			params.put(END_COUNT, totalCount);
			chooseTask(excelC, errl, threadPool, params);
		} else {
			for (int i = 0; i < cpuCount; i++) {
				File dest = copyFile(file);
				ExcelUtil excelC = new ExcelUtil(dest);
				Map<String, Object> params = setParams(flag, merchantId, merchantName, totalCount, startCount, serialNo,
						counter, statusCounter);
				if (i == 0) {// 第一次
					endCount = totalCount / cpuCount;
					// 当是第一次计算时的结束行数
					params.put(END_COUNT, endCount);
					chooseTask(excelC, errl, threadPool, params);
				} else {
					startCount = endCount + 1;
					// 循环计算后的开始行数
					params.put(START_COUNT, startCount);
					endCount = startCount + (totalCount / cpuCount);
					if (i == (cpuCount - 1)) {// 最后一次
						// 最后一次的结束行数为总行数
						params.put(END_COUNT, totalCount);
						chooseTask(excelC, errl, threadPool, params);
					} else {
						params.put(END_COUNT, endCount);
						chooseTask(excelC, errl, threadPool, params);
					}
				}
			}
		}
		threadPool.shutdown();
	}

	private Map<String, Object> setParams(int flag, String merchantId, String merchantName, int totalCount,
			int startCount, String serialNo, AtomicInteger counter, AtomicInteger statusCounter) {
		Map<String, Object> params = new HashMap<>();
		params.put(FLAG, flag);
		params.put(MERCHANT_ID, merchantId);
		params.put(MERCHANT_NAME, merchantName);
		params.put(TOTAL_COUNT, totalCount);
		params.put(START_COUNT, startCount);
		params.put(SERIAL_NO, serialNo);
		params.put(COUNTER, counter);
		params.put(STATUS_COUNTER, statusCounter);
		return params;

	}

	/**
	 * 复制原始文件
	 * 
	 * @param file
	 * @return
	 */
	private File copyFile(File file) {
		// 副本文件名
		String imgName = AppUtil.generateAppKey() + "_" + System.currentTimeMillis() + ".xlsx";
		File dest = new File(file.getParentFile() + "/" + imgName);
		try {
			FileUtils.copyFileUsingFileChannels(file, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dest;
	}

	/**
	 * 根据需求判断启动对应的线程进行Excel信息读取
	 * 
	 * @param flag
	 *            1-企邦(手工订单导入),2-国宗(手工订单导入),3-(批量导入未)备案商品
	 * @param excelC
	 *            工具类
	 * @param errl
	 *            错误信息
	 * @param merchantId
	 *            商户Id
	 * @param startCount
	 *            开始行数
	 * @param endCount
	 *            结束行数
	 * @param serialNo
	 *            批次号
	 * @param totalCount
	 *            总行数
	 * @param threadPool
	 *            线程池
	 * @param merchantName
	 *            商户名称
	 */
	private void chooseTask(ExcelUtil excelC, List<Map<String, Object>> errl, ExecutorService threadPool,
			Map<String, Object> params) {
		int flag = 0;
		try {
			flag = Integer.parseInt(params.get(FLAG) + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.remove(FLAG);
		switch (flag) {
		case 1:
			invokeQBExcelTask(excelC, errl, threadPool, params);
			break;
		case 2:
			invokeGZExcelTask(excelC, errl, threadPool, params);
			break;
		case 3:
			invokeNotRecordGoodsExcelTask(excelC, errl, threadPool, params);
			break;
		case 4:
			invokePretreatmentQBExcelTask(excelC, errl, threadPool, params);
			break;
		case 5:
			invokePretreatmentGZExcelTask(excelC, errl, threadPool, params);
			break;
		default:
			break;
		}
	}

	// 调用预处理企邦多线程
	private void invokePretreatmentQBExcelTask(ExcelUtil excelC, List<Map<String, Object>> errl,
			ExecutorService threadPool, Map<String, Object> params) {
		PretreatmentQBExcelTask task = new PretreatmentQBExcelTask(excelC, errl, manualService, params);
		threadPool.submit(task);
	}

	// 调用预处理国宗多任务
	private void invokePretreatmentGZExcelTask(ExcelUtil excel, List<Map<String, Object>> errl,
			ExecutorService threadPool, Map<String, Object> params) {
		GZExcelTask task = new GZExcelTask(excel, errl, manualService, params);
		threadPool.submit(task);
	}

	// 调用导入未备案商品任务
	private void invokeNotRecordGoodsExcelTask(ExcelUtil excelC, List<Map<String, Object>> errl,
			ExecutorService threadPool, Map<String, Object> params) {
		NotRGExcelTask task = new NotRGExcelTask(excelC, errl, goodsRecordTransaction, params);
		threadPool.submit(task);
	}

	// 调用国宗多任务
	private void invokeGZExcelTask(ExcelUtil excel, List<Map<String, Object>> errl, ExecutorService threadPool,
			Map<String, Object> params) {
		GZExcelTask task = new GZExcelTask(excel, errl, manualService, params);
		threadPool.submit(task);
	}

	// 调用企邦多线程
	private void invokeQBExcelTask(ExcelUtil excel, List<Map<String, Object>> errl, ExecutorService threadPool,
			Map<String, Object> params) {
		QBExcelTask task = new QBExcelTask(excel, errl, manualService, params);
		threadPool.submit(task);
	}

}
