package org.silver.shop.service.system.tenant;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.silver.util.FileUpLoadService;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
	@Autowired
	private FileUpLoadService fileUpLoadService;

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
	 * 获取商户银行卡信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @return Map
	 */
	public Map<String, Object> getMerchantBankInfo(String merchantId) {
		return merchantBankInfoService.getMerchantBankInfo(merchantId, 0, 0, 1);
	}

	// 添加交易记录
	public Map<String, Object> addPaymentReceiptLog(String merchantId, double amount, String serialNo, String type) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		String managerName = managerInfo.getManagerName();
		return paymentReceiptLogService.addMerchantLog(merchantId, amount, serialNo, managerName, type);
	}

	/**
	 * 根据商户Id查询商户钱包信息,后校验商户钱包是否有足够的现金结算
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param amount
	 */
	public Map<String, Object> getMerchantWallet(String merchantId, double amount) {
		Map<String, Object> reMap = merchantWalletService.getMerchantWallet(merchantId, null);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		if ((wallet.getCash() - amount) < 0) {
			return ReturnInfoUtils.errorInfo("资金不足,无法进行结算!");
		}
		return ReturnInfoUtils.successInfo();
	}

	public Map<String, Object> offlineRechargeApplication(HttpServletRequest req, Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		datasMap.put("merchantId", merchantId);
		datasMap.put("merchantName", merchantName);
		String storePath = "/opt/www/img/merchantApplication/" + merchantId + "/";
		// String storePath = "D:\\";
		Map<String, Object> reFileMap = fileUpLoadService.universalDoUpload(req, storePath, ".jpg", false, 800, 800,
				null);
		if (!"1".equals(reFileMap.get(BaseCode.STATUS.toString()) + "")) {
			return reFileMap;
		}
		List<String> fileList = (List<String>) reFileMap.get(BaseCode.DATAS.toString());
		if (fileList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请上传至少一张银行回执图片！");
		}
		StringBuilder path = new StringBuilder("https://ym.191ec.com/img/merchantApplication/" + merchantId + "/");
		for (int i = 0; i < fileList.size(); i++) {
			String name = fileList.get(i);
			path.append(name + "#");
		}
		datasMap.put("remittanceReceipt", path);
		return merchantWalletService.merchantApplication(datasMap);
	}

	// 商户查询钱包线下充值信息
	public Map<String, Object> getOfflineRechargeInfo(Map<String, Object> datasMap, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		datasMap.put("applicantId", merchantId);
		return merchantWalletService.getOfflineRechargeInfo(datasMap, page, size);
	}

	public Map<String,Object> fenZhang(String orderId, double amount) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		return merchantWalletService.fenZhang(orderId, amount,managerInfo);
	}
}
