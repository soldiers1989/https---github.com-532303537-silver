package org.silver.shop.service.system.tenant;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.OfflineRechargeService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.FileUpLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class OfflineRechargeTransaction {

	@Reference
	private OfflineRechargeService offlineRechargeService;
	@Autowired
	private FileUpLoadService fileUpLoadService;

	public Map<String, Object> merchantApplication(HttpServletRequest req, Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		datasMap.put("merchantId", merchantId);
		datasMap.put("merchantName", merchantName);
		// String storePath ="/opt/www/img/merchantApplication/" + merchantId +
		// "/";
		String storePath = "D:\\";
		Map<String, Object> reFileMap = fileUpLoadService.universalDoUpload(req, storePath, ".jpg", false, 800, 800,
				null);
		if (!"1".equals(reFileMap.get(BaseCode.STATUS.toString()) + "")) {
			return reFileMap;
		}
		List<String> fileList = (List<String>) reFileMap.get(BaseCode.DATAS.toString());
		StringBuilder path = new StringBuilder("https://ym.191ec.com/img/merchantApplication/" + merchantId + "/");
		for (int i = 0; i < fileList.size(); i++) {
			String name = fileList.get(i);
			path.append(name + "#");
		}
		datasMap.put("remittanceReceipt", path);
		return offlineRechargeService.merchantApplication(datasMap);
	}
	//商户查询钱包线下充值信息
	public Map<String, Object> merchantGetApplication(Map<String, Object> datasMap, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		datasMap.put("merchantId", merchantId);
		return offlineRechargeService.getApplication(datasMap, page, size);
	}

	//管理员查询钱包线下充值信息
	public Object managerGetApplication(Map<String, Object> datasMap, int page, int size) {
		return offlineRechargeService.getApplication(datasMap, page, size);
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
	
	//
	public Map<String,Object> financialReview(String offlineRechargeId, int reviewerFlag, String note) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		String managerId = managerInfo.getManagerId();
		return offlineRechargeService.financialReview(offlineRechargeId,managerName,managerId,reviewerFlag,note);
	}
}
