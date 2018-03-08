package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.SubMerchantService;
import org.silver.shop.model.system.organization.Manager;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONObject;

@Service
public class SubMerchantTransaction {

	@Reference
	private SubMerchantService subMerchantService;
	
	//添加子商户信息
	public Map<String,Object> addSubMerchantInfo(Map<String,Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		String managerId = managerInfo.getManagerId();
		return subMerchantService.addSubMerchantInfo(params,managerId,managerName);
	}

	//查询所有子商户信息
	public Map<String,Object> getSubMerchantInfo() {
		return subMerchantService.getSubMerchantInfo();
	}

	//修改子商户信息
	public Object editSubMerchantInfo(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		String managerId = managerInfo.getManagerId();
		return subMerchantService.editSubMerchantInfo(params,managerId,managerName);
	}

}
