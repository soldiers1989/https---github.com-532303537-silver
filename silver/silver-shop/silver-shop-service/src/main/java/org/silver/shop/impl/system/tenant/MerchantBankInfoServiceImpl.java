package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.MerchantBankInfoService;
import org.silver.shop.dao.BaseDao;
import org.silver.shop.dao.system.tenant.MerchantBankInfoDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantBankContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantBankInfoService.class)
public class MerchantBankInfoServiceImpl implements MerchantBankInfoService {

	@Autowired
	private MerchantBankInfoDao merchantBankInfoDao;
	@Autowired
	private MerchantUtils merchantUtils;

	@Override
	public Map<String, Object> findMerchantBankInfo(String merchantId, int page, int size) {
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("商户id不能为空!");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Map<String, Object> pamras = new HashMap<>();
		pamras.put("merchantId", merchantId);
		List<MerchantBankContent> reList = merchantBankInfoDao.findByProperty(MerchantBankContent.class, pamras, page, size);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户银行卡信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("未找到商户银行卡信息!");
		}
	}

	@Override
	public boolean addMerchantBankInfo(Object entity, String bankName, String bankAccount, int defaultFalg) {

		return false;
	}

	@Override
	public boolean selectMerchantBank(long id, String merchantId) {
		return false;
	}

	@Override
	public boolean deleteMerchantBankInfo(long id, String merchantId) {

		return false;
	}
}
