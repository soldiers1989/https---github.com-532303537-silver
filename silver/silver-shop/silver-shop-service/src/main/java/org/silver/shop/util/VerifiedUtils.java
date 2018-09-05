package org.silver.shop.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.config.YmMallConfig;
import org.silver.util.BankCardUtils;
import org.silver.util.IdcardValidator;
import org.silver.util.MD5;
import org.silver.util.MapSortUtils;
import org.silver.util.PhoneUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class VerifiedUtils {
	@Autowired
	private AccessTokenService accessTokenService;

	/**
	 * 发送（三要素认证）姓名+身份证号码+手机号码实名验证
	 * 
	 * @param idName
	 *            姓名
	 * @param idCard
	 *            身份证号码
	 * @param phone
	 *            手机号码
	 * @return Map
	 */
	public Map<String, Object> sendThreeElementsVerification(String idName, String idCard, String phone) {
		if (StringEmptyUtils.isEmpty(idName)) {
			return ReturnInfoUtils.errorInfo("发送身份证校验,请求参数不能为空!");
		}
		if (!IdcardValidator.validate18Idcard(idCard)) {
			return ReturnInfoUtils.errorInfo("身份证号码错误！");
		}
		if (!PhoneUtils.isPhone(phone)) {
			return ReturnInfoUtils.errorInfo("手机号码错误！");
		}
		// 使用银盟商城app请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(YmMallConfig.APPKEY, YmMallConfig.APPSECRET);
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			return reTokMap;
		}
		String accessToken = reTokMap.get(BaseCode.DATAS.toString()) + "";
		Map<String, Object> params = new HashMap<>();
		params.put("version", "1.0");
		params.put("merchantNo", YmMallConfig.ID_CARD_CERTIFICATION_MERCHANT_NO);
		params.put("businessCode", "PT03");
		JSONObject bizContent = new JSONObject();
		bizContent.put("user_ID", idCard);
		bizContent.put("user_name", idName);
		bizContent.put("bank_mobile", phone);
		params.put("bizContent", bizContent);
		params.put("timestamp", System.currentTimeMillis());
		params = new MapSortUtils().sortMap(params);
		String str2 = YmMallConfig.APPKEY + accessToken + params;
		String clientSign = null;
		try {
			clientSign = MD5.getMD5(str2.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("加密签名错误!");
		}
		params.put("clientSign", clientSign);
		// String result =
		// YmHttpUtil.HttpPost("http://localhost:8080/silver-web/real/auth",
		// params);
		String result = YmHttpUtil.HttpPost(YmMallConfig.REAL_URL, params);
		if (StringEmptyUtils.isEmpty(result)) {
			return ReturnInfoUtils.errorInfo("验证身份证失败,网络异常！");
		} else {
			return JSONObject.fromObject(result);
		}
	}

	/**
	 * 发送（四要素认证）姓名+身份证号码+银行卡号+银行预留手机号，实名验证
	 * 
	 * @param idName
	 *            姓名
	 * @param idCard
	 *            身份证号码
	 * @param bankAccountNo
	 *            银行卡号
	 * @param phone
	 *            银行预留手机号
	 * @return Map
	 */
	public Map<String, Object> sendFourElementsVerification(String idName, String idCard, String bankAccountNo,
			String phone) {
		if (StringEmptyUtils.isEmpty(idName)) {
			return ReturnInfoUtils.errorInfo("姓名不能为空!");
		}
		if (!IdcardValidator.validate18Idcard(idCard)) {
			return ReturnInfoUtils.errorInfo("身份证号码错误！");
		}
		if (!PhoneUtils.isPhone(phone)) {
			return ReturnInfoUtils.errorInfo("银行预留手机号错误！");
		}
		if (!BankCardUtils.checkBankCard(bankAccountNo)) {
			return ReturnInfoUtils.errorInfo("银行卡错误！");
		}

		// 使用银盟商城app请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(YmMallConfig.APPKEY, YmMallConfig.APPSECRET);
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			return reTokMap;
		}
		String accessToken = reTokMap.get(BaseCode.DATAS.toString()) + "";
		Map<String, Object> params = new HashMap<>();
		params.put("version", "1.0");
		params.put("merchantNo", YmMallConfig.ID_CARD_CERTIFICATION_MERCHANT_NO);
		params.put("businessCode", "YS04");
		JSONObject bizContent = new JSONObject();
		bizContent.put("user_ID", idCard);
		bizContent.put("user_name", idName);
		bizContent.put("bank_account_no", bankAccountNo);
		bizContent.put("bank_mobile", phone);
		params.put("bizContent", bizContent);
		params.put("timestamp", System.currentTimeMillis());
		params = new MapSortUtils().sortMap(params);
		String str2 = YmMallConfig.APPKEY + accessToken + params;
		String clientSign = null;
		try {
			clientSign = MD5.getMD5(str2.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("加密签名错误!");
		}
		params.put("clientSign", clientSign);
		// String result =
		// YmHttpUtil.HttpPost("http://localhost:8080/silver-web/real/auth",
		// params);
		String result = YmHttpUtil.HttpPost(YmMallConfig.REAL_URL, params);
		if (StringEmptyUtils.isEmpty(result)) {
			return ReturnInfoUtils.errorInfo("验证身份证失败,网络异常！");
		} else {
			return JSONObject.fromObject(result);
		}
	}

}
