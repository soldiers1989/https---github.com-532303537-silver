package org.silver.shop.impl.system.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.TradeReceiptLogService;
import org.silver.shop.dao.system.log.TradeReceiptLogDao;
import org.silver.shop.model.system.log.TradeReceiptLog;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = TradeReceiptLogService.class)
public class TradeReceiptLogServiceImpl implements TradeReceiptLogService {

	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private TradeReceiptLogDao tradeReceiptLogDao;
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
		TradeReceiptLog log = new TradeReceiptLog();
		log.setUserId(merchant.getMerchantId());
		log.setUserName(merchant.getMerchantName());
		log.setOrderId(orderId);
		double cash = wallet.getCash();
		log.setBeforeChangingBalance(cash);
		log.setAmount(amount);
		// 类型：recharge(充值)、transfer(转账)、withdraw(提现)
		if ("recharge".equals(type)) {
			log.setAfterChangeBalance(cash + amount);
		} else {
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
		if (!tradeReceiptLogDao.add(log)) {
			return ReturnInfoUtils.errorInfo("保存商户交易记录失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> addLog(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String, Object> reCheckMap = checkData("add", datasMap);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		TradeReceiptLog log = new TradeReceiptLog();
		log.setUserId(datasMap.get("userId") + "");
		log.setUserName(datasMap.get("userName") + "");
		log.setOrderId(datasMap.get("orderId") + "");
		// double cash = wallet.getCash();
		// log.setBeforeChangingBalance(cash);
		try {
			log.setAmount(Double.parseDouble(datasMap.get("amount") + ""));
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("金额错误！");
		}
		//
		log.setSourceType(datasMap.get("sourceType") + "");
		// 类型：recharge(充值)、transfer(转账)、withdraw(提现)
		log.setType(datasMap.get("type") + "");
		// 状态：success(交易成功)、failure(交易失败)、process(处理中)
		log.setTradingStatus(datasMap.get("status") + "");
		// log.setRemark("管理员对[" + merchant.getMerchantName() + "]进行资金清算");
		log.setCreateBy(datasMap.get("userName") + "");
		
		log.setCreateDate(new Date());
		if (!tradeReceiptLogDao.add(log)) {
			return ReturnInfoUtils.errorInfo("保存交易记录失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	private Map<String, Object> checkData(String type, Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("校验参数时，请求参数不能为空！");
		}
		List<String> noNullKeys = new ArrayList<>();
		JSONArray jsonList = null;
		switch (type) {
		case "add":// 添加
			noNullKeys.add("userId");
			noNullKeys.add("userName");
			noNullKeys.add("orderId");
			noNullKeys.add("amount");
			noNullKeys.add("type");
			noNullKeys.add("status");
			noNullKeys.add("sourceType");
			
			jsonList = new JSONArray();
			jsonList.add(datasMap);
			return CheckDatasUtil.checkData(jsonList, noNullKeys);
		default:
			return ReturnInfoUtils.errorInfo("校验信息时，[" + type + "]类型错误！");
		}
	}
	
	@Override
	public Map<String,Object> updateLog(TradeReceiptLog entity){
		if(entity == null){
			return ReturnInfoUtils.errorInfo("更新参数不能为null");
		}
		entity.setUpdateDate(new Date());
		if (!tradeReceiptLogDao.update(entity)) {
			return ReturnInfoUtils.errorInfo("更新交易记录失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successDataInfo(entity);
	}
	
}
