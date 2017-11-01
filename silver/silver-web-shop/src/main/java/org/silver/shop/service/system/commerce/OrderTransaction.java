package org.silver.shop.service.system.commerce;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.commerce.OrderService;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("orderTransaction")
public class OrderTransaction {

	@Reference
	private OrderService orderService;
	
	//创建但订单
	public Map<String,Object> createOrderInfo(String goodsInfoPack,int type,String recipientId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return orderService.createOrderInfo(memberId, memberName ,goodsInfoPack,type,recipientId);
	}

	//更新订单信息
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		return orderService.updateOrderRecordInfo(datasMap);
	}

	//商户查看所有订单信息
	public Map<String, Object> getMerchantOrderInfo(int page,int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		
		return orderService.getMerchantOrderInfo(merchantId,merchantName,page,size);
	}
}
