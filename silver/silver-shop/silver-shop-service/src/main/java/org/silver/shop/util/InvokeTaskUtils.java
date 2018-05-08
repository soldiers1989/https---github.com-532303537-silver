package org.silver.shop.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.silver.common.BaseCode;
import org.silver.shop.impl.system.cross.PaymentServiceImpl;
import org.silver.shop.impl.system.manual.MpayServiceImpl;
import org.silver.shop.task.GroupPaymentTask;
import org.silver.shop.task.OrderRecordTask;
import org.silver.shop.task.PaymentRecordTask;
import org.silver.util.CalculateCpuUtils;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.SplitListUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sf.json.JSONArray;

/**
 * 用于service层计算生成支付单、推送订单、推送支付单统一计算与调用
 *
 */
@Component
public class InvokeTaskUtils {

	@Autowired
	private MpayServiceImpl mpayServiceImpl;
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;

	/**
	 * 统一计算多线程后调用对应的方法
	 * 
	 * @param flag
	 *            1-生成支付单、2-推送订单、3-推送支付单
	 * @param totalCount
	 *            总数
	 * @param jsonList
	 *            参数集合
	 * @param errorList
	 *            错误集合
	 * @param customsMap
	 * @return Map
	 */
	public Map<String, Object> commonInvokeTask(int flag, int totalCount, JSONArray jsonList,
			List<Map<String, Object>> errorList, Map<String, Object> customsMap, Map<String, Object> paramMap) {
		String merchantId = paramMap.get("merchantId") + "";
		String merchantName = paramMap.get("merchantName") + "";
		String serialNo = paramMap.get("serialNo") + "";
		// 创建线程池
		ExecutorService threadPool = Executors.newCachedThreadPool();
		if (flag > 0 && totalCount >= 0 && jsonList != null && errorList != null
				&& StringEmptyUtils.isNotEmpty(merchantId) && StringEmptyUtils.isNotEmpty(merchantName)
				&& StringEmptyUtils.isNotEmpty(serialNo)) {
			// 完成数量
			AtomicInteger counter = new AtomicInteger(0);
			// 线程完成状态次数
			AtomicInteger threadCounter = new AtomicInteger(0);
			paramMap.put("counter", counter);
			paramMap.put("threadCounter", threadCounter);
			paramMap.put("totalCount", totalCount);
			int cpuCount = CalculateCpuUtils.calculateCpu(totalCount);
			if (cpuCount == 1) {
				chooseTask(flag, jsonList, errorList, customsMap, threadPool, paramMap);
			} else {
				// 分批处理
				Map<String, Object> reMap = SplitListUtils.batchList(jsonList, cpuCount);
				if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
					return reMap;
				}
				//
				List dataList = (List) reMap.get(BaseCode.DATAS.toString());
				for (int i = 0; i < dataList.size(); i++) {
					List newList = (List) dataList.get(i);
					chooseTask(flag, JSONArray.fromObject(newList), errorList, customsMap, threadPool, paramMap);
				}
			}
			threadPool.shutdown();
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("调用通用方法错误,参数有误!");
	}

	private void chooseTask(int flag, JSONArray jsonList, List<Map<String, Object>> errorList,
			Map<String, Object> customsMap, ExecutorService threadPool, Map<String, Object> paramsMap) {
		switch (flag) {
		case 1:
			GroupPaymentTask groupPaymentTask = new GroupPaymentTask(jsonList, paymentServiceImpl, errorList,
					paramsMap);
			threadPool.submit(groupPaymentTask);
			break;
		case 2:
			OrderRecordTask orderRecordTask = new OrderRecordTask(jsonList, errorList, customsMap, mpayServiceImpl,
					paramsMap);
			threadPool.submit(orderRecordTask);
			break;
		case 3:
			PaymentRecordTask paymentRecordTask = new PaymentRecordTask(jsonList, errorList, customsMap,
					paymentServiceImpl, paramsMap);
			threadPool.submit(paymentRecordTask);
			break;
		default:
			break;
		}
	}
}
