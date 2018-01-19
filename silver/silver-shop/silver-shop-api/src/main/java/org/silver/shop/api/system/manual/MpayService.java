package org.silver.shop.api.system.manual;

import java.util.Map;

public interface MpayService {

	/**
	 * 根据订单Id发起订单备案
	 * 
	 * @param merchantId 商户Id
	 * @param merchantName 商户名称
	 * @param customsMap 海关口岸信息
	 * @param orderNoPack 订单Id信息 
	 * @param proxyParentId 代理商Id
	 * @param proxyParentName 代理商名称
	 * @return
	 */
	public Object sendMorderRecord(String merchantId, Map<String, Object> customsMap, String orderNoPack,
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
	 *           修改参数
	 * @param flag 修改标识 1-订单,2-订单商品,3-订单与商品一起修改
	 * @return Map
	 */
	public Map<String, Object> editMorderInfo(String merchantId, String merchantName, String[] strArr, int flag);

}
