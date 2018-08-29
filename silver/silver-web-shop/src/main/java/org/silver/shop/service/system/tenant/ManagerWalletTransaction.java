package org.silver.shop.service.system.tenant;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.ManagerWalletService;
import org.silver.shop.model.system.organization.Manager;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("managerWalletTransaction")
public class ManagerWalletTransaction {

	@Reference
	private ManagerWalletService managerWalletService;
	
	
	public Map<String, Object> getMerchantWalletInfo(int page, int size, HttpServletRequest req) {
		Map<String,Object> params = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		params.remove("page");
		params.remove("size");
		return managerWalletService.getMerchantWalletInfo(page,size,params);
	}


	public Object updateMerchantWalletAmount(String merchantId, double amount) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		return managerWalletService.updateMerchantWalletAmount(merchantId,managerName,amount);
	}

}
