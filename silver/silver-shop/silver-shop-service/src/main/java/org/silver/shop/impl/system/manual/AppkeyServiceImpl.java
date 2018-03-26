package org.silver.shop.impl.system.manual;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.manual.AppkeyService;
import org.silver.shop.component.ChooseDatasourceHandler;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.SessionFactory;
import org.silver.shop.dao.system.manual.impl.AppkeyDaoImpl;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.util.AppUtil;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = AppkeyService.class)
public class AppkeyServiceImpl implements AppkeyService {

	// @Resource
	//@Autowired
	//private AppkeyDao appkeyDao;
	private static AppkeyDaoImpl appkeyDao = new AppkeyDaoImpl();

	@Override
	public Map<String, Object> createRecord(String app_name, String user_name, String user_mobile,
			String merchant_Id, String merchant_name ) {
		Map<String, Object> map = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(user_mobile) && StringEmptyUtils.isNotEmpty(merchant_Id)) {
			Appkey entity = new Appkey();
			String appKey = AppUtil.generateAppKey();
			String appSecret = AppUtil.generateAppSecret();
			entity.setApp_name(app_name);
			entity.setUser_name(user_name);
			entity.setUser_mobile(user_mobile);
			entity.setApp_key(appKey);
			entity.setMerchant_Id(merchant_Id);
			entity.setApp_secret(appSecret);
			entity.setDel_flag(0);
			entity.setCreate_date(new Date());
			entity.setCreate_by("system");
			entity.setMerchant_name(merchant_name);
			if (appkeyDao.add(entity)) {
				map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				map.put("msg", "生成成功,请注意保护好您的密钥!");
				map.put("appKey", appKey);
				map.put("appSecret", appSecret);
				return map;
			}
			return ReturnInfoUtils.errorInfo("存储密钥失败，服务器繁忙!");
		}
		return ReturnInfoUtils.errorInfo("生成失败,参数不符,请核实所需参数!");
	}

	@Override
	public Map<String, Object> createAccessToken(String appkey, String signature, String timestamp) {
		String appsecret = JedisUtil.get(appkey);
		Map<String, Object> statusMap = new HashMap<String, Object>();
		if (!AppUtil.checkTimestamp(System.currentTimeMillis(), timestamp, 3600 * 1000)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put("errMsg", "Invalid timestamp!");
			return statusMap;
		}
		if (!(appsecret != null && !appsecret.trim().equals(""))) {
			statusMap.clear();
			statusMap.put("app_key", appkey);
			statusMap.put("del_flag", 0);
			List<Appkey> list = appkeyDao.findByProperty(Appkey.class, statusMap, 1, 1);
			System.out.println(list);
			if (list != null && list.size() > 0) {
				appsecret = list.get(0).getApp_secret();
				JedisUtil.set(appkey, 3600 * 5, appsecret);// 设置缓存的appkey 5小时有效
			} else {
				statusMap.clear();
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
				statusMap.put("errMsg", "Appkey do not exist!");
				return statusMap;
			}
		}
		String str = appkey + appsecret.replaceAll("\"", "") + timestamp;
		if (MD5.getMD5(str.getBytes()).equals(signature)) {
			String accessToken = AppUtil.generateYMAccessToken();
			JedisUtil.set(appkey + "_accessToken", 3600, accessToken);// 设置缓存的accessToken
																		// 1小时有效
			statusMap.put("status", 1);
			statusMap.put("accessToken", accessToken);
			statusMap.put("expire", 3600);
			statusMap.put("errMsg", "");
			statusMap.put("server", "【银盟跨境商城-授权网关】");
			return statusMap;
		}
		statusMap.put("status", -2);
		statusMap.put("errMsg", "Invalid signature!");
		return statusMap;
	}

	@Override
	public Map<String, Object> CheckClientSign(String appkey, String clientsign, String list, String timestamp) {
		Map<String, Object> statusMap = new HashMap<>();
		if (!AppUtil.checkTimestamp(System.currentTimeMillis(), timestamp, 3600 * 1000)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put("msg", "Invalid timestamp!");
			return statusMap;
		}
		String accessToken = JedisUtil.get(appkey + "_accessToken");
		if (accessToken != null && !accessToken.trim().equals("")) {
			String tureSign = "";
			try {
				tureSign = MD5.getMD5((appkey + accessToken.replaceAll("\"", "") + list + timestamp).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			System.out.println(tureSign + "【tureSign】");
			if (tureSign.equals(clientsign)) {
				statusMap.put("status", 1);
				statusMap.put("msg", "accept");
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put("msg", "Incorrect clientsign!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
		statusMap.put("msg", "AccessToken is expire or missing!");
		return statusMap;
	}

	public static void main(String[] args) {
		ChooseDatasourceHandler.hibernateDaoImpl.setSession(SessionFactory.getSession());
		AppkeyServiceImpl as = new AppkeyServiceImpl();
		/*System.out.println(
				as.createRecord("银盟跨境商城-授权网关", "YM", "020-85668893", "YM_MerchantId_00001", "广州银盟信息科技有限公司", "http://www.191ec.com"));
*/		// {msg=生成成功,请注意保护好您的密钥, appKey=4a5de70025a7425dabeef6e8ea752976,
		// appSecret=NeMs1DFG8xFARwZeSlRZwlT22ayY5oIbkgZg1uCziQ3LfSgqcPN4qGydAt7s3jMW,
		// status=1}
		String value =  "4bb80f35608d4faa8923efb003f74b9a";
		String key =value+"_Shop_AccessToken";
		JedisUtil.set(key.getBytes(), value.getBytes(), 3600*5);
		//JedisUtil.set("4bb80f35608d4faa8923efb003f74b9a_Shop_AccessToken".getBytes(),value.getBytes(), 3600 * 5);
	}
}
