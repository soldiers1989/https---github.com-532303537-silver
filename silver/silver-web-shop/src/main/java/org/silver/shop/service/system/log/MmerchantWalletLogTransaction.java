package org.silver.shop.service.system.log;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.MerchantWalletLogService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MmerchantWalletLogTransaction {

	@Reference
	private MerchantWalletLogService merchantWalletLogService;
	
	//
	public Object getMerchantWalletLog(String startDate, String endDate, int type, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return merchantWalletLogService.getWalletLog(merchantId,type,page,size,startDate,endDate);
	}

}
