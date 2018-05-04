package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.MerchantFeeService;
import org.silver.shop.model.system.organization.Manager;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MerchantFeeTransaction {

	@Reference
	private MerchantFeeService merchantFeeService;
	
	//管理员添加
	public Map<String,Object> addMerchantServiceFee(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		params.put("managerName", managerName);
		return merchantFeeService.addMerchantServiceFee(params);
	}

	//管理员查询商户口岸费率信息
	public Map<String,Object> getMerchantServiceFee(String merchantId) {
		return merchantFeeService.getMerchantServiceFee(merchantId);
	}

	//管理员修改商户口岸费率信息
	public Object editMerchantServiceFee(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		params.put("managerName", managerName);
		return merchantFeeService.editMerchantServiceFee(params);
	}

	//商户获取口岸服务费信息
	public Object getServiceFee(String merchantId, String type) {
		return merchantFeeService.getServiceFee(merchantId,type);
	}

}
