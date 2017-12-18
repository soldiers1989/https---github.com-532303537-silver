package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.WalletLogService;
import org.silver.shop.dao.system.tenant.WalletLogDao;
import org.silver.shop.model.system.tenant.MerchantWalletLog;
import org.silver.shop.model.system.tenant.ProxyWalletLog;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class WalletLogServiceImpl implements WalletLogService {

	@Autowired
	private WalletLogDao walletLogDao;

	@Override
	public Map<String, Object> addWalletLog(int type, JSONObject params) {
		Map<String, Object> statusMap = new HashMap<>();
		if (type == 1 || type == 2 || type == 3 && params != null && !params.isEmpty()) {
			// 1-用户,2-商户,3-代理商
			switch (type) {
			case 1:

				break;
			case 2:
				return createMerchantWallet(params);
			case 3:
				return createProxyWallet(params);
			default:
				break;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.toString());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.toString());
		return statusMap;

	}

	// 创建商户日志
	private Map<String, Object> createMerchantWallet(JSONObject params) {
		Map<String, Object> statusMap = new HashMap<>();
		MerchantWalletLog walletLog = new MerchantWalletLog();
		// 分类:1-购物、2-充值、3-提现、4-缴费、5-代理商佣金
		int type = params.getInt("type");
		switch (type) {
		case 1:
			walletLog.setType(type);
			walletLog.setMemberId(params.get("memberId") + "");
			walletLog.setMemberName(params.get("memberName") + "");
			walletLog.setMerchantId(params.get("merchantId") + "");
			walletLog.setMerchantName(params.get("merchantName") + "");
			walletLog.setEntOrderNo(params.get("entOrderNo") + "");
			walletLog.setEntPayNo(params.get("entPayNo") + "");
			walletLog.setEntPayName(params.get("entPayName") + "");
			walletLog.setPayAmount(params.getDouble("payAmount"));
			walletLog.setBeforeChangingBalance(params.getDouble("oldBalance"));
			walletLog.setAfterChangeBalance(params.getDouble("oldBalance") + params.getDouble("payAmount"));
			// 状态：1-交易成功、2-交易失败、3-交易关闭
			walletLog.setStatus(1);
			walletLog.setCreateDate(new Date());
			break;
		case 2:

			break;
		case 3:

			break;
		case 4:

			break;
		case 5:
			String serialNo = params.get("entPayNo") + "";
			walletLog.setType(type);
			walletLog.setMerchantId(params.get("merchantId") + "");
			walletLog.setMerchantName(params.get("merchantName") + "");
			if(StringEmptyUtils.isNotEmpty(serialNo)){
				walletLog.setEntPayNo(serialNo);
			}else{
				walletLog.setEntOrderNo(params.get("entOrderNo") + "");
			}
			walletLog.setEntPayName(params.get("entPayName") + "");
			walletLog.setPayAmount(params.getDouble("payAmount"));
			walletLog.setBeforeChangingBalance(params.getDouble("oldBalance"));
			walletLog.setAfterChangeBalance(params.getDouble("oldBalance") - params.getDouble("payAmount"));
			walletLog.setProxyId(params.get("proxyId") + "");
			walletLog.setProxyName(params.get("proxyName") + "");
			// 状态：1-交易成功、2-交易失败、3-交易关闭
			walletLog.setStatus(1);
			break;
		default:
			break;
		}
		walletLog.setCreateDate(new Date());
		walletLog.setCreateBy("system");
		if (!walletLogDao.add(walletLog)) {
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	// 创建代理商日志
	private Map<String, Object> createProxyWallet(JSONObject params) {
		Map<String, Object> statusMap = new HashMap<>();
		ProxyWalletLog walletLog = new ProxyWalletLog();
		// 分类1-佣金、2-充值、3-提现、4-缴费
		int type = params.getInt("type");
		switch (type) {
		case 1:
			String serialNo = params.get("entPayNo") + "";
			walletLog.setType(type);
			walletLog.setProxyId(params.get("proxyId") + "");
			walletLog.setProxyName(params.get("proxyName") + "");
			walletLog.setMerchantId(params.get("merchantId") + "");
			walletLog.setMerchantName(params.get("merchantName") + "");
			//如果支付单流水号不为空则保存为支付单流水,为空时则是订单流水
			if(StringEmptyUtils.isNotEmpty(serialNo)){
				walletLog.setEntPayNo(serialNo);
			}else{
				walletLog.setEntOrderNo(params.get("entOrderNo") + "");
			}
			walletLog.setSerialName(params.get("entPayName") + "");
			walletLog.setAmount(params.getDouble("payAmount"));
			walletLog.setBeforeChangingBalance(params.getDouble("oldBalance"));
			walletLog.setAfterChangeBalance(params.getDouble("oldBalance") + params.getDouble("payAmount"));
			// 状态：1-交易成功、2-交易失败、3-交易关闭
			walletLog.setStatus(1);
			break;
		case 2:

			break;
		case 3:

			break;
		case 4:

			break;
		default:
			break;
		}
		walletLog.setCreateDate(new Date());
		walletLog.setCreateBy("system");
		if (!walletLogDao.add(walletLog)) {
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

}
