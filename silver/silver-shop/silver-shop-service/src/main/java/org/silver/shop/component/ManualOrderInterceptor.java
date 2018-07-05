package org.silver.shop.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.log.MerchantWalletLogService;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.api.system.organization.AgentService;
import org.silver.shop.api.system.tenant.MerchantIdCardCostService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.dao.system.manual.MorderDao;
import org.silver.shop.impl.system.manual.MpayServiceImpl;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.AgentWalletContent;
import org.silver.shop.model.system.tenant.MerchantIdCardCostContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 手工订单Aop拦截方法
 */
public class ManualOrderInterceptor {
	@Autowired
	private MerchantIdCardCostService merchantIdCardCostService;
	@Resource
	private MorderDao morderDao;
	@Autowired
	private MerchantWalletService merchantWalletService;
	@Autowired
	private MerchantWalletLogService merchantWalletLogService;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private MpayService mpayService;
	@Autowired
	private WalletUtils walletUtils;
	@Autowired
	private AgentService agentService;

	/**
	 * 钱包流水Id
	 */
	private static final String WALLET_ID = "walletId";
	/**
	 * 驼峰写法：商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 驼峰写法：商户名称
	 */
	private static final String MERCHANT_NAME = "merchantName";

	private static Logger logger = LogManager.getLogger(MpayServiceImpl.class);
	
	public void methodBefore(JoinPoint joinPoint) {
	/*	System.out.println(
				joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + " Start");*/
	}

	public void methodAfter(JoinPoint joinPoint, Object reValue) {

		Class<?> targetClass = joinPoint.getTarget().getClass();
		String methodName = joinPoint.getSignature().getName();
		// sendMpayByRecord
		if ("sendMorderRecord".equals(methodName)) {
			checkInfo(joinPoint, reValue);
			System.out.println(
					joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + " end");
		}
	}

	private void checkInfo(JoinPoint joinPoint, Object reValue) {
		// 拦截方法所传入的参数
		Object[] args = joinPoint.getArgs();
		if (args == null) {
			System.out.println("--方法参数为-null--");
		} else {
			JSONObject json = null;
			try {
				json = JSONObject.fromObject(reValue);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("--拦截返回参数格式错误--");
			}
			if ("1".equals(json.get(BaseCode.STATUS.toString()))) {
				String merchantId = args[0] + "";
				try {
					JSONArray idCardList = JSONArray.fromObject(json.get("idCardList"));
					JSONArray orderList = JSONArray.fromObject(json.get("orderList"));
					Map<String,Object> reTollMap = merchantWalletToll(idCardList, orderList, merchantId);
					logger.error("---商户实名认证与订单申报手续费---结果---"+reTollMap.toString());
					System.out.println("----reTollMap-->>>"+reTollMap.toString());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("---商户实名认证与订单申报手续费--错误--",e);
				}
			} else {

			}
		}
	}

	private Map<String, Object> merchantWalletToll(JSONArray idCardList, JSONArray orderList, String merchantId) {

		Map<String, Object> reCostMap = merchantIdCardCostService.getIdCardCostInfo(merchantId);
		if (!"1".equals(reCostMap.get(BaseCode.STATUS.toString()))) {
			return reCostMap;
		}
		MerchantIdCardCostContent merchantCost = (MerchantIdCardCostContent) reCostMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		// 查询商户钱包
		Map<String, Object> reMap = walletUtils.checkWallet(1, merchant.getMerchantId(), merchant.getMerchantName());
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		MerchantWalletContent merchantWallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		// 查询代理商钱包
		Map<String, Object> reAgentMap = walletUtils.checkWallet(3, merchant.getAgentParentId(),
				merchant.getAgentParentName());
		if (!"1".equals(reAgentMap.get(BaseCode.STATUS.toString()))) {
			return reAgentMap;
		}
		AgentWalletContent agentWallet = (AgentWalletContent) reAgentMap.get(BaseCode.DATAS.toString());
		//
		int count = idCardList.size();
		// 计算实名认证的费用
		double serviceFee = count * merchantCost.getPlatformCost();
		Map<String, Object> reIdCardMap = idCardCertificationToll(merchant, merchantWallet, serviceFee, agentWallet,
				idCardList, merchantCost.getPlatformCost());
		if (!"1".equals(reIdCardMap.get(BaseCode.STATUS.toString()))) {
			return reIdCardMap;
		}
		// 订单申报平台服务费
		return orderToll(orderList, merchantWallet,merchant);

	}

	private Map<String, Object> orderToll(JSONArray orderList, MerchantWalletContent merchantWallet, Merchant merchant) {
		if(orderList == null){
			return ReturnInfoUtils.errorInfo("订单参数错误-不能为null");
		}else if(orderList.isEmpty()){//当需要订单的集合为空时,则表示不需要进行计费
			return ReturnInfoUtils.successInfo();
		}
		Map<String, Object> reOrderMap = getOrderDetails(orderList);
		if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()))) {
			return reOrderMap;
		}
		List<JSONObject> jsonList = (List<JSONObject>) reOrderMap.get(BaseCode.DATAS.toString());
		double totalAmount = Double.parseDouble(reOrderMap.get("totalAmount") + "");
		//订单费率
		double fee = Double.parseDouble(reOrderMap.get("rate") + "");
		// 钱包余额
		double balance = merchantWallet.getBalance();
		// 订申报单手续费
		double serviceFee = totalAmount * fee;
		// 商户钱包扣款
		Map<String, Object> reWalletDeductionMap = merchantWalletService.walletDeduction(merchantWallet, balance,
				serviceFee);
		if (!"1".equals(reWalletDeductionMap.get(BaseCode.STATUS.toString()))) {
			return reWalletDeductionMap;
		}
		// 查询代理商钱包
		Map<String, Object> reAgentMap = walletUtils.checkWallet(3, merchant.getAgentParentId(),
				merchant.getAgentParentName());
		if (!"1".equals(reAgentMap.get(BaseCode.STATUS.toString()))) {
			return reAgentMap;
		}
		AgentWalletContent agentWallet = (AgentWalletContent) reAgentMap.get(BaseCode.DATAS.toString());
		Map<String, Object> datas = new HashMap<>();
		datas.put(MERCHANT_ID, merchant.getMerchantId());
		datas.put(WALLET_ID, merchantWallet.getWalletId());
		datas.put(MERCHANT_NAME, merchant.getMerchantName());
		datas.put("serialName", "订单申报-手续费");
		datas.put("balance", balance);
		datas.put("amount", serviceFee);
		datas.put("type", 4);
		datas.put("flag", "out");
		datas.put("note", "推送[" + jsonList.size() + "]单#" + jsonList.toString());
		datas.put("targetWalletId", agentWallet.getWalletId());
		datas.put("targetName", agentWallet.getAgentName());
		datas.put("count", jsonList.size());
		datas.put("status", "success");
		// 添加商户钱包流水日志
		Map<String, Object> reWalletLogMap = merchantWalletLogService.addWalletLog(datas);
		if (!"1".equals(reWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reWalletLogMap;
		}
		datas.put("totalAmountPaid", totalAmount);
		// 代理商收取订单申报服务费
		return agentChargeFee(merchant.getAgentParentId(), serviceFee, datas);
	}

	private Map<String, Object> getOrderDetails(JSONArray orderList) {
		if (orderList == null) {
			return ReturnInfoUtils.errorInfo("订单id参数集合不能为空!");
		}
		List<JSONObject> jsonList = new ArrayList<>();
		JSONObject idcardJSON = new JSONObject();
		Map<String, Object> params = new HashMap<>();
		double totalAmount = 0;
		double fee = 0;
		double rate =0;
		for (int i = 0; i < orderList.size(); i++) {
			String orderId = orderList.get(i) + "";
			params.clear();
			params.put("order_id", orderId);
			List<Morder> reList = morderDao.findByProperty(Morder.class, params, 0, 0);
			if (reList != null && !reList.isEmpty()) {
				for (Morder order : reList) {
					// 订单服务费率
					rate = order.getPlatformFee();
					idcardJSON.put("orderId", order.getOrder_id());
					double amount = order.getActualAmountPaid();
					int backCoverFlag = order.getBackCoverFlag();
					// 封底标识：1-不封底计算、2-100封底计算
					if (backCoverFlag == 1) {
						fee = amount * rate;
					} else if (backCoverFlag == 2) {
						if (amount < 100) {
							fee = 100 * rate;
						} else {
							fee = amount * rate;
						}
					}
					totalAmount += amount;
					idcardJSON.put("amount ", amount);
					idcardJSON.put("backCoverFlag", backCoverFlag);
					idcardJSON.put("rate", rate);
					idcardJSON.put("fee", fee);
					jsonList.add(idcardJSON);
				}
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("totalAmount", totalAmount);
		map.put(BaseCode.DATAS.toString(), jsonList);
		map.put("rate", rate);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	private Map<String, Object> idCardCertificationToll(Merchant merchant, MerchantWalletContent merchantWallet,
			double serviceFee, AgentWalletContent agentWallet, JSONArray idCardList, double fee) {
		if (merchant == null || merchantWallet == null || agentWallet == null || idCardList == null) {
			return ReturnInfoUtils.errorInfo("身份证实名认证清算时,参数不能为null!");
		}
		if(idCardList.isEmpty()){//当需要实名认证的集合为空时,则表示不需要进行计费
			return ReturnInfoUtils.successInfo();
		}
		Map<String, Object> reDetailsMap = getidCardDetails(idCardList, fee);
		if (!"1".equals(reDetailsMap.get(BaseCode.STATUS.toString()))) {
			return reDetailsMap;
		}
		List<JSONObject> detailsList = (List<JSONObject>) reDetailsMap.get(BaseCode.DATAS.toString());
		//
		Map<String, Object> reMerchantWalletMap = updateMerchantWallet(merchant, merchantWallet, serviceFee,
				agentWallet, detailsList);
		if (!"1".equals(reMerchantWalletMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantWalletMap;
		}
		//
		return updateAgentWallet(merchantWallet, serviceFee, agentWallet, detailsList);
	}

	private Map<String, Object> getidCardDetails(JSONArray idCardList, double fee) {
		if (idCardList == null) {
			return ReturnInfoUtils.errorInfo("身份证实名信息集合不能为null");
		}
		List<JSONObject> jsonList = new ArrayList<>();
		JSONObject idcardJSON = new JSONObject();
		Map<String, Object> params = new HashMap<>();
		for (int i = 0; i < idCardList.size(); i++) {
			String orderId = idCardList.get(i) + "";
			params.clear();
			params.put("order_id", orderId);
			List<Morder> reList = morderDao.findByProperty(Morder.class, params, 0, 0);
			if (reList != null && !reList.isEmpty()) {
				for (Morder order : reList) {
					idcardJSON.put("name", order.getOrderDocName());
					idcardJSON.put("idNumber ", order.getOrderDocId());
					idcardJSON.put("fee ", fee);
					jsonList.add(idcardJSON);
				}
			}
		}
		return ReturnInfoUtils.successDataInfo(jsonList);
	}

	/**
	 * 商户钱包进行身份证实名扣费更新
	 * 
	 * @param merchant
	 *            商户信息实体类
	 * @param merchantWallet
	 *            商户钱包信息
	 * @param serviceFee
	 *            平台服务费
	 * @param agentWallet
	 *            代理商钱包
	 * @param detailsList
	 * 
	 * @return Map
	 */
	private Map<String, Object> updateMerchantWallet(Merchant merchant, MerchantWalletContent merchantWallet,
			double serviceFee, AgentWalletContent agentWallet, List<JSONObject> detailsList) {
		double balance = merchantWallet.getBalance();
		// 商户钱包扣款
		Map<String, Object> reWalletDeductionMap = merchantWalletService.walletDeduction(merchantWallet, balance,
				serviceFee);
		if (!"1".equals(reWalletDeductionMap.get(BaseCode.STATUS.toString()))) {
			return reWalletDeductionMap;
		}
		Map<String, Object> datas = new HashMap<>();
		datas.put(MERCHANT_ID, merchant.getMerchantId());
		datas.put(WALLET_ID, merchantWallet.getWalletId());
		datas.put(MERCHANT_NAME, merchant.getMerchantName());
		datas.put("serialName", "实名认证-手续费");
		datas.put("balance", balance);
		datas.put("amount", serviceFee);
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
		datas.put("type", 4);
		datas.put("flag", "out");
		datas.put("note", "实名认证" + detailsList.size() + "条#" + detailsList.toString());
		datas.put("targetWalletId", agentWallet.getWalletId());
		datas.put("targetName", agentWallet.getAgentName());
		datas.put("status", "success");
		// 添加商户钱包流水日志
		return merchantWalletLogService.addWalletLog(datas);
	}

	/**
	 * 商户申报订单时,代理商进行实名认证费用收取
	 * @param merchantWallet 商户钱包信息
	 * @param serviceFee 平台服务费
	 * @param agentWallet 代理商钱包信息
	 * @param detailsList 收费详情集合
	 * @return Map
	 */
	private Map<String, Object> updateAgentWallet(MerchantWalletContent merchantWallet, double serviceFee,
			AgentWalletContent agentWallet, List<JSONObject> detailsList) {
		// 获取钱包余额
		double agentBalance = agentWallet.getBalance();
		agentWallet.setBalance(agentBalance + serviceFee);
		if (!morderDao.update(agentWallet)) {
			return ReturnInfoUtils.errorInfo("实名认证,代理商收款失败,服务器繁忙!");
		}
		Map<String, Object> datas = new HashMap<>();
		datas.put(WALLET_ID, agentWallet.getWalletId());
		datas.put("agentName", agentWallet.getAgentName());
		datas.put("serialName", "实名认证-手续费");
		datas.put("balance", agentBalance);
		datas.put("amount", serviceFee);
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
		datas.put("type", 4);
		datas.put("flag", "in");
		datas.put("note", "商户身份证实名认证" + detailsList.size() + "条,平台收取手续费#" + detailsList.toString());
		datas.put("targetWalletId", merchantWallet.getWalletId());
		datas.put("targetName", merchantWallet.getMerchantId());
		datas.put("status", "success");
		// 代理商支付平台佣金后记录支付日志
		return mpayService.addAgentWalletLog(datas);
	}
	/**
	 * 代理商下商户推送订单后佣金结算
	 * 
	 * @param agentId
	 *            代理商Id
	 * @param serviceFee
	 *            平台服务费
	 * @param datas
	 *            参数
	 * @return Map
	 */
	private Map<String, Object> agentChargeFee(String agentId, double serviceFee, Map<String, Object> datas) {
		Map<String, Object> reAgentWalletMap = agentWalletReceipt(agentId, serviceFee, datas);
		if (!"1".equals(reAgentWalletMap.get(BaseCode.STATUS.toString()))) {
			return reAgentWalletMap;
		}
		double agentFee = Double.parseDouble(datas.get("agentFee") + "");
		// 银盟平台进行代理商订单佣金收取
		Map<String, Object> rePlatformMap = platformCommissionFee(serviceFee);
		if (!"1".equals(rePlatformMap.get(BaseCode.STATUS.toString()))) {
			return rePlatformMap;
		}
		AgentWalletContent agentWallet = (AgentWalletContent) rePlatformMap.get(BaseCode.DATAS.toString());
		// 获取以更新后得总代理钱包余额剪掉佣金,得到原来未变更的钱包余额
		double balance = agentWallet.getBalance() - agentFee;
		// 代理商收款来源钱包Id与商户名称
		String walletId = datas.get(WALLET_ID) + "";
		String agentName = datas.get("agentName") + "";
		datas.put(WALLET_ID, agentWallet.getWalletId());
		datas.put("agentName", "银盟");
		datas.put("serialName", "申报订单-平台佣金");
		datas.put("balance", balance);
		datas.put("amount", agentFee);
		datas.put("type", 4);
		datas.put("flag", "in");
		datas.put("note", "商户申报[" + datas.get("count") + "]订单后,平台收取代理商佣金");
		datas.put("targetWalletId", walletId);
		datas.put("targetName", agentName);

		// 添加平台总代理商钱包流水日志
		Map<String, Object> reTotalWalletLogMap = mpayService.addAgentWalletLog(datas);
		if (!"1".equals(reTotalWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reTotalWalletLogMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 代理商钱包清算，先将商户申报订单时服务费加入至代理商钱包中,并生成加款日志记录,然后根据代理商与总代理(暂写死为银盟为总代理)协商的佣金率,
	 * 进行平台平台服务费的扣款,并进行日志记录
	 * 
	 * @param agentId
	 *            商户对应的代理商Id
	 * @param serviceFee
	 *            平台服务费
	 * @param datas
	 *            日志参数
	 * @return Map
	 */
	private Map<String, Object> agentWalletReceipt(String agentId, double serviceFee, Map<String, Object> datas) {
		Map<String, Object> reAgentMap = agentService.getAgentInfo(agentId);
		if (!"1".equals(reAgentMap.get(BaseCode.STATUS.toString()))) {
			return reAgentMap;
		}
		AgentBaseContent agent = (AgentBaseContent) reAgentMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reWalletMap = walletUtils.checkWallet(3, agent.getAgentId(), agent.getAgentName());
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return reWalletMap;
		}
		AgentWalletContent agentWallet = (AgentWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		// 获取钱包余额
		double balance = agentWallet.getBalance();
		agentWallet.setBalance(balance + serviceFee);
		if (!morderDao.update(agentWallet)) {
			return ReturnInfoUtils.errorInfo("订单申报时,代理商收款失败,服务器繁忙!");
		}
		// 代理商收款来源钱包Id与商户名称
		String walletId = datas.get(WALLET_ID) + "";
		String merchantName = datas.get(MERCHANT_NAME) + "";
		// 重新放入钱包日志参数
		datas.put(WALLET_ID, agentWallet.getWalletId());
		datas.put("agentName", agent.getAgentName());
		datas.put("balance", balance);
		datas.put("flag", "in");
		// 类型1-佣金、2-充值、3-提现、4-缴费
		datas.put("type", 1);
		datas.put("targetWalletId", walletId);
		datas.put("targetName", merchantName);
		datas.put("serialName", "商户申报订单-手续费");
		// 添加代理商进账钱包流水日志
		Map<String, Object> reWalletLogMap = mpayService.addAgentWalletLog(datas);
		if (!"1".equals(reWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reWalletLogMap;
		}
		String strAmount = String.valueOf(datas.get("totalAmountPaid"));
		if (StringEmptyUtils.isEmpty(strAmount)) {
			return ReturnInfoUtils.errorInfo("订单总金额不能为空!");
		}
		double totalAmountPaid = Double.parseDouble(strAmount);
		// 代理商订单佣金率
		double agentFee = totalAmountPaid * agent.getOrderCommissionRate();
		// 变更前的代理商钱包余额
		double beforeChangingBalance = agentWallet.getBalance();
		// 代理商支付平台佣金
		Map<String, Object> reAgentTollMap = agentTollCommissionRate(agentWallet, agentFee);
		if (!"1".equals(reAgentTollMap.get(BaseCode.STATUS.toString()))) {
			return reAgentTollMap;
		}
		// 使用代理商钱包余额替换缓存中的商户钱包余额
		// 重新放入钱包日志参数
		datas.put(WALLET_ID, agentWallet.getWalletId());
		datas.put("agentName", agent.getAgentName());
		datas.put("balance", beforeChangingBalance);
		datas.put("name", "商户申报[" + datas.get("count") + "]订单后,缴纳代理商佣金");
		datas.put("serialName", "申报订单-平台佣金");
		datas.put("flag", "out");
		Map<String, Object> rePlatformWalletMap = walletUtils.checkWallet(3, "AgentId_00001", "银盟");
		if (!"1".equals(rePlatformWalletMap.get(BaseCode.STATUS.toString()))) {
			return rePlatformWalletMap;
		}
		AgentWalletContent platformWallet = (AgentWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		datas.put("targetWalletId", platformWallet.getWalletId());
		datas.put("targetName", "银盟");
		// 代理商支付平台佣金后记录支付日志
		Map<String, Object> reAgentWalletLogMap = mpayService.addAgentWalletLog(datas);
		if (!"1".equals(reAgentWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reAgentWalletLogMap;
		}
		datas.put("agentFee", agentFee);
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 订单申报后,代理商抽取佣金
	 * 
	 * @param agentWallet
	 * @param fee
	 * @return
	 */
	private Map<String, Object> agentTollCommissionRate(AgentWalletContent agentWallet, double fee) {
		double agentOldBalance = agentWallet.getBalance();
		agentWallet.setBalance(agentOldBalance - fee);
		if (!morderDao.update(agentWallet)) {
			return ReturnInfoUtils.errorInfo("订单申报后,代理商抽取佣金失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 平台收取代理商对应的订单申报佣金
	 * 
	 * @param fee
	 *            订单申报佣金
	 * @return Map
	 */
	private Map<String, Object> platformCommissionFee(double fee) {
		// 查询平台总代理的钱包信息
		Map<String, Object> reWalletMap = walletUtils.checkWallet(3, "AgentId_00001", "银盟");
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return reWalletMap;
		}
		AgentWalletContent agentWallet = (AgentWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		// 获取总代理商钱包余额
		double oldB = agentWallet.getBalance();
		agentWallet.setBalance(oldB + fee);
		if (!morderDao.update(agentWallet)) {
			return ReturnInfoUtils.errorInfo("订单申报时,代理商收款失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successDataInfo(agentWallet);
	}

	
	public void methodException(JoinPoint joinPoint) {
		System.out.println(
				joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + " mett Error");
	}
}
