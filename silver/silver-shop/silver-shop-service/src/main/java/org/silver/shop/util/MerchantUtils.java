package org.silver.shop.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于获取通用的商户信息
 *
 */
@Component
public class MerchantUtils {
	@Autowired
	private PaymentDao paymentDao;

	/**
	 * 根据商户Id及口岸获取商户对应的备案信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param eport
	 *            口岸
	 * @return Map
	 */
	public final Map<String, Object> getMerchantRecordInfo(String merchantId, int eport) {
		Map<String, Object> param = new HashMap<>();
		param.put("merchantId", merchantId);
		param.put("customsPort", eport);
		List<MerchantRecordInfo> recordList = paymentDao.findByProperty(MerchantRecordInfo.class, param, 1, 1);
		if (recordList == null) {
			return ReturnInfoUtils.errorInfo("查询商户备案信息失败,服务器繁忙!");
		} else if (!recordList.isEmpty()) {
			MerchantRecordInfo entity = recordList.get(0);
			return ReturnInfoUtils.successDataInfo(entity, 0);
		} else {
			return ReturnInfoUtils.errorInfo("未找到对应的商户备案信息!");
		}
	}
}
