package org.silver.shop.service.system.log;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.MemberWalletLogService;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MemberWalletLogTransaction {

	@Reference
	private MemberWalletLogService memberWalletLogService;
	
	public Map<String,Object> getInfo(String startDate, String endDate, int type, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		return memberWalletLogService.getInfo(memberInfo,startDate,endDate,type,page,size);
	}

}
