package org.silver.shop.impl.system.tenant;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.dao.system.tenant.MerchantWalletDao;
import org.silver.shop.impl.system.manual.MpayServiceImpl;
import org.silver.shop.model.system.log.MerchantWalletLog;
import org.silver.shop.model.system.log.PaymentReceiptLog;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.AgentWalletContent;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantWalletService.class)
public class MerchantWalletServiceImpl implements MerchantWalletService {
	private static Logger logger = LogManager.getLogger(Object.class);
	@Autowired
	private MerchantWalletDao merchantWalletDao;
	@Autowired
	private WalletUtils walletUtils;

	@Override
	public Map<String, Object> getMerchantWallet(String merchantId, String merchantName) {
		return walletUtils.checkWallet(1, merchantId, merchantName);
	}

	@Override
	public Map<String, Object> walletDeduction(MerchantWalletContent merchantWallet, double balance,
			double serviceFee) {
		if (merchantWallet == null) {
			return ReturnInfoUtils.errorInfo("商户钱包扣费时,请求参数不能为空!");
		}
		// 扣除服务费后余额
		double surplus = balance - serviceFee;
		if (surplus < 0) {
			return ReturnInfoUtils.errorInfo("余额不足,请先充值后再进行操作!");
		}
		merchantWallet.setBalance(surplus);
		merchantWallet.setUpdateDate(new Date());
		merchantWallet.setUpdateBy("system");
		if (!merchantWalletDao.update(merchantWallet)) {
			return ReturnInfoUtils.errorInfo("钱包结算手续费失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public void addWalletRechargeLog(String merchantId, String merchantName, double amount, String orderId) {
		if (StringEmptyUtils.isNotEmpty(merchantId) && StringEmptyUtils.isNotEmpty(merchantName)
				&& StringEmptyUtils.isNotEmpty(orderId) && amount >= 0.01) {
			Map<String, Object> reWalletMap = walletUtils.checkWallet(1, merchantId, merchantName);
			if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
				logger.error("--查询商户钱包信息失败-->" + reWalletMap.get(BaseCode.MSG.toString()));
			}
			MerchantWalletContent merchantWallet = (MerchantWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
			//
			PaymentReceiptLog log = new PaymentReceiptLog();
			log.setUserId(merchantId);
			log.setUserName(merchantName);
			log.setOrderId(orderId);
			log.setBeforeChangingBalance(merchantWallet.getBalance());
			log.setAmount(amount);
			log.setAfterChangeBalance(merchantWallet.getBalance() + amount);
			log.setType("recharge");
			log.setTradingStatus("process");
			log.setCreateBy(merchantName);
			log.setCreateDate(new Date());
			if (!merchantWalletDao.add(log)) {
				logger.error(merchantName + "--商户钱包充值日志记录失败--");
			}
		}
	}

}
