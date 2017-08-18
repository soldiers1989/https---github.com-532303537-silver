package org.silver.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.service.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

/**
 * 授权网关  获取访问token
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value="/oauth")
public class AccessTokenController {
    
	 @Autowired
	 private AccessTokenService accessTokenService;
	
	 @RequestMapping(value="/token" ,produces = "application/json; charset=utf-8")
	 @ResponseBody
	 public String getAccessToken(HttpServletRequest req,HttpServletResponse resp,String appkey,String signature,String timestamp,String type){
		
		Map<String,Object> reqMap = new HashMap<String,Object>();
		reqMap.put("status", -3);
		reqMap.put("errMsg", "Invalid params!");
		reqMap.put("server", "【银盟跨境电商一站式服务平台-授权网关】");
		if(appkey!=null&&signature!=null&&timestamp!=null&&type!=null&&type.equals("oauth_token")){
			reqMap=accessTokenService.createAccessToken(appkey, signature, timestamp);
		}
		return JSONObject.fromObject(reqMap).toString();
		 
	 }
}
