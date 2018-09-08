package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.ExpadndMerchantService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;


@Service
public class ExpadndMerchantTransaction {

	@Reference
	private ExpadndMerchantService expadndMerchantService;
	
	//添加子商户信息
	public Map<String,Object> addInfo(Map<String,Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		params.put("supMerchantId", merchantInfo.getMerchantId());
		params.put("supMerchantName", merchantInfo.getMerchantName());
		return expadndMerchantService.addInfo(params);
	}

	//查询所有子商户信息
	public Map<String,Object> getSubMerchantInfo() {
		return expadndMerchantService.getSubMerchantInfo();
	}

	//修改子商户信息
	public Object editSubMerchantInfo(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		String managerId = managerInfo.getManagerId();
		return expadndMerchantService.editSubMerchantInfo(params,managerId,managerName);
	}

}
