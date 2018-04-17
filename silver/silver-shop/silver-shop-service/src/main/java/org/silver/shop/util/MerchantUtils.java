package org.silver.shop.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
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
	
	/**
	 * 根据商户Id查询商户信息
	 * @param merchantId 商户Id
	 * @return Map
	 */
	public Map<String, Object> getMerchantInfo(String merchantId) {
		if(StringEmptyUtils.isEmpty(merchantId)){
			return ReturnInfoUtils.errorInfo("商户Id不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<Merchant> merchantList = paymentDao.findByProperty(Merchant.class, params, 1, 1);
		if (merchantList == null) {
			return ReturnInfoUtils.errorInfo("查询商户信息失败,服务器繁忙!");
		} else if (!merchantList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(merchantList.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("未找到商户信息!");
		}
	}
	
	/**
	 * 根据商户Id查询商户在appkey
	 * @param merchantId 商户Id
	 * @return Map
	 */
	public Map<String, Object> getMerchantAppkey(String merchantId) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchant_Id", merchantId);
		List<Appkey> appkeyList = paymentDao.findByProperty(Appkey.class, params, 1, 1);
		if (appkeyList == null) {
			return ReturnInfoUtils.errorInfo("查询appkey失败,服务器繁忙!");
		} else if (!appkeyList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(appkeyList.get(0), 0);
		} else {
			return ReturnInfoUtils.errorInfo("未找到商户appkey信息!");
		}
	}
}
