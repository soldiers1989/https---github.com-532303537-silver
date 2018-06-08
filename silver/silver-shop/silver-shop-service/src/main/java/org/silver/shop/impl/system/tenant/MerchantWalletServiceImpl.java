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
	private MerchantUtils merchantUtils;
	@Autowired
	private WalletUtils walletUtils;

	@Override
	public Map<String, Object> walletRecharge(String merchantId, String merchantName, Double money) {
		Date date = new Date();
		Map<String, Object> reMap = walletUtils.checkWallet(1, merchantId, merchantName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		double oldBalance = wallet.getBalance();
		wallet.setBalance(oldBalance + money);
		wallet.setUpdateDate(date);
		if (!merchantWalletDao.update(wallet)) {
			return ReturnInfoUtils.errorInfo("充值失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getMerchantWallet(String merchantId, String merchantName) {
		return walletUtils.checkWallet(1, merchantId, merchantName);
	}

	@Override
	public Map<String, Object> getMerchantWalletLog(String merchantId, String merchantName, int type, int page,
			int size, int timeLimit) {
		if (page >= 0 && size >= 0 && timeLimit >= 0 && type >= 0) {
			Map<String, Object> params = new HashMap<>();
			Date endDate = new Date(); // 当前时间
			Calendar calendar = Calendar.getInstance(); // 得到日历
			calendar.setTime(endDate);// 把当前时间赋给日历
			Date startDate = null;
			// 查询时间范围 1-三个月内,2-一年内,3-今天
			switch (timeLimit) {
			case 1:// 查询最近三个月
				calendar.add(Calendar.MONTH, -3); // 设置为前3月
				startDate = calendar.getTime(); // 得到前3月的时间
				break;
			case 2:// 查询最近一年
				calendar.add(Calendar.YEAR, -1); // 设置为前1年
				startDate = calendar.getTime(); // 得到前1年的时间
				break;
			case 3:// 查询今天
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				startDate = calendar.getTime();// 当天零点零分零秒
				break;
			default:

				break;
			}
			Map<String,Object> reWalletMap= walletUtils.checkWallet(1, merchantId, merchantName);
			MerchantWalletContent wallet = (MerchantWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("merchantWalletId", wallet.getWalletId());
			if (type > 0) {
				params.put("type", type);
			}
			List<MerchantWalletLog> reList = merchantWalletDao.findByPropertyLike(MerchantWalletLog.class, params, null, page,
					size);
			long tatolCount = merchantWalletDao.findByPropertyLikeCount(MerchantWalletLog.class, params, null);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {
				return ReturnInfoUtils.successDataInfo(reList, tatolCount);
			} else {
				return ReturnInfoUtils.errorInfo("暂无数据!");
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数不能为空!");
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
	public Map<String, Object> addWalletLog(Map<String, Object> datasMap) {
		try {
			if (datasMap == null || datasMap.isEmpty()) {
				return ReturnInfoUtils.errorInfo("添加钱包日志,请求参数不能为空!");
			}
			String merchantId = datasMap.get("merchantId") + "";
			// 根据商户Id获取商户信息
			Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
			if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
				return reMerchantMap;
			}
			Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
			//校验日志添加参数
			Map<String,Object> reCheckMap = WalletUtils.checkWalletInfo(datasMap);
			if(!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))){
				return reCheckMap;
			}
			MerchantWalletLog walletLog = new MerchantWalletLog();
			walletLog.setMerchantWalletId(datasMap.get("walletId") + "");
			walletLog.setSerialName(datasMap.get("serialName") + "");
			int serialNo = SerialNoUtils.getSerialNo("logs");
			if (serialNo < 0) {
				return ReturnInfoUtils.errorInfo("查询流水号自增Id失败,服务器繁忙!");
			}
			walletLog.setSerialNo(SerialNoUtils.getSerialNo("L", serialNo));
			walletLog.setMerchantName(merchant.getMerchantName());
			double balance = Double.parseDouble(datasMap.get("balance") + "");
			walletLog.setBeforeChangingBalance(balance);
			double amount = Double.parseDouble(datasMap.get("amount") + "");
			walletLog.setAmount(amount);
			String flag = datasMap.get("flag")+"";
			if("in".equals(flag)){
				walletLog.setAfterChangeBalance(balance + amount);
			}else if("out".equals(flag)){
				walletLog.setAfterChangeBalance(balance - amount);
			}
			walletLog.setFlag(flag);
			int type = Integer.parseInt(datasMap.get("type") + "");
			// 分类1-购物、2-充值、3-提现、4-缴费
			walletLog.setType(type);
			if(StringEmptyUtils.isNotEmpty(datasMap.get("note"))){
				walletLog.setNote(datasMap.get("note") + "");
			}
			walletLog.setTargetWalletId(datasMap.get("targetWalletId") + "");
			walletLog.setTargetName(datasMap.get("targetName") + "");
			walletLog.setCreateBy("system");
			walletLog.setCreateDate(new Date());
			// 状态：success-交易成功、fail-交易失败
			walletLog.setStatus("success");
			if (!merchantWalletDao.add(walletLog)) {
				return ReturnInfoUtils.errorInfo("保存商户钱包日志失败,服务器繁忙!");
			}
		} catch (Exception e) {
			logger.error("----商户钱包扣费并且记录钱包日志-->>" + e);
		}
		return ReturnInfoUtils.successInfo();
	}
}
