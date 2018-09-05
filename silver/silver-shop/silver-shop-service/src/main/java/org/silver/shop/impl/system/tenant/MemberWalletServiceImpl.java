package org.silver.shop.impl.system.tenant;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.MemberWalletLogService;
import org.silver.shop.api.system.log.MerchantWalletLogService;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.api.system.tenant.MemberWalletService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.tenant.MemberWalletDao;
import org.silver.shop.model.system.log.TradeReceiptLog;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.WalletUtils;
import org.silver.util.DoubleOperationUtil;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MemberWalletService.class)
public class MemberWalletServiceImpl implements MemberWalletService {

	/**
	 * 钱包计算数只保留后五位
	 */
	private static DecimalFormat format = new DecimalFormat("#.00000");

	/**
	 * 储备金额-暂定用于商户订单导入时选择指定的用户进行资金扣费
	 */
	private static String RESERVE_AMOUNT = "reserveAmount";

	private static Logger logger = LogManager.getLogger(Object.class);

	@Autowired
	private MemberWalletDao memberWalletDao;
	@Autowired
	private MerchantWalletLogService merchantWalletLogService;
	@Autowired
	private MemberWalletLogService memberWalletLogService;
	@Autowired
	private WalletUtils walletUtils;
	
	
	@Override
	public void reserveAmountTransfer(String memberId, String merchantId, String tradeNo, Double amount) {
		System.out.println("---开始支付单储备资金结算---");
		try {
			if (StringEmptyUtils.isEmpty(memberId) || StringEmptyUtils.isEmpty(merchantId)
					|| StringEmptyUtils.isEmpty(tradeNo) || amount < 0.01) {
				logger.error("--商户清算用户储备资金--请求参数错误--");
				throw new Exception();
			}
			// 用户储备资金扣款
			Map<String, Object> reMemberMap = memberWalletReserveAmountTransfer(memberId, amount);
			if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
				logger.error(reMemberMap.get(BaseCode.MSG.toString()));
				throw new Exception(reMemberMap.get(BaseCode.MSG.toString()) + "");
			}
			MemberWalletContent memberWalletContent = (MemberWalletContent) reMemberMap.get(BaseCode.DATAS.toString());
			Map<String, Object> params = new HashMap<>();
			params.put("memberWalletId", memberWalletContent.getWalletId());
			params.put("memberName", memberWalletContent.getMemberName());
			// 交易流水号
			params.put("serialNo", tradeNo);
			// 交易名称
			params.put("serialName", "转账");
			params.put("beforeChangingBalance",
					DoubleOperationUtil.add(memberWalletContent.getReserveAmount(), amount));
			params.put("amount", amount);
			params.put("afterChangeBalance", memberWalletContent.getReserveAmount());
			// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
			params.put("type", 5);
			// 交易状态
			params.put("status", "success");
			// 进出帐标识
			params.put("flag", "out");
			Map<String, Object> merchantWalletMap = walletUtils.checkWallet(1, merchantId, "");
			if (!"1".equals(merchantWalletMap.get(BaseCode.STATUS.toString()))) {
				logger.error(merchantWalletMap.get(BaseCode.MSG.toString()));
				throw new Exception(merchantWalletMap.get(BaseCode.MSG.toString()) + "");
			}
			MerchantWalletContent merchantWallet = (MerchantWalletContent) merchantWalletMap
					.get(BaseCode.DATAS.toString());
			params.put("targetWalletId", merchantWallet.getWalletId());
			params.put("targetName", merchantWallet.getMerchantName());

			// 用户钱包日志记录
			Map<String, Object> reMemberWalletLogMap = memberWalletLogService.addWalletLog(params);
			if (!"1".equals(reMemberWalletLogMap.get(BaseCode.STATUS.toString()))) {
				logger.error(reMemberWalletLogMap.get(BaseCode.MSG.toString()));
				throw new Exception(reMemberWalletLogMap.get(BaseCode.MSG.toString()) + "");
			}
			// 商户储备资金收款
			Map<String, Object> reMerchantMap = merchantWalletReserveAmountTransfer(merchantWallet, amount);
			if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
				logger.error(reMerchantMap.get(BaseCode.MSG.toString()));
				throw new Exception(reMerchantMap.get(BaseCode.MSG.toString()) + "");
			}
			params.clear();
			params.put("walletId", merchantWallet.getWalletId());
			params.put("memberName", merchantWallet.getMerchantName());
			// 交易流水号
			params.put("serialNo", tradeNo);
			// 交易名称
			params.put("serialName", "转账");
			params.put("balance", DoubleOperationUtil.sub(merchantWallet.getReserveAmount(), amount));
			params.put("amount", amount);
			// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
			params.put("type", 5);
			// 交易状态
			params.put("status", "success");
			// 进出帐标识
			params.put("flag", "in");
			params.put("targetWalletId", memberWalletContent.getWalletId());
			params.put("targetName", memberWalletContent.getMemberName());
			params.put("merchantId", merchantWallet.getMerchantId());
			Map<String, Object> reMerchantWalletLogMap = merchantWalletLogService.addWalletLog(params);
			if (!"1".equals(reMerchantWalletLogMap.get(BaseCode.STATUS.toString()))) {
				logger.error(reMerchantWalletLogMap.get(BaseCode.MSG.toString()));
				throw new Exception(reMerchantWalletLogMap.get(BaseCode.MSG.toString()) + "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("---结束支付单储备资金结算---");
	}

	/**
	 * 商户钱包收取关联用户的货款
	 * 
	 * @param merchantWallet
	 * @param amount
	 * @return
	 */
	private Map<String, Object> merchantWalletReserveAmountTransfer(MerchantWalletContent merchantWallet,
			Double amount) {
		if (merchantWallet == null) {
			return ReturnInfoUtils.errorInfo("商户钱包储备资金收款时请求参数不能为空！");
		}
		// 原储备资金
		double oldReserveAmount = merchantWallet.getReserveAmount();
		merchantWallet.setReserveAmount(Double.parseDouble(format.format(oldReserveAmount + amount)));
		merchantWallet.setUpdateBy("system");
		merchantWallet.setUpdateDate(new Date());
		if (memberWalletDao.update(merchantWallet)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("商户钱包储备资金收款失败！");
	}

	/**
	 * 用户储备资金的扣款
	 * 
	 * @param memberId
	 * @param amount
	 * @return
	 */
	private Map<String, Object> memberWalletReserveAmountTransfer(String memberId, Double amount) {
		Map<String, Object> checkMap = checkEnoughMoney(memberId, amount, RESERVE_AMOUNT);
		if (!"1".equals(checkMap.get(BaseCode.STATUS.toString()))) {
			return checkMap;
		}
		return reserveAmountOperating(memberId, amount, "sub");
	}

	/**
	 * 更新钱包信息，并将更新后的钱包信息返回
	 * 
	 * @param entity
	 *            钱包信息实体类
	 * @return Map
	 */
	private Map<String, Object> updateWallet(MemberWalletContent entity) {
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("更新钱包失败，请求参数不能为null");
		}
		entity.setUpdateDate(new Date());
		if (!memberWalletDao.update(entity)) {
			return ReturnInfoUtils.errorInfo("更新失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successDataInfo(entity);
	}

	@Override
	public Map<String, Object> addPayReceipt(Member memberInfo, double amount, String serialNo) {
		if (memberInfo == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		if (amount < 0.01) {
			return ReturnInfoUtils.errorInfo("金额错误！");
		}
		if (StringEmptyUtils.isEmpty(serialNo)) {
			return ReturnInfoUtils.errorInfo("交易订单号不能为空！");
		}
		// Map<String, Object> reWalletMap = walletUtils.checkWallet(2,
		// memberInfo.getMemberId(),null);
		// if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
		// logger.error("--查询钱包信息失败-->" +
		// reWalletMap.get(BaseCode.MSG.toString()));
		// }
		// MemberWalletContent wallet = (MemberWalletContent)
		// reWalletMap.get(BaseCode.DATAS.toString());
		//
		TradeReceiptLog log = new TradeReceiptLog();
		log.setUserId(memberInfo.getMemberId());
		log.setUserName(memberInfo.getMemberName());
		log.setOrderId(serialNo);
		// log.setBeforeChangingBalance(merchantWallet.getBalance());
		log.setAmount(amount);
		// log.setAfterChangeBalance(merchantWallet.getBalance() + amount);
		// 类型：recharge(充值)、transfer(转账)、withdraw(提现)
		log.setType("recharge");
		// 状态：success(交易成功)、failure(交易失败)、process(处理中)
		log.setTradingStatus("process");
		log.setCreateBy(memberInfo.getMemberName());
		log.setCreateDate(new Date());
		if (!memberWalletDao.add(log)) {
			return ReturnInfoUtils.errorInfo("保存交易日志错误，服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getInfo(String memberId) {
		return walletUtils.checkWallet(2, memberId, null);
	}

	@Override
	public Map memberRechargeReceive(Map datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		String tradeNo = datasMap.get("trade_no") + "";
		String outTradeNo = datasMap.get("out_trade_no") + "";
		Map<String, Object> rePayLogMap = updatePaymentReceiptLog(outTradeNo, tradeNo);
		if (!"1".equals(rePayLogMap.get(BaseCode.STATUS.toString()))) {
			return rePayLogMap;
		}
		TradeReceiptLog log = (TradeReceiptLog) rePayLogMap.get(BaseCode.DATAS.toString());
		double amount = Double.parseDouble(datasMap.get("total_amount") + "");
		Map<String, Object> reUpdateMap = reserveAmountOperating(log.getUserId(), amount, "add");
		if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
			return reUpdateMap;
		}
		MemberWalletContent wallet = (MemberWalletContent) reUpdateMap.get(BaseCode.DATAS.toString());
		Map<String, Object> datas = new HashMap<>();
		datas.put("memberWalletId", wallet.getWalletId());
		datas.put("memberName", wallet.getMemberName());
		// 交易流水号
		datas.put("serialNo", tradeNo);
		// 交易名称
		datas.put("serialName", "充值");
		datas.put("beforeChangingBalance", wallet.getReserveAmount() - amount);
		datas.put("amount", amount);
		datas.put("afterChangeBalance", wallet.getReserveAmount());
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
		datas.put("type", 2);
		// 交易状态
		datas.put("status", "success");
		// 进出帐标识
		datas.put("flag", "in");
		datas.put("targetWalletId", "000000");
		datas.put("targetName", "银盛");
		// 用户钱包日志记录
		return memberWalletLogService.addWalletLog(datas);
	}

	/**
	 * 用户统一的更新钱包储备资金方法
	 * 
	 * @param memberId
	 *            用户id
	 * @param amount
	 *            金额
	 * @param flag
	 *            标识：in-进账、out-出账
	 * @return Map datas-用户钱包信息
	 */
	private Map<String, Object> updateReserveAmount(String memberId, double amount, String flag) {
		Map<String, Object> reWalletMap = walletUtils.checkWallet(2, memberId, null);
		MemberWalletContent wallet = (MemberWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		if (amount < 0.01) {
			return ReturnInfoUtils.errorInfo("更新储备资金，金额错误！");
		}
		double oldReserveAmount = wallet.getReserveAmount();
		double newReserveAmount = 0;
		switch (flag) {
		case "in":
			newReserveAmount = oldReserveAmount + amount;
			break;
		case "out":
			newReserveAmount = oldReserveAmount - amount;
			break;
		default:
			return ReturnInfoUtils.errorInfo("更新失败，未知标识！");
		}
		wallet.setReserveAmount(newReserveAmount);
		wallet.setUpdateDate(new Date());
		if (!memberWalletDao.update(wallet)) {
			return ReturnInfoUtils.errorInfo("更新用户储备资金失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successDataInfo(wallet);
	}

	/**
	 * 更新用户充值的交易日志记录信息
	 * 
	 * @param outTradeNo
	 *            交易订单号
	 * @param tradeNo
	 *            成功后、交易流水号
	 * @return Map
	 */
	private Map<String, Object> updatePaymentReceiptLog(String outTradeNo, String tradeNo) {
		if (StringEmptyUtils.isEmpty(tradeNo) || StringEmptyUtils.isEmpty(outTradeNo)) {
			return ReturnInfoUtils.errorInfo("更新交易日志时，流水号或订单不能为空！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("orderId", outTradeNo);
		List<TradeReceiptLog> reList = memberWalletDao.findByProperty(TradeReceiptLog.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询交易日志失败，服务器繁忙！");
		} else if (reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("交易订单号[" + outTradeNo + "]未找到对应的交易日志记录！");
		}
		TradeReceiptLog log = reList.get(0);
		log.setTradeNo(tradeNo);
		// 交易状态：success(交易成功)、failure(交易失败)、process(处理中)
		log.setTradingStatus("success");
		log.setUpdateDate(new Date());
		if (!memberWalletDao.update(log)) {
			return ReturnInfoUtils.errorInfo("更新日志信息失败，服务器繁忙！");
		}
		return ReturnInfoUtils.successDataInfo(log);
	}

	/**
	 * 根据用户id查询用户钱包信息
	 * 
	 * @param memberId
	 *            用户钱包
	 * @return MemberWalletContent 用户钱包实体信息
	 */
	private MemberWalletContent findByIdWallet(String memberId) {
		Map<String, Object> reWalletMap = walletUtils.checkWallet(2, memberId, null);
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			logger.error("--用户钱包查询失败-->" + reWalletMap.toString());
			return null;
		}
		return (MemberWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
	}

	@Override
	public Map<String, Object> checkReserveAmount(String memberId, double amount) {
		return checkEnoughMoney(memberId, amount, "reserveAmount");
	}

	/**
	 * 根据类型校验钱包是否有足够的金额，进行扣款
	 * 
	 * @param memberId
	 * 
	 * @param amount
	 *            金额
	 * @param type
	 *            类型：balance-余额、reserveAmount-储备资金、freezingFunds-冻结资金
	 * 
	 */
	private Map<String, Object> checkEnoughMoney(String memberId, double amount, String type) {
		MemberWalletContent entity = findByIdWallet(memberId);
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
			return ReturnInfoUtils.errorInfo("校验钱包余额是否满足扣款失败，未知类型[" + type + "]");
		}
		return ReturnInfoUtils.successInfo();
	}

	
	@Override
	public Map<String, Object> reserveAmountOperating(String memberId, double amount, String type) {
		Map<String, Object> verifyMap = verifySign(memberId);
		if (!"1".equals(verifyMap.get(BaseCode.STATUS.toString()))) {
			return verifyMap;
		}
		MemberWalletContent entity = findByIdWallet(memberId);
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
		entity.setVerifyCode(generateSign(entity.getWalletId(), entity.getBalance(), entity.getReserveAmount(),
				entity.getFreezingFunds()));
		return updateWallet(entity);
	}

	/**
	 * 根据用户钱包信息生成校验码
	 * 
	 * @param entity
	 *            用户钱包信息实体
	 */
	private String generateSign(String walletId, double balance, double reserveAmount, double freezingFunds) {
		MD5 md5 = new MD5();
		return md5.getMD5ofStr("YM_" + walletId + balance + reserveAmount + freezingFunds);
	}

	/**
	 * 检查用户钱包校验码
	 * <li>校验码=钱包id+余额+用户储备资金+冻结资金</li>
	 * 
	 * @param memberId
	 *            用户id
	 * @return
	 */
	private Map<String, Object> verifySign(String memberId) {
		MemberWalletContent entity = findByIdWallet(memberId);
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("查询资金失败！");
		}
		MD5 md5 = new MD5();
		String id = entity.getWalletId();
		double balance = entity.getBalance();
		double reserveAmount = entity.getReserveAmount();
		double freezingFunds = entity.getFreezingFunds();
		String oldSign = md5.getMD5ofStr("YM_" + id + balance + reserveAmount + freezingFunds);
		if (!entity.getVerifyCode().equals(oldSign)) {
			return ReturnInfoUtils.errorInfo("资金异常！");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> generateSign(String memberId) {
		if (StringEmptyUtils.isNotEmpty(memberId)) {
			MemberWalletContent entity = findByIdWallet(memberId);
			if (entity == null) {
				return ReturnInfoUtils.errorInfo("钱包查询失败！");
			}
			entity.setVerifyCode(generateSign(entity.getWalletId(), entity.getBalance(), entity.getReserveAmount(),
					entity.getFreezingFunds()));
			return updateWallet(entity);
		} else {
			List<MemberWalletContent> reList = memberWalletDao.findByProperty(MemberWalletContent.class, null, 0, 0);
			if (reList != null && !reList.isEmpty()) {
				for (MemberWalletContent entity : reList) {
					entity.setVerifyCode(generateSign(entity.getWalletId(), entity.getBalance(),
							entity.getReserveAmount(), entity.getFreezingFunds()));
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
	public Map<String, Object> reserveAmountDeduction(String memberId, double amount) {
		return reserveAmountOperating(memberId, amount, "sub");
	}

	@Override
	public Object tmpAddAmount(String memberId, double amount) {
		return reserveAmountOperating(memberId, amount, "add");
	}

	@Override
	public Map<String,Object> checkPayPassword(String memberId, String payPassword) {
		Map<String,Object> params = new HashMap<>();
		params.put("memberId", memberId);
		List<Member> reList = memberWalletDao.findByProperty(Member.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (!reList.isEmpty()) {
			MD5 md5 = new MD5();
			Member member = reList.get(0);
			if(member.getPaymentPassword().equals(md5.getMD5ofStr(payPassword))){
				return ReturnInfoUtils.successInfo();
			}else{
				return ReturnInfoUtils.errorInfo("交易密码错误！");
			}
		} else {
			return ReturnInfoUtils.noDatas();
		}
	}
	
	
	@Override
	public Map<String, Object> freezingFundsOperating(String memberId, double amount, String type) {
		Map<String, Object> verifyMap = verifySign(memberId);
		if (!"1".equals(verifyMap.get(BaseCode.STATUS.toString()))) {
			return verifyMap;
		}
		MemberWalletContent entity = findByIdWallet(memberId);
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("钱包查询失败！");
		}
		double oldAmount = entity.getFreezingFunds();
		switch (type) {
		case "add":// 冻结资金加钱
			entity.setFreezingFunds(DoubleOperationUtil.add(oldAmount, amount));
			break;
		case "sub":// 冻结资金扣钱
			entity.setFreezingFunds(DoubleOperationUtil.sub(oldAmount, amount));
			break;
		default:
			return ReturnInfoUtils.errorInfo("操作失败，未知类型[" + type + "]");
		}
		entity.setVerifyCode(generateSign(entity.getWalletId(), entity.getBalance(), entity.getReserveAmount(),
				entity.getFreezingFunds()));
		return updateWallet(entity);
	}


}

