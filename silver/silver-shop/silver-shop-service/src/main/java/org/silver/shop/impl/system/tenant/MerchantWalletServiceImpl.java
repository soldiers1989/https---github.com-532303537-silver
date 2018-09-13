package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.TradeReceiptLogService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.dao.system.tenant.MerchantWalletDao;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.log.OfflineRechargeLog;
import org.silver.shop.model.system.log.TradeReceiptLog;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.OfflineRechargeContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.DoubleOperationUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = MerchantWalletService.class)
public class MerchantWalletServiceImpl implements MerchantWalletService {

	private static Logger logger = LogManager.getLogger(Object.class);
	@Autowired
	private MerchantWalletDao merchantWalletDao;
	@Autowired
	private WalletUtils walletUtils;
	@Autowired
	private TradeReceiptLogService tradeReceiptLogService;
	@Autowired
	private MerchantUtils merchantUtils;

	@Override
	public Map<String, Object> getMerchantWallet(String merchantId, String merchantName) {
		return walletUtils.checkWallet(1, merchantId, merchantName);
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
			TradeReceiptLog log = new TradeReceiptLog();
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

	@Override
	public Map<String, Object> merchantApplication(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		JSONArray arr = new JSONArray();
		arr.add(datasMap);
		Map<String, Object> reCheckMap = checkData(arr);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		OfflineRechargeContent content = new OfflineRechargeContent();
		long idCount = merchantWalletDao.findByPropertyCount(OfflineRechargeContent.class, null);
		if (idCount < 0) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		}
		String offlineRechargeId = SerialNoUtils.getSerialNo("OR", idCount);
		content.setOfflineRechargeId(offlineRechargeId);
		content.setApplicantId(datasMap.get("merchantId") + "");
		content.setApplicant(datasMap.get("merchantName") + "");
		String beneficiaryAccountType = datasMap.get("beneficiaryAccountType") + "";
		Map<String, Object> reChooseMap = chooseBeneficiaryAccount(beneficiaryAccountType, content);
		if (!"1".equals(reChooseMap.get(BaseCode.STATUS.toString()))) {
			return reChooseMap;
		}
		double amount;
		try {
			amount = Double.parseDouble(datasMap.get("remittanceAmount") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("提交失败，金额错误！");
		}
		if (amount < 0.01) {
			return ReturnInfoUtils.errorInfo("提交失败，汇款金额不能低于0.01元！");
		}
		content.setRemittanceAmount(amount);
		content.setRemittanceAccount(datasMap.get("remittanceAccount") + "");
		content.setRemittanceName(datasMap.get("remittanceName") + "");
		content.setRemittanceBank(datasMap.get("remittanceBank") + "");
		content.setRemittanceSerialNo(datasMap.get("remittanceSerialNo") + "");
		String remittanceDate = datasMap.get("remittanceDate") + "";
		try {
			content.setRemittanceDate(DateUtil.parseDate(remittanceDate, "yyyy-MM-dd"));
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("汇款时间错误！");
		}
		content.setRemittanceReceipt(datasMap.get("remittanceReceipt") + "");
		// 审核类型：firstTrial-运营初审、financialAudit-财务审核、end-结束
		content.setReviewerType("firstTrial");
		content.setCreateBy(datasMap.get("merchantName") + "");
		content.setCreateDate(new Date());
		if (!merchantWalletDao.add(content)) {
			return ReturnInfoUtils.errorInfo("提交失败,服务器繁忙！");
		}
		OfflineRechargeLog logs = new OfflineRechargeLog();
		logs.setOfflineRechargeId(offlineRechargeId);
		logs.setCurrentNodeName("运营审核");
		// 审核标识：1-待审核、2-审核通过、3-审核不通过
		logs.setReviewerFlag(1);
		logs.setCreateBy(datasMap.get("merchantName") + "");
		logs.setCreateDate(new Date());
		if (!merchantWalletDao.add(logs)) {
			return ReturnInfoUtils.errorInfo("提交失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据收款人账号类型，选择对应的收款人信息
	 * 
	 * @param beneficiaryAccountType
	 *            收款人账号类型：public-对公账号、private-对私账号
	 * @param content
	 * @return
	 */
	private Map<String, Object> chooseBeneficiaryAccount(String beneficiaryAccountType,
			OfflineRechargeContent content) {
		if (StringEmptyUtils.isEmpty(beneficiaryAccountType)) {
			return ReturnInfoUtils.errorInfo("收款账号类型不能为空！");
		}
		if (content == null) {
			return ReturnInfoUtils.errorInfo("申请参数不能为null！");
		}
		switch (beneficiaryAccountType) {
		case "public":
			content.setBeneficiaryAccount("广州银盟信息科技有限公司");
			content.setBeneficiaryName("3602062709200219758");
			content.setBeneficiaryBank("中国工商银行股份有限公司广州科技园支行");
			break;
		case "private":
			return ReturnInfoUtils.errorInfo("暂不允许对私账号！");
		default:
			return ReturnInfoUtils.errorInfo("未知账号类型！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验商户提交的线下加款申请参数是否齐全
	 * 
	 * @param jsonList
	 * @return
	 */
	private Map<String, Object> checkData(JSONArray jsonList) {
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("remittanceAmount");
		noNullKeys.add("remittanceAccount");
		noNullKeys.add("remittanceName");
		noNullKeys.add("remittanceBank");
		noNullKeys.add("remittanceDate");
		noNullKeys.add("remittanceSerialNo");
		return CheckDatasUtil.changeOfflineRechargeMsg(jsonList, noNullKeys);
	}

	@Override
	public Map<String, Object> getOfflineRechargeInfo(Map<String, Object> datasMap, int page, int size) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String, Object> reDatasMap = SearchUtils.universalOfflineRechargeSearch(datasMap);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		paramMap.put("deleteFlag", 0);
		List<OfflineRechargeContent> reList = merchantWalletDao.findByProperty(OfflineRechargeContent.class, paramMap,
				page, size);
		long count = merchantWalletDao.findByPropertyCount(OfflineRechargeContent.class, paramMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据！");
		}
	}

	@Override
	public Map<String, Object> fenZhang(String orderId, double amount, Manager managerInfo) {
		Map<String, Object> params = new HashMap<>();
		params.put("entOrderNo", orderId);
		List<OrderRecordContent> reList = merchantWalletDao.findByProperty(OrderRecordContent.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询订单信息失败！");
		} else if (!reList.isEmpty()) {
			OrderRecordContent order = reList.get(0);
			return initiateFenZhang(order, amount, managerInfo);
		} else {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]未找到对应订单信息！");
		}
	}

	private Map<String, Object> initiateFenZhang(OrderRecordContent order, double amount, Manager managerInfo) {
		if (StringEmptyUtils.isEmpty(amount) || order == null || managerInfo == null) {
			return ReturnInfoUtils.errorInfo("发起分帐时，请求参数不能为null");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(order.getMerchantId());
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		Map<String, Object> item = new HashMap<>();
		item.put("out_trade_no", order.getEntOrderNo());
		item.put("total_amount", order.getActualAmountPaid());
		item.put("master_user_code", "yinmeng1119");
		if (amount > order.getActualAmountPaid()) {
			return ReturnInfoUtils.errorInfo("结算金额[" + amount + "]不能大于订单金额[" + order.getActualAmountPaid() + "]");
		}
		// 平台服务费(原分润),0.0X
		item.put("master_amount", DoubleOperationUtil.mul(order.getActualAmountPaid(), merchant.getMerchantProfit()));
		// https://ym.191ec.com/silver-web-shop/yspay-receive/orderReceive
		item.put("notify_url", "https://ym.191ec.com/silver-web-shop/yspay-receive/fenZhangReceive");
		JSONArray divList = new JSONArray();
		JSONObject subJson1 = new JSONObject();
		subJson1.put("division_mer_usercode", "yinmeng1116");
		subJson1.put("div_amount", amount);
		divList.add(subJson1);
		item.put("fz_content", divList);
		// String result =
		// YmHttpUtil.HttpPost("http://192.168.1.161:8080/silver-web-ezpay/fz/trade",
		// item);
		String result = YmHttpUtil.HttpPost("https://ezpay.191ec.com/silver-web-ezpay/fz/trade", item);
		System.out.println("--->>" + result);
		if (StringEmptyUtils.isEmpty(result)) {
			return ReturnInfoUtils.errorInfo("操作失败，服务器繁忙！");
		} else {
			JSONObject json = JSONObject.fromObject(result);
			if (!"1".equals(json.get(BaseCode.STATUS.toString()) + "")) {
				return ReturnInfoUtils.errorInfo(json.get("msg") + "");
			}
			Map<String, Object> reLogMap = tradeReceiptLogService.addMerchantLog(order.getMerchantId(), amount,
					order.getEntOrderNo(), managerInfo.getManagerName(), "withdraw");
			if (!"1".equals(reLogMap.get(BaseCode.STATUS.toString()))) {
				return reLogMap;
			}
			//
			return walletCashDeduction(order.getMerchantId(), amount);
		}
	}

	/**
	 * 商户现金(货款)的扣款与加款
	 * 
	 * @param merchantId
	 *            商户id
	 * @param amount
	 *            金额
	 * @return Map
	 */
	private Map<String, Object> walletCashDeduction(String merchantId, double amount) {
		//
		Map<String, Object> reWalletMap = walletUtils.checkWallet(1, merchantId, "");
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return reWalletMap;
		}
		MerchantWalletContent merchantWallet = (MerchantWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		double oldCash = merchantWallet.getCash();

		merchantWallet.setCash(amount - oldCash);
		merchantWallet.setFreezingFunds(amount - oldCash);
		if (!merchantWalletDao.update(merchantWallet)) {
			return ReturnInfoUtils.errorInfo("更新钱包现金(货款)失败！");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> freezingFundFeduction(MerchantWalletContent merchantWallet, double amount) {
		if (merchantWallet == null) {
			return ReturnInfoUtils.errorInfo("商户钱包扣费时,请求参数不能为空!");
		}
		double freezingFunds = merchantWallet.getFreezingFunds();
		double surplus = 0;
		try {
			surplus = DoubleOperationUtil.sub(freezingFunds, amount);
			logger.error("--金额->" + amount + "----剩余冻结资金-->" + surplus);
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("冻结资金扣款失败，金额错误");
		}
		if (surplus < 0) {
			return ReturnInfoUtils.errorInfo("扣款失败,账户冻结资金不足，请联系管理员！");
		}
		// 扣除手续费后剩余的冻结资金
		merchantWallet.setFreezingFunds(surplus);
		merchantWallet.setUpdateDate(new Date());
		if (!merchantWalletDao.update(merchantWallet)) {
			return ReturnInfoUtils.errorInfo("钱包结算手续费失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> balanceTransferFreezingFunds(MerchantWalletContent merchantWallet, double amount) {
		if (merchantWallet == null) {
			return ReturnInfoUtils.errorInfo("转移冻结资金请求参数不能为null");
		}
		double oldBalance = merchantWallet.getBalance();
		try {
			double newBalance = DoubleOperationUtil.sub(oldBalance, amount);
			// double newBalance = Double.parseDouble(format.format(oldBalance -
			// amount));
			merchantWallet.setFreezingFunds(amount);
			logger.error("-余额转移至冻结金额为->>" + amount);
			// 将余额转移至冻结金额
			merchantWallet.setBalance(newBalance);
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("余额转移冻结，金额错误！");
		}
		return updateWallet(merchantWallet);
	}

	/**
	 * 根据类型校验钱包是否有足够的金额，进行扣款
	 * 
	 * @param merchantId
	 *            商户
	 * @param amount
	 *            金额
	 * @param type
	 *            类型：balance-余额、reserveAmount-储备资金、freezingFunds-冻结资金
	 * 
	 */
	private Map<String, Object> checkEnoughMoney(String merchantId, double amount, String type) {
		MerchantWalletContent entity = findByIdWallet(merchantId);
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("钱包查询失败！");
		}
		switch (type) {
		case "balance":
			if (DoubleOperationUtil.sub(entity.getBalance(), amount) < 0) {
				return ReturnInfoUtils.errorInfo("操作失败，余额不足！");
			}
			break;
		case "reserveAmount":
			if (DoubleOperationUtil.sub(entity.getReserveAmount(), amount) < 0) {
				return ReturnInfoUtils.errorInfo("操作失败，资金不足！");
			}
			break;
		case "freezingFunds":
			if (DoubleOperationUtil.sub(entity.getFreezingFunds(), amount) < 0) {
				return ReturnInfoUtils.errorInfo("操作失败，冻结金额不足！");
			}
			break;
		default:
			return ReturnInfoUtils.errorInfo("校验钱包余额是否满足扣款，未知类型[" + type + "]");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据商户id查询、钱包信息
	 * 
	 * @param merchantId
	 *            商户id
	 * @return MerchantWalletContent 商户钱包实体
	 */
	private MerchantWalletContent findByIdWallet(String merchantId) {
		Map<String, Object> reWalletMap = walletUtils.checkWallet(1, merchantId, null);
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			logger.error("--商户钱包查询失败-->" + reWalletMap.toString());
			return null;
		}
		return (MerchantWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
	}

	@Override
	public Map<String, Object> reserveAmountOperating(String merchantId, double amount, String type) {
		Map<String, Object> verifyMap = walletUtils.verifySign(1, merchantId);
		if (!"1".equals(verifyMap.get(BaseCode.STATUS.toString()))) {
			return verifyMap;
		}
		MerchantWalletContent entity = findByIdWallet(merchantId);
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("查询资金失败！");
		}
		double oldAmount = entity.getReserveAmount();
		switch (type) {
		case "add":// 加款
			entity.setReserveAmount(DoubleOperationUtil.add(oldAmount, amount));
			break;
		case "sub":// 扣款,并将金额转移至冻结金额
			entity.setReserveAmount(DoubleOperationUtil.sub(oldAmount, amount));
			double oldFreezingFunds = entity.getFreezingFunds();
			entity.setFreezingFunds(DoubleOperationUtil.add(oldFreezingFunds, amount));
			break;
		default:
			return ReturnInfoUtils.errorInfo("操作失败，未知类型[" + type + "]");
		}
		String sign = WalletUtils.generateSign(entity.getWalletId(), entity.getBalance(), entity.getReserveAmount(),
				entity.getFreezingFunds(), entity.getCash());
		entity.setVerifyCode(sign);
		return updateWallet(entity);
	}

	/**
	 * 更新商户钱包
	 * 
	 * @param entity
	 *            商户钱包实体
	 * @return Map
	 */
	private Map<String, Object> updateWallet(MerchantWalletContent entity) {
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("更新钱包时，请求参数不能为null");
		}
		entity.setUpdateDate(new Date());
		if (!merchantWalletDao.update(entity)) {
			return ReturnInfoUtils.errorInfo("商户钱包更新失败！");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> generateSign(String merchantId) {
		if (StringEmptyUtils.isNotEmpty(merchantId)) {
			MerchantWalletContent entity = findByIdWallet(merchantId);
			if (entity == null) {
				return ReturnInfoUtils.errorInfo("钱包查询失败！");
			}
			String newSign = WalletUtils.generateSign(entity.getWalletId(), entity.getBalance(),
					entity.getReserveAmount(), entity.getFreezingFunds(), entity.getCash());
			entity.setVerifyCode(newSign);
			return updateWallet(entity);
		} else {
			List<MerchantWalletContent> reList = merchantWalletDao.findByProperty(MerchantWalletContent.class, null, 0,
					0);
			if (reList != null && !reList.isEmpty()) {
				for (MerchantWalletContent entity : reList) {
					String newSign = WalletUtils.generateSign(entity.getWalletId(), entity.getBalance(),
							entity.getReserveAmount(), entity.getFreezingFunds(), entity.getCash());
					entity.setVerifyCode(newSign);					
					Map<String, Object> reUpdateMap = updateWallet(entity);
					if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
						return reUpdateMap;
					}
				}
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> balanceOperating(String merchantId, double amount, String type) {
		Map<String, Object> verifyMap = walletUtils.verifySign(1, merchantId);
		if (!"1".equals(verifyMap.get(BaseCode.STATUS.toString()))) {
			return verifyMap;
		}
		MerchantWalletContent entity = findByIdWallet(merchantId);
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("查询资金失败！");
		}
		double oldBalance = entity.getBalance();
		switch (type) {
		case "add":// 加款
			entity.setBalance(DoubleOperationUtil.add(oldBalance, amount));
			break;
		case "sub":// 扣款,并将金额转移至冻结金额
			entity.setBalance(DoubleOperationUtil.sub(oldBalance, amount));
			double oldFreezingFunds = entity.getFreezingFunds();
			entity.setFreezingFunds(DoubleOperationUtil.add(oldFreezingFunds, amount));
			break;
		default:
			return ReturnInfoUtils.errorInfo("操作失败，未知类型[" + type + "]");
		}
		String sign = WalletUtils.generateSign(entity.getWalletId(), entity.getBalance(), entity.getReserveAmount(),
				entity.getFreezingFunds(), entity.getCash());
		entity.setVerifyCode(sign);
		return updateWallet(entity);
	}

	/**
	 * 商户余额清算
	 * 
	 * @param merchantId
	 *            商户id
	 * @param amount
	 *            金额
	 * @return Map
	 */
	@Override
	public Map<String, Object> balanceDeduction(String merchantId, Double amount) {
		Map<String, Object> checkMap = checkEnoughMoney(merchantId, amount, "balance");
		if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()))) {
			return checkMap;
		}
		return balanceOperating(merchantId, amount, "sub");
	}
	


	@Override
	public Map<String, Object> freezingFundsOperating(String merchantId, double amount, String type) {
		Map<String, Object> verifyMap = walletUtils.verifySign(1, merchantId);
		if (!"1".equals(verifyMap.get(BaseCode.STATUS.toString()))) {
			return verifyMap;
		}
		MerchantWalletContent entity = findByIdWallet(merchantId);
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("钱包查询失败！");
		}
		// 获取钱包原冻结金额
		double oldFreezingFunds = entity.getFreezingFunds();
		switch (type) {
		case "add":// 冻结资金加钱
			entity.setFreezingFunds(DoubleOperationUtil.add(oldFreezingFunds, amount));
			break;
		case "sub":// 冻结资金扣钱
			double remaining = DoubleOperationUtil.sub(entity.getFreezingFunds(), amount);
			if ( remaining < 0) {
				return ReturnInfoUtils.errorInfo("操作失败，冻结金额不足！");
			}
			entity.setFreezingFunds(remaining);
			break;
		default:
			return ReturnInfoUtils.errorInfo("操作失败，未知类型[" + type + "]");
		}
		String sgin = WalletUtils.generateSign(entity.getWalletId(), entity.getBalance(),
				entity.getReserveAmount(), entity.getFreezingFunds(),entity.getCash());
		entity.setVerifyCode(sgin);
		return updateWallet(entity);
	}
	
}
