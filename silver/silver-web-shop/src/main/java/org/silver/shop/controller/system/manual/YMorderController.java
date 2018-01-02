package org.silver.shop.controller.system.manual;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.shop.controller.system.cross.ApipaySubmit;
import org.silver.shop.controller.system.cross.DirectPayConfig;
import org.silver.shop.task.ExcelTask;
import org.silver.shop.utils.ExcelUtil;
import org.silver.util.MD5;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javassist.compiler.ast.Symbol;
import net.sf.json.JSONObject;

//银盟电子订单
@Controller
@RequestMapping("/ympay")
public class YMorderController {

	@Autowired
	private org.silver.shop.service.system.manual.YMorderBusiness YMorderBusiness;

	/**
	 * 下单入口
	 */
	@RequestMapping("/dopay")
	public String dopay(HttpServletRequest req, HttpServletResponse resp, String merchant_no, String out_trade_no,
			String amount, String return_url, String notify_url, String extra_common_param, String client_sign,
			String timestamp,String errBack) {

		if (merchant_no != null && out_trade_no != null && amount != null && client_sign != null && timestamp != null
				&& notify_url != null) {
			Map<String, Object> reqMap = YMorderBusiness.doBusiness(merchant_no, out_trade_no, amount, notify_url,
					extra_common_param, client_sign, timestamp);
			if ((int) reqMap.get("status") != 1) {
				req.setAttribute("msg", reqMap.get("msg")+"<a href=\""+errBack+"\">"+"点击返回</a>");
				return "ympay-err";
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			req.setAttribute("method", "ysepay.online.directpay.createbyuser");
			req.setAttribute("partner_id", DirectPayConfig.PLATFORM_PARTNER_NO);
			req.setAttribute("timestamp", sdf.format(new Date()));
			req.setAttribute("charset", DirectPayConfig.DEFAULT_CHARSET);
			req.setAttribute("sign_type", DirectPayConfig.SIGN_ALGORITHM);
			// request.setAttribute("sign", userName);
			req.setAttribute("notify_url", "http://ym.191ec.com/silver-web/ympay/callback,http://ym.191ec.com/silver-web-pay/pro/yspay-receive");
			req.setAttribute("return_url", return_url);
			req.setAttribute("version", DirectPayConfig.VERSION);
			req.setAttribute("out_trade_no", reqMap.get("order_id"));// 商户订单号
			req.setAttribute("subject", "即时到账");
			req.setAttribute("total_amount", amount);// 支付总金额
			req.setAttribute("seller_id", DirectPayConfig.PLATFORM_PARTNER_NO);
			req.setAttribute("seller_name", DirectPayConfig.PLATFORM_PARTNER_NAME);
			req.setAttribute("timeout_express", "1h");
			req.setAttribute("business_code", "01000010");
			req.setAttribute("extra_common_param", extra_common_param);// 支付人姓名
			// request.setAttribute("pay_mode", "internetbank");
			req.setAttribute("bank_type", "");
			req.setAttribute("bank_account_type", "");
			req.setAttribute("support_card_type", "");
			req.setAttribute("bank_account_no", "");
			return "yspayapi";
		}
		req.setAttribute("msg", "下单参数有误");
		return "ympay-err";

	}

	@RequestMapping("/callback")
	@ResponseBody
	public String callback(HttpServletRequest req, HttpServletResponse resp) {
		Map reqMap = new HashMap<String, Object>();
		Enumeration<String> strs = req.getParameterNames();
		String key = "", value = "";
		while (strs.hasMoreElements()) {
			key = strs.nextElement();
			value = req.getParameter(key);
			reqMap.put(key, value);
		}
		System.out.println(reqMap);
		boolean b = ApipaySubmit.verifySign(req, reqMap);
		if (b) {
			String order_id=reqMap.get("out_trade_no")+"";
			String trade_no=reqMap.get("trade_no")+"";
			String trade_status=reqMap.get("trade_status")+"";
			Map<String,Object> statusMap=YMorderBusiness.doCallBack(order_id,trade_no,trade_status);
			System.out.println(statusMap);
			if((int)statusMap.get("status")==1){
				return "success";
			}
			System.out.println("异步通知签名校验通过");
		} else {
			System.out.println("异步通知签名校验失败");
		}
		return null;
	}
	
	
    /********************模拟第四方下单***************/
	@RequestMapping("/test")
	public String test(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
		String timestamp = System.currentTimeMillis() + "";
		String appkey = "4a5de70025a7425dabeef6e8ea752976";
		String appsecret = "NeMs1DFG8xFARwZeSlRZwlT22ayY5oIbkgZg1uCziQ3LfSgqcPN4qGydAt7s3jMW";
		String signature = MD5.getMD5((appkey + appsecret + timestamp).getBytes("UTF-8"));
		Map<String, Object> params2 = new HashMap<String, Object>();
		params2.put("appkey", appkey);
		params2.put("signature", signature);
		params2.put("timestamp", timestamp);
		params2.put("type", "oauth_token");
		JSONObject result = net.sf.json.JSONObject
				.fromObject(YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/oauth/token", params2));
		String accessToken = result.get("accessToken") + "";
		String merchant_no = "YM20170000015078659178651922";
		String out_trade_no = "test" + timestamp;
		String amount = "0.01";
		String notify_url = "http://www.baidu.com";
		String str = merchant_no + out_trade_no + amount + notify_url + timestamp;
		String client_sign = "";
		try {
			client_sign = MD5.getMD5((appkey + accessToken + str + timestamp).getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "redirect:http://ym.191ec.com/silver-web/ympay/dopay?merchant_no=" + merchant_no
				+ "&out_trade_no=" + out_trade_no + "&amount=" + amount + "&return_url=" + "www.baidu.com" + "&notify_url="
				+ notify_url+"&client_sign="+client_sign+"&timestamp="+timestamp;
	}

	
	public static void main(String[] args) {
		int totalCount = 200;
		int cpuCount = Runtime.getRuntime().availableProcessors();
		int start = 0;
		int end = 0;
		for (int i = 0; i < cpuCount; i++) {
		//	ExcelUtil excelC = new ExcelUtil(f);
			if (i == 0) {
				end = totalCount / cpuCount;
				System.out.println("----开始第一次"+end);
				//excelTask = new ExcelTask(0, excelC, errl, merchantId, 1, end, this);
			} else {
				start = end + 1;
				end = start +(totalCount / cpuCount);
				if (i == (cpuCount - 1)) {// 最后一次
					System.out.println("----开始最后一次"+start);
					
					//excelTask = new ExcelTask(0, excelC, errl, merchantId, start, totalCount, this);
				}else{
					System.out.println("----开始每一次"+start);
					System.out.println("----开始每一次结束---->>>>"+end);
					//excelTask = new ExcelTask(0, excelC, errl, merchantId, start, end, this);
				}
			}
			
			//threadPool.submit(excelTask);
		}
	}
}
