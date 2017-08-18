package org.silver.service;

import java.util.HashMap;
import java.util.Map;

import org.silver.sys.api.AppkeyService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;


@Service("oauthService")
public class OauthService {
	
    @Reference
    private AppkeyService appkeyService;
                                            
     public Map<String,Object> checkSign(String appkey,String clientsign,String list,String notifyurl,String timestamp){
    	Map<String, Object> statusMap = new HashMap<String, Object>();
 		try{
 			
 			statusMap=appkeyService.CheckClientSign(appkey, clientsign, list+notifyurl, timestamp);
 		}catch(Exception e){
 			e.printStackTrace();
 			statusMap.put("status", -5);
 			statusMap.put("warn", "The OauthServer is extremely busy at the moment, please try later .");
 		}
 		return statusMap;
     }
}
