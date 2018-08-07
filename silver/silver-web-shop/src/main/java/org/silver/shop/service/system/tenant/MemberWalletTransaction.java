package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.MemberWalletService;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MemberWalletTransaction {

	@Reference
	private  MemberWalletService memberWalletService;
	
	public Map<String,Object> getInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		return memberWalletService.getInfo(memberInfo);
	}

}
