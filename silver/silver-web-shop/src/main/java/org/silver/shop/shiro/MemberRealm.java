package org.silver.shop.shiro;

import java.util.Map;
import java.util.Scanner;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.service.system.organization.MemberTransaction;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class MemberRealm extends AuthorizingRealm {

	@Autowired
	private MemberTransaction memberTransaction;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Member member = (Member) WebUtil.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
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
		Map<String, Object> reLoginMap = memberTransaction.memberLogin(account, pass);
		String msg = reLoginMap.get(BaseCode.MSG.toString()) + "";
		if ("你输入的密码和账户名不匹配!".equals(msg)) {
			throw new IncorrectCredentialsException();
		} else if ("密码错误".equals(msg)) {
			throw new AuthenticationException(reLoginMap.get(BaseCode.DATAS.toString()) + "");
		} else if ("账号已被锁定".equals(msg)) {
			throw new LockedAccountException(reLoginMap.get(BaseCode.DATAS.toString()) + "");
		}else{
			authcInfo = new SimpleAuthenticationInfo(account, pass, LoginType.MEMBER.toString());
			return authcInfo;
		}
	}

	public static void main(String[] args) {
		int h = 12862 / 3600;
		int m = (896 % 3600) / 60;
		int s = (895 % 3600) % 60;
		System.out.println(h + "时" + m + "分" + s + "秒");
	}
}
