package org.silver.shop.impl.system.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.MerchantWalletLogService;
import org.silver.shop.dao.system.log.MerchantWalletLogDao;
import org.silver.shop.model.system.log.MerchantWalletLog;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MerchantWalletLogService.class)
public class MerchantWalletLogServiceImpl implements MerchantWalletLogService {

	private static Logger logger = LogManager.getLogger(Object.class);

	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private MerchantWalletLogDao merchantWalletLogDao;
	@Autowired
	private WalletUtils walletUtils;
	
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
			Map<String, Object> reCheckMap = walletUtils.checkMerchantWalletLogInfo(datasMap);
			if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
				return reCheckMap;
			}
			MerchantWalletLog walletLog = new MerchantWalletLog();
			walletLog.setMerchantWalletId(datasMap.get("walletId") + "");
			walletLog.setSerialName(datasMap.get("serialName") + "");
			String serialNo = datasMap.get("serialNo") + "";
			if (StringEmptyUtils.isEmpty(serialNo)) {
				int serial = SerialNoUtils.getSerialNo("logs");
				if (serial < 0) {
					return ReturnInfoUtils.errorInfo("查询流水号自增Id失败,服务器繁忙!");
				}
				walletLog.setSerialNo(SerialNoUtils.getSerialNo("L", serial));
			} else {
				walletLog.setSerialNo(serialNo);
			}
			walletLog.setMerchantName(merchant.getMerchantName());
			double balance = Double.parseDouble(datasMap.get("balance") + "");
			walletLog.setBeforeChangingBalance(balance);
			double amount = Double.parseDouble(datasMap.get("amount") + "");
			walletLog.setAmount(amount);
			String flag = datasMap.get("flag") + "";
			if ("in".equals(flag)) {
				walletLog.setAfterChangeBalance(balance + amount);
			} else if ("out".equals(flag)) {
				walletLog.setAfterChangeBalance(balance - amount);
			}
			walletLog.setFlag(flag);
			int type = Integer.parseInt(datasMap.get("type") + "");
			// 分类1-购物、2-充值、3-提现、4-缴费
			walletLog.setType(type);
			if (StringEmptyUtils.isNotEmpty(datasMap.get("note"))) {
				walletLog.setNote(datasMap.get("note") + "");
			}
			walletLog.setTargetWalletId(datasMap.get("targetWalletId") + "");
			walletLog.setTargetName(datasMap.get("targetName") + "");
			walletLog.setCreateBy("system");
			walletLog.setCreateDate(new Date());
			// 状态：success-交易成功、fail-交易失败
			walletLog.setStatus(datasMap.get("status") + "");
			if (!merchantWalletLogDao.add(walletLog)) {
				return ReturnInfoUtils.errorInfo("保存商户钱包日志失败,服务器繁忙!");
			}
		} catch (Exception e) {
			logger.error("----商户钱包扣费并且记录钱包日志-->>" + e);
		}
		return ReturnInfoUtils.successInfo();
	}



}
