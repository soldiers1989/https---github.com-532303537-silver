package org.silver.shop.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.silver.shop.service.system.manual.ManualService;
import org.silver.shop.task.GZExcelTask;
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

	/**
	 * 启动线程,根据启动线程数量对应CPU数
	 * 
	 * @param flag
	 *            1-企邦,2-国宗
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
	 */
	public void startTask(int flag, int totalCount, File file, String merchantId, String serialNo,
			String merchantName) {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		List<Map<String, Object>> errl = new Vector();
		// 开始行数
		int startCount = flag == 1 ? 2 : 1;
		// 结束行数
		int end = 0;
		int cpuCount = CalculateCpuUtils.calculateCpu(totalCount);
		// 本地端PC
		// cpuCount = 6;
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
					end = totalCount / cpuCount;
					startTask(flag, excelC, errl, merchantId, startCount, end, serialNo, totalCount, threadPool,
							merchantName);
				} else {
					startCount = end + 1;
					end = startCount + (totalCount / cpuCount);
					if (i == (cpuCount - 1)) {// 最后一次
						startTask(flag, excelC, errl, merchantId, startCount, totalCount, serialNo, totalCount,
								threadPool, merchantName);
					} else {
						startTask(flag, excelC, errl, merchantId, startCount, end, serialNo, totalCount, threadPool,
								merchantName);
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
	 *            1-企邦,2-国宗
	 * @param excelC
	 *            工具类
	 * @param errl
	 *            错误信息
	 * @param merchantId
	 *            商户Id
	 * @param startCount
	 *            开始行数
	 * @param end
	 *            结束行数
	 * @param serialNo
	 *            批次号
	 * @param totalCount
	 *            总行数
	 * @param threadPool
	 *            线程池
	 * @param merchantName
	 * 
	 */
	private void startTask(int flag, ExcelUtil excelC, List<Map<String, Object>> errl, String merchantId,
			int startCount, int end, String serialNo, int totalCount, ExecutorService threadPool, String merchantName) {
		switch (flag) {
		case 1:
			invokeQBExcelTask(excelC, errl, merchantId, startCount, end, manualService, serialNo, totalCount,
					threadPool, merchantName);
			break;
		case 2:
			invokeGZExcelTask(excelC, errl, merchantId, startCount, end, manualService, serialNo, totalCount,
					threadPool, merchantName);
			break;
		default:
			break;
		}
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
