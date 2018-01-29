package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.ManagerWalletService;
import org.silver.shop.dao.system.tenant.ManagerWalletDao;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.SearchUtils;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = ManagerWalletService.class)
public class ManagerWalletServiceImpl implements ManagerWalletService {

	@Autowired
	private ManagerWalletDao managerWalletDao;

	@Override
	public Map<String, Object> getMerchantWalletInfo(int page, int size, Map<String, Object> dataMap) {
		Map<String, Object> reDatasMap = SearchUtils.universalSearch(dataMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List<MerchantWalletContent> reList = managerWalletDao.findByProperty(MerchantWalletContent.class, paramMap,
				page, size);
		long count = managerWalletDao.findByPropertyCount(MerchantWalletContent.class, null);
		if (reList != null && !reList.isEmpty()) {
			Map<String, Object> statusMap = new HashMap<>();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.TOTALCOUNT.toString(), count);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据");
		}
	}

	@Override
	public Map<String, Object> updateMerchantWalletAmount(String merchantId, String managerName, double amount) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<MerchantWalletContent> reList = managerWalletDao.findByProperty(MerchantWalletContent.class, params, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			MerchantWalletContent merchantWallet = reList.get(0);
			Double oldBalance = merchantWallet.getBalance();
			merchantWallet.setBalance(oldBalance + amount);
			merchantWallet.setUpdateBy(managerName);
			merchantWallet.setUpdateDate(new Date());
			if(!managerWalletDao.update(merchantWallet)){
				return ReturnInfoUtils.errorInfo("充值失败,服务器繁忙,请重试!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("未找到商户,请核对信息是否正确!");
	}
}
