package org.silver.shop.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.silver.shop.impl.system.manual.MpayServiceImpl;

import net.sf.json.JSONArray;

/**
 * 多线程推送订单备案信息
 *
 */
public class OrderRecordTask implements Callable<Object> {

	private JSONArray dataList;//
	private List<Map<String, Object>> errorList;// 错误信息
	private Map<String, Object> customsMap;// 海关信息
	private MpayServiceImpl mpayServiceImpl;//
	private Map<String, Object> paramsMap;//

	/**
	 * 推送订单信息
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
	 * @param mpayServiceImpl
	 *            实现类
	 * @param threadPool
	 */
	public OrderRecordTask(JSONArray dataList, List<Map<String, Object>> errorList, Map<String, Object> customsMap,
			MpayServiceImpl mpayServiceImpl, Map<String, Object> paramsMap) {
		this.dataList = dataList;
		this.errorList = errorList;
		this.customsMap = customsMap;
		this.mpayServiceImpl = mpayServiceImpl;
		this.paramsMap = paramsMap;
	}

	@Override
	public Object call() throws Exception {
		try {
			mpayServiceImpl.startSendOrderRecord(dataList, errorList, customsMap, paramsMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
