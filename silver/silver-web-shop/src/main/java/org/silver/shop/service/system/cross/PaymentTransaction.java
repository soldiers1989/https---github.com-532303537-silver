package org.silver.shop.service.system.cross;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;

@Service
public class PaymentTransaction {

	@Reference
	private PaymentService paymentService;

	//
	public Map<String, Object> updatePaymentInfo(Map<String, Object> datasMap) {
		return paymentService.updatePaymentStatus(datasMap);
	}

	// 发送支付单备案
	public Object sendMpayRecord(Map<String, Object> recordMap, String tradeNoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		String proxyParentId = merchantInfo.getAgentParentId();
		String proxyParentName = merchantInfo.getAgentParentName();
		return paymentService.sendMpayByRecord(merchantId, recordMap, tradeNoPack, proxyParentId, merchantName,
				proxyParentName);
	}

	public Map<String, Object> updatePayRecordInfo(Map<String, Object> datasMap) {
		return paymentService.updatePayRecordInfo(datasMap);
	}

	// 获取商户支付单备案信息
	public Object getMpayRecordInfo(HttpServletRequest req, int page, int size) {
		Map<String, Object> params = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		params.remove("page");
		params.remove("size");
		return paymentService.getMpayRecordInfo(merchantId, merchantName, params, page, size);
	}

	public Map<String, Object> getMerchantPaymentReport(String startDate, String endDate) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return paymentService.getMerchantPaymentReport(merchantId, merchantName, startDate, endDate);
	}

	public Map<String, Object> groupCreateMpay(List<String> orderIdList) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return paymentService.splitStartPaymentId(orderIdList, merchantId, merchantName);
	}

	public Map<String, Object> managerGetPaymentReport(String startDate, String endDate, String merchantId) {
		return paymentService.getMerchantPaymentReport(merchantId, null, startDate, endDate);
	}

	// 管理员查询所有商户手工支付单信息
	public Map<String, Object> managerGetMpayInfo(Map<String, Object> params, int page, int size) {
		return paymentService.managerGetMpayInfo(params, page, size);
	}

	// 管理员修改商户手工支付单信息
	public Map<String, Object> managerEditMpayInfo(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return paymentService.managerEditMpayInfo(params, managerId, managerName);
	}

	//
	public Object getAgentPaymentReport(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取用户登录时,shiro存入在session中的数据
		AgentBaseContent agentBaseContent = (AgentBaseContent) currentUser.getSession()
				.getAttribute(LoginType.AGENTINFO.toString());
		String agentId = agentBaseContent.getAgentId();
		String agentName = agentBaseContent.getAgentName();
		// datasMap.put("agentId", agentId);
		// datasMap.put("agentName", agentName);
		return paymentService.getAgentPaymentReport(datasMap);
	}

	public Map<String, Object> managerHideMpayInfo(JSONArray jsonArray) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		return paymentService.managerHideMpayInfo(jsonArray, managerName);
	}

	public Object checkPaymentPort(List<String> tradeNos) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		return paymentService.checkPaymentPort(tradeNos,merchantId);
	}
}
