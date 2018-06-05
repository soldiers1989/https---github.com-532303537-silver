package org.silver.shop.task;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.impl.system.cross.PaymentServiceImpl;
import org.silver.util.TaskUtils;

/**
 * 多线程调用模拟生成(银盛)支付单流水号方法
 */
public class GroupPaymentTask extends TaskUtils {
	private static Logger logger = LogManager.getLogger(GroupPaymentTask.class);
	private PaymentService paymentService;//
	private List dataList; //
	private List<Map<String, Object>> errorList;// 错误信息
	private Map<String, Object> paramsMap;//

	public GroupPaymentTask(List dataList, PaymentService paymentService, List<Map<String, Object>> errorList,
			Map<String, Object> paramsMap) {
		this.dataList = dataList;
		this.paymentService = paymentService;
		this.errorList = errorList;
		this.paramsMap = paramsMap;
	}

	@Override
	public Map<String, Object> call() {
		try {
			paymentService.groupCreateMpay(dataList, errorList, paramsMap);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("-----生成支付单错误-----", e);
		}
		return null;
	}
}
