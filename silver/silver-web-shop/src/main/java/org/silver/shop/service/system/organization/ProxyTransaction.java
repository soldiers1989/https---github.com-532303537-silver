package org.silver.shop.service.system.organization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.ProxyService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.organization.Proxy;
import org.silver.util.MD5;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("proxyTransaction")
public class ProxyTransaction {
	
	@Autowired
	private ProxyService<Proxy> proxyService;
	
	
	public Map<String, Object> proxyLogin(String account, String loginPassword) {
		MD5 md5 = new MD5();
		Map<String, Object> datasMap = new HashMap<>();
		List<Proxy> reList = proxyService.findProxyBy(account);
		if (reList != null && !reList.isEmpty()) {
			Proxy proxy =  reList.get(0);
			String name = proxy.getLoginAccount();
			String loginpas = proxy.getLoginPassword();
			String md5Pas = md5.getMD5ofStr(loginPassword);
			// 判断查询出的账号密码与前台登录的账号密码是否一致
			if (account.equals(name) && md5Pas.equals(loginpas)) {
				Subject currentUser = SecurityUtils.getSubject();
				// 获取商户登录时,shiro存入在session中的数据
				Proxy proxyInfo = (Proxy) currentUser.getSession().getAttribute(LoginType.PROXYINFO.toString());
				if (proxyInfo == null) {
					WebUtil.getSession().setAttribute(LoginType.PROXYINFO.toString(), reList.get(0));
				}
				datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
				datasMap.put(BaseCode.MSG.getBaseCode(), "登录成功");
				return datasMap;
			}
		}
		return null;
	}


	public Map<String, Object> getProxyMerchantInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Proxy proxyInfo = (Proxy) currentUser.getSession().getAttribute(LoginType.PROXYINFO.toString());
		// 获取登录后的商户账号
		String proxyUUid = proxyInfo.getProxyUUid();
		String proxyName = proxyInfo.getProxyName();
		return proxyService.getProxyMerchantInfo(proxyUUid);
	}


	
}
