package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.MerchantBusinessService;
import org.silver.shop.model.system.organization.Manager;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MerchantBusinessTransaction {
	
	@Reference
	private MerchantBusinessService merchantBusinessService;
	
	public Object addInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		return merchantBusinessService.addInfo(managerName,datasMap);
	}

	public Map<String,Object> getInfo(Map<String, Object> datasMap) {
		return merchantBusinessService.getInfo(datasMap);
	}

}
