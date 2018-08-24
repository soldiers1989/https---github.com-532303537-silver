package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.MemberBankService;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MemberBankTransaction {

	@Reference
	private MemberBankService memberBankService;
	
	public Map<String,Object> addInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		return memberBankService.addInfo(memberInfo,datasMap);
	}

	public Map<String,Object> getInfo(int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		return memberBankService.getInfo(memberInfo,page,size);
	}

	
}
