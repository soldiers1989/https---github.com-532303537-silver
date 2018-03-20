package org.silver.shop.task;

import java.util.List;
import java.util.Map;

import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.impl.system.cross.PaymentServiceImpl;
import org.silver.util.TaskUtils;

/**
 * 多线程调用模拟生成(银盛)支付单流水号方法
 *
 */
public class GroupPaymentTask extends TaskUtils {

	private PaymentServiceImpl paymentServiceImpl;//
	private List dataList; //
	private List<Map<String, Object>> errorList;// 错误信息
	private Map<String, Object> paramsMap;//

	public GroupPaymentTask(List dataList, PaymentServiceImpl paymentServiceImpl, List<Map<String, Object>> errorList,
			Map<String, Object> paramsMap) {
		this.dataList = dataList;
		this.paymentServiceImpl = paymentServiceImpl;
		this.errorList = errorList;
		this.paramsMap = paramsMap;
	}

	@Override
	public Map<String, Object> call() {
		return paymentServiceImpl.groupCreateMpay( dataList,   errorList,paramsMap);
	}
}
