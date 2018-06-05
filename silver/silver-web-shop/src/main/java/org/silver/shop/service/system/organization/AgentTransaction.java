package org.silver.shop.service.system.organization;

import java.util.List;
import java.util.Map;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.organization.AgentService;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Manager;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AgentTransaction {

	@Autowired
	private AgentService agentService;
	
	public Map<String,Object> addAgentBaseInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return agentService.addAgentBaseInfo(datasMap,managerId,managerName);
	}

	
	public Map<String, Object> agentLogin(String account, String loginPassword) {
		MD5 md5 = new MD5();
		List<Object> reList = agentService.findAngetBy(account);
		if (reList != null && !reList.isEmpty()) {
			AgentBaseContent agent = (AgentBaseContent) reList.get(0);
			String name = agent.getAgentName();
			String loginpas = agent.getLoginPassword();
			String md5Pas = md5.getMD5ofStr(loginPassword);
			// 判断查询出的账号密码与前台登录的账号密码是否一致
			if (account.equals(name) && md5Pas.equals(loginpas)) {
				Subject currentUser = SecurityUtils.getSubject();
				// 获取商户登录时,shiro存入在session中的数据
				AgentBaseContent agentInfo = (AgentBaseContent) currentUser.getSession().getAttribute(LoginType.AGENT_INFO.toString());
				if (agentInfo == null) {
					WebUtil.getSession().setAttribute(LoginType.AGENT_INFO.toString(), reList.get(0));
				}
				return ReturnInfoUtils.successInfo();
			}
		}
		return null;
	}

	//获取所有代理商信息
	public Map<String,Object> getAllAgentInfo(Map<String, Object> params, int page, int size) {
		return agentService.getAllAgentInfo(params,page,size);
	}

	//管理员设置代理商下子商户信息
	public Map<String,Object> setAgentSubMerchant(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		AgentBaseContent agentBaseContent = (AgentBaseContent) currentUser.getSession().getAttribute(LoginType.AGENT_INFO.toString());
		String agentId = agentBaseContent.getAgentId();
		String agentName = agentBaseContent.getAgentName();
		params.put("agentId", agentId);
		params.put("agentName", agentName);
		return agentService.setAgentSubMerchant(params);
	}

	//代理商查询所有子商户信息
	public Map<String,Object> getSubMerchantInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		AgentBaseContent agentBaseContent = (AgentBaseContent) currentUser.getSession().getAttribute(LoginType.AGENT_INFO.toString());
		String agentId = agentBaseContent.getAgentId();
		String agentName = agentBaseContent.getAgentName();
		return agentService.getSubMerchantInfo(agentId,agentName);
	}

}
