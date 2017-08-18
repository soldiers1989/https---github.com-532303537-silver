package org.silver.shiro;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;

public class CustomizedModularRealmAuthenticator extends ModularRealmAuthenticator{
	   @Override
	    protected AuthenticationInfo doAuthenticate(AuthenticationToken authenticationToken)
	            throws AuthenticationException {
	        // 判断getRealms()是否返回为空
	        assertRealmsConfigured();
	        CustomizedToken customizedToken = (CustomizedToken) authenticationToken;
	        String loginType = customizedToken.getLoginType();
	        Collection<Realm> realms = getRealms();
	        Collection<Realm> typeRealms = new ArrayList<>();
	        for (Realm realm : realms) {
	            if (realm.getName().contains(loginType))
	                typeRealms.add(realm);
	        }

	        // 判断是单Realm还是多Realm
	        if (typeRealms.size() == 1)
	            return doSingleRealmAuthentication(typeRealms.iterator().next(), customizedToken);
	        else
	            return doMultiRealmAuthentication(typeRealms, customizedToken);
	    }
}
