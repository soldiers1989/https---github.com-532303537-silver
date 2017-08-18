package org.silver.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.silver.util.WebUtil;

public class Realm extends AuthorizingRealm{
	
	
    //授权
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		String userId = WebUtil.getCurrentUser()+"";
		
        System.out.println(userId);
       
		System.out.println(arg0);
		info.addRole("admin");
		info.addStringPermission("sys.logs.read");
		System.out.println(info+"------>");
		return info;
		
	}

	//验证
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
		//System.out.println(token.getUsername());
		//System.out.println(token.getPassword());
		//System.out.println(token.getPrincipal());
	//	System.out.println(getName());
		//getName();
		
		AuthenticationInfo authcInfo = new SimpleAuthenticationInfo("admin", "password",
				"admin");
		//doGetAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
		return authcInfo;
		
	}

}
