package org.silver.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class UserRealm extends AuthorizingRealm{

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		CustomizedToken customizedToken = (CustomizedToken) token;
		System.out.println("user 用户登录");
		AuthenticationInfo authcInfo =null;
		String pass=new String(customizedToken.getPassword());
		if(pass.equals("truePass")){
			 authcInfo = new SimpleAuthenticationInfo("admin", "truePass",
					"admin");
		}
		
		return authcInfo;
	}

}
