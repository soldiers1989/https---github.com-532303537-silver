package org.silver.shop.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.silver.common.LoginType;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.service.system.organization.MemberTransaction;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class MemberRealm extends AuthorizingRealm{
	
	@Autowired
	private MemberTransaction memberTransaction;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Member member = (Member) WebUtil.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		SimpleAuthorizationInfo info = null;
		if (member != null) { 
			info = new SimpleAuthorizationInfo();
			info.addRole(LoginType.MEMBER.toString());
		}
		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		CustomizedToken customizedToken = (CustomizedToken) token;
		AuthenticationInfo authcInfo = null;
		String account = customizedToken.getUsername();
		String pass = new String(customizedToken.getPassword());
		if (memberTransaction.memberLogin(account, pass) != null) {
			authcInfo = new SimpleAuthenticationInfo(account, pass, LoginType.MEMBER.toString());
			// WebUtil.getSession().setAttribute(
			// LoginType.MERCHANT.toString()+"_info", value);
		}
		return authcInfo;
	}

}
