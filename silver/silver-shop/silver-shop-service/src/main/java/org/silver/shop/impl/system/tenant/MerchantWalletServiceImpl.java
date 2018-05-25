package org.silver.shop.impl.system.tenant;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.dao.system.tenant.MerchantWalletDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.AgentWalletContent;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletLog;
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

	@Autowired
	private MerchantWalletDao merchantWalletDao;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private WalletUtils walletUtils;

	@Override
	public Map<String, Object> walletRecharge(String merchantId, String merchantName, Double money) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reMap = walletUtils.checkWallet(1, merchantId, merchantName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		double oldBalance = wallet.getBalance();
		wallet.setBalance(oldBalance + money);
		wallet.setUpdateDate(date);
		if (!merchantWalletDao.update(wallet)) {
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
			statusMap.put(BaseCode.MSG.toString(), "充值失败,服务器繁忙!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> getMerchantWallet(String merchantId, String merchantName) {
		return walletUtils.checkWallet(1, merchantId, merchantName);
	}

	@Override
	public Map<String, Object> getMerchantWalletLog(String merchantId, String merchantName, int type, int page,
			int size, int timeLimit) {
		Map<String, Object> statusMap = new HashMap<>();
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
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("merchantId", merchantId);
			if (type > 0) {
				params.put("type", type);
			}
			List<Object> reList = merchantWalletDao.findByPropertyLike(MerchantWalletLog.class, params, null, page,
					size);
			long tatolCount = merchantWalletDao.findByPropertyLikeCount(MerchantWalletLog.class, params, null);
			if (reList == null) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			} else if (!reList.isEmpty()) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
				statusMap.put(BaseCode.DATAS.toString(), reList);
				statusMap.put(BaseCode.TOTALCOUNT.toString(), tatolCount);
				return statusMap;
			} else {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		return statusMap;
	}

	/**
	 * 商户钱包扣款
	 * 
	 * @param merchantWallet
	 *            商户钱包实体类
	 * @param balance
	 *            商户原钱包余额
	 * @param serviceFee
	 *            手续费(平台服务费)
	 * @return Map
	 */
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

	/**
	 * 商户钱包扣费并且记录钱包日志
	 * 
	 * @param datasMap
	 *            参数
	 * @return Map
	 */
	public Map<String, Object> addWalletLog(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("添加钱包日志,请求参数不能为空!");
		}
		String merchantId = datasMap.get("merchantId") + "";
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		MerchantWalletLog walletLog = new MerchantWalletLog();
		if (StringEmptyUtils.isEmpty(datasMap.get("name"))) {
			return ReturnInfoUtils.errorInfo("添加钱包日志,日志名称不能为空!");
		}
		walletLog.setEntPayName(datasMap.get("name") + "");
		walletLog.setMerchantId(merchant.getMerchantId());
		walletLog.setMerchantName(merchant.getMerchantName());
		if (StringEmptyUtils.isEmpty(datasMap.get("balance"))) {
			return ReturnInfoUtils.errorInfo("添加钱包日志,余额不能为空!");
		}
		double balance = Double.parseDouble(datasMap.get("balance") + "");
		walletLog.setBeforeChangingBalance(balance);
		if (StringEmptyUtils.isEmpty(datasMap.get("serviceFee"))) {
			return ReturnInfoUtils.errorInfo("添加钱包日志,平台服务费不能为空!");
		}
		double serviceFee = Double.parseDouble(datasMap.get("serviceFee") + "");
		walletLog.setPayAmount(serviceFee);
		walletLog.setAfterChangeBalance(balance - serviceFee);
		// 分类1-购物、2-充值、3-提现、4-缴费、5-支付代理商佣金
		walletLog.setType(5);
		walletLog.setNote(datasMap.get("note") + "");
		walletLog.setCreateBy("system");
		walletLog.setCreateDate(new Date());
		// 状态：1-交易成功、2-交易失败、3-交易关闭
		walletLog.setStatus(1);
		// 代理商暂定获取商户的代理商信息
		walletLog.setProxyId(merchant.getAgentParentId());
		walletLog.setProxyName(merchant.getAgentParentName());
		if (!merchantWalletDao.add(walletLog)) {
			return ReturnInfoUtils.errorInfo("保存商户钱包日志失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}
}
