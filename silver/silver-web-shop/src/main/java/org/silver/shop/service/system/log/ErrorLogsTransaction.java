package org.silver.shop.service.system.log;

import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.ErrorLogsService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class ErrorLogsTransaction {
	
	@Reference
	private ErrorLogsService errorLogsService;

	//添加错误日志
	public Map<String,Object> addErrorLogs(List<Map<String, Object>> errorList, int totalCount, String serialNo,String action) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return errorLogsService.addErrorLogs(errorList,totalCount,serialNo,merchantId,merchantName,action);
	}
	
	
	
}
