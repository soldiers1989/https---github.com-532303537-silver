package org.silver.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.yspay.DirectPayConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

//银盛支付入口
@Controller
@RequestMapping(value="/yspay")
public class YsPayController {

	@RequestMapping(value="/dopay")
	public String doPay(HttpServletRequest req,HttpServletResponse resp){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		req.setAttribute("method", "ysepay.online.directpay.createbyuser");
		req.setAttribute("partner_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		req.setAttribute("timestamp", sdf.format(new Date()));
		req.setAttribute("charset", DirectPayConfig.DEFAULT_CHARSET);
		req.setAttribute("sign_type", DirectPayConfig.SIGN_ALGORITHM);
		// request.setAttribute("sign", userName);
		req.setAttribute("notify_url", "http://150.242.58.22:80/silver-web/upload/img");
		req.setAttribute("return_url", "http://ym.191ec.com");
		req.setAttribute("version", DirectPayConfig.VERSION);
		req.setAttribute("out_trade_no", "testOrder"+System.currentTimeMillis());//商户订单号
		req.setAttribute("subject", "即时到账");
		req.setAttribute("total_amount", 0.01 + "");//支付总金额
		req.setAttribute("seller_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		req.setAttribute("seller_name", DirectPayConfig.PLATFORM_PARTNER_NAME);
		req.setAttribute("timeout_express", "1h");
		req.setAttribute("business_code", "01000010");
		req.setAttribute("extra_common_param", "Ezreal");//支付人姓名
		// request.setAttribute("pay_mode", "internetbank");
		req.setAttribute("bank_type", "");
		req.setAttribute("bank_account_type", "");
		req.setAttribute("support_card_type", "");
		req.setAttribute("bank_account_no", "");
		
		return "yspayapi";
		
	}
}
