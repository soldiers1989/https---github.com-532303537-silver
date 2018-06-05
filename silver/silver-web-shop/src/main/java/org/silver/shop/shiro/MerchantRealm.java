package org.silver.shop.shiro;

import java.io.Serializable;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.silver.common.LoginType;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.service.system.organization.MerchantTransaction;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class MerchantRealm extends AuthorizingRealm implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2900919279652771749L;
	@Autowired
	private MerchantTransaction merchantTransaction;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Merchant merchant = (Merchant) WebUtil.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		SimpleAuthorizationInfo info = null;
		if (merchant != null) {
			info = new SimpleAuthorizationInfo();
			info.addRole(LoginType.MERCHANT.toString());
			//查询商户权限
			List<String> authorityList = merchantTransaction.getMerchantAuthority();
			//@RequiresPermissions
			//info.addStringPermissions(authorityList);
		}
		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		CustomizedToken customizedToken = (CustomizedToken) token;
		AuthenticationInfo authcInfo = null;
		String account = customizedToken.getUsername();
		String pass = new String(customizedToken.getPassword());
		if (merchantTransaction.merchantLogin(account, pass) != null) {
			authcInfo = new SimpleAuthenticationInfo(account, pass, LoginType.MERCHANT.toString());
			
			return authcInfo;
		}else{
			throw new IncorrectCredentialsException();  
			//return authcInfo;
		}
	}

}
