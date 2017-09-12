package org.silver.shop.shiro;

import java.io.Serializable;
import java.util.Map;

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
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.service.system.organization.MerchantTransaction;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class MerchantRealm extends AuthorizingRealm implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2900919279652771749L;
	@Autowired
	private MerchantTransaction merchantTransaction;
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		 Merchant merchant =(Merchant) WebUtil.getSession().getAttribute(LoginType.MERCHANT.toString() + "_info");
		 SimpleAuthorizationInfo info =null;
		 if(merchant!=null){
			 info = new SimpleAuthorizationInfo();
			 info.addRole(LoginType.MERCHANT.toString());
		 }
		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		CustomizedToken customizedToken = (CustomizedToken) token;
		AuthenticationInfo authcInfo =null;
		String account = customizedToken.getUsername();
		String pass=new String(customizedToken.getPassword());
		Map a= merchantTransaction.merchantLogin(account, pass);
		if(a!=null){
			 authcInfo = new SimpleAuthenticationInfo(account, pass, LoginType.MERCHANT.toString());
			 //WebUtil.getSession().setAttribute( LoginType.MERCHANT.toString()+"_info", value);
		}
		return authcInfo;
	}

}
