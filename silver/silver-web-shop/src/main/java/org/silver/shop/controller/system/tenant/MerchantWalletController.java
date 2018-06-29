package org.silver.shop.controller.system.tenant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.shop.controller.system.cross.DaiFuPay;
import org.silver.shop.controller.system.cross.DirectPayConfig;
import org.silver.shop.model.system.tenant.MerchantBankContent;
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

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商户钱包Controller
 */
@Controller
@RequestMapping("/merchantWallet")
public class MerchantWalletController {

	@Autowired
	private MerchantWalletTransaction merchantWalletTransaction;
	@Autowired
	private DaiFuPay daiFuPay;

	private static final String DAI_FU_NOTIFY_URL= "https://ym.191ec.com/silver-web-shop/yspay-receive/dfReceive";
	
	/**
	 * 商户钱包充值
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/walletRecharge", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ApiOperation("商户往钱包充值")
	// @RequiresPermissions("merchantWallet:walletRecharge")
	public String walletRecharge(HttpServletRequest req, HttpServletResponse resp, String amount) {
		if (StringEmptyUtils.isNotEmpty(amount) && Double.parseDouble(amount) >= 0.01) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			req.setAttribute("method", "ysepay.online.directpay.createbyuser");
			req.setAttribute("partner_id", DirectPayConfig.PLATFORM_PARTNER_NO);
			req.setAttribute("timestamp", sdf.format(new Date()));
			req.setAttribute("charset", DirectPayConfig.DEFAULT_CHARSET);
			req.setAttribute("sign_type", DirectPayConfig.SIGN_ALGORITHM);
			// request.setAttribute("sign", userName);
			// 生成商户订单 out_trade_no total_amount
			req.setAttribute("notify_url",
					"https://ym.191ec.com/silver-web-shop/yspay-receive/walletRecharge");
			req.setAttribute("return_url", "");
			// req.setAttribute("return_url",
			// "https://www.191ec.com/silver-web-shop/");
			req.setAttribute("version", DirectPayConfig.VERSION);
			// 商品交易订单号
			String serialNo = SerialNoUtils.getSerialNo("YMP", SerialNoUtils.getSerialNo("wallet_operator"));
			req.setAttribute("out_trade_no", serialNo);
			req.setAttribute("subject", "余额充值");
			req.setAttribute("total_amount", amount);// 支付总金额
			req.setAttribute("seller_id", DirectPayConfig.PLATFORM_PARTNER_NO);
			req.setAttribute("seller_name", DirectPayConfig.PLATFORM_PARTNER_NAME);
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
			merchantWalletTransaction.addWalletRechargeLog(Double.parseDouble(amount), serialNo);
			return "yspayapi";
		} else {
			// 跳转至错误页面
			return "yspay_error";
		}
	}

	@RequestMapping(value = "/getMerchantWallet", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	@ApiOperation("商户查看钱包余额")
	public String getMerchantWallet(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantWalletTransaction.getMerchantWallet()).toString();
	}

	@RequestMapping(value = "/managerClearMerchantCash", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Manager")
	@ResponseBody
	@ApiOperation("管理员结算商户资金")
	// @RequiresPermissions("merchantWallet:managerClearMerchantCash")
	public String managerClearMerchantCash(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId, double amount) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("商户id不能为空!")).toString();
		}
		if (StringEmptyUtils.isEmpty(amount) || amount < 0.01) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("结算金额错误!")).toString();
		}
		Map<String,Object> reCheckWalletMap = merchantWalletTransaction.getMerchantWallet(merchantId,amount);
		if(!"1".equals(reCheckWalletMap.get(BaseCode.STATUS.toString()))){
			return JSONObject.fromObject(reCheckWalletMap).toString();
		}
		Map<String, Object> reBankMap = merchantWalletTransaction.getMerchantBankInfo(merchantId);
		if (!"1".equals(reBankMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reBankMap).toString();
		}
		MerchantBankContent bankContent = (MerchantBankContent) reBankMap.get(BaseCode.DATAS.toString());
		String serialNo= createSerialNo();
		//向银盛发起代付请求
		Map<String,Object> reMap= daiFuPay.dfTrade(DAI_FU_NOTIFY_URL, serialNo, serialNo, amount, "商户资金结算", bankContent.getBankProvince(),
				bankContent.getBankCity(), bankContent.getBankName(), bankContent.getBankAccountNo(),
				bankContent.getBankAccountName(), bankContent.getBankAccountType(), bankContent.getBankCardType());
		if(!"1".equals(reMap.get(BaseCode.STATUS.toString()))){
			return JSONObject.fromObject(reMap).toString();
		}
		Map<String,Object> reReceiptLogMap = merchantWalletTransaction.addPaymentReceiptLog(merchantId,amount,serialNo,"withdraw");
		if(!"1".equals(reReceiptLogMap.get(BaseCode.STATUS.toString()))){
			return JSONObject.fromObject(reReceiptLogMap).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
	}
	
	/**
	 * 生成代付16位流水号
	 * @return String 流水号
	 */
	private String createSerialNo() {
		String no = SerialNoUtils.getSerialNo("merchant_daifu") + "";
		while (no.length() < 7) {
			no = "0" + no;
		}
		return  "F" + DateUtil.formatDate(new Date(), "yyyyMMdd") + no;
	}
	
}
