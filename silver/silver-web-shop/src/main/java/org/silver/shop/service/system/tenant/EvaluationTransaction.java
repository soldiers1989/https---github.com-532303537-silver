package org.silver.shop.service.system.tenant;

import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.EvaluationService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class EvaluationTransaction {

	@Reference
	private EvaluationService evaluationService;
	
	public Map<String,Object> getInfo(String goodsId, int page, int size) {
		return evaluationService.getInfo(goodsId,page,size);
	}

	public Object addInfo(String goodsId, String content, double level, String memberId, String memberName) {
//		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
//		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
//		String memberId = memberInfo.getMemberId();
//		String memberName = memberInfo.getMemberName();
		return evaluationService.addInfo(goodsId,content,level,memberId,memberName);
	}

	public Object randomMember() {
		return evaluationService.randomMember();
	}

	public Object addEvaluation(String entOrderNo, String goodsInfoPack, String ipAddress) {
		Subject currentUser = SecurityUtils.getSubject();
	// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return evaluationService.addEvaluation(entOrderNo,goodsInfoPack,memberId,memberName,ipAddress);
	}

	public Object merchantGetInfo(String goodsName, String memberName, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return evaluationService.merchantGetInfo(goodsName,memberName,merchantId,page,size);
	}

	public Object managerGetInfo(Map<String, Object> datasMap, int page, int size) {
		return evaluationService.merchantGetInfo(datasMap.get("goodsName")+"",datasMap.get("memberName")+"",datasMap.get("merchantId")+"", page,  size);
	}

	public Object managerDeleteInfo(List<String> idList) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return evaluationService.managerDeleteInfo(idList,managerId,managerName);
	}

}
