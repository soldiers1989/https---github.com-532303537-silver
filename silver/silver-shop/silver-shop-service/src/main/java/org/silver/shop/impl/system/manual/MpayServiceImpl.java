package org.silver.shop.impl.system.manual;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.api.system.tenant.MerchantFeeService;
import org.silver.shop.api.system.tenant.MerchantIdCardCostService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.manual.MorderDao;
import org.silver.shop.dao.system.manual.MpayDao;
import org.silver.shop.model.common.base.IdCard;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.log.AgentWalletLog;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.manual.PaymentCallBack;
import org.silver.shop.model.system.manual.ThirdPartyOrderCallBack;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.model.system.tenant.MerchantIdCardCostContent;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.BufferUtils;
import org.silver.shop.util.InvokeTaskUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.CompressUtils;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.MD5;
import org.silver.util.PhoneUtils;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.SortUtil;
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

@Service(interfaceClass = MpayService.class)
public class MpayServiceImpl implements MpayService {
	@Resource
	private MpayDao mpayDao;
	@Resource
	private MorderDao morderDao;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private GoodsRecordService goodsRecordService;
	@Autowired
	private BufferUtils bufferUtils;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private InvokeTaskUtils invokeTaskUtils;
	@Autowired
	private MerchantFeeService merchantFeeService;
	@Autowired
	private WalletUtils walletUtils;
	@Autowired
	private MerchantIdCardCostService merchantIdCardCostService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private MerchantWalletService merchantWalletService;

	private static Logger logger = LogManager.getLogger(MpayServiceImpl.class);

	// 进出境标志I-进，E-出
	private static final String IEFLAG = "I";

	// 币制默认为人民币
	private static final String CURRCODE = "142";
	/**
	 * 错误标识
	 */
	private static final String ERROR = "error";
	/**
	 * 驼峰命名：商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 下划线命名：订单Id
	 */
	private static final String ORDER_ID = "order_id";
	/**
	 * 驼峰命名：商户名称
	 */
	private static final String MERCHANT_NAME = "merchantName";

	/**
	 * 口岸
	 */
	private static final String E_PORT = "eport";

	/**
	 * 驼峰命名：检验检疫机构代码
	 */
	private static final String CIQ_ORG_CODE = "ciqOrgCode";

	/**
	 * 驼峰命名：主管海关代码
	 */
	private static final String CUSTOMS_CODE = "customsCode";

	/**
	 * 驼峰命名：钱包流水Id
	 */
	private static final String WALLET_ID = "walletId";

	@Override
	public Object sendMorderRecord(String merchantId, Map<String, Object> customsMap, String orderNoPack,
			String merchantName) {
		// 总参数
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(orderNoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单Id信息包格式错误！");
		}
		if (jsonList == null || jsonList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("订单Id信息包不能为空!");
		}
		int eport = Integer.parseInt(customsMap.get(E_PORT) + "");
		String ciqOrgCode = customsMap.get(CIQ_ORG_CODE) + "";
		String customsCode = customsMap.get(CUSTOMS_CODE) + "";
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
		customsMap.put("ebEntNo", merchantRecordInfo.getEbEntNo());
		customsMap.put("ebEntName", merchantRecordInfo.getEbEntName());
		customsMap.put("ebpEntNo", merchantRecordInfo.getEbpEntNo());
		customsMap.put("ebpEntName", merchantRecordInfo.getEbpEntName());
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
			customsMap.put("appkey", appkey.getApp_key());
			customsMap.put("appSecret", appkey.getApp_secret());
		} else {
			// 当不是第三方时则使用银盟商城appkey
			customsMap.put("appkey", YmMallConfig.APPKEY);
			customsMap.put("appSecret", YmMallConfig.APPSECRET);
		}
		// 请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(customsMap.get("appkey") + "",
				customsMap.get("appSecret") + "");
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			return ReturnInfoUtils.errorInfo(reTokMap.get("errMsg") + "");
		}
		String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
		//
		// 商户口岸费率Id
		String merchantFeeId = customsMap.get("merchantFeeId") + "";
		Map<String, Object> reCheckMap = computingCostsManualOrder(jsonList, merchant, merchantFeeId, customsMap,
				errorList);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		// 总数
		int totalCount = jsonList.size();
		params.put(MERCHANT_ID, merchantId);
		params.put(MERCHANT_NAME, merchantName);
		params.put("tok", tok);
		// 获取流水号
		String serialNo = "orderRecord_" + SerialNoUtils.getSerialNo("orderRecord");
		params.put("serialNo", serialNo);
		String pushType = customsMap.get("pushType") + "";
		// 当商户为自主申报时
		if (StringEmptyUtils.isNotEmpty(pushType) && "selfReportOrder".equals(pushType)) {
			double fee = Double.parseDouble(reCheckMap.get("fee") + "");
			int backCoverFlag = Integer.parseInt(reCheckMap.get("backCoverFlag") + "");
			Map<String, Object> reMap = updateOrderRecordStatus(jsonList, merchantId, customsMap, errorList, fee,
					backCoverFlag);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
		} else {
			Map<String, Object> reMap = invokeTaskUtils.commonInvokeTask(2, totalCount, jsonList, errorList, customsMap,
					params);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("serialNo", serialNo);
		map.put("orderList", reCheckMap.get("list"));
		map.put("idCardList", reCheckMap.get("idCardList"));
		map.put(BaseCode.ERROR.toString(), errorList);
		map.put(BaseCode.TOTALCOUNT.toString(), totalCount);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		map.put(BaseCode.MSG.toString(), "执行成功,开始推送订单备案.......");
		return map;
	}

	/**
	 * 将商户选择的自助申报的订单修改为申报状态(10)
	 * 
	 * @param jsonList
	 *            订单Id集合
	 * @param merchantId
	 *            商户Id
	 * @param ciqOrgCode
	 *            国检检疫机构代码
	 * @param customsCode
	 *            海关代码
	 * @param eport
	 *            口岸
	 * @param errorList
	 *            错误信息集合
	 * @param backCoverFlag
	 *            封底标识 封底标识：1-正常计算、2-不满100提至100计算
	 * @param fee
	 *            订单费率
	 * @return Map
	 */
	private Map<String, Object> updateOrderRecordStatus(JSONArray jsonList, String merchantId,
			Map<String, Object> customsMap, List<Map<String, Object>> errorList, double fee, int backCoverFlag) {
		System.out.println("-------将商户选择的自助申报的订单修改为申报状态(10)--------");
		if (jsonList == null || customsMap == null || errorList == null) {
			return ReturnInfoUtils.errorInfo("自助申报失败,服务器繁忙！");
		}
		String memberId = customsMap.get("memberId") + "";
		if (StringEmptyUtils.isEmpty(memberId)) {
			memberId = "Member_2017000025928";
		}
		Map<String, Object> reMemberMap = memberService.getMemberInfo(memberId);
		if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
			return reMemberMap;
		}
		Member member = (Member) reMemberMap.get(BaseCode.DATAS.toString());
		Map<String, Object> errMap = null;
		for (int i = 0; i < jsonList.size(); i++) {
			JSONObject json = JSONObject.fromObject(jsonList.get(i));
			Map<String, Object> param = new HashMap<>();
			param.put("merchant_no", merchantId);
			param.put(ORDER_ID, json.get("orderNo"));
			List<Morder> orderList = morderDao.findByProperty(Morder.class, param, 1, 1);
			if (orderList != null && !orderList.isEmpty()) {
				Morder order = orderList.get(0);
				Map<String, Object> reCheckMap = beforePushingCheck(order);
				if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
					errorList.add(reCheckMap);
				} else {
					int eport = Integer.parseInt(customsMap.get(E_PORT) + "");
					String ciqOrgCode = customsMap.get(CIQ_ORG_CODE) + "";
					String customsCode = customsMap.get(CUSTOMS_CODE) + "";
					if (eport > 0 && StringEmptyUtils.isNotEmpty(customsCode)
							&& StringEmptyUtils.isNotEmpty(ciqOrgCode)) {
						order.setEport(eport + "");
						order.setCustomsCode(customsCode);
						order.setCiqOrgCode(ciqOrgCode);
					}
					setOrderPayer(member, order);
					// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
					order.setOrder_record_status(10);
					// 订单推送至网关接收状态： 0-未发起,1-已发起,2-接收成功,3-接收失败
					order.setStatus(1);
					order.setUpdate_date(new Date());
					order.setPlatformFee(fee);
					order.setBackCoverFlag(backCoverFlag);
					if (!morderDao.update(order)) {
						errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "订单号[" + order.getOrder_id() + "]申报失败,服务器繁忙,请重试!");
						errorList.add(errMap);
						continue;
					}
				}
			}
		}
		return ReturnInfoUtils.errorInfo(errorList, jsonList.size());
	}

	private Map<String, Object> beforePushingCheck(Morder order) {
		// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
		if (order.getOrder_record_status() == 10 || order.getOrder_record_status() == 2) {
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]正在申报中,请勿重复申报！");
		} else if (order.getOrder_record_status() == 3) {
			return ReturnInfoUtils
					.errorInfo("订单[" + order.getOrder_id() + "]已在[" + order.getCreate_date() + "]申报成功,请勿重复申报！");
		} else if (order.getIdcardCertifiedFlag() == 2) {// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]实名认证不通过,不允许申报！");
		}
		return checkManualOrderInfo(order);
	}

	private void setOrderPayer(Member member, Morder order) {
		if (order != null && member != null) {
			order.setOrderPayerId(member.getMemberId());
			order.setOrderPayerName(member.getMemberName());
		}
	}

	@Override
	public Map<String, Object> computingCostsManualOrder(JSONArray jsonList, Merchant merchant, String merchantFeeId,
			Map<String, Object> customsMap, List<Map<String, Object>> errorList) {
		if (jsonList == null || merchant == null) {
			return ReturnInfoUtils.errorInfo("计算商户钱包余额请求参数不能为空!");
		}
		Map<String, Object> reCheckMap = beforePushingCheckManualOrder(
				JSONArray.toList(jsonList, new HashMap<>(), new JsonConfig()));
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		// 获取校验通过的订单信息集合
		List<Object> newList = (List<Object>) reCheckMap.get(BaseCode.DATAS.toString());
		// 查询商户钱包
		Map<String, Object> reMap = walletUtils.checkWallet(1, merchant.getMerchantId(), merchant.getMerchantName());
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		MerchantWalletContent merchantWallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		//
		Map<String, Object> reCostMap = merchantIdCardCostService.getIdCardCostInfo(merchant.getMerchantId());
		if (!"1".equals(reCostMap.get(BaseCode.STATUS.toString()))) {
			return reCostMap;
		}
		MerchantIdCardCostContent merchantCost = (MerchantIdCardCostContent) reCostMap.get(BaseCode.DATAS.toString());
		double idCost = merchantCost.getPlatformCost();
		// 实名认证每笔手续费
		Map<String, Object> reIdCardMap = getTollIdCardList(jsonList);
		if (!"1".equals(reIdCardMap.get(BaseCode.STATUS.toString()))) {
			return reIdCardMap;
		}
		//收费的身份证集合
		List<Object> reIdCardList = (List<Object>) reIdCardMap.get("idCardList");
		//免费身份证集合
		List<Object> reIdCardFreeList = (List<Object>) reIdCardMap.get("idCardFreeList");
		// 计算需要实名认证收费的费用之和
		double idCertificationFee = reIdCardList.size() * idCost;
		logger.error("--订申报时-身份证认证数量-->" + reIdCardList.size() + ";--费率->" + idCost + ";--结果-->" + idCertificationFee);
		// 初始化平台服务费
		double fee;
		// 封底标识：1-正常计算、2-不满100提至100计算
		int backCoverFlag = 0;
		// 当商户口岸费率Id不为空时获取商户当前口岸的平台服务费率
		Map<String, Object> reFeeMap = getMerchantFee(merchantFeeId, customsMap, merchant.getMerchantId());
		if (!"1".equals(reFeeMap.get(BaseCode.STATUS.toString()))) {
			return reFeeMap;
		}
		fee = Double.parseDouble(reFeeMap.get("fee") + "");
		backCoverFlag = Integer.parseInt(reFeeMap.get("backCoverFlag") + "");
		double totalAmountPaid = 0;
		// 封底标识：1-正常计算、2-不满100提至100计算
		if (backCoverFlag == 2) {
			// 当订单实际支付金额不足100提升至100,后统计订单实际支付金额
			totalAmountPaid = morderDao.backCoverStatisticalManualOrderAmount(newList);
		} else {
			// 统计未发起过备案的手工订单实际支付金额的总额
			totalAmountPaid = morderDao.statisticalManualOrderAmount(newList);
		}
		// 当小于0时,代表查询数据库信息错误
		if (totalAmountPaid < 0) {
			return ReturnInfoUtils.errorInfo("查询订单总金额失败,服务器繁忙!");
		} else if (totalAmountPaid > 0) {// 当查询出来手工订单总金额大于0时,进行平台服务费计算
			// 订申报单手续费
			double serviceFee = totalAmountPaid * fee;
			// 申报手续+实名认证手续费
			double totalFee = serviceFee + idCertificationFee;
			// 钱包余额
			double oldBalance = merchantWallet.getBalance();
			logger.error("--订申报时-订单总金额->>" + totalAmountPaid + ";--费率-->" + fee + ";--结果->" + serviceFee);
			if ((oldBalance - totalFee) < 0) {
				return ReturnInfoUtils.errorInfo("操作失败,余额不足!");
			}
			//
			Map<String, Object> reTransferMap = merchantWalletService.balanceTransferFreezingFunds(merchantWallet,
					totalFee);
			if (!"1".equals(reTransferMap.get(BaseCode.STATUS.toString()))) {
				return reTransferMap;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("fee", fee);
		map.put("backCoverFlag", backCoverFlag);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		map.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		map.put("list", newList);
		reIdCardList.addAll(reIdCardFreeList);
		map.put("idCardList", reIdCardList);
		return map;
	}

	/**
	 * 根据订单id查询订单中未实名，并且实名库中未认证成功的身份证信息进行统计数量
	 * 
	 * @param jsonList
	 * @return
	 */
	private Map<String, Object> getTollIdCardList(JSONArray jsonList) {
		if (jsonList == null) {
			return ReturnInfoUtils.errorInfo("订单id集合不能为null");
		}
		//收费的身份证集合
		List<String> idCardList = new ArrayList<>();
		//不需要计费的身份证集合
		List<String> idCardFreeList = new ArrayList<>();
		Map<String, Object> params = null;
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> map = (Map<String, Object>) jsonList.get(i);
			params = new HashMap<>();
			params.put(ORDER_ID, map.get("orderNo") + "");
			List<Morder> reList = morderDao.findByProperty(Morder.class, params, 1, 1);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("获取订单中实名认证不通过的订单时，查询订单信息失败,服务器繁忙！");
			} else if (!reList.isEmpty()) {
				Morder order = reList.get(0);
				// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
				params.clear();
				params.put(MERCHANT_ID, order.getMerchant_no());
				params.put("name", order.getOrderDocName().trim());
				params.put("idNumber", order.getOrderDocId().trim());
				List<IdCard> reIdList = morderDao.findByProperty(IdCard.class, params, 1, 1);
				if (reIdList == null) {
					logger.error(order.getOrder_id() + "获取订单中实名认证不通过的订单时--查询实名库失败--");
				} else if (!reIdList.isEmpty()) {// 实名库已存在身份证信息
					IdCard idCard = reIdList.get(0);
					// 只要是实名库的认证状态是认证失败,则都需要进行实名计费
					if ("failure".equals(idCard.getStatus())) {
						idCardList.add(order.getOrder_id());
					}else{
						idCardFreeList.add(order.getOrder_id());
					}
				} else {// 当实名库中没有该商户的姓名+身份证号码，则进行计费统计
					idCardList.add(order.getOrder_id());
				}
			}
		}
		Map<String,Object> map = new HashMap<>();
		map.put("idCardList", idCardList);
		map.put("idCardFreeList", idCardFreeList);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		map.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return map;
	}

	/**
	 * 获取商户口岸费率
	 * 
	 * @param merchantFeeId
	 *            商户口岸费率id
	 * @param customsMap
	 *            海关信息集合
	 * @param merchantId
	 *            商户id
	 * @return Map
	 */
	private Map<String, Object> getMerchantFee(String merchantFeeId, Map<String, Object> customsMap,
			String merchantId) {
		// 封底标识：1-正常计算、2-不满100提至100计算
		int backCoverFlag = 0;
		// 支付单服务费率
		double paymentFee = 0;
		// 初始化平台服务费
		double fee;
		if (StringEmptyUtils.isNotEmpty(merchantFeeId)) {
			String pushType = customsMap.get("pushType") + "";
			// 当推送类型为商户自助申报时,将商户的订单与支付单口岸费率合并一次清算
			if (StringEmptyUtils.isNotEmpty(pushType) && "selfReportOrder".equals(pushType)) {
				Map<String, Object> params = new HashMap<>();
				int eport = Integer.parseInt(customsMap.get(E_PORT) + "");
				String ciqOrgCode = customsMap.get(CIQ_ORG_CODE) + "";
				String customsCode = customsMap.get(CUSTOMS_CODE) + "";
				params.put(MERCHANT_ID, merchantId);
				params.put("customsPort", eport);
				params.put(CUSTOMS_CODE, customsCode);
				params.put(CIQ_ORG_CODE, ciqOrgCode);
				params.put("type", "paymentRecord");
				List<MerchantFeeContent> reFeeList = morderDao.findByProperty(MerchantFeeContent.class, params, 1, 1);
				if (reFeeList != null && !reFeeList.isEmpty()) {
					MerchantFeeContent feeContent = reFeeList.get(0);
					paymentFee = feeContent.getPlatformFee();
				} else {
					return ReturnInfoUtils.errorInfo("查询商户费率失败,请联系管理员!");
				}
			}
			// 根据前台传递的商户订单备案口岸费率Id查询
			Map<String, Object> reFeeMap = merchantFeeService.getMerchantFeeInfo(merchantFeeId);
			if (!"1".equals(reFeeMap.get(BaseCode.STATUS.toString()))) {
				return reFeeMap;
			}
			MerchantFeeContent merchantFee = (MerchantFeeContent) reFeeMap.get(BaseCode.DATAS.toString());
			//
			fee = merchantFee.getPlatformFee() + paymentFee;
			backCoverFlag = merchantFee.getBackCoverFlag();
		} else {
			fee = 0.001;
		}
		Map<String, Object> map = new HashMap<>();
		map.put("fee", fee);
		map.put("backCoverFlag", backCoverFlag);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	@Override
	public Map<String, Object> addAgentWalletLog(Map<String, Object> datas) {
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
		if (!morderDao.add(log)) {
			return ReturnInfoUtils.errorInfo("保存代理商钱包日志流水信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 推送订单前进行订单校验,保证扣费订单都未校验通过的订单信息
	 * 
	 * @param list
	 * @return
	 */
	private Map<String, Object> beforePushingCheckManualOrder(List<Map<String, Object>> list) {
		if (list == null) {
			return ReturnInfoUtils.errorInfo("推送订单扣费前校验订单信息失败,请求参数不能为空!");
		}
		List<Object> newOrderIdList = new ArrayList<>();
		// 过滤掉已推送过网关得订单
		List<Morder> reList = morderDao.findByPropertyIn(list);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("推送订单前校验订单数据失败!");
		} else if (!reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Morder order = reList.get(i);
				Map<String, Object> reCheckOrderMap = checkManualOrderInfo(order);
				if ("1".equals(reCheckOrderMap.get(BaseCode.STATUS.toString()))) {
					newOrderIdList.add(order.getOrder_id());
				}
			}
		}
		return ReturnInfoUtils.successDataInfo(newOrderIdList);
	}

	@Override
	public final void startSendOrderRecord(JSONArray dataList, List<Map<String, Object>> errorList,
			Map<String, Object> customsMap, Map<String, Object> paramsMap) {
		String merchantId = paramsMap.get(MERCHANT_ID) + "";
		String tok = paramsMap.get("tok") + "";
		paramsMap.put("name", "orderRecord");
		for (int i = 0; i < dataList.size(); i++) {
			try {
				Map<String, Object> orderMap = (Map<String, Object>) dataList.get(i);
				String orderNo = orderMap.get("orderNo") + "";
				Map<String, Object> param = new HashMap<>();
				param.put("merchant_no", merchantId);
				param.put(ORDER_ID, orderNo);
				List<Morder> orderList = morderDao.findByProperty(Morder.class, param, 1, 1);
				param.clear();
				param.put(ORDER_ID, orderNo);
				param.put("deleteFlag", 0);
				List<MorderSub> orderSubList = morderDao.findByProperty(MorderSub.class, param, 0, 0);
				if (orderList == null || orderSubList == null) {
					String msg = "订单号[" + orderNo + "]查询订单信息失败,服务器繁忙!";
					RedisInfoUtils.commonErrorInfo(msg, errorList, ERROR, paramsMap);
					continue;
				} else {
					Morder order = orderList.get(0);
					Map<String, Object> reCheckMap = checkManualOrderInfo(order);
					if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
						RedisInfoUtils.commonErrorInfo(reCheckMap.get(BaseCode.MSG.toString()) + "", errorList, ERROR,
								paramsMap);
						continue;
					}
					Map<String, Object> reOrderMap = sendOrder(customsMap, orderSubList, tok, order);
					if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()) + "")) {
						StringBuilder msg = new StringBuilder("订单号[" + orderNo + "]-->");
						Map<String, Object> reUpdateMap = updateOrderErrorStatus(orderNo);
						if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
							msg.append(reUpdateMap.get(BaseCode.MSG.toString()) + "");
						} else {
							msg.append(reOrderMap.get(BaseCode.MSG.toString()) + "");
						}
						RedisInfoUtils.commonErrorInfo(msg.toString(), errorList, ERROR, paramsMap);
						continue;
					} else {
						String reOrderMessageID = reOrderMap.get("messageID") + "";
						// 更新服务器返回订单Id
						Map<String, Object> reUpdateMap = updateOrderInfo(orderNo, reOrderMessageID, customsMap);
						if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()) + "")) {
							RedisInfoUtils.commonErrorInfo(reUpdateMap.get(BaseCode.MSG.toString()) + "", errorList,
									ERROR, paramsMap);
							continue;
						}
					}
				}
				Thread.sleep(200);
			} catch (Exception e) {
				logger.error("------推送订单信息错误-----", e);
			} finally {
				bufferUtils.writeRedis(errorList, paramsMap);
			}
		}
		bufferUtils.writeCompletedRedis(errorList, paramsMap);
	}

	/**
	 * 推送手工订单信息时校验数据
	 * 
	 * @param order
	 *            手工订单信息
	 * @return Map
	 */
	private Map<String, Object> checkManualOrderInfo(Morder order) {
		if (order == null) {
			return ReturnInfoUtils.errorInfo("申报订单时,核对订单数据时订单信息不能为空!");
		}
		if (order.getFCY() > 2000) {
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]申报失败,订单商品总金额超过2000,请核对订单信息!");
		}
		if (!checkProvincesCityAreaCode(order)) {
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]申报失败,订单收货人省市区编码不能为空,请核对订单信息!");
		}
		if (!PhoneUtils.isPhone(order.getRecipientTel().trim())) {
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]申报失败,收货人手机号码格式不正确,请核对订单信息!");
		}
		if (!PhoneUtils.isPhone(order.getOrderDocTel().trim())) {
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]申报失败,下单人手机号码格式不正确,请核对订单信息!");
		}
		if (!IdcardValidator.validate18Idcard(order.getOrderDocId().trim())) {
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]申报失败,下单人身份证号码错误,请核对订单信息!");
		}
		// if (!IdcardValidator.validate18Idcard(order.getRecipientID())) {
		// return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() +
		// "]推送失败,收货人身份证号码实名认证失败,请核对订单信息!");
		// }
		String recipientName = order.getRecipientName().trim();
		if (!StringUtil.isChinese(recipientName) || recipientName.contains("先生") || recipientName.contains("女士")
				|| recipientName.contains("小姐")) {
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]申报失败,收货人姓名错误,请核对订单信息!");
		}
		String orderDocName = order.getOrderDocName().trim();
		if (!StringUtil.isChinese(orderDocName) || orderDocName.contains("先生") || orderDocName.contains("女士")
				|| orderDocName.contains("小姐")) {
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]申报失败,订单人姓名错误,请核对订单信息!");
		}
		if (StringEmptyUtils.isEmpty(order.getOrderDocAcount())) {
			return ReturnInfoUtils.errorInfo("订单[" + order.getOrder_id() + "]申报失败,订单下单人账号错误,请核对订单信息!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> updateOrderErrorStatus(String orderNo) {
		Map<String, Object> params = new HashMap<>();
		params.put(ORDER_ID, orderNo);
		List<Morder> reList = morderDao.findByProperty(Morder.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("推送订单备案接收失败后,更新订单接收状态时查询订单信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			Morder order = reList.get(0);
			// 订单接收状态： 0-未发起,1-已发起,2-接收成功,3-接收失败
			order.setStatus(3);
			order.setUpdate_date(new Date());
			if (!morderDao.update(order)) {
				return ReturnInfoUtils.errorInfo("推送订单备案接收失败后,更新订单接收状态失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("推送订单备案接收失败后,更新订单接收状态时,找不到订单信息!");
		}
	}

	/**
	 * 校验手工订单省市区编码是否为空
	 * 
	 * @param order
	 *            手工订单信息
	 * @return boolean
	 */
	private boolean checkProvincesCityAreaCode(Morder order) {
		if (StringEmptyUtils.isNotEmpty(order.getRecipientAreaCode())) {
			return true;
		}
		if (StringEmptyUtils.isNotEmpty(order.getRecipientCityCode())) {
			return true;
		}
		return StringEmptyUtils.isNotEmpty(order.getRecipientProvincesCode());
	}

	@Override
	public Map<String, Object> updateOrderInfo(String orderNo, String reOrderMessageID,
			Map<String, Object> customsMap) {
		String eport = customsMap.get(E_PORT) + "";
		String ciqOrgCode = customsMap.get(CIQ_ORG_CODE) + "";
		String customsCode = customsMap.get(CUSTOMS_CODE) + "";
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(ORDER_ID, orderNo);
		List<Morder> reList = morderDao.findByProperty(Morder.class, paramMap, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Morder order = reList.get(i);
				order.setOrder_serial_no(reOrderMessageID);
				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				order.setOrder_record_status(2);
				// 订单接收状态： 0-未发起,1-已发起,2-接收成功,3-接收失败
				order.setStatus(2);
				order.setUpdate_date(new Date());
				if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
						&& StringEmptyUtils.isNotEmpty(customsCode)) {
					order.setEport(eport);
					order.setCiqOrgCode(ciqOrgCode);
					order.setCustomsCode(customsCode);
				}
				if (!morderDao.update(order)) {
					return ReturnInfoUtils.errorInfo("更新服务器返回messageID错误!");
				}
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("更新支付订单返回messageID错误,服务器繁忙！");
		}
	}

	@Override
	public Map<String, Object> sendOrder(Map<String, Object> customsMap, List<MorderSub> orderSubList, String tok,
			Morder order) {
		if (orderSubList == null || order == null || customsMap == null || StringEmptyUtils.isEmpty(tok)) {
			return ReturnInfoUtils.errorInfo("推送订单时,请求参数不能为空!");
		}
		String timestamp = String.valueOf(System.currentTimeMillis());
		List<JSONObject> goodsList = new ArrayList<>();
		List<JSONObject> orderJsonList = new ArrayList<>();
		Map<String, Object> orderMap = new HashMap<>();
		JSONObject goodsJson = null;
		JSONObject orderJson = new JSONObject();
		// 排序商品seq
		SortUtil.sortList(orderSubList, "seqNo", "ASC");
		String ebEntNo = "";
		String ebEntName = "";
		// 电子口岸16位编码
		String dzkaNo = "";

		for (int i = 0; i < orderSubList.size(); i++) {
			goodsJson = new JSONObject();
			MorderSub goodsInfo = orderSubList.get(i);
			goodsJson.element("Seq", i + 1);
			goodsJson.element("EntGoodsNo", goodsInfo.getEntGoodsNo());
			goodsJson.element("CIQGoodsNo", goodsInfo.getCIQGoodsNo());
			goodsJson.element("CusGoodsNo", goodsInfo.getCusGoodsNo());
			goodsJson.element("HSCode", goodsInfo.getHSCode());
			goodsJson.element("GoodsName", goodsInfo.getGoodsName());
			goodsJson.element("GoodsStyle", goodsInfo.getGoodsStyle());
			goodsJson.element("GoodsDescribe", "");
			goodsJson.element("OriginCountry", goodsInfo.getOriginCountry());
			goodsJson.element("BarCode", goodsInfo.getBarCode());
			goodsJson.element("Brand", goodsInfo.getBrand());
			goodsJson.element("Qty", goodsInfo.getQty());
			goodsJson.element("Unit", goodsInfo.getUnit());
			goodsJson.element("Price", goodsInfo.getPrice());
			goodsJson.element("Total", goodsInfo.getTotal());
			goodsJson.element("CurrCode", "142");
			goodsJson.element("Notes", "");
			// 企邦商品业务字段
			String jsonGoods = goodsInfo.getSpareParams();
			if (StringEmptyUtils.isNotEmpty(jsonGoods)) {
				JSONObject params = JSONObject.fromObject(jsonGoods);
				// 企邦专属字段
				goodsJson.element("marCode", params.get("marCode"));
				goodsJson.element("sku", params.get("SKU"));
				// 电商企业编号
				ebEntNo = params.get("ebEntNo") + "";
				// 电商企业名称
				ebEntName = params.get("ebEntName") + "";
				// 电子口岸(16)编码
				dzkaNo = params.get("DZKNNo") + "";
			}
			goodsList.add(goodsJson);
		}
		orderJson.element("orderGoodsList", goodsList);
		orderJson.element("EntOrderNo", order.getOrder_id());
		orderJson.element("OrderStatus", 1);
		orderJson.element("PayStatus", 0);
		orderJson.element("OrderGoodTotal", order.getFCY());
		orderJson.element("OrderGoodTotalCurr", order.getFcode());
		orderJson.element("Freight", 0);
		orderJson.element("Tax", order.getTax());
		orderJson.element("OtherPayment", 0);
		orderJson.element("OtherPayNotes", "");
		orderJson.element("OtherCharges", 0);
		orderJson.element("ActualAmountPaid", order.getActualAmountPaid());
		orderJson.element("RecipientName", order.getRecipientName());
		orderJson.element("RecipientAddr", order.getRecipientAddr());
		orderJson.element("RecipientTel", order.getRecipientTel());
		orderJson.element("RecipientCountry", "142");
		orderJson.element("RecipientProvincesCode", order.getRecipientProvincesCode());
		orderJson.element("RecipientCityCode", order.getRecipientCityCode());
		orderJson.element("RecipientAreaCode", order.getRecipientAreaCode());
		orderJson.element("OrderDocAcount", order.getOrderDocAcount());
		orderJson.element("OrderDocName", order.getOrderDocName());
		orderJson.element("OrderDocType", order.getOrderDocType());
		orderJson.element("OrderDocId", order.getOrderDocId());
		orderJson.element("OrderDocTel", order.getOrderDocTel());
		orderJson.element("OrderDate", order.getOrderDate());
		orderJson.element("entPayNo", order.getTrade_no());
		orderJson.element("waybill", order.getWaybill());
		addQBOrderInfo(order, orderJson);
		orderJsonList.add(orderJson);
		// 客戶端签名
		String clientsign = "";
		// YM APPKEY = "4a5de70025a7425dabeef6e8ea752976";
		String appkey = customsMap.get("appkey") + "";
		try {
			clientsign = MD5
					.getMD5((appkey + tok + orderJsonList.toString() + YmMallConfig.MANUAL_ORDER_NOTIFY_URL + timestamp)
							.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("------推送订单失败,MD5加密客户端签名失败-----", e);
			return ReturnInfoUtils.errorInfo("推送订单失败,MD5加密客户端签名失败!");
		}
		// 0:商品备案 1:订单推送 2:支付单推送
		orderMap.put("type", 1);
		int eport = Integer.parseInt(customsMap.get(E_PORT) + "");
		// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		// 1-特殊监管区域BBC保税进口;2-保税仓库BBC保税进口;3-BC直购进口
		int businessType = eport == 1 ? 3 : 2;
		orderMap.put("businessType", businessType);
		orderMap.put("ieFlag", IEFLAG);
		orderMap.put("currCode", CURRCODE);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String inputDate = sdf.format(date);
		// 商品发起备案(录入)日期
		orderMap.put("inputDate", inputDate);
		// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
		orderMap.put(E_PORT, eport);
		if (eport == 1 && StringEmptyUtils.isNotEmpty(dzkaNo)) {// 往电子口岸
			// 电商企业编号
			orderMap.put("ebEntNo", dzkaNo);
			// 电商企业名称
			orderMap.put("ebEntName", ebEntName);
		} else if (StringEmptyUtils.isNotEmpty(ebEntNo) && StringEmptyUtils.isNotEmpty(ebEntName)) {
			// 电商企业编号
			orderMap.put("ebEntNo", ebEntNo);
			// 电商企业名称
			orderMap.put("ebEntName", ebEntName);
		} else {
			String ebEntNo2 = eport == 1 ? "C010000000537118" : "1509007917";
			orderMap.put("ebEntNo", ebEntNo2);
			// 电商企业名称
			orderMap.put("ebEntName", "广州银盟信息科技有限公司");
		}
		orderMap.put(CIQ_ORG_CODE, customsMap.get(CIQ_ORG_CODE));
		orderMap.put(CUSTOMS_CODE, customsMap.get(CUSTOMS_CODE));
		orderMap.put("appkey", appkey);
		orderMap.put("clientsign", clientsign);
		orderMap.put("timestamp", timestamp);
		orderMap.put("datas", orderJsonList.toString());
		orderMap.put("notifyurl", YmMallConfig.MANUAL_ORDER_NOTIFY_URL);
		orderMap.put("note", "");
		// 报文类型
		String opType = customsMap.get("opType") + "";
		if (StringEmptyUtils.isNotEmpty(opType)) {
			// A-新增；M-修改；D-
			orderMap.put("opType", opType);
		} else {
			// 当前台不传参数时默认
			orderMap.put("opType", "A");
		}
		// 是否像海关发送
		// orderMap.put("uploadOrNot", false);
		// 发起订单备案
		Map<String, Object> newOrderMap = orderMap;
		String resultStr = YmHttpUtil.HttpPost(YmMallConfig.REPORT_URL, orderMap);
		// String resultStr =
		// YmHttpUtil.HttpPost("http://192.168.1.183:8080/silver-web/Eport/Report",
		// orderMap);
		// 当端口号为2(智检时)再往电子口岸多发送一次
		if (eport == 2 || "443400".equals(customsMap.get(CIQ_ORG_CODE))) {
			if (eport == 1) {
				// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
				newOrderMap.put(E_PORT, 2);
				// 1-特殊监管区域BBC保税进口;2-保税仓库BBC保税进口;3-BC直购进口
				newOrderMap.put("businessType", 3);
				// 国检 电商企业编号
				newOrderMap.put("ebEntNo", ebEntNo);
				// 电商企业名称
				newOrderMap.put("ebEntName", ebEntName);
			} else if (eport == 2) {
				// 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
				newOrderMap.put(E_PORT, 1);
				if (StringEmptyUtils.isNotEmpty(dzkaNo) && StringEmptyUtils.isNotEmpty(ebEntName)) {
					// 电子口岸 电商企业编号
					newOrderMap.put("ebEntNo", dzkaNo);
					// 电商企业名称
					newOrderMap.put("ebEntName", ebEntName);
				} else {
					newOrderMap.put("ebEntNo", "C010000000537118");
					// 电商企业名称
					newOrderMap.put("ebEntName", "广州银盟信息科技有限公司");
				}
			}
			System.out.println("------订单第二次发送----");
			// 检验检疫机构代码
			newOrderMap.put(CIQ_ORG_CODE, "443400");
			// resultStr =
			// YmHttpUtil.HttpPost("http://192.168.1.183:8080/silver-web/Eport/Report",
			// newOrderMap);
			resultStr = YmHttpUtil.HttpPost(YmMallConfig.REPORT_URL, newOrderMap);
		}
		if (StringEmptyUtils.isNotEmpty(resultStr)) {
			return JSONObject.fromObject(resultStr);
		} else {
			return ReturnInfoUtils.errorInfo("服务器接收订单信息失败！");
		}
	}

	/**
	 * 根据业务需求添加企邦订单专属字段
	 * 
	 * @param order
	 *            订单信息
	 * @param orderJson
	 *            订单集合
	 */
	private void addQBOrderInfo(Morder order, JSONObject orderJson) {
		// 企邦字段
		String orderSpare = order.getSpareParams();
		if (StringEmptyUtils.isNotEmpty(orderSpare)) {
			JSONObject params = JSONObject.fromObject(orderSpare);
			// 企邦快递承运商
			orderJson.element("tmsServiceCode", params.getString("ehsEntName"));
		}
	}

	@Override
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> orMap = new HashMap<>();
		String messageId = datasMap.get("messageID") + "";
		String entOrderNo = datasMap.get("entOrderNo") + "";
		orMap.put("order_serial_no", messageId);
		orMap.put(ORDER_ID, entOrderNo);
		String reMsg = datasMap.get("msg") + "";
		List<Morder> reList = morderDao.findByPropertyOr2(Morder.class, orMap, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			Morder order = reList.get(0);
			String orderId = order.getOrder_id();
			String merchantId = order.getMerchant_no();
			String status = datasMap.get("status") + "";
			String note = order.getOrder_re_note();
			if (StringEmptyUtils.isEmpty(note)) {
				note = "";
			}
			if (StringEmptyUtils.isNotEmpty(reMsg)) {
				order.setOrder_re_note(note + defaultDate + " " + reMsg + "#");
			}
			if ("1".equals(status)) {
				//
				findOrderGoodsInfo(orderId, merchantId);
				order.setOrder_record_status(3);
			} else {
				// 备案失败
				order.setOrder_record_status(4);
			}
			Map<String, Object> reUpdateMap = updateOrderInfo(order);
			if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
				System.out.println("--更新手工订单信息失败-->" + reUpdateMap.get(BaseCode.MSG.toString()));
			}
			return reThirdPartyOrderInfo(order, status, reMsg);
		} else {
			return ReturnInfoUtils.errorInfo("根据订单[" + entOrderNo + "]与messageId[" + messageId + "]未找到订单信息,请核对信息!");
		}
	}

	@Override
	public Map<String, Object> reThirdPartyOrderInfo(Morder order, String status, String reMsg) {
		if (order == null) {
			return ReturnInfoUtils.errorInfo("返回第三方订单时，请求参数不能为null");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(order.getMerchant_no());
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		// 第三方标识：1-银盟(银盟商城平台),2-第三方商城平台
		int thirdPartyFlag = merchant.getThirdPartyFlag();
		if (thirdPartyFlag == 2) {
			System.out.println("---------返回第三方订单信息----------");
			Map<String, Object> item = new HashMap<>();
			JSONObject orderJSON = new JSONObject();
			orderJSON.element("thirdPartyId", order.getThirdPartyId());
			orderJSON.element("EntOrderNo", order.getOrder_id());
			orderJSON.element(MERCHANT_ID, order.getMerchant_no());
			orderJSON.element("status", status);
			orderJSON.element("notes", reMsg);
			item.put("order", orderJSON.toString());
			String result = YmHttpUtil.HttpPost(YmMallConfig.THIRD_PARTY_NOTIFY_URL, item);
			// String result =
			// YmHttpUtil.HttpPost("http://192.168.1.102:8080/silver-web/Eport/getway-callback",
			// item);
			if (StringEmptyUtils.isNotEmpty(result) && result.replace("\n", "").equalsIgnoreCase("success")) {
				updateSuccessOrderCallBack(order);
			} else {
				// 当第三方接收订单回执失败时,保存重发记录
				saveOrderCallBack(order);
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 订单回传第三方接收失败后，保存回传记录
	 * 
	 * @param order
	 */
	private void saveOrderCallBack(Morder order) {
		Map<String, Object> params = new HashMap<>();
		params.put("thirdPartyId", order.getThirdPartyId());
		params.put(MERCHANT_ID, order.getMerchant_no());
		List<ThirdPartyOrderCallBack> reTpOrderCallBackList = morderDao.findByProperty(ThirdPartyOrderCallBack.class,
				params, 0, 0);
		if (reTpOrderCallBackList != null && !reTpOrderCallBackList.isEmpty()) {
			// 更新订单回传记录中的回传计数器
			ThirdPartyOrderCallBack thirdPartyOrderCallBack = reTpOrderCallBackList.get(0);
			int count = thirdPartyOrderCallBack.getResendCount();
			System.out.println(thirdPartyOrderCallBack.getOrderId() + "--订单->第" + (count + 1) + "次重发接受失败");
			if (count == 9) {
				String remark = thirdPartyOrderCallBack.getRemark();
				String note = DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss") + " 订单重发第10次,接收失败!";
				if (StringEmptyUtils.isNotEmpty(remark)) {
					thirdPartyOrderCallBack.setNote(remark + "#" + note);
				} else {
					thirdPartyOrderCallBack.setNote(note);
				}
			}
			count++;
			thirdPartyOrderCallBack.setResendCount(count);
			thirdPartyOrderCallBack.setResendStatus("failure");
			if (!morderDao.update(thirdPartyOrderCallBack)) {
				logger.error("--异步更新第三方订单计数器失败--");
			}
			order.setResendThirdPartyStatus("failure");
			if (!morderDao.update(order)) {
				logger.error("--异步回调第三方订单后更新支付单失败状态--");
			}
		} else {
			ThirdPartyOrderCallBack entity = new ThirdPartyOrderCallBack();
			entity.setMerchantId(order.getMerchant_no());
			entity.setMerchantName(order.getCreate_by());
			entity.setOrderId(order.getOrder_id());
			entity.setThirdPartyId(order.getThirdPartyId());
			entity.setCreateBy("system");
			entity.setCreateDate(new Date());
			entity.setResendCount(0);
			entity.setResendStatus("failure");
			if (!morderDao.add(entity)) {
				logger.error("--异步保存订单第三方回调信息失败--");
			}
		}
	}

	/**
	 * 订单回传第三方接收成功后，更新订单与回传记录信息
	 * 
	 * @param order
	 *            订单信息实体
	 */
	private void updateSuccessOrderCallBack(Morder order) {
		if (order != null) {
			Map<String, Object> params = new HashMap<>();
			params.put("thirdPartyId", order.getThirdPartyId());
			params.put(MERCHANT_ID, order.getMerchant_no());
			List<ThirdPartyOrderCallBack> reTpOrderCallBackList = morderDao
					.findByProperty(ThirdPartyOrderCallBack.class, params, 0, 0);
			if (reTpOrderCallBackList != null && !reTpOrderCallBackList.isEmpty()) {
				// 当有重发记录时，则更新订单重发记录信息
				Date date = new Date();
				ThirdPartyOrderCallBack thirdPartyOrderCallBack = reTpOrderCallBackList.get(0);
				thirdPartyOrderCallBack.setResendStatus("SUCCESS");
				thirdPartyOrderCallBack.setUpdateDate(date);
				System.out.println(DateUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss") + " 订单重发第"
						+ thirdPartyOrderCallBack.getResendCount() + "次,接收成功!");
				thirdPartyOrderCallBack.setRemark(DateUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss") + " 订单重发第"
						+ (thirdPartyOrderCallBack.getResendCount() + 1) + "次,接收成功!");
				if (!morderDao.update(thirdPartyOrderCallBack)) {
					logger.error("--异步回调第三方支付单成功后保存信息失败--");
				}
			}
			order.setResendThirdPartyStatus("SUCCESS");
			// order.setResendDate(new Date());
			if (!morderDao.update(order)) {
				logger.error("--异步回调第三方支付单成功后更新支付单回调状态失败--");
			}
		} else {
			logger.error("--异步回调第三方订单成功后，订单等于null--");
		}
	}

	/**
	 * 更新手工订单时间
	 * 
	 * @param order
	 *            订单实体类
	 * @return Map
	 */
	private Map<String, Object> updateOrderInfo(Morder order) {
		if (order == null) {
			return ReturnInfoUtils.errorInfo("异步更新订单备案信息失败，请求参数不能为null");
		}
		order.setUpdate_date(new Date());
		if (!morderDao.update(order)) {
			return ReturnInfoUtils.errorInfo("异步更新订单备案信息错误!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据商户Id与订单Id查询到订单下所有商品信息
	 * 
	 * @param orderId
	 *            订单Id
	 * @param merchantId
	 *            商户Id
	 */
	private void findOrderGoodsInfo(String orderId, String merchantId) {
		Map<String, Object> params = new HashMap<>();
		params.put(ORDER_ID, orderId);
		params.put("merchant_no", merchantId);
		List<MorderSub> reGoodsList = morderDao.findByProperty(MorderSub.class, params, 0, 0);
		if (reGoodsList != null && !reGoodsList.isEmpty()) {
			for (MorderSub goods : reGoodsList) {
				String entGoodsNo = goods.getEntGoodsNo();
				String spareParams = goods.getSpareParams();
				// 当是第三方商户提供的商品信息时
				if (StringEmptyUtils.isNotEmpty(spareParams)) {
					JSONObject json = JSONObject.fromObject(spareParams);
					String marCode = json.get("marCode") + "";
					entGoodsNo = entGoodsNo + "_" + marCode;
				}
				// 订单商品数量
				int count = goods.getQty();
				updateStockDoneCount(entGoodsNo, count, merchantId);
			}
		} else {
			System.out.println("---------订单商品未找到----------");
		}
	}

	/**
	 * 根据商户Id与商品自编号，查询出来的订单商品数量更新库存售卖数量
	 * 
	 * @param entGoodsNo
	 *            商品自编号
	 * @param count
	 *            数量
	 * @param merchantId
	 *            商户Id
	 */
	private void updateStockDoneCount(String entGoodsNo, int count, String merchantId) {
		Map<String, Object> params = new HashMap<>();
		params.put("entGoodsNo", entGoodsNo);
		params.put(MERCHANT_ID, merchantId);
		List<StockContent> reStockList = morderDao.findByProperty(StockContent.class, params, 1, 1);
		if (reStockList != null && !reStockList.isEmpty()) {
			for (StockContent stock : reStockList) {
				int oldDoneCount = stock.getDoneCount();
				int oldReadIngCount = stock.getReadingCount();
				stock.setDoneCount(oldDoneCount + count);
				stock.setReadingCount(oldReadIngCount + RandomUtils.getRandom(1));
				stock.setUpdateBy("system");
				stock.setUpdateDate(new Date());
				morderDao.update(stock);
			}
		}

	}

	@Override
	public Map<String, Object> downOrderExcelByDateSerialNo(String merchantId, String merchantName, String filePath,
			String date, String serialNo) {
		Table reList = morderDao.getOrderAndOrderGoodsInfo(merchantId, date, Integer.parseInt(serialNo));
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器异常!");
		} else if (!reList.getRows().isEmpty()) {
			String dataStr = Transform.tableToJson(reList).getJSONArray("rows") + "";
			try {
				dataStr = CompressUtils.compress(dataStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ReturnInfoUtils.successDataInfo(dataStr, 0);
		} else {
			return ReturnInfoUtils.errorInfo("未找到订单数据！");
		}
	}
}
