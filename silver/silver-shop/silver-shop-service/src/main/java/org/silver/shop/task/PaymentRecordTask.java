package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.silver.shop.impl.system.cross.PaymentServiceImpl;
import org.silver.shop.impl.system.manual.MpayServiceImpl;

import net.sf.json.JSONArray;

/**
 * 多线程推送支付单备案信息
 *
 */
public class PaymentRecordTask implements Callable<Object> {

	private JSONArray dataList;//
	private String merchantId;// 商户Id
	private String merchantName; // 商户名称
	private List<Map<String, Object>> errorList;// 错误信息
	private Map<String, Object> customsMap;// 海关信息
	private String tok;//
	private int totalCount;// 总数
	private String serialNo;// 批次号
	private PaymentServiceImpl paymentServiceImpl;//

	/**
	 * 推送支付单信息
	 * 
	 * @param dataList
	 *            订单信息
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param errorList
	 *            错误信息
	 * @param customsMap
	 *            海关口岸信息
	 * @param tok
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            批次号
	 * @param paymentServiceImpl
	 *            实现类
	 */
	public PaymentRecordTask(JSONArray dataList, String merchantId, String merchantName,
			List<Map<String, Object>> errorList, Map<String, Object> customsMap, String tok, int totalCount,
			String serialNo, PaymentServiceImpl paymentServiceImpl) {
		this.dataList = dataList;
		this.merchantId = merchantId;
		this.merchantName = merchantName;
		this.errorList = errorList;
		this.customsMap = customsMap;
		this.tok = tok;
		this.totalCount = totalCount;
		this.serialNo = serialNo;
		this.paymentServiceImpl = paymentServiceImpl;
	}

	@Override
	public Object call() throws Exception {
		paymentServiceImpl.startSendPaymentRecord(dataList, merchantId, merchantName, errorList, customsMap, tok,
				totalCount, serialNo);
		return null;
	}

}
