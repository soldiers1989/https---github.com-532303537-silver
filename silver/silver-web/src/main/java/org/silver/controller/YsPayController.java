package org.silver.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.util.YmHttpUtil;
import org.silver.yspay.ApipaySubmit;
import org.silver.yspay.DirectPayConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

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
	
	@RequestMapping(value="/getBillInfo")
	@ResponseBody
	public String getBillInfo(HttpServletRequest req,HttpServletResponse resp,String orderNo){
		Map textMap = new HashMap<>();
		JSONObject biz_content = new JSONObject();
		biz_content.put("out_trade_no", orderNo);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		textMap.put("method", "ysepay.online.trade.query");
		textMap.put("version", "3.0");
		textMap.put("partner_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		textMap.put("timestamp",  sdf.format(new Date()));
		textMap.put("charset","utf-8");
		textMap.put("sign_type", "RSA");
		textMap.put("biz_content", biz_content.toString());
		//textMap.put("out_trade_no", "testOrder1452323666556");
		textMap.put("sign", ApipaySubmit.sign1(req, textMap));
		//System.out.println(textMap);
	//	ApipaySubmit.verifyJsonSign(req, sign, responseBody, charset)
		//method=ysepay.online.trade.query
		JSONObject result = JSONObject.fromObject(YmHttpUtil.HttpPost("https://search.ysepay.com/gateway.do", textMap));
		System.out.println(result.get("ysepay_online_trade_query_response"));
		boolean b =ApipaySubmit.verifyJsonSign(req, result.getString("sign")+"", result.get("ysepay_online_trade_query_response")+"", "utf-8");
		return b+"";
		
	}
	
	/**
	 * 不可用
	 * @param req
	 * @param resp
	 * @param orderNo
	 * @return
	 */
	@RequestMapping(value="/directpay")
	@ResponseBody
	public String directpay(HttpServletRequest req,HttpServletResponse resp,String orderNo){
		Map textMap = new HashMap<>();
		JSONObject biz_content = new JSONObject();
		biz_content.put("out_trade_no", orderNo);
		biz_content.put("refund_amount", 100);
		biz_content.put("refund_reason", "不想买了");
		biz_content.put("out_request_no", "ym_directpay"+System.currentTimeMillis());//退款唯一标识码
		biz_content.put("out_trade_no", "testOrder"+System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		textMap.put("method", "ysepay.online.directpay.createbyuser");
		textMap.put("version", "3.0");
		textMap.put("partner_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		textMap.put("timestamp",  sdf.format(new Date()));
		textMap.put("charset","utf-8");
		textMap.put("sign_type", "RSA");
		textMap.put("biz_content", biz_content.toString());
		textMap.put("notify_url", "http://150.242.58.22:80/silver-web/upload/img");
		textMap.put("return_url", "");
		textMap.put("seller_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		textMap.put("seller_name", DirectPayConfig.PLATFORM_PARTNER_NAME);
		textMap.put("timeout_express", "1h");
		textMap.put("business_code","01000010");
		textMap.put("subject","退款");
		textMap.put("total_amount","100");
		textMap.put("out_trade_no","testOrder"+System.currentTimeMillis());
		
		req.setAttribute("timeout_express", "1h");
		req.setAttribute("business_code", "01000010");
		req.setAttribute("seller_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		req.setAttribute("seller_name", DirectPayConfig.PLATFORM_PARTNER_NAME);
		
		req.setAttribute("method", "ysepay.online.directpay.createbyuser");
		req.setAttribute("version", "3.0");
		req.setAttribute("partner_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		req.setAttribute("timestamp",  sdf.format(new Date()));
		req.setAttribute("charset","utf-8");
		req.setAttribute("sign_type", "RSA");
		req.setAttribute("biz_content", biz_content.toString());
		req.setAttribute("notify_url", "http://150.242.58.22:80/silver-web/upload/img");
		req.setAttribute("return_url", "");
		req.setAttribute("seller_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		req.setAttribute("seller_name", DirectPayConfig.PLATFORM_PARTNER_NAME);
		req.setAttribute("timeout_express", "1h");
		req.setAttribute("business_code","01000010");
		req.setAttribute("subject","退款");
		req.setAttribute("total_amount","100");
		req.setAttribute("out_trade_no","testOrder"+System.currentTimeMillis());
		
		//textMap.put("out_trade_no", "testOrder1452323666556");
		textMap.put("sign", ApipaySubmit.sign1(req, textMap));
		req.setAttribute("sign",ApipaySubmit.sign1(req, textMap));
		//System.out.println(textMap);
	//	ApipaySubmit.verifyJsonSign(req, sign, responseBody, charset)
		//method=ysepay.online.trade.query
		String result = YmHttpUtil.HttpPost("https://mertest.ysepay.com/openapi_gateway/gateway.do", textMap);
		System.out.println(result);
		//boolean b =ApipaySubmit.verifyJsonSign(req, result.getString("sign")+"", result.get("ysepay_online_trade_query_response")+"", "utf-8");
		
		return "";
	}
	
	
	
}
