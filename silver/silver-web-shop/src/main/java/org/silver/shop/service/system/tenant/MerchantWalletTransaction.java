package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("merchantWalletTransaction")
public class MerchantWalletTransaction {

	@Reference
	private MerchantWalletService merchantWalletService;

	// 商户钱包充值
	public Map<String, Object> merchantWalletRecharge(Double money) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return merchantWalletService.walletRecharge(merchantId, merchantName, money);
	}

	// 商户获取钱包信息
	public Map<String, Object> getMerchantWallet() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return merchantWalletService.getMerchantWallet(merchantId, merchantName);
	}

	//商户获取钱包日志
	public Map<String, Object> getMerchantWalletLog(int type, int page, int size, int timeLimit) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return merchantWalletService.getMerchantWalletLog(merchantId,merchantName,type,page,size,timeLimit);
	}
}
