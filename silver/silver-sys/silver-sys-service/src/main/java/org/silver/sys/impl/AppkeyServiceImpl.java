package org.silver.sys.impl;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.silver.sys.api.AppkeyService;
import org.silver.sys.dao.AppkeyDao;
import org.silver.sys.model.Appkey;
import org.silver.util.AppUtil;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=AppkeyService.class)
public class AppkeyServiceImpl implements AppkeyService {

	@Resource
	private AppkeyDao appkeyDao;

	@Override
	public Map<String, String> createRecord(String app_name, String user_name, String user_mobile,String user_id,String company_name,String website) {

		Map<String, String> map = new HashMap<String, String>();
		if (user_mobile != null && !"".equals(user_mobile.trim())) {
			Appkey entity = new Appkey();
			String appKey = AppUtil.generateAppKey();
			String appSecret = AppUtil.generateAppSecret();
			entity.setApp_name(app_name);
			entity.setUser_name(user_name);
			entity.setUser_mobile(user_mobile);
			entity.setUser_id(user_id);
			entity.setApp_key(appKey);
			entity.setApp_secret(appSecret);
			entity.setCreate_date(new Date());
			entity.setCreate_by(user_name);
			if (appkeyDao.add(entity)) {
				map.put("status", 1 + "");
				map.put("msg", "生成成功,请注意保护好您的密钥");
				map.put("appKey", appKey);
				map.put("appSecret", appSecret);
				return map;
			}
			map.put("status", -1 + "");
			map.put("msg", "存储失败，请重试");
			return map;
		}
		map.put("status", -3 + "");
		map.put("msg", "生成失败,请确认好参数再提交");
		return map;
	}

	@Override
	public Map<String, Object> createAccessToken(String appkey, String signature, String timestamp) {
		String appsecret = JedisUtil.get(appkey);
		Map<String, Object> statusMap = new HashMap<String, Object>();
		if(!AppUtil.checkTimestamp(System.currentTimeMillis(), timestamp, 3600*1000)){
			statusMap.put("status", -4);
			statusMap.put("errMsg", "Invalid timestamp!");
			return statusMap;
		}
		if (!(appsecret != null && !appsecret.trim().equals(""))) {
			statusMap.clear();
			statusMap.put("app_key", appkey);
			statusMap.put("del_flag", 0);
			List<Appkey> list = appkeyDao.findByProperty(statusMap, 1, 1);
			if (list != null && list.size() > 0) {
				appsecret = list.get(0).getApp_secret();
				JedisUtil.set(appkey, 3600 * 5, appsecret);// 设置缓存的appkey 5小时有效
			}else{
				statusMap.clear();
				statusMap.put("status", -1);
				statusMap.put("errMsg", "Appkey do not exist!");
				return statusMap;
			}
		}
		String str = appkey + appsecret.replaceAll("\"", "") + timestamp;
		if (MD5.getMD5(str.getBytes()).equals(signature)) {
		    String accessToken=AppUtil.generateYMAccessToken();
		    JedisUtil.set(appkey+"_accessToken", 3600 ,accessToken);// 设置缓存的accessToken 1小时有效
			statusMap.put("status", 1);
			statusMap.put("accessToken", accessToken);
			statusMap.put("expire", 3600);
			statusMap.put("errMsg", "");
			statusMap.put("server", "【银盟跨境电商一站式服务平台-授权网关】");
			return statusMap;
		}
		statusMap.put("status", -2);
		statusMap.put("errMsg", "Invalid signature!");
		return statusMap;
	}
	

	@Override
	public Map<String, Object> CheckClientSign(String appkey, String clientsign, String list, String timestamp) {
		 Map<String,Object> statusMap = new HashMap<String,Object>();
    	 if(!AppUtil.checkTimestamp(System.currentTimeMillis(), timestamp, 3600*1000)){
 			statusMap.put("status", -4);
 			statusMap.put("msg", "Invalid timestamp!");
 			return statusMap;
 		}
    	 String accessToken=JedisUtil.get(appkey+"_accessToken");
    	 if(accessToken!=null&&!accessToken.trim().equals("")){
    		String tureSign = "";
			try {
				tureSign = MD5.getMD5((appkey+accessToken.replaceAll("\"", "")+list+timestamp).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		if(tureSign.equals(clientsign)){
    			statusMap.put("status", 1);
    			statusMap.put("msg", "accept");
    			return statusMap;
    		}
    		statusMap.put("status", -2);
			statusMap.put("msg", "Incorrect clientsign!");
			return statusMap;
    	 }
    	 statusMap.put("status", -1);
		 statusMap.put("msg", "AccessToken is expire or missing!");
		 return statusMap;
	}
}
