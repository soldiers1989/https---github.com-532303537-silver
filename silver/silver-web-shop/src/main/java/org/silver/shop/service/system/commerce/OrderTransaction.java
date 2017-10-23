package org.silver.shop.service.system.commerce;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.commerce.OrderService;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("orderTransaction")
public class OrderTransaction {

	@Reference
	private OrderService orderService;
	
	public Map<String,Object> createOrderInfo(String goodsInfoPack,int type,double totalPrice) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return orderService.createOrderInfo(memberId, memberName ,goodsInfoPack,type,totalPrice);
	}

}
