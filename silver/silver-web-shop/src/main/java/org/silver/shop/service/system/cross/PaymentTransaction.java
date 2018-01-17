package org.silver.shop.service.system.cross;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.task.ExcelTask;
import org.silver.util.SerialNoUtils;
import org.silver.util.SplitListUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

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
		String proxyParentId = merchantInfo.getProxyParentId();
		String proxyParentName = merchantInfo.getProxyParentName();
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

	public Map<String, Object> getMerchantPaymentReport(int page, int size, String startDate, String endDate) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return paymentService.getMerchantPaymentReport(merchantId, merchantName, page, size, startDate, endDate);
	}

	public Map<String, Object> groupCreateMpay(List<String> orderIdList) {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		Map<String, Object> statusMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		// 判断当前计算机CPU线程个数
		int cpuCount = Runtime.getRuntime().availableProcessors();
		String serialNo = "payment_" + SerialNoUtils.getSerialNo("payment");
		// 总数
		int realRowCount = orderIdList.size();
		if (realRowCount < cpuCount) {
			return paymentService.groupCreateMpay(merchantId, orderIdList, serialNo, realRowCount);
		} else {
			// 分批处理
			Map<String, Object> reMap = SplitListUtils.batchList(orderIdList, cpuCount);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			//
			List dataList = (List) reMap.get(BaseCode.DATAS.toString());
			for (int i = 0; i < dataList.size(); i++) {
				List list = (List) dataList.get(i);
				ExcelTask excelTask = new ExcelTask(list, merchantId, paymentService, serialNo, realRowCount);
				threadPool.submit(excelTask);
			}
			threadPool.shutdown();
			statusMap.put("status", 1);
			statusMap.put("msg", "执行成功,正在生成支付流水号.......");
			statusMap.put("serialNo", serialNo);
			return statusMap;
		}
		// 单35458ms
		// 多14675ms
		// return paymentService.groupCreateMpay(merchantId, orderIdList,
		// serialNo, realRowCount);
	}
}
