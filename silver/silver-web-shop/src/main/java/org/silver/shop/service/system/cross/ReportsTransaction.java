package org.silver.shop.service.system.cross;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.cross.ReportsService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class ReportsTransaction {

	@Reference
	private ReportsService reportsService;
	
	public Map<String,Object> getSynthesisReportDetails(Map<String, Object> params) {
		return reportsService.getSynthesisReportDetails(params);
	}

	public Object merchantGetSynthesisReportDetails(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		params.put("merchantId", merchantId);
		return reportsService.getSynthesisReportDetails(params);
	}

	public Map<String,Object> merchantGetIdCardCertification(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		params.put("merchantId", merchantId);
		return reportsService.getIdCardCertification(params);
	}

	public Object getIdCardCertification(Map<String, Object> params) {
		return reportsService.getIdCardCertification(params);
	}
}
