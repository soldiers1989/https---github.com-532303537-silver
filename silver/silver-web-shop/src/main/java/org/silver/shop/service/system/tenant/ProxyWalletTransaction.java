package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.ProxyWalletService;
import org.silver.shop.model.system.organization.Proxy;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;


@Service
public class ProxyWalletTransaction {

	@Reference
	private ProxyWalletService proxyWalletService;

	public Map<String, Object> getProxyWalletInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Proxy proxyInfo = (Proxy) currentUser.getSession().getAttribute(LoginType.PROXYINFO.toString());
		// 获取登录后的商户账号
		String proxyUUid = proxyInfo.getProxyUUid();
		String proxyName = proxyInfo.getProxyName();
		return proxyWalletService.getProxyWalletInfo(proxyUUid,proxyName);
	}
	
	public Map<String, Object> getProxyWalletLog(int type, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Proxy proxyInfo = (Proxy) currentUser.getSession().getAttribute(LoginType.PROXYINFO.toString());
		// 获取登录后的商户账号
		String proxyUUid = proxyInfo.getProxyUUid();
		String proxyName = proxyInfo.getProxyName();
		return proxyWalletService.getProxyWalletLog(proxyUUid, proxyName, type, page, size);
	}

}
