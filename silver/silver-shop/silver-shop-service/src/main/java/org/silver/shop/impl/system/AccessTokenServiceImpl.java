package org.silver.shop.impl.system;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.config.YmMallConfig;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {

	@Override
	public Map<String, Object> getAccessToken(String appkey, String appSecret) {
		Map<String, Object> params = new HashMap<>();
		String timestamp = String.valueOf(System.currentTimeMillis());
		String signature = "";
		try {
			signature = MD5.getMD5((appkey + appSecret + timestamp).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		params.put("appkey", appkey);
		params.put("timestamp", timestamp);
		params.put("signature", signature);
		params.put("type", "oauth_token");
		String resultStr = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/oauth/token", params);
		if (StringEmptyUtils.isNotEmpty(resultStr)) {
			JSONObject json = JSONObject.fromObject(resultStr);
			String status = json.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {// 请求成功后
				return ReturnInfoUtils.successDataInfo(json.getString("accessToken"));
			} else {
				return json;
			}
		} else {
			return ReturnInfoUtils.errorInfo("服务器请求获取tok失败,服务器繁忙!");
		}
	}

	public Map<String, Object> getTestToken() {
		Map<String, Object> params = new HashMap<>();
		String timestamp = System.currentTimeMillis() + "";
		String signature = MD5.getMD5(("cf285ec49c724019a297c0dc6941df5d"
				+ "jbTEF04KL350s0bCOmGL8q8p1dawGXHTPq9SKKk8l5gzAuWbmdYqjhdpgyyHnPco" + timestamp).getBytes());
		params.put("appkey", "cf285ec49c724019a297c0dc6941df5d");
		params.put("timestamp", timestamp);
		params.put("signature", signature);
		params.put("type", "oauth_token");
		String resultStr = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/oauth/token", params);
		if (StringEmptyUtils.isNotEmpty(resultStr)) {
			JSONObject json = JSONObject.fromObject(resultStr);
			if ("1".equals(json.get(BaseCode.STATUS.toString()) + "")) {// 请求成功后
				return ReturnInfoUtils.successDataInfo(json.getString("accessToken"));
			} else {
				return json;
			}
		} else {
			return ReturnInfoUtils.errorInfo("服务器请求获取tok失败,服务器繁忙!");
		}
	}

	@Override
	public Map<String, Object> getRedisToks(String appkey, String appSecret) {
		if (StringEmptyUtils.isEmpty(appkey) || StringEmptyUtils.isEmpty(appSecret)) {
			return ReturnInfoUtils.errorInfo("获取缓存中Tok时,参数不能为空!");
		}
		// 缓存中的键
		String redisKey = "SHOP_KEY_PUSH_RECORD_TOK_" + appkey;
		String redisTok = JedisUtil.get(redisKey);
		// 当缓存中已有tok时
		if (StringEmptyUtils.isNotEmpty(redisTok)) {
			return ReturnInfoUtils.successDataInfo(redisTok.replaceAll("\"", ""));
		} else {
			// 请求获取tok
			Map<String, Object> tokMap = getAccessToken(appkey, appSecret);
			if (!"1".equals(tokMap.get(BaseCode.STATUS.toString()))) {
				return tokMap;
			}
			// 由于服务器缓存时间为1小时,为岔开时间故而商城为50分钟
			JedisUtil.set(redisKey, 3000, tokMap.get(BaseCode.DATAS.toString()));
			return tokMap;
		}
	}

	public static void main(String[] args) {
		/*
		 * Map<String, Object> params = new HashMap<>(); String timestamp =
		 * String.valueOf(System.currentTimeMillis()); String signature = "";
		 * try { signature = MD5.getMD5((YmMallConfig.APPKEY +
		 * YmMallConfig.APPSECRET + timestamp).getBytes("UTF-8")); } catch
		 * (UnsupportedEncodingException e) { e.printStackTrace(); }
		 * params.put("appkey", YmMallConfig.APPKEY); params.put("timestamp",
		 * timestamp); params.put("signature", signature); params.put("type",
		 * "oauth_token"); String resultStr =
		 * YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/oauth/token",
		 * params); System.out.println("---->?>>"+resultStr);
		 */
		AccessTokenServiceImpl a = new AccessTokenServiceImpl();
		System.out.println(a.getRedisToks(YmMallConfig.APPKEY, YmMallConfig.APPSECRET));
	}
}