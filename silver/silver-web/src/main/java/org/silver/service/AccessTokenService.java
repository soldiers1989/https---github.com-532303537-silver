package org.silver.service;

import java.util.HashMap;
import java.util.Map;

import org.silver.sys.api.AppkeyService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class AccessTokenService {

	@Reference
	private AppkeyService appkeyService;
	
	public Map<String, Object> createAccessToken(String appkey, String signature, String timestamp) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		try{
			statusMap=appkeyService.createAccessToken(appkey, signature, timestamp);
		}catch(Exception e){
			statusMap.put("status", "-5");
			statusMap.put("warn", "The OauthServer is extremely busy at the moment, please try later .");
		}
		return statusMap;
	}
    
	
}
