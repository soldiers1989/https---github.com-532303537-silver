package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.log4j.jmx.Agent;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.AgentWalletService;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Proxy;
import org.silver.shop.model.system.tenant.AgentWalletContent;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;


@Service
public class AgentWalletTransaction {

	@Reference
	private AgentWalletService agentWalletService;

	public Map<String, Object> getWalletInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		AgentBaseContent agentInfo = (AgentBaseContent) currentUser.getSession().getAttribute(LoginType.AGENT_INFO.toString());
		// 获取登录后的商户账号
		String agentId = agentInfo.getAgentId();
		String agentName = agentInfo.getAgentName();
		return agentWalletService.getAgentWalletInfo(agentId,agentName);
	}
	
	public Map<String, Object> getProxyWalletLog(int type, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Proxy proxyInfo = (Proxy) currentUser.getSession().getAttribute(LoginType.PROXY_INFO.toString());
		// 获取登录后的商户账号
		String proxyUUid = proxyInfo.getProxyUUid();
		String proxyName = proxyInfo.getProxyName();
		return agentWalletService.getProxyWalletLog(proxyUUid, proxyName, type, page, size);
	}

	//
	public Map<String,Object> generateSign(String agentId) {
		return agentWalletService.generateSign(agentId);
	}

}
