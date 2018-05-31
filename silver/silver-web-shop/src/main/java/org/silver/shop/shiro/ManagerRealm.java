package org.silver.shop.shiro;

import java.io.Serializable;
import java.util.List;

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
import org.silver.shop.service.system.organization.ManagerTransaction;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class ManagerRealm extends AuthorizingRealm {

	@Autowired
	private ManagerTransaction managerTransaction;
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Manager manager = (Manager) WebUtil.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		SimpleAuthorizationInfo info = null;
		if (manager != null) {
			info = new SimpleAuthorizationInfo();
			info.addRole(LoginType.MANAGER.toString());
			//查询管理员权限
			List<String> authorityList = managerTransaction.getManagerAuthority();
			info.addStringPermissions(authorityList);
		}
		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		CustomizedToken customizedToken = (CustomizedToken) token;
		AuthenticationInfo authcInfo = null;
		String account = customizedToken.getUsername();
		String pass = new String(customizedToken.getPassword());
		if (managerTransaction.managerLogin(account, pass) != null) {
			authcInfo = new SimpleAuthenticationInfo(account, pass, LoginType.MANAGER.toString());
			return authcInfo;
		}else{
			throw new IncorrectCredentialsException();  
		}
	}

}
