package org.silver.shop.api.system.manual;

import java.util.List;
import java.util.Map;

public interface MpayService {

	/**
	 * 根据订单id 列 批量生成相应支付单
	 * 
	 * @param merchant_no
	 * @param orderIDs
	 * @return
	 */
	public Map<String, Object> groupCreateMpay(String merchant_no, List<String> orderIDs);

	/**
	 * 根据订单Id发起订单备案
	 * 
	 * @param merchantId
	 * @param recordMap
	 * @param orderNoPack
	 * @param proxyParentName
	 * @param merchantName
	 * @param proxyParentId
	 * @return
	 */
	public Object sendMorderRecord(String merchantId, Map<String, Object> recordMap, String orderNoPack,
			String proxyParentId, String merchantName, String proxyParentName);

	/**
	 * 异步回调订单备案信息
	 * 
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap);

	/**
	 * 根据订单日期与批次号下载订单信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param topPath
	 *            下载路径头部
	 * @param serialNo
	 * @param date
	 * @return Map
	 */
	public Map<String, Object> downOrderExcelByDateSerialNo(String merchantId, String merchantName, String filePath,
			String date, String serialNo);

	/**
	 * 商户修改手工(导入)的订单
	 * 
	 * @param strArr
	 *            订单信息包
	 * @param flag 
	 * @return
	 */
	public Map<String, Object> editMorderInfo(String merchantId, String merchantName, String[] strArr, int flag);

}
