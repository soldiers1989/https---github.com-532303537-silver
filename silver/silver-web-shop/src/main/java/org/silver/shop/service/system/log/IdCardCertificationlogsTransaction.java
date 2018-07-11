package org.silver.shop.service.system.log;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.IdCardCertificationlogsService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class IdCardCertificationlogsTransaction {

	@Reference
	private IdCardCertificationlogsService idCardCertificationService;
	
	public Object getlogsInfo(Map<String, Object> datasMap, int page, int size) {
		return idCardCertificationService.getlogsInfo(datasMap,page,size);
	}

	public Object merchantGetInfo(Map<String, Object> datasMap, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		datasMap.put("merchantId", merchantId);
		return idCardCertificationService.getlogsInfo(datasMap,page,size);
	}

}
