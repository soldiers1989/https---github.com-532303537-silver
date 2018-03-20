package org.silver.shop.impl.system;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.impl.system.manual.AppkeyServiceImpl;
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
	public Map<String, Object> getAccessToken() {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		String timestamp = String.valueOf(System.currentTimeMillis());
		String signature = "";
		try {
			signature = MD5.getMD5((YmMallConfig.APPKEY + YmMallConfig.APPSECRET + timestamp).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		params.put("appkey", YmMallConfig.APPKEY);
		params.put("timestamp", timestamp);
		params.put("signature", signature);
		params.put("type", "oauth_token");
		String resultStr = YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/oauth/token", params);
		if (StringEmptyUtils.isNotEmpty(resultStr)) {
			JSONObject json = JSONObject.fromObject(resultStr);
			String status = json.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.DATAS.toString(), json.getString("accessToken"));
			} else {
				return json;
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "服务器请求获取tok失败,服务器繁忙!");
		}
		return statusMap;
	}

	public Map<String, Object> getTestToken() {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		String timestamp = System.currentTimeMillis() + "";
		String signature = MD5.getMD5(("cf285ec49c724019a297c0dc6941df5d"
				+ "jbTEF04KL350s0bCOmGL8q8p1dawGXHTPq9SKKk8l5gzAuWbmdYqjhdpgyyHnPco" + timestamp).getBytes());
		params.put("appkey", "cf285ec49c724019a297c0dc6941df5d");
		params.put("timestamp", timestamp);
		params.put("signature", signature);
		params.put("type", "oauth_token");
		String resultStr = YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/oauth/token", params);
		if (StringEmptyUtils.isNotEmpty(resultStr)) {
			JSONObject json = JSONObject.fromObject(resultStr);
			String status = json.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.DATAS.toString(), json.getString("accessToken"));
			} else {
				return json;
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "服务器请求获取tok失败,服务器繁忙!");
		}
		return statusMap;
	}

	public static void main(String[] args) {
		AccessTokenServiceImpl as = new AccessTokenServiceImpl();
		/*
		 * System.out.println(as.createRecord("银盟通网网关", "YM", "020-85668893",
		 * "123", "广州银盟信息科技有限公司", "http://www.191ec.com"));
		 * //{msg=生成成功,请注意保护好您的密钥, appKey=4a5de70025a7425dabeef6e8ea752976,
		 * appSecret=
		 * NeMs1DFG8xFARwZeSlRZwlT22ayY5oIbkgZg1uCziQ3LfSgqcPN4qGydAt7s3jMW,
		 * status=1} JedisUtil.set("".getBytes(), "value".getBytes(), 3600*5);
		 */
		System.out.println(as.getAccessToken());

	}

	@Override
	public Map<String, Object> getRedisToks() {
		// 缓存中的键
		String redisKey = "Shop_Key_PushRecord_Tok";
		String redisTok = JedisUtil.get(redisKey);
		// 当缓存中已有tok时
		if (StringEmptyUtils.isNotEmpty(redisTok)) {
			return ReturnInfoUtils.successDataInfo(redisTok.replaceAll("\"", ""), 0);
		} else {
			// 请求获取tok
			Map<String, Object> tokMap = getAccessToken();
			if (!"1".equals(tokMap.get(BaseCode.STATUS.toString()))) {
				return tokMap;
			}
			// 由于服务器缓存时间为3小时,为岔开时间故而商城为2.5小时
			JedisUtil.set(redisKey, 9000, tokMap.get(BaseCode.DATAS.toString()));
			return tokMap;
		}
	}
}
