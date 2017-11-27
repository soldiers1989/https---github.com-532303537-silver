package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.RecipientService;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("recipientTransaction")
public class RecipientTransaction {

	@Reference
	private  RecipientService recipientSerivce;
	
	public Map<String,Object> addRecipientInfo(String recipientInfo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return recipientSerivce.addRecipientInfo(memberId,memberName,recipientInfo);
	}

	public Map<String,Object> getMemberRecipientInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return recipientSerivce.getMemberRecipientInfo(memberId,memberName);
	}

	//用户删除地址信息
	public Map<String, Object> deleteMemberRecipientInfo(String recipientId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return recipientSerivce.deleteMemberRecipientInfo(memberId,memberName,recipientId);
	}

}
