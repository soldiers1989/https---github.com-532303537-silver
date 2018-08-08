package org.silver.shop.controller.system.tenant;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.config.FenZhangConfig;
import org.silver.shop.controller.system.cross.DirectPayConfig;
import org.silver.shop.service.system.tenant.ManagerWalletTransaction;
import org.silver.shop.service.system.tenant.MemberWalletTransaction;
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
	//@RequiresPermissions("merchantWallet:getMerchantWalletInfo")
	public String getInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(memberWalletTransaction.getInfo()).toString();
	}
	
	@RequestMapping(value = "/onlineRecharge", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("线上充值")
	@RequiresRoles("Member")
	//@RequiresPermissions("merchantWallet:getMerchantWalletInfo")
	public String onlineRecharge(HttpServletRequest req, HttpServletResponse response,String amount) {
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
			req.setAttribute("notify_url", "https://ym.191ec.com/silver-web-shop/yspay-receive/memberRecharge");
			req.setAttribute("return_url", "https://asme.191ec.com/200.html");
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
			//merchantWalletTransaction.addWalletRechargeLog(Double.parseDouble(amount), serialNo);
			//return JSONObject.fromObject(memberWalletTransaction.onlineRecharge(amount)).toString();
			return "fen_zhang_pay";
		} else {
			// 跳转至错误页面
			return "yspay_error";
		}
	}
	
	private static Logger logger = LogManager.getLogger(Object.class);
	public static void main(String[] args) {
		logger.error("-error测试！！！！！！！！！！！-");
		logger.warn("-debug测试！！！！！！！！！！！-");
	}
}
