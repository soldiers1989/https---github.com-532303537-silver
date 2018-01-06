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
import org.silver.util.DateUtil;
import org.silver.util.MD5;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

//银盟电子订单
@Controller
@RequestMapping("/ympay")
public class YMorderController {

	@Autowired
	private org.silver.shop.service.system.manual.YMorderBusiness YMorderBusiness;

	

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
	@RequestMapping("/dopay")
	public String test(HttpServletRequest req, HttpServletResponse resp,String merchant_cus_no,String client_sign,String out_trade_no,Double amount,String notify_url,String timestamp,String return_url,String comm_extra_params) throws Throwable {
		//String timestamp = System.currentTimeMillis() + "";
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
	
		String str = merchant_cus_no + out_trade_no + amount + notify_url + timestamp;
		//String client_sign = "";
		try {
			client_sign = MD5.getMD5((appkey + accessToken + str + timestamp).getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "redirect:http://ym.191ec.com/silver-web/ympay/enter?merchant_cus_no=" + merchant_cus_no
				+ "&out_trade_no=" + out_trade_no + "&amount=" + amount + "&return_url=" + "www.baidu.com" + "&notify_url="
				+ notify_url+"&client_sign="+client_sign+"&timestamp="+timestamp;
	}

	
	public static void main(String[] args) {
	 	 String timestamp =System.currentTimeMillis()+""; 
		 String appkey="appkey";
		 String accessToken =  "accessToken";
		 String merchant_cus_no = "YM20170000015078659178651922";
		 String out_trade_no = "test" + timestamp;
		 Double amount = 0.01;
		 String notify_url = "http://www.abc.com/callback";
		 String str = amount+merchant_cus_no + out_trade_no + notify_url+timestamp;
		 String client_sign = "";
		try {
			client_sign = MD5.getMD5((appkey + accessToken + str + timestamp).getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
