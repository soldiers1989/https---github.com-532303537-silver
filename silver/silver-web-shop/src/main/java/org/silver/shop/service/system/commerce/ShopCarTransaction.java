package org.silver.shop.service.system.commerce;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.commerce.ShopCarService;
import org.silver.shop.model.system.organization.Member;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class ShopCarTransaction {

	@Reference
	private ShopCarService shopCarService;

	// 用户添加商品至购物车
	public Map<String, Object> addGoodsToShopCar(String entGoodsNo, int count) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return shopCarService.addGoodsToShopCar(memberId, memberName, entGoodsNo, count);
	}

	// 用户查询购物车信息
	public Map<String, Object> getGoodsToShopCartInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return shopCarService.getGoodsToShopCartInfo(memberId, memberName);
	}
	
	//用户删除购物车信息
	public Map<String, Object> deleteShopCartGoodsInfo(String goodsId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return shopCarService.deleteShopCartGoodsInfo(goodsId, memberId, memberName);
	}

	public Map<String, Object> editShopCarGoodsInfo(String goodsInfo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return shopCarService.editShopCarGoodsInfo(memberId,memberName,goodsInfo);
	}
	
}
