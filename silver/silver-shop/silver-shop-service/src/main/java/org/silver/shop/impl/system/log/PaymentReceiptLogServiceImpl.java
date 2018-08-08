package org.silver.shop.impl.system.log;

import java.util.Date;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.PaymentReceiptLogService;
import org.silver.shop.dao.system.log.PaymentReceiptLogDao;
import org.silver.shop.model.system.log.PaymentReceiptLog;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = PaymentReceiptLogService.class)
public class PaymentReceiptLogServiceImpl implements PaymentReceiptLogService {

	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private PaymentReceiptLogDao paymentReceiptLogDao;
	@Autowired
	private WalletUtils walletUtils;

	@Override
	public Map<String, Object> addMerchantLog(String merchantId, double amount, String orderId, String operator,
			String type) {
		if (StringEmptyUtils.isEmpty(merchantId) || StringEmptyUtils.isEmpty(amount)
				|| StringEmptyUtils.isEmpty(orderId)) {
			return ReturnInfoUtils.errorInfo("添加交易日志时,请求参数不能为空");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reWalletMap = walletUtils.checkWallet(1, merchantId, "");
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return reWalletMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		PaymentReceiptLog log = new PaymentReceiptLog();
		log.setUserId(merchant.getMerchantId());
		log.setUserName(merchant.getMerchantName());
		log.setOrderId(orderId);
		double cash = wallet.getCash();
		log.setBeforeChangingBalance(cash);
		log.setAmount(amount);
		// 类型：recharge(充值)、transfer(转账)、withdraw(提现)
		if ("recharge".equals(type)) {
			log.setAfterChangeBalance(cash + amount);
		}else{
			log.setAfterChangeBalance(cash - amount);
		}
		log.setType("withdraw");
		// 状态：success(交易成功)、failure(交易失败)、process(处理中)
		log.setTradingStatus("process");
		log.setRemark("管理员对[" + merchant.getMerchantName() + "]进行资金清算");
		if (StringEmptyUtils.isNotEmpty(operator)) {
			log.setCreateBy(operator);
		} else {
			log.setCreateBy("system");
		}
		log.setCreateDate(new Date());
		if (!paymentReceiptLogDao.add(log)) {
			return ReturnInfoUtils.errorInfo("保存商户交易记录失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

}
