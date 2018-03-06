package org.silver.shop.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		// 错误信息集合
		List<Map<String, Object>> errl = new Vector();
		// 开始行数
		int startCount = 0;
		if (flag == 2 || flag ==5) {
			startCount = 1;
		} else {
			// 企邦表(银盟定义的模板)多了一行说明,所以开始行数为2
			startCount = 2;
		}
		// 结束行数
		int endCount = 0;
		int cpuCount = CalculateCpuUtils.calculateCpu(totalCount);

		// int cpuCount = 1;
		if (cpuCount == 1) {// 不需要开辟多线程
			File dest = copyFile(file);
			ExcelUtil excelC = new ExcelUtil(dest);
			startTask(flag, excelC, errl, merchantId, startCount, totalCount, serialNo, totalCount, threadPool,
					merchantName);
		} else {
			for (int i = 0; i < cpuCount; i++) {
				File dest = copyFile(file);
				ExcelUtil excelC = new ExcelUtil(dest);
				if (i == 0) {// 第一次
					endCount = totalCount / cpuCount;
					startTask(flag, excelC, errl, merchantId, startCount, endCount, serialNo, totalCount, threadPool,
							merchantName);
				} else {
					startCount = endCount + 1;
					endCount = startCount + (totalCount / cpuCount);
					if (i == (cpuCount - 1)) {// 最后一次
						startTask(flag, excelC, errl, merchantId, startCount, totalCount, serialNo, totalCount,
								threadPool, merchantName);
					} else {
						startTask(flag, excelC, errl, merchantId, startCount, endCount, serialNo, totalCount,
								threadPool, merchantName);
					}
				}
			}
		}
		threadPool.shutdown();
	}

	/**
	 * 复制原始文件
	 * 
	 * @param file
	 * @return
	 */
	private  File copyFile(File file) {
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
	private void startTask(int flag, ExcelUtil excelC, List<Map<String, Object>> errl, String merchantId,
			int startCount, int endCount, String serialNo, int totalCount, ExecutorService threadPool,
			String merchantName) {
		switch (flag) {
		case 1:
			invokeQBExcelTask(excelC, errl, merchantId, startCount, endCount, manualService, serialNo, totalCount,
					threadPool, merchantName);
			break;
		case 2:
			invokeGZExcelTask(excelC, errl, merchantId, startCount, endCount, manualService, serialNo, totalCount,
					threadPool, merchantName);
			break;
		case 3:
			invokeNotRecordGoodsExcelTask(excelC, errl, merchantId, startCount, endCount, goodsRecordTransaction,
					serialNo, totalCount, threadPool, merchantName);
			break;
		case 4:
			invokePretreatmentQBExcelTask(excelC, errl, merchantId, startCount, endCount, goodsRecordTransaction,
					serialNo, totalCount, threadPool, merchantName);
			break;
		case 5:
			invokePretreatmentGZExcelTask(excelC, errl, startCount, endCount, manualService, serialNo, totalCount,
					threadPool);
			break;
		default:
			break;
		}
	}

	// 调用预处理企邦多线程
	private void invokePretreatmentQBExcelTask(ExcelUtil excelC, List<Map<String, Object>> errl, String merchantId,
			int startCount, int endCount, GoodsRecordTransaction goodsRecordTransaction2, String serialNo,
			int totalCount, ExecutorService threadPool, String merchantName) {
		PretreatmentQBExcelTask task = new PretreatmentQBExcelTask(0, excelC, errl, merchantId, startCount, endCount, manualService, serialNo,
				totalCount, merchantName);
		threadPool.submit(task);
	}

	// 调用预处理国宗多任务
	private void invokePretreatmentGZExcelTask(ExcelUtil excelC, List<Map<String, Object>> errl, int startCount,
			int endCount, ManualService manualService, String serialNo, int totalCount, ExecutorService threadPool) {
		GZExcelTask task = new GZExcelTask(0, excelC, errl, startCount, endCount, manualService, serialNo, totalCount);
		threadPool.submit(task);
	}

	// 调用导入未备案商品任务
	private void invokeNotRecordGoodsExcelTask(ExcelUtil excelC, List<Map<String, Object>> errl, String merchantId,
			int startCount, int endCount, GoodsRecordTransaction goodsRecordTransaction, String serialNo,
			int totalCount, ExecutorService threadPool, String merchantName) {
		NotRGExcelTask task = new NotRGExcelTask(excelC, errl, merchantId, startCount, endCount, goodsRecordTransaction,
				serialNo, totalCount, merchantName);
		threadPool.submit(task);
	}

	// 调用国宗多任务
	private void invokeGZExcelTask(ExcelUtil excelC, List<Map<String, Object>> errl, String merchantId, int startCount,
			int endCount, ManualService manualService, String serialNo, int totalCount, ExecutorService threadPool,
			String merchantName) {
		GZExcelTask task = new GZExcelTask(0, excelC, errl, merchantId, startCount, endCount, manualService, serialNo,
				totalCount, merchantName);
		threadPool.submit(task);
	}

	// 调用企邦多线程
	private void invokeQBExcelTask(ExcelUtil excel, List<Map<String, Object>> errl, String merchantId, int startCount,
			int endCount, ManualService manualService, String serialNo, int totalCount, ExecutorService threadPool,
			String merchantName) {
		QBExcelTask task = new QBExcelTask(0, excel, errl, merchantId, startCount, endCount, manualService, serialNo,
				totalCount, merchantName);
		threadPool.submit(task);
	}

}
