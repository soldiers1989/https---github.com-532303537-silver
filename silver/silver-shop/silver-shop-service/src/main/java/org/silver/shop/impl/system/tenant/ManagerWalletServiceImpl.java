package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.ManagerWalletService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
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
	@Autowired
	private MerchantWalletService merchantWalletService;

	@Override
	public Map<String, Object> getMerchantWalletInfo(int page, int size, Map<String, Object> dataMap) {
		Map<String, Object> reDatasMap = SearchUtils.universalMerchantWalletSearch(dataMap);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List<MerchantWalletContent> reList = managerWalletDao.findByProperty(MerchantWalletContent.class, paramMap,
				page, size);
		Long count = managerWalletDao.findByPropertyCount(MerchantWalletContent.class, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户钱包信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count.intValue());
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> updateMerchantWalletAmount(String merchantId, String managerName, double amount) {

		return merchantWalletService.balanceOperating(merchantId, amount, "add");
	}
}
