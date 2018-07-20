package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.MerchantIdCardCostService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MerchantIdCardCostTransaction {

	@Reference
	private MerchantIdCardCostService merchantIdCardCostService;

	public Map<String,Object> getInfo(String merchantId,int page,int size) {
		return merchantIdCardCostService.getInfo(merchantId, page, size);
	}
	
	//
	public Object addInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		datasMap.put("managerName", managerName);
		return merchantIdCardCostService.addInfo(datasMap);
	}

	public Object updateInfo(Map<String, Object> datasMap) {
		return merchantIdCardCostService.updateInfo(datasMap);
	}

	//商户获取实名认证费率信息
	public Object merchantGetInfo(int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return merchantIdCardCostService.getInfo(merchantId, page, size);
	}
	
	
}
