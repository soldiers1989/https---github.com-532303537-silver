package org.silver.shop.impl.system.cross;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.api.system.cross.YsPayReceiveService;
import org.silver.shop.api.system.log.MerchantWalletLogService;
import org.silver.shop.api.system.organization.AgentService;
import org.silver.shop.api.system.tenant.MemberWalletService;
import org.silver.shop.api.system.tenant.MerchantFeeService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.dao.system.organization.MerchantDao;
import org.silver.shop.model.system.cross.PaymentContent;
import org.silver.shop.model.system.log.AgentWalletLog;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.manual.OldManualPayment;
import org.silver.shop.model.system.manual.PaymentCallBack;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.AgentWalletContent;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.task.WalletTransferTask;
import org.silver.shop.util.BufferUtils;
import org.silver.shop.util.InvokeTaskUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.CopyUtils;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Service(interfaceClass = PaymentService.class)
public class PaymentServiceImpl implements PaymentService {

	private static Logger logger = LogManager.getLogger(PaymentServiceImpl.class);
	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private YsPayReceiveService ysPayReceiveService;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private GoodsRecordService goodsRecordService;
	@Autowired
	private WalletUtils walletUtils;
	@Autowired
	private BufferUtils bufferUtils;
	@Autowired
	private InvokeTaskUtils invokeTaskUtils;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private MerchantWalletService merchantWalletService;
	@Autowired
	private MerchantWalletLogService merchantWalletLogService;
	@Autowired
	private MerchantFeeService merchantFeeService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private MerchantDao merchantDao;
	@Autowired
	private MemberWalletService memberWalletService;

	/**
	 * 错误标识
	 */
	private static final String ERROR = "error";
	/**
	 * appkey键
	 */
	private static final String APPKEY = "appkey";

	/**
	 * 驼峰命名:商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";

	/**
	 * 下划线命名:商户Id
	 */
	private static final String MERCHANT_NO = "merchant_no";
	/**
	 * 口岸
	 */
	private static final String E_PORT = "eport";

	/**
	 * 检验检疫机构代码
	 */
	private static final String CIQ_ORG_CODE = "ciqOrgCode";

	/**
	 * 主管海关代码
	 */
	private static final String CUSTOMS_CODE = "customsCode";
	/**
	 * 驼峰命名：订单Id
	 */
	private static final String ORDER_ID = "orderId";
	/**
	 * 下划线命名:交易流水号
	 */
	private static final String TRADE_NO = "trade_no";
	/**
	 * 钱包流水Id
	 */
	private static final String WALLET_ID = "walletId";

	/**
	 * 商户名称
	 */
	private static final String MERCHANT_NAME = "merchantName";

	@Override
	public Map<String, Object> updatePaymentStatus(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("reSerialNo", datasMap.get("messageID") + "");
		String reMsg = datasMap.get("errMsg") + "";
		List<Object> reList = paymentDao.findByProperty(PaymentContent.class, paramMap, 1, 1);
		if (reList != null && reList.size() > 0) {
			PaymentContent payment = (PaymentContent) reList.get(0);
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			String note = payment.getReNote();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if ("1".equals(status)) {
				// 支付单备案状态修改为成功
				payment.setPayRecord(2);
			} else {
				payment.setPayRecord(3);
			}
			payment.setReNote(note + defaultDate + ":" + reMsg + ";");
			payment.setUpdateDate(date);
			if (!paymentDao.update(payment)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "异步更新支付单备案信息错误!");
				return paramMap;
			}
			return ReturnInfoUtils.successInfo();
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return statusMap;
	}

	@Override
	public Object sendMpayByRecord(String merchantId, Map<String, Object> recordMap, String tradeNoPack,
			String proxyParentId, String merchantName, String proxyParentName) {
		Map<String, Object> statusMap = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(tradeNoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("支付流水号包格式错误,请核实信息！");
		}
		if (jsonList == null || jsonList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("支付流水信息包不能为空!");
		}
		int eport = Integer.parseInt(recordMap.get(E_PORT) + "");
		String ciqOrgCode = recordMap.get(CIQ_ORG_CODE) + "";
		String customsCode = recordMap.get(CUSTOMS_CODE) + "";
		// 校验前台传递口岸、海关、智检编码
		Map<String, Object> reCustomsMap = goodsRecordService.checkCustomsPort(eport, customsCode, ciqOrgCode);
		if (!"1".equals(reCustomsMap.get(BaseCode.STATUS.toString()))) {
			return reCustomsMap;
		}
		// 获取商户在对应口岸的备案信息
		Map<String, Object> merchantRecordMap = merchantUtils.getMerchantRecordInfo(merchantId, eport);
		if (!"1".equals(merchantRecordMap.get(BaseCode.STATUS.toString()))) {
			return merchantRecordMap;
		}
		MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) merchantRecordMap.get(BaseCode.DATAS.toString());
		recordMap.put("ebEntNo", merchantRecordInfo.getEbEntNo());
		recordMap.put("ebEntName", merchantRecordInfo.getEbEntName());
		recordMap.put("ebpEntNo", merchantRecordInfo.getEbpEntNo());
		recordMap.put("ebpEntName", merchantRecordInfo.getEbpEntName());

		// 获取商户信息
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		int thirdPartyFlag = merchant.getThirdPartyFlag();
		// 当商户标识为第三方平台商户时,则使用第三方appkey
		if (thirdPartyFlag == 2) {
			Map<String, Object> reAppkeyMap = merchantUtils.getMerchantAppkey(merchantId);
			if (!"1".equals(reAppkeyMap.get(BaseCode.STATUS.toString()))) {
				return reAppkeyMap;
			}
			Appkey appkey = (Appkey) reAppkeyMap.get(BaseCode.DATAS.toString());
			// 打包至海关备案信息Map中
			recordMap.put(APPKEY, appkey.getApp_key());
			recordMap.put("appSecret", appkey.getApp_secret());
		} else {
			// 当不是第三方时则使用银盟商城appkey
			recordMap.put(APPKEY, YmMallConfig.APPKEY);
			recordMap.put("appSecret", YmMallConfig.APPSECRET);
		}
		String merchantFeeId = recordMap.get("merchantFeeId") + "";
		Map<String, Object> reCheckMap = computingCostsManualPayment(jsonList, merchant, merchantFeeId);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		// 请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(recordMap.get(APPKEY) + "",
				recordMap.get("appSecret") + "");
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			return ReturnInfoUtils.errorInfo(reTokMap.get("errMsg") + "");
		}
		String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
		// 获取流水号
		String serialNo = "paymentRecord_" + SerialNoUtils.getSerialNo("paymentRecord");
		// 总数
		int totalCount = jsonList.size();
		Map<String, Object> params = new HashMap<>();
		params.put(MERCHANT_ID, merchantId);
		params.put(MERCHANT_NAME, merchantName);
		params.put("tok", tok);
		params.put("serialNo", serialNo);
		Map<String, Object> reMap = invokeTaskUtils.commonInvokeTask(3, totalCount, jsonList, errorList, recordMap,
				params);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		statusMap.clear();
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put("msg", "执行成功,开始推送支付单备案.......");
		statusMap.put("serialNo", serialNo);
		return statusMap;
	}

	/**
	 * 计算商户余额是否有足够的钱支付本次推送支付单手续费
	 * 
	 * @param jsonList
	 *            支付流水号集合
	 * @param merchant
	 *            商户基本信息实体类
	 * @param merchantFeeId
	 *            商户口岸费率Id
	 * @return Map
	 */
	public Map<String, Object> computingCostsManualPayment(JSONArray jsonList, Merchant merchant,
			String merchantFeeId) {
		// 查询商户钱包
		Map<String, Object> reMap = walletUtils.checkWallet(1, merchant.getMerchantId(), merchant.getMerchantName());
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		MerchantWalletContent merchantWallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		double fee;
		int backCoverFlag = 0;
		if (StringEmptyUtils.isNotEmpty(merchantFeeId)) {
			Map<String, Object> reFeeMap = merchantFeeService.getMerchantFeeInfo(merchantFeeId);
			if (!"1".equals(reFeeMap.get(BaseCode.STATUS.toString()))) {
				return reFeeMap;
			}
			MerchantFeeContent merchantFee = (MerchantFeeContent) reFeeMap.get(BaseCode.DATAS.toString());
			fee = merchantFee.getPlatformFee();
			backCoverFlag = merchantFee.getBackCoverFlag();
		} else {
			// 支付单平台服务费
			fee = 0.002;
		}
		// 所有支付单总金额
		double totalAmountPaid = 0;
		List<Object> itemList = JSONArray.toList(jsonList, new HashMap<>(), new JsonConfig());
		// 封底标识：1-正常计算、2-不满100提至100计算
		if (backCoverFlag == 2) {
			// 当支付单实际支付金额不足100提升至100,后统计支付单金额
			totalAmountPaid = paymentDao.backCoverStatisticalManualPaymentAmount(itemList);
		} else {
			totalAmountPaid = paymentDao.statisticalManualPaymentAmount(itemList);
		}

		if (totalAmountPaid < 0) {
			return ReturnInfoUtils.errorInfo("查询手工支付单总金额失败,服务器繁忙!");
		} else if (totalAmountPaid > 0) {
			return paymentToll(merchantWallet, fee, totalAmountPaid, itemList, merchant);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 支付单推送手续费结算
	 * 
	 * @param merchantWallet
	 *            商户钱包实体类
	 * @param fee
	 *            商户口岸费率
	 * @param totalAmountPaid
	 *            支付单总金额
	 * @param itemList
	 *            数量
	 * @param merchant
	 *            商户基本信息实体类
	 * @return Map
	 */
	private Map<String, Object> paymentToll(MerchantWalletContent merchantWallet, double fee, double totalAmountPaid,
			List<Object> itemList, Merchant merchant) {

		// 支付单手续费
		double serviceFee = totalAmountPaid * fee;
		double walletBalance = merchantWallet.getBalance();
		Map<String, Object> reWalletDeductionMap = merchantWalletService.walletDeduction(merchantWallet, walletBalance,
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
		datas.put(MERCHANT_ID, merchantWallet.getMerchantId());
		datas.put(WALLET_ID, merchantWallet.getWalletId());
		datas.put(MERCHANT_NAME, merchant.getMerchantName());
		datas.put("balance", walletBalance);
		datas.put("amount", serviceFee);
		datas.put("serialName", "支付单申报-手续费");
		datas.put("type", 4);
		datas.put("flag", "out");
		List<Object> cacheList = new ArrayList<>();
		for(int i = 0 ; i < itemList.size() ; i ++){
			Map<String, Object> treadeMap = (Map<String, Object>) itemList.get(i);
			cacheList.add(treadeMap.get("treadeNo"));
		}
		datas.put("note", "[" + itemList.size() + "]单,支付单申报手续费#"+cacheList.toString());
		datas.put("targetWalletId", agentWallet.getWalletId());
		datas.put("targetName", agentWallet.getAgentName());
		datas.put("count", itemList.size());
		datas.put("status", "success");
		Map<String, Object> reWalletLogMap = merchantWalletLogService.addWalletLog(datas);
		if (!"1".equals(reWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reWalletLogMap;
		}
		// 添加所选支付单的总金额
		datas.put("totalAmountPaid", totalAmountPaid);
		// 代理商钱包加款,记录日志
		Map<String, Object> reChargeFeeMap = agentChargeFee(merchant.getAgentParentId(), merchant.getAgentParentName(),
				serviceFee, datas);
		if (!"1".equals(reChargeFeeMap.get(BaseCode.STATUS.toString()))) {
			return reChargeFeeMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 代理商钱包结算
	 * @param agentId 代理商Id
	 * @param agentName 代理商名称
	 * @param serviceFee 平台服务费率
	 * @param datas 参数
	 * @return
	 */
	private Map<String, Object> agentChargeFee(String agentId, String agentName, double serviceFee,
			Map<String, Object> datas) {
		Map<String, Object> reAgentWalletMap = agentWalletReceipt(agentId, agentName, serviceFee, datas);
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
		// String agentName = datas.get("agentName") + "";
		datas.put(WALLET_ID, agentWallet.getWalletId());
		datas.put("agentName", "银盟");
		datas.put("serialName", "申报支付单-平台佣金");
		datas.put("balance", balance);
		datas.put("amount", agentFee);
		datas.put("type", 4);
		datas.put("flag", "in");
		datas.put("note", "商户申报[" + datas.get("count") + "]支付单,平台收取代理商佣金");
		datas.put("targetWalletId", walletId);
		datas.put("targetName", agentName);

		// 添加平台总代理商钱包流水日志
		Map<String, Object> reTotalWalletLogMap = addAgentWalletLog(datas);
		if (!"1".equals(reTotalWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reTotalWalletLogMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 总代理商钱包收款
	 * @param fee 服务费
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
		if (!paymentDao.update(agentWallet)) {
			return ReturnInfoUtils.errorInfo("支付单申报时,代理商收款失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successDataInfo(agentWallet);
	}

	/**
	 * 代理商钱包清算
	 * @param agentId 代理商Id
	 * @param agentName 代理商名称
	 * @param serviceFee 平台服务费率
	 * @param datas 参数
	 * @return Map
	 */
	private Map<String, Object> agentWalletReceipt(String agentId, String agentName, double serviceFee,
			Map<String, Object> datas) {
		Map<String, Object> reAgentMap = agentService.getAgentInfo(agentId);
		if (!"1".equals(reAgentMap.get(BaseCode.STATUS.toString()))) {
			return reAgentMap;
		}
		AgentBaseContent agent = (AgentBaseContent) reAgentMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reWalletMap = walletUtils.checkWallet(3, agentId, agentName);
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return reWalletMap;
		}
		AgentWalletContent agentWallet = (AgentWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		// 获取钱包余额
		double balance = agentWallet.getBalance();
		agentWallet.setBalance(balance + serviceFee);
		if (!paymentDao.update(agentWallet)) {
			return ReturnInfoUtils.errorInfo("支付单申报时,代理商收款失败,服务器繁忙!");
		}
		// 代理商收款来源钱包Id与商户名称
		String walletId = datas.get(WALLET_ID) + "";
		String merchantName = datas.get(MERCHANT_NAME) + "";
		// 重新放入钱包日志参数
		datas.put(WALLET_ID, agentWallet.getWalletId());
		datas.put("agentName", agentWallet.getAgentName());
		datas.put("balance", balance);
		datas.put("flag", "in");
		// 类型1-佣金、2-充值、3-提现、4-缴费
		datas.put("type", 1);
		// 钱包资金流向目标的钱包Id
		datas.put("targetWalletId", walletId);
		datas.put("targetName", merchantName);
		// 添加代理商进账钱包流水日志
		Map<String, Object> reWalletLogMap = addAgentWalletLog(datas);
		if (!"1".equals(reWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reWalletLogMap;
		}

		String strAmount = String.valueOf(datas.get("totalAmountPaid"));
		if (StringEmptyUtils.isEmpty(strAmount)) {
			return ReturnInfoUtils.errorInfo("支付单总金额不能为空!");
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
		datas.put("note", "商户申报[" + datas.get("count") + "]支付单后,缴纳平台佣金");
		datas.put("serialName", "商户申报支付单-手续费");
		datas.put("flag", "out");
		Map<String, Object> rePlatformWalletMap = walletUtils.checkWallet(3, "AgentId_00001", "银盟");
		if (!"1".equals(rePlatformWalletMap.get(BaseCode.STATUS.toString()))) {
			return rePlatformWalletMap;
		}
		AgentWalletContent platformWallet = (AgentWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		datas.put("targetWalletId", platformWallet.getWalletId());
		datas.put("targetName", "银盟");
		// 代理商支付平台佣金后记录支付日志
		Map<String, Object> reAgentWalletLogMap = addAgentWalletLog(datas);
		if (!"1".equals(reAgentWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reAgentWalletLogMap;
		}

		datas.put("agentFee", agentFee);
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 代理商缴纳支付单申报平台佣金
	 * @param agentWallet 代理商钱包信息
	 * @param fee 平台服务费
	 * @return Map
	 */
	private Map<String, Object> agentTollCommissionRate(AgentWalletContent agentWallet, double fee) {
		double agentOldBalance = agentWallet.getBalance();
		agentWallet.setBalance(agentOldBalance - fee);
		if (!paymentDao.update(agentWallet)) {
			return ReturnInfoUtils.errorInfo("支付单申报后,代理商抽取佣金失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 添加代理商钱包日志
	 * @param datas 参数
	 * @return Map
	 */
	private Map<String, Object> addAgentWalletLog(Map<String, Object> datas) {
		if (datas == null || datas.isEmpty()) {
			return ReturnInfoUtils.errorInfo("添加代理商钱包日志时,代理商信息不能为空!");
		}
		Map<String, Object> reCheckMap = walletUtils.checkMerchantWalletLogInfo(datas);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		AgentWalletLog log = new AgentWalletLog();
		log.setAgentWalletId(datas.get(WALLET_ID) + "");
		log.setAgentName(datas.get("agentName") + "");
		int serialNo = SerialNoUtils.getSerialNo("logs");
		if (serialNo < 0) {
			return ReturnInfoUtils.errorInfo("查询流水号自增Id失败,服务器繁忙!");
		}
		log.setSerialNo(SerialNoUtils.getSerialNo("L", serialNo));
		log.setSerialName(datas.get("serialName") + "");
		double balance = Double.parseDouble(datas.get("balance") + "");
		log.setBeforeChangingBalance(balance);
		double amount = Double.parseDouble(datas.get("amount") + "");
		log.setAmount(amount);
		String flag = datas.get("flag") + "";
		log.setFlag(flag);
		if ("in".equals(flag)) {
			log.setAfterChangeBalance(balance + amount);
		} else if ("out".equals(flag)) {
			log.setAfterChangeBalance(balance - amount);
		}
		log.setType(Integer.parseInt(datas.get("type") + ""));
		log.setStatus("success");
		log.setNote(datas.get("note") + "");
		log.setTargetWalletId(datas.get("targetWalletId") + "");
		log.setTargetName(datas.get("targetName") + "");
		log.setCreateBy("system");
		log.setCreateDate(new Date());
		if (!paymentDao.add(log)) {
			return ReturnInfoUtils.errorInfo("保存代理商钱包日志流水信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 开始发起支付单推送
	 * 
	 * @param jsonList
	 * @param errorList
	 *            错误信息
	 * @param recordMap
	 *            商户备案信息
	 * @param paramsMap
	 *            缓存参数
	 */
	@Override
	public void startSendPaymentRecord(JSONArray jsonList, List<Map<String, Object>> errorList,
			Map<String, Object> recordMap, Map<String, Object> paramsMap) {
		String merchantId = paramsMap.get(MERCHANT_ID) + "";
		String tok = paramsMap.get("tok") + "";
		paramsMap.put("name", "paymentRecord");
		Map<String, Object> param = new HashMap<>();
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> treadeMap = (Map<String, Object>) jsonList.get(i);
			String treadeNo = treadeMap.get("treadeNo") + "";
			try {
				param.clear();
				param.put(MERCHANT_NO, merchantId);
				param.put(TRADE_NO, treadeNo);
				List<Mpay> payList = paymentDao.findByProperty(Mpay.class, param, 1, 1);
				if (payList != null && !payList.isEmpty()) {
					Mpay payInfo = payList.get(0);
					Map<String, Object> paymentInfoMap = new HashMap<>();
					paymentInfoMap.put("EntPayNo", payInfo.getTrade_no());
					paymentInfoMap.put("PayStatus", payInfo.getPay_status());
					paymentInfoMap.put("PayAmount", payInfo.getPay_amount());
					paymentInfoMap.put("PayCurrCode", payInfo.getPay_currCode());
					paymentInfoMap.put("PayTime", payInfo.getPay_time());
					paymentInfoMap.put("PayerName", payInfo.getPayer_name());
					paymentInfoMap.put("PayerDocumentType", payInfo.getPayer_document_type());
					paymentInfoMap.put("PayerDocumentNumber", payInfo.getPayer_document_number());
					paymentInfoMap.put("PayerPhoneNumber", payInfo.getPayer_phone_number());
					paymentInfoMap.put("EntOrderNo", payInfo.getMorder_id());
					paymentInfoMap.put("Notes", payInfo.getRemarks());
					Map<String, Object> paymentMap = ysPayReceiveService.sendPayment(merchantId, paymentInfoMap, tok,
							recordMap, YmMallConfig.MANUAL_PAYMENT_NOTIFY_URL);
					if (!"1".equals(paymentMap.get(BaseCode.STATUS.toString()) + "")) {
						String msg = "";
						Map<String, Object> rePaymentMap = updatePaymentFailureStatus(treadeNo);
						if (!"1".equals(rePaymentMap.get(BaseCode.STATUS.toString()))) {
							msg = rePaymentMap.get(BaseCode.MSG.toString()) + "";
						} else {
							msg = "支付流水号[" + treadeNo + "]-->" + paymentMap.get(BaseCode.MSG.toString());
						}
						RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
						continue;
					}
					String rePayMessageID = paymentMap.get("messageID") + "";
					// 更新服务器返回支付Id
					Map<String, Object> rePaymentMap2 = updatePaymentInfo(treadeNo, rePayMessageID, recordMap);
					if (!"1".equals(rePaymentMap2.get(BaseCode.STATUS.toString()) + "")) {
						String msg = "支付流水号[" + treadeNo + "]-->" + rePaymentMap2.get(BaseCode.MSG.toString());
						RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
						continue;
					}
				} else {
					String msg = "支付流水号[" + treadeNo + "]查询支付单信息失败,请核实流水号!";
					RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
					continue;
				}
				bufferUtils.writeRedis(errorList, paramsMap);
				Thread.sleep(200);
			} catch (Exception e) {
				logger.error(Thread.currentThread().getName() + "-支付单推送失败->", e);
				String msg = "[" + treadeNo + "]支付单推送失败,系統繁忙!";
				RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
			}
		}
		bufferUtils.writeCompletedRedis(errorList, paramsMap);
	}

	@Override
	public Map<String, Object> updatePaymentFailureStatus(String treadeNo) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(TRADE_NO, treadeNo);
		List<Mpay> reList = paymentDao.findByProperty(Mpay.class, paramMap, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			Mpay payment = reList.get(0);
			// 支付单推送至网关接收状态： 0-未发起,1-接收成功,2-接收失败
			payment.setNetworkStatus(2);
			payment.setUpdate_date(new Date());
			if (!paymentDao.update(payment)) {
				return ReturnInfoUtils.errorInfo("更新支付单网络状态失败,服务器繁忙!");
			}
		} else {
			return ReturnInfoUtils.errorInfo("更新支付单网络状态失败,[" + treadeNo + "]支付单未找到!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> updatePaymentInfo(String entPayNo, String rePayMessageID,
			Map<String, Object> customsMap) {
		String eport = null;
		String ciqOrgCode = null;
		String customsCode = null;
		if (customsMap != null) {
			eport = customsMap.get(E_PORT) + "";
			ciqOrgCode = customsMap.get(CIQ_ORG_CODE) + "";
			customsCode = customsMap.get(CUSTOMS_CODE) + "";
		}
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(TRADE_NO, entPayNo);
		List<Mpay> reList = paymentDao.findByProperty(Mpay.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Mpay payment = reList.get(i);
				payment.setPay_serial_no(rePayMessageID);
				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				payment.setPay_record_status(2);
				if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
						&& StringEmptyUtils.isNotEmpty(customsCode)) {
					payment.setEport(eport);
					payment.setCiqOrgCode(ciqOrgCode);
					payment.setCustomsCode(customsCode);
				}
				// 支付单推送至网关接收状态： 0-未发起,1-接收成功,2-接收失败
				payment.setNetworkStatus(1);
				payment.setUpdate_date(new Date());
				if (!paymentDao.update(payment)) {
					return ReturnInfoUtils.errorInfo("更新服务器返回messageID错误,服务器繁忙!");
				}
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("交易流水号[" + entPayNo + "]未找到支付单信息!");
		}
	}

	@Override
	public Map<String, Object> updatePayRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("pay_serial_no", datasMap.get("messageID") + "");
		String entPayNo = datasMap.get("entPayNo") + "";
		paramMap.put(TRADE_NO, entPayNo);
		String reMsg = datasMap.get(BaseCode.MSG.toString()) + "";
		List<Mpay> reList = paymentDao.findByPropertyOr2(Mpay.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			Mpay pay = reList.get(0);
			String status = datasMap.get("status") + "";
			String note = pay.getPay_re_note();
			if (StringEmptyUtils.isEmpty(note)) {
				note = "";
			}
			if ("1".equals(status)) {
				// 支付单备案状态修改为成功
				pay.setPay_record_status(3);
			} else {
				// 备案失败
				pay.setPay_record_status(4);
			}
			if (StringEmptyUtils.isNotEmpty(reMsg)) {
				pay.setPay_re_note(note + defaultDate + ":" + reMsg + ";");
			}
			Map<String, Object> reUpdateMap = updatePaymentRecordInfo(pay);
			if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
				System.out.println("--更新支付单信息失败--" + reUpdateMap.get(BaseCode.MSG.toString()));
			}
			return reThirdPartyPaymentInfo(pay, pay.getMerchant_no());
		} else {
			return ReturnInfoUtils.errorInfo("支付单[" + entPayNo + "]为找到对应信息,请核对信息!");
		}
	}

	private Map<String, Object> reThirdPartyPaymentInfo(Mpay pay, String merchantId) {
		if (StringEmptyUtils.isEmpty(merchantId) || pay == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		// 第三方标识：1-银盟(银盟商城平台),2-第三方商城平台
		int thirdPartyFlag = merchant.getThirdPartyFlag();
		if (thirdPartyFlag == 2 && StringEmptyUtils.isEmpty(pay.getResendStatus())) {
			System.out.println("---------返回第三方支付单信息----------");
			rePaymentInfo(pay);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 返回给第三方电商平台,支付单信息
	 * 
	 * @param pay
	 *            手工支付单实体类
	 * @return Map
	 */
	@Override
	public void rePaymentInfo(Mpay pay) {
		if (pay != null) {
			Map<String, Object> item = new HashMap<>();
			JSONObject payment = new JSONObject();
			payment.element("thirdPartyId", pay.getThirdPartyId());
			payment.element("EntPayNo", pay.getTrade_no());
			payment.element("PayStatus", pay.getPay_status());
			payment.element("PayAmount", pay.getPay_amount());
			payment.element("PayCurrCode", pay.getPay_currCode());
			payment.element("PayTime", DateUtil.formatDate(pay.getPay_time(), "yyyyMMddhhmmss"));
			payment.element("PayerName", pay.getPayer_name());
			payment.element("PayerDocumentType", pay.getPayer_document_type());
			payment.element("PayerDocumentNumber", pay.getPayer_document_number());
			payment.element("PayerPhoneNumber", pay.getPayer_phone_number());
			payment.element("EntOrderNo", pay.getMorder_id());
			payment.element("Notes", pay.getRemarks());
			payment.element("payRecordNote", pay.getPay_re_note());
			payment.element("payRecordStatus", pay.getPay_record_status());
			payment.element(MERCHANT_ID, pay.getMerchant_no());
			payment.element(CIQ_ORG_CODE, pay.getCiqOrgCode());
			payment.element(CUSTOMS_CODE, pay.getCustomsCode());
			item.put("payment", payment.toString());
			String result = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/Eport/getway-callback", item);
			// String result =
			// YmHttpUtil.HttpPost("http://192.168.1.104:8080/silver-web/Eport/getway-callback",
			// item);
			if (StringEmptyUtils.isNotEmpty(result) && result.replace("\n", "").equalsIgnoreCase("success")) {
				updateSuccessPaymentCallBack(pay);
			} else {
				// 当第三方接收支付单回执失败时,保存信息
				savePaymentCallBack(pay);
			}
		}
	}

	/**
	 * 当支付单回调给第三方电商平台返回成功(success)后,更新成功的支付单第三方回调信息
	 * 
	 * @param Mpay
	 *            手工支付单实体类
	 */
	private void updateSuccessPaymentCallBack(Mpay pay) {
		Map<String, Object> params = new HashMap<>();
		params.put("thirdPartyId", pay.getThirdPartyId());
		params.put(MERCHANT_ID, pay.getMerchant_no());
		List<PaymentCallBack> rePaymentCallBackList = paymentDao.findByProperty(PaymentCallBack.class, params, 0, 0);
		if (rePaymentCallBackList != null && !rePaymentCallBackList.isEmpty()) {
			Date date = new Date();
			PaymentCallBack paymentCallBack = rePaymentCallBackList.get(0);
			paymentCallBack.setResendStatus("SUCCESS");
			paymentCallBack.setUpdateBy("system");
			paymentCallBack.setUpdateDate(date);
			System.out.println(DateUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss") + " 支付单重发第"
					+ paymentCallBack.getResendCount() + "次,接收成功!");
			paymentCallBack.setRemark(DateUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss") + " 支付单重发第"
					+ (paymentCallBack.getResendCount() + 1) + "次,接收成功!");
			if (!paymentDao.update(paymentCallBack)) {
				logger.error("--异步回调第三方支付单成功后保存信息失败--");
			}
		}
		pay.setResendStatus("SUCCESS");
		pay.setResendDate(new Date());
		if (!paymentDao.update(pay)) {
			logger.error("--异步回调第三方支付单成功后更新支付单回调状态失败--");
		}
	}

	/**
	 * 当第三方电商平台接收支付单回执失败时,保存信息至支付回调信息
	 * 
	 * @param pay
	 */
	private void savePaymentCallBack(Mpay pay) {
		Map<String, Object> params = new HashMap<>();
		params.put("thirdPartyId", pay.getThirdPartyId());
		params.put("tradeNo", pay.getTrade_no());
		params.put(MERCHANT_ID, pay.getMerchant_no());
		List<PaymentCallBack> rePaymentCallBackList = paymentDao.findByProperty(PaymentCallBack.class, params, 0, 0);
		if (rePaymentCallBackList != null && !rePaymentCallBackList.isEmpty()) {
			// 更新支付单回传记录中的回传计数器
			PaymentCallBack paymentCallBack = rePaymentCallBackList.get(0);
			int count = paymentCallBack.getResendCount();
			System.out.println(pay.getTrade_no() + "--支付单->第" + (count + 1) + "次重发接受失败");
			if (count == 9) {
				String remark = paymentCallBack.getRemark();
				String note = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + "_支付单重发第10次,接收失败!";
				if (StringEmptyUtils.isNotEmpty(remark)) {
					paymentCallBack.setRemark(remark + "#" + note);
				} else {
					paymentCallBack.setRemark(note);
				}
			}
			count++;
			paymentCallBack.setResendCount(count);
			paymentCallBack.setResendStatus("failure");
			if (!paymentDao.update(paymentCallBack)) {
				logger.error("--异步更新第三方支付单计数器失败--");
			}
		} else {
			PaymentCallBack paymentCallBack = new PaymentCallBack();
			paymentCallBack.setMerchantId(pay.getMerchant_no());
			paymentCallBack.setOrderId(pay.getMorder_id());
			paymentCallBack.setTradeNo(pay.getTrade_no());
			paymentCallBack.setThirdPartyId(pay.getThirdPartyId());
			paymentCallBack.setCreateBy("system");
			paymentCallBack.setCreateDate(new Date());
			paymentCallBack.setResendCount(0);
			paymentCallBack.setResendStatus("failure");
			if (!paymentDao.add(paymentCallBack)) {
				logger.error("--异步保存支付单第三方回调信息失败--");
			}
			pay.setResendStatus("failure");
			if (!paymentDao.update(pay)) {
				logger.error("--异步回调第三方支付单后更新支付单失败状态--");
			}
		}
	}

	/**
	 * 重复回执后更新旧手工支付单备案状态,如果为备案失败则修改为备案成功
	 * 
	 * @param pay
	 * @return
	 */
	private Map<String, Object> updateOldPaymentInfo(Mpay pay) {
		if (pay.getPay_record_status() == 4) {
			System.out.println("-------旧报文备案失败修改为成功--");
			pay.setPay_record_status(3);
			return updatePaymentRecordInfo(pay);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 更新手工支付单状态
	 * 
	 * @param pay
	 * @return
	 */
	private Map<String, Object> updatePaymentRecordInfo(Mpay pay) {
		pay.setUpdate_date(new Date());
		if (!paymentDao.update(pay)) {
			return ReturnInfoUtils.errorInfo("异步更新订单备案信息错误!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Object getMpayRecordInfo(String merchantId, String merchantName, Map<String, Object> params, int page,
			int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalMPaymentSearch(params);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		paramMap.put(MERCHANT_NO, merchantId);
		paramMap.put("del_flag", 0);
		List<Object> reList = paymentDao.findByPropertyLike(Mpay.class, paramMap, null, page, size);
		long tatolCount = paymentDao.findByPropertyLikeCount(Mpay.class, paramMap, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, tatolCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> getMerchantPaymentReport(String merchantId, String merchantName, String startDate,
			String endDate) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(merchantId)) {
			paramsMap.put(MERCHANT_ID, merchantId);
		}
		if (StringEmptyUtils.isNotEmpty(merchantName)) {
			paramsMap.put(MERCHANT_NAME, merchantName);
		}
		paramsMap.put("startDate", startDate);
		paramsMap.put("endDate", endDate);
		Table reList = paymentDao.getPaymentReport(Morder.class, paramsMap, 0, 0);
		Table totalCount = paymentDao.getPaymentReport(Morder.class, paramsMap, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), Transform.tableToJson(reList).getJSONArray("rows"));
			statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount.getRows().size());
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public final Map<String, Object> groupCreateMpay(List<String> orderIDs, List<Map<String, Object>> errorList,
			Map<String, Object> redisMap) {
		String merchantId = redisMap.get(MERCHANT_ID) + "";
		if (StringEmptyUtils.isNotEmpty(merchantId) && orderIDs != null && !orderIDs.isEmpty()) {
			// 创建线程池
			ExecutorService threadPool = Executors.newCachedThreadPool();
			//
			redisMap.put("name", "createPaymentId");
			for (String order_id : orderIDs) {
				Map<String, Object> params = new HashMap<>();
				params.put(MERCHANT_NO, merchantId);
				params.put("order_id", order_id);
				List<Morder> morder = paymentDao.findByProperty(Morder.class, params, 1, 1);
				if (morder != null && !morder.isEmpty()) {
					Morder order = morder.get(0);
					Map<String, Object> checkInfoMap = new HashMap<>();
					checkInfoMap.put(ORDER_ID, order.getOrder_id());
					checkInfoMap.put("orderDocId", order.getOrderDocId());
					checkInfoMap.put("orderDocName", order.getOrderDocName());
					Map<String, Object> reCheckMap = checkPaymentInfo(checkInfoMap);
					if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
						RedisInfoUtils.commonErrorInfo(reCheckMap.get(BaseCode.MSG.toString()) + "", errorList, ERROR,
								redisMap);
						continue;
					}
					//
					Map<String, Object> paymentMap = new HashMap<>();
					paymentMap.put(MERCHANT_ID, merchantId);
					int count = SerialNoUtils.getSerialNo("paymentId");
					String tradeNo = SerialNoUtils.createTradeNo("01O", (count + 1), new Date());
					paymentMap.put("tradeNo", tradeNo);
					paymentMap.put(ORDER_ID, order_id);
					paymentMap.put("amount", order.getActualAmountPaid());
					paymentMap.put("orderDocName", order.getOrderDocName());
					paymentMap.put("orderDocId", order.getOrderDocId());
					paymentMap.put("orderDocTel", order.getOrderDocTel());
					paymentMap.put("orderDate", order.getOrderDate());
					paymentMap.put("createBy", order.getCreate_by());

					paymentMap.put(E_PORT, order.getEport());
					paymentMap.put(CIQ_ORG_CODE, order.getCiqOrgCode());
					paymentMap.put(CUSTOMS_CODE, order.getCustomsCode());
					paymentMap.put("thirdPartyId", order.getThirdPartyId());
					if (addEntity(paymentMap) && updateOrderPayNo(merchantId, order_id, tradeNo)) {
						// 当创建完支付流水号之后
						bufferUtils.writeRedis(errorList, redisMap);
						// 创建一个生成钱包流水子任务
						String memberId = redisMap.get("memberId") + "";
						if (StringEmptyUtils.isNotEmpty(memberId)) {
							WalletTransferTask walletTransferTask = new WalletTransferTask(memberId, merchantId,
									tradeNo, memberWalletService, order.getActualAmountPaid());
							threadPool.submit(walletTransferTask);
						}
						continue;
					}
					RedisInfoUtils.commonErrorInfo("订单号[" + order_id + "]生成支付单失败,请稍后重试!", errorList, ERROR, redisMap);
					continue;
				}
				RedisInfoUtils.commonErrorInfo("订单号[" + order_id + "]不存在,请核对信息!", errorList, ERROR, redisMap);
			}
			threadPool.shutdown();

			bufferUtils.writeCompletedRedis(errorList, redisMap);
			return null;
		} else {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
	}

	@Override
	public Map<String, Object> checkPaymentInfo(Map<String, Object> infoMap) {
		if (infoMap == null || infoMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("校验支付单参数不能为空!");
		}
		String orderId = infoMap.get(ORDER_ID) + "";
		Map<String, Object> params = new HashMap<>();
		params.put("morder_id", orderId);
		List<Mpay> mpayl = paymentDao.findByProperty(Mpay.class, params, 1, 1);
		if (mpayl != null && !mpayl.isEmpty()) {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]关联的支付单已经存在,无需再次支付!");
		}
		String orderDocId = infoMap.get("orderDocId") + "";
		if (!IdcardValidator.validate18Idcard(orderDocId)) {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]订单人身份证号码错误,请核信息!");
		}
		String orderDocName = infoMap.get("orderDocName") + "";
		if (!StringUtil.isContainChinese(orderDocName.trim().replace("·", "")) || orderDocName.trim().contains("先生")
				|| orderDocName.trim().contains("女士") || orderDocName.trim().contains("小姐")) {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]下单人姓名错误,请核实信息!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public boolean addEntity(Map<String, Object> paymentMap) {
		if (paymentMap == null || paymentMap.isEmpty()) {
			return false;
		}
		Mpay entity = new Mpay();
		entity.setMerchant_no(paymentMap.get(MERCHANT_ID) + "");
		entity.setTrade_no(paymentMap.get("tradeNo") + "");
		entity.setMorder_id(paymentMap.get(ORDER_ID) + "");
		entity.setPay_amount(Double.parseDouble(paymentMap.get("amount") + ""));
		entity.setPayer_name(paymentMap.get("orderDocName") + "");
		entity.setPayer_document_type("01");
		entity.setPayer_document_number(paymentMap.get("orderDocId") + "");
		entity.setPayer_phone_number(paymentMap.get("orderDocTel") + "");
		entity.setTrade_status("TRADE_SUCCESS");
		entity.setDel_flag(0);
		entity.setCreate_date(new Date());
		entity.setYear(DateUtil.formatDate(new Date(), "yyyy"));
		entity.setPay_status("D");
		entity.setPay_currCode("142");
		if (StringEmptyUtils.isNotEmpty(paymentMap.get("pay_record_status") + "")) {
			entity.setPay_record_status(Integer.parseInt(paymentMap.get("pay_record_status") + ""));
		} else {
			// 申报状态：1-待申报、2-申报中、3-申报成功、4-申报失败、10-申报中(待系统处理)
			entity.setPay_record_status(1);
		}
		// 网关接收状态： 0-未发起,1-接收成功,2-接收失败
		entity.setNetworkStatus(0);
		String orderDate = paymentMap.get("orderDate") + "";
		Date payTime = DateUtil.randomPaymentDate(orderDate);
		entity.setPay_time(payTime);
		entity.setCreate_by(paymentMap.get("createBy") + "");
		// 口岸标识
		String eport = paymentMap.get(E_PORT) + "";
		// 国检检疫编码
		String ciqOrgCode = paymentMap.get(CIQ_ORG_CODE) + "";
		// 海关关区编码
		String customsCode = paymentMap.get(CUSTOMS_CODE) + "";
		if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
				&& StringEmptyUtils.isNotEmpty(customsCode)) {
			entity.setEport(eport);
			entity.setCiqOrgCode(ciqOrgCode);
			entity.setCustomsCode(customsCode);
		}
		// 第三方业务Id
		String thirdPartyId = paymentMap.get("thirdPartyId") + "";
		if (StringEmptyUtils.isNotEmpty(thirdPartyId)) {
			entity.setThirdPartyId(thirdPartyId);
		}
		return paymentDao.add(entity);
	}

	@Override
	public boolean updateOrderPayNo(String merchantId, String order_id, String trade_no) {
		Map<String, Object> param = new HashMap<>();
		param.put(MERCHANT_NO, merchantId);
		param.put("order_id", order_id);
		List<Morder> reList = paymentDao.findByProperty(Morder.class, param, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			Morder entity = reList.get(0);
			entity.setTrade_no(trade_no);
			entity.setUpdate_date(new Date());
			return paymentDao.update(entity);
		}
		return false;
	}

	@Override
	public Map<String, Object> splitStartPaymentId(List<String> orderIdList, String merchantId, String merchantName,
			String memberId) {
		Map<String, Object> statusMap = new HashMap<>();
		String serialNo = "payment_" + SerialNoUtils.getSerialNo("payment");
		// 总数
		int totalCount = orderIdList.size();
		List<Map<String, Object>> errorList = new Vector();
		// 缓存参数
		Map<String, Object> params = new HashMap<>();
		params.put(MERCHANT_ID, merchantId);
		params.put(MERCHANT_NAME, merchantName);
		params.put("serialNo", serialNo);
		params.put("memberId", memberId);
		if (StringEmptyUtils.isNotEmpty(memberId)) {
			Map<String, Object> reCheckMap = checkReserveAmount(orderIdList, merchantId, memberId);
			if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
				return reCheckMap;
			}
		}
		//
		Map<String, Object> reMap = invokeTaskUtils.commonInvokeTask(1, totalCount, JSONArray.fromObject(orderIdList),
				errorList, null, params);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		statusMap.put("status", 1);
		statusMap.put("msg", "执行成功,正在生成支付流水号.......");
		statusMap.put("serialNo", serialNo);
		return statusMap;
	}

	/**
	 * 商户生成支付单时,校验商户关联的用户钱包储备金是否有足够的金额支付订单费用
	 * 
	 * @param orderIdList
	 *            手工订单Id
	 * @param merchantId
	 *            商户Id
	 * @param memberId
	 *            用户Id
	 * @return Map
	 */
	private Map<String, Object> checkReserveAmount(List<String> orderIdList, String merchantId, String memberId) {
		// 统计订单总金额
		double totalAmount = paymentDao.statisticsManualOrderAmount(orderIdList);
		if (totalAmount < 0) {
			return ReturnInfoUtils.errorInfo("统计订单总金额失败,服务器繁忙!");
		}
		// 根据用户Id查询用户钱包是否有足够的储备资金支付
		Table table = merchantDao.getRelatedMemberFunds(merchantId, memberId, 0, 0);
		if (table == null || table.getRows().isEmpty()) {
			return ReturnInfoUtils.errorInfo("查询商户关联的用户储备资金错误,服务器繁忙!");
		} else {
			double reserveAmount = 0;
			try {
				reserveAmount = Double.parseDouble(table.getRows().get(0).getValue("reserveAmount") + "");
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("储备金,金额错误!");
			}
			// 手工订单总金额 减 商户关联的用户钱包储备金
			if ((reserveAmount - totalAmount) < 0) {
				return ReturnInfoUtils.errorInfo("储备金不足,请先充值,再进行操作!");
			}
			return ReturnInfoUtils.successInfo();
		}
	}

	@Override
	public Map<String, Object> managerGetMpayInfo(Map<String, Object> params, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalMPaymentSearch(params);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List<Mpay> reList = paymentDao.findByPropertyLike(Mpay.class, paramMap, null, page, size);
		long tatolCount = paymentDao.findByPropertyLikeCount(Mpay.class, paramMap, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, tatolCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> managerEditMpayInfo(Map<String, Object> datasMap, String managerId, String managerName) {
		if (datasMap != null && !datasMap.isEmpty()) {
			Map<String, Object> params = new HashMap<>();
			String tradeNo = datasMap.get(TRADE_NO) + "";
			params.put(TRADE_NO, tradeNo);
			List<Mpay> mPayList = paymentDao.findByProperty(Mpay.class, params, 1, 1);
			if (mPayList != null && !mPayList.isEmpty()) {
				Mpay pay = mPayList.get(0);
				double payAmount = 0.0;
				try {
					payAmount = Double.parseDouble(datasMap.get("pay_amount") + "");
				} catch (Exception e) {
					e.printStackTrace();
					return ReturnInfoUtils.errorInfo("交易金额错误,请重新输入!");
				}
				pay.setPay_amount(payAmount);
				pay.setPayer_name(datasMap.get("payer_name") + "");
				pay.setPayer_document_number(datasMap.get("payer_document_number") + "");
				pay.setPayer_phone_number(datasMap.get("payer_phone_number") + "");
				pay.setDel_flag(Integer.parseInt(datasMap.get("del_flag") + ""));
				pay.setUpdate_by(managerName);
				pay.setUpdate_date(new Date());
				if (paymentDao.update(pay)) {
					return ReturnInfoUtils.successInfo();
				}
				return ReturnInfoUtils.errorInfo("更新支付单失败,服务器繁忙!");
			}
			return ReturnInfoUtils.errorInfo("支付单信息未找到,请核实信息!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	@Override
	public Map<String, Object> getAgentPaymentReport(Map<String, Object> datasMap) {
		if (datasMap != null && !datasMap.isEmpty()) {
			Table reTable = paymentDao.getAgentPaymentReport(datasMap);
			if (reTable == null) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			} else if (!reTable.getRows().isEmpty()) {
				return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reTable).getJSONArray("rows"));
			} else {
				return ReturnInfoUtils.errorInfo("暂无数据");
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数错误！");
	}

	@Override
	public Map<String, Object> managerHideMpayInfo(JSONArray jsonArray, String managerName) {
		if (jsonArray != null && !jsonArray.isEmpty()) {
			Map<String, Object> params = new HashMap<>();
			for (int i = 0; i < jsonArray.size(); i++) {
				String tradeNo = jsonArray.get(i) + "";
				params.put(TRADE_NO, tradeNo);
				List<Mpay> reMpayList = paymentDao.findByProperty(Mpay.class, params, 0, 0);
				if (reMpayList == null) {
					return ReturnInfoUtils.errorInfo("支付流水号[" + tradeNo + "]查询失败,服务器繁忙!");
				} else if (!reMpayList.isEmpty()) {
					Mpay pay = reMpayList.get(0);
					pay.setDel_flag(1);
					pay.setUpdate_by(managerName);
					pay.setUpdate_date(new Date());
					if (!paymentDao.update(pay)) {
						return ReturnInfoUtils.errorInfo("支付流水号[" + tradeNo + "]修改状态失败,服务器繁忙!");
					}
				} else {
					return ReturnInfoUtils.errorInfo("支付流水号[" + tradeNo + "]未找到对应的支付单信息!");
				}
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	/**
	 * 根据支付流水号更新支付单中海关信息
	 * 
	 * @param datasMap
	 */
	public Map<String, Object> updatePaymentPortInfo(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		String eport = datasMap.get(E_PORT) + "";
		String ciqOrgCode = datasMap.get(CIQ_ORG_CODE) + "";
		String customsCode = datasMap.get(CUSTOMS_CODE) + "";
		String tradeNo = datasMap.get("tradeNo") + "";
		Map<String, Object> params = new HashMap<>();
		params.put(TRADE_NO, tradeNo);
		List<Mpay> reMpayList = paymentDao.findByProperty(Mpay.class, params, 1, 1);
		if (reMpayList == null) {
			return ReturnInfoUtils.errorInfo("查询支付单失败,服务器繁忙!");
		} else if (!reMpayList.isEmpty()) {
			Mpay pay = reMpayList.get(0);
			if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
					&& StringEmptyUtils.isNotEmpty(customsCode)) {
				pay.setEport(eport);
				pay.setCiqOrgCode(ciqOrgCode);
				pay.setCustomsCode(customsCode);
			}
			pay.setUpdate_by("system");
			pay.setUpdate_date(new Date());
			if (!paymentDao.update(pay)) {
				return ReturnInfoUtils.errorInfo("更新支付单信息失败,服务器繁忙!");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> checkPaymentPort(List<String> tradeNos, String merchantId) {
		if (tradeNos == null || StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}

		List<Mpay> cacheList = new ArrayList<>();
		for (int i = 0; i < tradeNos.size(); i++) {
			String tradeNo = tradeNos.get(i);
			Map<String, Object> params = new HashMap<>();
			params.put(TRADE_NO, tradeNo);
			params.put(MERCHANT_NO, merchantId);
			List<Mpay> reList = paymentDao.findByProperty(Mpay.class, params, 0, 0);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询支付单信息失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {
				Mpay payment = reList.get(0);
				String eport = payment.getEport();
				String ciqOrgCode = payment.getCiqOrgCode();
				String customsCode = payment.getCustomsCode();
				if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
						&& StringEmptyUtils.isNotEmpty(customsCode)) {
					cacheList.add(payment);
				}
				for (int c = 0; c < cacheList.size(); c++) {
					Mpay cachePayment = cacheList.get(c);
					String cacheEport = cachePayment.getEport();
					String cacheCiqOrgCode = cachePayment.getCiqOrgCode();
					String cacheCustomsCode = cachePayment.getCustomsCode();
					if (!eport.equals(cacheEport) && !ciqOrgCode.equals(cacheCiqOrgCode)
							&& !customsCode.equals(cacheCustomsCode)) {
						return ReturnInfoUtils.errorInfo("所选支付单为多个不同的口岸/海关关区/国检检疫机构信息,请重新选择!");
					}
				}
			} else {
				return ReturnInfoUtils.errorInfo("支付单流水号[" + tradeNo + "]未找到支付单信息,请重新选择!");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getThirdPartyInfo(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(datasMap.get(MERCHANT_ID) + "");
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Map<String, Object> reSearchMap = SearchUtils.universalMPaymentSearch(datasMap);
		if (!"1".equals(reSearchMap.get(BaseCode.STATUS.toString()))) {
			return reSearchMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reSearchMap.get("param");
		return thirdPartyPaymentInfo(paramMap);
	}

	/**
	 * 第三方平获取支付单信息
	 * 
	 * @return Map
	 */
	private Map<String, Object> thirdPartyPaymentInfo(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return ReturnInfoUtils.errorInfo("查询参数不能为空!");
		}
		List<Mpay> rePayList = paymentDao.findByProperty(Mpay.class, params, 1, 1);
		if (rePayList == null) {
			return ReturnInfoUtils.errorInfo("查询支付单信息失败,服务器繁忙!");
		} else if (!rePayList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(rePayList.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("未找到对应的支付单信息!");
		}
	}

	@Override
	public Map<String, Object> managerGetPaymentReportInfo(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Table reList = paymentDao.getPaymentReportInfo(params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reList).getJSONArray("rows"));
		} else {
			return ReturnInfoUtils.errorInfo("暂无支付单报表数据!");
		}
	}

	@Override
	public Map<String, Object> managerGetPaymentReportDetails(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Table reList = paymentDao.getPaymentReportDetails(params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reList).getJSONArray("rows"));
		} else {
			return ReturnInfoUtils.errorInfo("暂无支付单报表数据!");
		}
	}

	@Override
	public Map<String, Object> managerDeleteMpay(JSONArray json, String note, String managerName) {
		if (json == null || json.isEmpty() || StringEmptyUtils.isEmpty(note) || StringEmptyUtils.isEmpty(managerName)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String,Object> params = new HashMap<>();
		Map<String,Object> errMap = null;
		List<Map<String,Object>> errorList = new ArrayList<>();
		for (int i = 0; i < json.size(); i++) {
			params.clear();
			String tradeNo = String.valueOf(json.get(i));
			params.put(TRADE_NO, tradeNo);
			System.out.println("---tradeNo->>"+tradeNo);
			List<Mpay> rePayList = paymentDao.findByProperty(Mpay.class, params, 1, 1);
			if (rePayList == null) {
				errMap = new HashMap<>();
				errMap.put(BaseCode.MSG.toString(), "支付流水号["+tradeNo+"]查询支付单信息失败,服务器繁忙!");
				errorList.add(errMap);
			} else if (!rePayList.isEmpty()) {
				Mpay payment = rePayList.get(0);
				OldManualPayment oldPayment = new OldManualPayment();
				try {
					CopyUtils.copy(payment, oldPayment);
				} catch (Exception e) {
					errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(), "支付流水号["+tradeNo+"]转换失败!");
					errorList.add(errMap);
				}
				// 注明删除原因
				oldPayment.setRemarks(note);
				oldPayment.setDeleteBy(managerName);
				oldPayment.setDeleteDate(new Date());
				Map<String, Object> reMpayMap = transferMpay(oldPayment);
				if (!"1".equals(reMpayMap.get(BaseCode.STATUS.toString()))) {
					errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(),reMpayMap.get(BaseCode.MSG.toString()));
					errorList.add(errMap);
				}
				Map<String, Object> reDelMpayMap = deleteMpay(payment);
				if (!"1".equals(reDelMpayMap.get(BaseCode.STATUS.toString()))) {
					errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(),reDelMpayMap.get(BaseCode.MSG.toString()));
					errorList.add(errMap);
				}
			} else {
				errMap = new HashMap<>();
				errMap.put(BaseCode.MSG.toString(),"支付流水号["+tradeNo+"]未找到对应的支付单信息!");
				errorList.add(errMap);
			}
		}
		return ReturnInfoUtils.errorInfo(errorList);
	}

	/**
	 * 真实删除手工支付单表中的支付单信息,前提条件为必须已经移植到历史表中
	 * @param payment
	 * @return
	 */
	private Map<String, Object> deleteMpay(Mpay payment) {
		if (!paymentDao.delete(payment)) {			
			return ReturnInfoUtils.errorInfo("支付流水号["+payment.getTrade_no()+"]删除失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 转移手工支付单信息
	 * @param oldPayment 
	 * @return
	 */
	private Map<String, Object> transferMpay(OldManualPayment oldPayment) {
		if (!paymentDao.add(oldPayment)) {			
			return ReturnInfoUtils.errorInfo("支付流水号["+oldPayment.getTrade_no()+"]添加至历史表中失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}
}
