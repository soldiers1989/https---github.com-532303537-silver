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

	private String merchantId;// 商户Id
	private String serialNo;// 流水号
	private PaymentServiceImpl paymentServiceImpl;//
	private List dataList; //
	private int realRowCount;// 总行数
	private List<Map<String, Object>> errorList;// 错误信息

	public GroupPaymentTask(List dataList, String merchantId, PaymentServiceImpl paymentServiceImpl, String serialNo,
			int realRowCount, List<Map<String, Object>> errorList) {
		this.dataList = dataList;
		this.merchantId = merchantId;
		this.paymentServiceImpl = paymentServiceImpl;
		this.serialNo = serialNo;
		this.realRowCount = realRowCount;
		this.errorList = errorList;
	}

	@Override
	public Map<String, Object> call() {
		return paymentServiceImpl.groupCreateMpay(merchantId, dataList, serialNo, realRowCount, errorList);
	}
}
