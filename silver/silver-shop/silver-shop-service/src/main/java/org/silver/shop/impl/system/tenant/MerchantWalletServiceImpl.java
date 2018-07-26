package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.dao.system.tenant.MerchantWalletDao;
import org.silver.shop.model.system.log.OfflineRechargeLog;
import org.silver.shop.model.system.log.PaymentReceiptLog;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.OfflineRechargeContent;
import org.silver.shop.util.SearchUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;

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
			return ReturnInfoUtils.errorInfo("扣款失败,账户余额不足!");
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
		Map<String,Object> reChooseMap = chooseBeneficiaryAccount(beneficiaryAccountType,content);
		if(!"1".equals(reChooseMap.get(BaseCode.STATUS.toString()))){
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
	 * @param beneficiaryAccountType 
	 * 			收款人账号类型：public-对公账号、private-对私账号
	 * @param content
	 * @return
	 */
	private Map<String, Object> chooseBeneficiaryAccount(String beneficiaryAccountType, OfflineRechargeContent content) {
		if (StringEmptyUtils.isEmpty(beneficiaryAccountType)) {
			return ReturnInfoUtils.errorInfo("收款账号类型不能为空！");
		}
		if(content == null){
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
		if (datasMap == null ) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String, Object> reDatasMap = SearchUtils.universalOfflineRechargeSearch(datasMap);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		paramMap.put("deleteFlag", 0);
		List<OfflineRechargeContent> reList = merchantWalletDao.findByProperty(OfflineRechargeContent.class, paramMap, page, size);
		long count = merchantWalletDao.findByPropertyCount(OfflineRechargeContent.class,paramMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList,count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据！");
		}
	}
}
