package org.silver.shop.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.silver.common.LoginType;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.service.system.organization.AgentTransaction;
import org.silver.shop.service.system.organization.ManagerTransaction;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class AgentRealm extends AuthorizingRealm{
	@Autowired
	private AgentTransaction agentTransaction;
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Manager manager = (Manager) WebUtil.getSession().getAttribute(LoginType.AGENTINFO.toString());
		SimpleAuthorizationInfo info = null;
		if (manager != null) {
			info = new SimpleAuthorizationInfo();
			info.addRole(LoginType.AGENT.toString());
		}
		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		CustomizedToken customizedToken = (CustomizedToken) token;
		AuthenticationInfo authcInfo = null;
		String account = customizedToken.getUsername();
		String pass = new String(customizedToken.getPassword());
		if (agentTransaction.agentLogin(account, pass) != null) {
			authcInfo = new SimpleAuthenticationInfo(account, pass, LoginType.AGENT.toString());
			return authcInfo;
		}else{
			throw new IncorrectCredentialsException();  
		}
	}
}

