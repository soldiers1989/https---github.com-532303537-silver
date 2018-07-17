package org.silver.shop.service.system.tenant;

import java.util.Map;


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.api.system.tenant.OfflineRechargeService;
import org.silver.shop.model.system.organization.Manager;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class OfflineRechargeTransaction {

	@Reference
	private OfflineRechargeService offlineRechargeService;
	
	@Reference
	private MerchantWalletService merchantWalletService;

	//管理员查询钱包线下充值信息
	public Object managerGetApplication(Map<String, Object> datasMap, int page, int size) {
		return merchantWalletService.getOfflineRechargeInfo(datasMap, page, size);
	}
	
	//
	public Map<String,Object> getApplicationDetail(String offlineRechargeId) {
		return offlineRechargeService.getApplicationDetail(offlineRechargeId);
	}
	
	//管理员审核
	public Map<String,Object> managerReview(String offlineRechargeId, int reviewerFlag, String note) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		String managerId = managerInfo.getManagerId();
		return offlineRechargeService.managerReview(offlineRechargeId,managerName,managerId,reviewerFlag,note);
	}
	
	//财务审核信息
	public Map<String,Object> financialReview(String offlineRechargeId, int reviewerFlag, String note) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		String managerId = managerInfo.getManagerId();
		return offlineRechargeService.financialReview(offlineRechargeId,managerName,managerId,reviewerFlag,note);
	}
}
