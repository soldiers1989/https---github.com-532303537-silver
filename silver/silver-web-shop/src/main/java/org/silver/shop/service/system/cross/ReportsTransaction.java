package org.silver.shop.service.system.cross;

import java.util.Calendar;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.cross.ReportsService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.DateUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class ReportsTransaction {

	@Reference(timeout = 20000)
	private ReportsService reportsService;
	
	public Map<String,Object> getSynthesisReportDetails(Map<String, Object> datasMap) {
		return reportsService.getSynthesisReportDetails(datasMap);
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


	public Map<String,Object> tmpCreate(String merchantId) {
		return reportsService.tmpCreate(merchantId);
	}

	public Map<String,Object> getSynthesisReportInfo(Map<String, Object> datasMap) {
		return reportsService.getSynthesisReportInfo(datasMap);
	}
}
