package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.PaymentReceiptLogService;
import org.silver.shop.api.system.tenant.MerchantBankInfoService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.util.ReturnInfoUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MerchantWalletTransaction {

	@Reference
	private MerchantWalletService merchantWalletService;
	@Reference
	private MerchantBankInfoService merchantBankInfoService;	
	@Reference
	private PaymentReceiptLogService paymentReceiptLogService;
	
	// 商户获取钱包信息
	public Map<String, Object> getMerchantWallet() {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return merchantWalletService.getMerchantWallet(merchantId, merchantName);
	}

	public void addWalletRechargeLog(double amount, String orderId) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		merchantWalletService.addWalletRechargeLog(merchantId, merchantName, amount, orderId);
	}

	/**
	 *  获取商户银行卡信息
	 * @param merchantId 商户Id
	 * @return Map
	 */
	public Map<String, Object> getMerchantBankInfo(String merchantId) {
		return merchantBankInfoService.findMerchantBankInfo(merchantId, 0, 0);
	}

	//添加交易记录
	public Map<String,Object> addPaymentReceiptLog(String merchantId, double amount, String serialNo, String type) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		return paymentReceiptLogService.addPaymentReceiptLog(merchantId, amount,serialNo,managerName,type);
	}

	/**
	 * 根据商户Id查询商户钱包信息,后校验商户钱包是否有足够的现金结算
	 * @param merchantId 商户Id
	 * @param amount 
	 */
	public Map<String,Object> getMerchantWallet(String merchantId, double amount) {
		Map<String,Object> reMap =  merchantWalletService.getMerchantWallet(merchantId, null);
		if(!"1".equals(reMap.get(BaseCode.STATUS.toString()))){
			return reMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		if((wallet.getCash() - amount) < 0){
			return ReturnInfoUtils.errorInfo("资金不足,无法进行结算!");
		}
		return ReturnInfoUtils.successInfo();
	}
}
