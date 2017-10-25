package org.silver.shop.impl.system;

import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.config.YmMallConfig;
import org.silver.util.MD5;
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
		String signature = MD5.getMD5((YmMallConfig.APPKEY + YmMallConfig.APPSECRET + timestamp).getBytes());
		params.put("appkey", YmMallConfig.APPKEY);
		params.put("timestamp", timestamp);
		params.put("signature", signature);
		params.put("type", "oauth_token");
		String resultStr = YmHttpUtil.HttpPost("http://ym.191ec.com/silver-web/oauth/token", params);
		if (StringEmptyUtils.isNotEmpty(resultStr)) {
			JSONObject json = JSONObject.fromObject(resultStr);
			String status = json.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status) ) {
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
		AccessTokenServiceImpl a = new AccessTokenServiceImpl();
		System.out.println(a.getAccessToken());
	}
}
