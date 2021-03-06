package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.impl.system.cross.PaymentServiceImpl;
import org.silver.shop.impl.system.manual.MpayServiceImpl;

import net.sf.json.JSONArray;

/**
 * 多线程推送支付单备案信息
 *
 */
public class PaymentRecordTask implements Callable<Object> {

	private JSONArray dataList;//
	private List<Map<String, Object>> errorList;// 错误信息
	private Map<String, Object> customsMap;// 海关信息
	private PaymentService paymentService;//
	private Map<String,Object> paramsMap;//
	
	
	/**
	 * 推送支付单信息
	 * 
	 * @param dataList
	 *            订单信息
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
	public PaymentRecordTask(JSONArray dataList, List<Map<String, Object>> errorList, Map<String, Object> customsMap,
			PaymentService paymentService,Map<String,Object> paramsMap) {
		this.dataList = dataList;
		this.errorList = errorList;
		this.customsMap = customsMap;
		this.paymentService = paymentService;
		this.paramsMap = paramsMap;
	}

	@Override
	public Object call() throws Exception {
		paymentService.startSendPaymentRecord(dataList,  errorList, customsMap, paramsMap);
		return null;
	}

}
