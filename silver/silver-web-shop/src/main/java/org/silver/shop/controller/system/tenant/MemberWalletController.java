package org.silver.shop.controller.system.tenant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.TradeReceiptLogService;
import org.silver.shop.api.system.tenant.MemberBankService;
import org.silver.shop.config.FenZhangConfig;
import org.silver.shop.controller.system.cross.DaiFuPay;
import org.silver.shop.controller.system.cross.DirectPayConfig;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberBankContent;
import org.silver.shop.model.system.tenant.MerchantBankContent;
import org.silver.shop.service.system.log.TradeReceiptLogTransaction;
import org.silver.shop.service.system.tenant.ManagerWalletTransaction;
import org.silver.shop.service.system.tenant.MemberBankTransaction;
import org.silver.shop.service.system.tenant.MemberWalletTransaction;
import org.silver.shop.service.system.tenant.MerchantWalletTransaction;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 用户钱包
 */
@Controller
@RequestMapping("/memberWallet")
public class MemberWalletController {

	@Autowired
	private MemberWalletTransaction memberWalletTransaction;


	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户查询钱包信息")
	@RequiresRoles("Member")
	// @RequiresPermissions("merchantWallet:getMerchantWalletInfo")
	public String getInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(memberWalletTransaction.getInfo()).toString();
	}

	@RequestMapping(value = "/onlineRecharge", produces = "application/json; charset=utf-8")
	@ApiOperation("线上充值")
	@RequiresRoles("Member")
	// @RequiresPermissions("merchantWallet:getMerchantWalletInfo")
	public String onlineRecharge(HttpServletRequest req, HttpServletResponse response, String amount) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isNotEmpty(amount) && Double.parseDouble(amount) >= 0.01) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			req.setAttribute("method", "ysepay.online.directpay.createbyuser");
			req.setAttribute("partner_id", FenZhangConfig.PLATFORM_PARTNER_NO);
			req.setAttribute("timestamp", sdf.format(new Date()));
			req.setAttribute("charset", FenZhangConfig.DEFAULT_CHARSET);
			req.setAttribute("sign_type", FenZhangConfig.SIGN_ALGORITHM);
			// request.setAttribute("sign", userName);
			// 生成商户订单 out_trade_no total_amount
			req.setAttribute("notify_url", "https://ym.191ec.com/silver-web-shop/yspay-receive/memberRechargeReceive");
			req.setAttribute("return_url", "https://www.191ec.com/member/wallet");
			// req.setAttribute("return_url",
			// "https://www.191ec.com/silver-web-shop/");
			req.setAttribute("version", FenZhangConfig.VERSION);
			// 商品交易订单号
			String serialNo = SerialNoUtils.getSerialNo("YMP", SerialNoUtils.getSerialNo("wallet_operator"));
			req.setAttribute("out_trade_no", serialNo);
			req.setAttribute("subject", "充值");
			req.setAttribute("total_amount", amount);// 支付总金额
			req.setAttribute("seller_id", FenZhangConfig.PLATFORM_PARTNER_NO);
			req.setAttribute("seller_name", FenZhangConfig.PLATFORM_PARTNER_NAME);
			req.setAttribute("timeout_express", "1h");
			req.setAttribute("business_code", "01000010");
			// 公共回传参数
			req.setAttribute("extra_common_param", "Ezreal");
			// request.setAttribute("pay_mode", "internetbank");
			req.setAttribute("bank_type", "");
			req.setAttribute("bank_account_type", "");
			req.setAttribute("support_card_type", "");
			req.setAttribute("bank_account_no", "");
			// 进行日志记录
			Map<String, Object> reMap = memberWalletTransaction.addPayReceipt(Double.parseDouble(amount), serialNo);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return JSONObject.fromObject(reMap).toString();
			}
			// return
			// JSONObject.fromObject(memberWalletTransaction.onlineRecharge(amount)).toString();
			return "ys_pay";
		} else {
			// 跳转至错误页面
			return "yspay_error";
		}
	}

	@RequestMapping(value = "/reserveAmountWithdraw", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Member")
	@ResponseBody
	@ApiOperation("钱包-储备资金提现")
	public String reserveAmountWithdraw(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("amount") double amount, String payPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(amount) || amount < 0.01) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("结算金额错误!")).toString();
		}
		return JSONObject.fromObject(memberWalletTransaction.reserveAmountWithdraw(amount,payPassword)).toString();
	}

	
	@RequestMapping(value = "/generateSign", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("生成校验码")
	@RequiresRoles("Manager")
	// @RequiresPermissions("merchantWallet:getMerchantWalletInfo")
	public String generateSign(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("memberId") String memberId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(memberWalletTransaction.generateSign(memberId)).toString();
	}

	@RequestMapping(value = "/tmpAddAmount", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("临时-加钱")
	@RequiresRoles("Manager")
	// @RequiresPermissions("merchantWallet:getMerchantWalletInfo")
	public String tmpAddAmount(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("memberId") String memberId, @RequestParam("amount") double amount) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(memberWalletTransaction.tmpAddAmount(memberId, amount)).toString();
	}
}
