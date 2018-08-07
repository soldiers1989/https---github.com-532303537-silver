package org.silver.shop.impl.system.tenant;

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
import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.api.system.tenant.MemberWalletService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.dao.system.tenant.MemberWalletDao;
import org.silver.shop.model.system.log.MerchantWalletLog;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.WalletUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MemberWalletService.class)
public class MemberWalletServiceImpl implements MemberWalletService {

	private static Logger logger = LogManager.getLogger(Object.class);

	@Autowired
	private MemberWalletDao memberWalletDao;
	@Autowired
	private MerchantWalletLogService merchantWalletLogService;
	@Autowired
	private MerchantWalletService merchantWalletService;
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
			params.put("beforeChangingBalance", memberWalletContent.getReserveAmount() + amount);
			params.put("amount", amount);
			params.put("afterChangeBalance", memberWalletContent.getReserveAmount());
			// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
			params.put("type", 5);
			// 交易状态
			params.put("status", "success");
			// 进出帐标识
			params.put("flag", "out");
			Map<String, Object> merchantWalletMap = merchantWalletService.getMerchantWallet(merchantId, null);
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
			params.put("balance", merchantWallet.getReserveAmount() - amount);
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

	private Map<String, Object> merchantWalletReserveAmountTransfer(MerchantWalletContent merchantWallet,
			Double amount) {
		if (merchantWallet == null) {
			return ReturnInfoUtils.errorInfo("商户钱包储备资金收款时请求参数不能为空！");
		}
		// 原储备资金
		double oldReserveAmount = merchantWallet.getReserveAmount();
		merchantWallet.setReserveAmount(oldReserveAmount + amount);
		merchantWallet.setUpdateBy("system");
		merchantWallet.setUpdateDate(new Date());
		if (memberWalletDao.update(merchantWallet)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("商户钱包储备资金收款失败！");
	}

	private Map<String, Object> memberWalletReserveAmountTransfer(String memberId, Double amount) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		List<MemberWalletContent> reMemberWalletList = memberWalletDao.findByProperty(MemberWalletContent.class, params,
				0, 0);
		if (reMemberWalletList == null) {
			return ReturnInfoUtils.errorInfo("查询用户钱包信息失败");
		} else if (!reMemberWalletList.isEmpty()) {
			MemberWalletContent memberWallet = reMemberWalletList.get(0);
			// 原储备资金
			double oldReserveAmount = memberWallet.getReserveAmount();
			double balance = oldReserveAmount - amount;
			if (balance < 0) {
				return ReturnInfoUtils.errorInfo("用户储备资金扣款失败,钱包余额为--->" + balance);
			} else {
				memberWallet.setReserveAmount(balance);
				memberWallet.setUpdateBy("system");
				memberWallet.setUpdateDate(new Date());
				if (memberWalletDao.update(memberWallet)) {
					return ReturnInfoUtils.successDataInfo(memberWallet);
				}
				return ReturnInfoUtils.errorInfo("更新用户储备资金失败！");
			}
		} else {
			return ReturnInfoUtils.errorInfo("用户Id[" + memberId + "]未找到钱包信息!");
		}
	}

	@Override
	public Map<String, Object> getInfo(Member memberInfo) {
		if(memberInfo == null){
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		return walletUtils.checkWallet(2, memberInfo.getMemberId(), memberInfo.getMemberName());
	}

}
