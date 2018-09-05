package org.silver.shop.service.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.TradeReceiptLogService;
import org.silver.shop.api.system.tenant.MemberBankService;
import org.silver.shop.api.system.tenant.MemberWalletService;
import org.silver.shop.controller.system.cross.DaiFuPay;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberBankContent;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONObject;

@Service
public class MemberWalletTransaction {

	@Reference
	private  MemberWalletService memberWalletService;
	@Reference
	private MemberBankService memberBankService;
	@Reference
	private TradeReceiptLogService tradeReceiptLogService;
	@Autowired
	private DaiFuPay daiFuPay;
	
	/**
	 * 代付回调RUL
	 */
	private static final String DAI_FU_NOTIFY_URL = "https://ym.191ec.com/silver-web-shop/yspay-receive/memberWithdraw";
	
	public Map<String,Object> getInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		return memberWalletService.getInfo(memberInfo.getMemberId());
	}

	public Map<String,Object> addPayReceipt(double amount, String serialNo) {
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		return memberWalletService.addPayReceipt(memberInfo,amount,serialNo);
	}

	//
	public Map<String, String> memberRechargeReceive(Map<String, String> params) {
		return memberWalletService.memberRechargeReceive(params);
	}


	public Object generateSign(String memberId) {
		return memberWalletService.generateSign(memberId);
	}

	public Map<String, Object> getBankCard() {
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		Map<String,Object> params= new HashMap<>();
		params.put("memberId", memberInfo.getMemberId());
		// 选中标识：1-默认选中,2-备用
		params.put("defaultFlag", 1);
		Map<String,Object> reMap = memberBankService.getInfo("display", params, 0, 0);
		if ("-1".equals(reMap.get(BaseCode.ERROR_CODE.toString()))) {
			return ReturnInfoUtils.errorInfo("暂无银行卡信息，请先绑定银行卡！");
		} else if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		return reMap;
	}

	//保存交易日志
	public Map<String,Object> addLog(String userId,String userName,String orderId,Double amount,String sourceType) {
		Map<String,Object> datas =new HashMap<>();
		datas.put("userId", userId);
		datas.put("userName", userName);
		datas.put("orderId", orderId);
		datas.put("type", "withdraw");
		datas.put("status", "process");
		datas.put("amount", amount);
		datas.put("sourceType", sourceType);
		return tradeReceiptLogService.addLog(datas);
	}

	public Object tmpAddAmount(String memberId, double amount) {
		return memberWalletService.tmpAddAmount(memberId,amount);
	}

	//
	public Map<String, Object> reserveAmountWithdraw(double amount, String payPassword) {
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		Map<String, Object> reCheckPasswordMap = memberWalletService.checkPayPassword(memberInfo.getMemberId(),payPassword);
		if (!"1".equals(reCheckPasswordMap.get(BaseCode.STATUS.toString()))) {
			return reCheckPasswordMap;
		}
		Map<String, Object> reCheckWalletMap = memberWalletService.checkReserveAmount(memberInfo.getMemberId(),amount);
		if (!"1".equals(reCheckWalletMap.get(BaseCode.STATUS.toString()))) {
			return reCheckWalletMap;
		}
		Map<String, Object> reBankCardMap = getBankCard();
		if (!"1".equals(reBankCardMap.get(BaseCode.STATUS.toString()))) {
			return reBankCardMap;
		}
		List<MemberBankContent> reList = (List<MemberBankContent>) reBankCardMap.get(BaseCode.DATAS.toString());
		MemberBankContent bankContent = reList.get(0);
		String serialNo = createSerialNo();
		// 向银盛发起代付请求
		Map<String, Object> reMap = daiFuPay.dfTrade(DAI_FU_NOTIFY_URL, serialNo, serialNo, amount, "用户资金提现",
				bankContent.getBankProvince(), bankContent.getBankCity(), bankContent.getBankName(),
				bankContent.getBankAccountNo(), bankContent.getBankAccountName(), bankContent.getBankAccountType(),
				bankContent.getBankCardType());
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		Map<String, Object> reLogMap = addLog(bankContent.getMemberId(),
				bankContent.getMemberName(), serialNo, amount, "reserveAmount");
		if (!"1".equals(reLogMap.get(BaseCode.STATUS.toString()))) {
			return reLogMap;
		}
		return  memberWalletService.reserveAmountDeduction(memberInfo.getMemberId(),amount);
	}
	
	/**
	 * 生成代付16位流水号
	 * 
	 * @return String 流水号
	 */
	private String createSerialNo() {
		String no = SerialNoUtils.getSerialNo("merchant_daifu") + "";
		while (no.length() < 7) {
			no = "0" + no;
		}
		return "F" + DateUtil.formatDate(new Date(), "yyyyMMdd") + no;
	}

}
