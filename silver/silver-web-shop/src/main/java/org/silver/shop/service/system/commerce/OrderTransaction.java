package org.silver.shop.service.system.commerce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.common.base.IdCardService;
import org.silver.shop.api.system.commerce.OrderService;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.DateUtil;
import org.silver.util.YmHttpUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("orderTransaction")
public class OrderTransaction {

	@Reference
	private OrderService orderService;
	@Reference
	private IdCardService idCardService;
	

	// 创建订单
	public Map<String, Object> createOrderInfo(String goodsInfoPack, int type, String recipientId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
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
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return orderService.getMerchantOrderRecordInfo(merchantId, page, size);
	}

	// 检查订单商品是否都属于一个海关口岸
	public Map<String, Object> checkOrderGoodsCustoms(String orderGoodsInfoPack, String recipientId) {
		return orderService.checkOrderGoodsCustoms(orderGoodsInfoPack, recipientId);
	}

	public Map<String, Object> getMemberOrderInfo(int page, int size, Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		String memberId = memberInfo.getMemberId();
		datasMap.put("memberId", memberId);
		return orderService.getMemberOrderInfo(datasMap, page, size);
	}

	// 商户查看订单详情
	public Map<String, Object> getMerchantOrderDetail(String entOrderNo) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return orderService.getMerchantOrderDetail(merchantId, entOrderNo);
	}

	// 用户查看订单详情
	public Map<String, Object> getMemberOrderDetail(String entOrderNo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		String memberId = memberInfo.getMemberId();
		String memberName = memberInfo.getMemberName();
		return orderService.getMemberOrderDetail(memberId, memberName, entOrderNo);
	}

	//
	public Map<String, Object> searchMerchantOrderInfo(HttpServletRequest req, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
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
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return orderService.getMerchantOrderDailyReport(merchantId, merchantName, startDate, endDate);
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
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		String memberName = memberInfo.getMemberName();
		return orderService.memberDeleteOrderInfo(entOrderNo, memberName);
	}

	//
	public Object getAgentOrderReport(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		AgentBaseContent agentBaseContent = (AgentBaseContent) currentUser.getSession()
				.getAttribute(LoginType.AGENT_INFO.toString());
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
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return orderService.checkOrderPort(orderIDs, merchantId);
	}

	// 管理员查询商户订单报表
	public Map<String, Object> managerGetOrderReportInfo(String startDate, String endDate, String merchantId) {
		return orderService.managerGetOrderReportInfo(startDate, endDate, merchantId);
	}

	// 新-管理员查询订单报表详情信息
	public Object managerGetOrderReportDetails(Map<String, Object> params) {
		return orderService.managerGetOrderReportDetails(params);
	}

	// 第三方商城推广订单下单入口
	public Object thirdPromoteBusiness(Map<String, Object> params) {
		//idCardService.sendIdCardPhoneCertification(idName, idCard, phone);
		return orderService.thirdPromoteBusiness(params);
	}

	public static void main(String[] args) {
		// Map<String, Object> item = new HashMap<>();
		// item.put("entGoodsNo", "HX171030BJ9111111134");
		// item.put("count", "1");
		// item.put("idName", "来");
		// item.put("idcard", "210502199312170624");
		// item.put("phone", "13825004872");
		// item.put("address", "景德镇");
		// item.put("recProvincesName", "广东省");
		// item.put("recCityName", "广州市");
		// item.put("recAreaName", "天河区");
		//
		// System.out.println("------->>"
		// +
		// YmHttpUtil.HttpPost("http://localhost:8080/silver-web-shop/order/thirdPromoteBusiness",
		// item));

		System.out.println("--->"+DateUtil.parseDate("订单日期", "yyyyMMddHHmmss"));
	}

	//
	public Map<String,Object> managerOrderFenZhang(List<String> orderList) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		return orderService.managerOrderFenZhang(orderList,managerInfo.getManagerId(),managerInfo.getManagerName());
	}
}
