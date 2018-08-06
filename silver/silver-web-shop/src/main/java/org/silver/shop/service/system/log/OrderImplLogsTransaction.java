package org.silver.shop.service.system.log;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.OrderImplLogsService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class OrderImplLogsTransaction {
	
	@Reference
	private OrderImplLogsService orderImplLogsService;

	//添加错误日志
	public Map<String,Object> addErrorLogs(List<Map<String, Object>> errorList, int totalCount, String serialNo,String action) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return orderImplLogsService.addErrorLogs(errorList,totalCount,serialNo,merchantId,merchantName,action);
	}

	//获取商户日志
	public Object merchantGetErrorLogs(HttpServletRequest req ,int page,int size) {
		Map<String,Object> params = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Enumeration<String>  isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key =  isKey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		params.remove("page");
		params.remove("size");
		return orderImplLogsService.merchantGetErrorLogs(params,page,size,merchantId,merchantName); 
	}
	
	
	
}
