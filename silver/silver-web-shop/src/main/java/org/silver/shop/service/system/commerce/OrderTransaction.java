package org.silver.shop.service.system.commerce;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.commerce.OrderService;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.YmHttpUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("orderTransaction")
public class OrderTransaction {

	@Reference
	private OrderService orderService;

	// 创建订单
	public Map<String, Object> createOrderInfo(String goodsInfoPack, int type, String recipientId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return orderService.createOrderInfo(memberId, memberName, goodsInfoPack, type, recipientId);
	}

	// 更新订单信息
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		return orderService.updateOrderRecordInfo(datasMap);
	}

	// 商户查看备案订单信息
	public Map<String, Object> getMerchantOrderRecordInfo(int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		return orderService.getMerchantOrderRecordInfo(merchantId, merchantName, page, size);
	}

	// 检查订单商品是否都属于一个海关口岸
	public Map<String, Object> checkOrderGoodsCustoms(String orderGoodsInfoPack, String recipientId) {
		return orderService.checkOrderGoodsCustoms(orderGoodsInfoPack,recipientId);
	}

	public Map<String, Object> getMemberOrderInfo(int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return orderService.getMemberOrderInfo(memberId, memberName, page, size);
	}

	// 商户查看订单详情
	public Map<String, Object> getMerchantOrderDetail(String entOrderNo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return orderService.getMerchantOrderDetail(merchantId, merchantName, entOrderNo);
	}

	// 用户查看订单详情
	public Map<String, Object> getMemberOrderDetail(String entOrderNo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return orderService.getMemberOrderDetail(memberId, memberName, entOrderNo);
	}

	//
	public Map<String, Object> searchMerchantOrderInfo(HttpServletRequest req, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Map<String, Object> param = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			param.put(key, value);
		}
		return orderService.searchMerchantOrderInfo(merchantId, merchantName, param, page, size);
	}

	// 获取商户每日订单报表
	public Map<String, Object> getMerchantOrderReport(String startDate, String endDate) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return orderService.getMerchantOrderDailyReport(merchantId, merchantName, startDate, endDate);
	}

	public Map<String, Object> doBusiness(String merchantCusNo, String outTradeNo, String amount, String notifyUrl,
			String extraCommonParam, String clientSign, String timestamp) {

		return orderService.doBusiness(merchantCusNo, outTradeNo, amount, notifyUrl, extraCommonParam, clientSign,
				timestamp);
	}

	// 管理员查询手动订单信息
	public Map<String, Object> getManualOrderInfo(int page, int size, HttpServletRequest req) {
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		params.remove("page");
		params.remove("size");
		return orderService.getManualOrderInfo(params, page, size);
	}

	// 管理员查询商户订单报表
	public Map<String, Object> managerGetOrderReport(String startDate, String endDate, String merchantId) {
		return orderService.getMerchantOrderDailyReport(merchantId, null, startDate, endDate);
	}

	// 用户删除订单信息
	public Object memberDeleteOrderInfo(String entOrderNo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		String memberName = memberInfo.getMemberName();
		return orderService.memberDeleteOrderInfo(entOrderNo, memberName);
	}

	//
	public Object getAgentOrderReport(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		AgentBaseContent agentBaseContent = (AgentBaseContent) currentUser.getSession()
				.getAttribute(LoginType.AGENTINFO.toString());
		String agentId = agentBaseContent.getAgentId();
		String agentName = agentBaseContent.getAgentName();
		// datasMap.put("agentId", agentId);
		// datasMap.put("agentName", agentName);
		return orderService.getAgentOrderReport(datasMap);
	}

	public Map<String, Object> getAlreadyDelOrderInfo(Map<String, Object> datasMap, int page, int size) {

		return orderService.getAlreadyDelOrderInfo(datasMap, page, size);
	}

	// 第三方商城平台传递订单信息入口
	public Map<String, Object> thirdPartyBusiness(Map<String, Object> datasMap) {
		return orderService.thirdPartyBusiness(datasMap);
	}

	// 第三方查询订单信息
	public Object getThirdPartyInfo(Map<String, Object> datasMap) {
		return orderService.getThirdPartyInfo(datasMap);
	}

	//
	public Object checkOrderPort(List<String> orderIDs) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return orderService.checkOrderPort(orderIDs,merchantId);
	}

	public static void main(String[] args) {
		Map<String,Object> item = new HashMap<>();
		//YM180125052191327
		//YM180125052181629
		//YM180125052176708
		item.put("a", "YM180125052209119");
		item.put("b", "YM180125052181629");
		item.put("c", "YM180125052176708");
		System.out.println("------->>"+YmHttpUtil.HttpPost("http://localhost:8080/silver-web-shop/order/checkOrderPort", item));
	}
}
