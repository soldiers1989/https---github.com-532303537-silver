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

	public Map<String,Object> tmpUpdate() {
		return reportsService.tmpUpdate();
	}
	
	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DateUtil.parseDate("2018-07-01 00:00:00", "yyyy-MM-dd HH:mm:ss"));
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		System.out.println("---endDate->>" + DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"));
	}
}
