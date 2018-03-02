package org.silver.shop.utils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.service.system.log.ErrorLogsTransaction;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.DateUtil;
import org.silver.util.FileUtils;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.silver.util.SortUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
		System.out.println("------------运行完成写入方法----------");
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
			datasMap.put("cpuCount", cpuCount);
			datasMap.put(BaseCode.STATUS.toString(), "1");
			if (statusCounter.get() == cpuCount) {// 当最后一次线程时
				datasMap.put(BaseCode.MSG.toString(), "完成!");
				datasMap.put(BaseCode.STATUS.toString(), "2");
				// 重置计数器
				counter = new AtomicInteger(0);
				statusCounter = new AtomicInteger(0);
				Map<String, Object> reMap = finishProcessing(errl, totalCount, serialNo, name);
				datasMap.put("orderTotalAmount", reMap.get("orderTotalAmount"));
				datasMap.put("fcy", reMap.get("fcy"));
				datasMap.put(BaseCode.ERROR.toString(), reMap.get(BaseCode.ERROR.toString()));
				datasMap.remove("cpuCount");
			}
			datasMap.put("totalCount", totalCount);
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
			errorLogs.addErrorLogs(SortUtil.sortList(errl), totalCount, serialNo, name);
			break;
		case "checkGZOrderImport":// 国宗订单导入预处理
			 key = "Shop_Key_CheckGZOrder_List_" + serial;
			return checkGZOrderImport(serialNo,SortUtil.sortList(errl),key);
		case "checkQBOrderImport":
			key = "Shop_Key_CheckQBOrder_List_" + serial;
			return checkGZOrderImport(serialNo,SortUtil.sortList(errl),key);
		default:
			break;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.ERROR.toString(), errl);
		return statusMap;
	}

	private Map<String,Object> checkGZOrderImport(String serialNo, List<Map<String, Object>> errl, String key) {
		Map<String,Object> statusMap = new HashMap<>();
		double orderTotalAmount = 0.0;
		double fcy = 0.0;
		byte[] redisByte = JedisUtil.get(key.getBytes());
		if (redisByte != null && redisByte.length > 0) {
			// 换取缓存中的Map
			ConcurrentMap<String, Object> item = (ConcurrentMap<String, Object>) SerializeUtil.toObject(redisByte);
			for (Map.Entry<String, Object> entry : item.entrySet()) {
				List<Morder> mOrderList = (List<Morder>) entry.getValue();
				Morder order = mOrderList.get(0);
				// 总订单实际支付金额
				orderTotalAmount += order.getActualAmountPaid();
				// 总订单商品总金额
				fcy += order.getFCY();
			}
		}
		statusMap.put("orderTotalAmount", orderTotalAmount);
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
}
