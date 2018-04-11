package org.silver.shop.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;

public class RedisInfoUtils {

	/**
	 * 
	 * @param msg
	 *            错误信息
	 * @param errorList
	 *            错误集合
	 * @param type
	 *             错误类型:
	 *            error-错误,orderExcess-订单超额,address-地址信息,idCard-身份证,overweight-超重,phone-手机号码,member-会员信息..待续
	 * @param paramsMap
	 *            参数
	 */
	public static final void commonErrorInfo(String msg, List<Map<String, Object>> errorList, String type,
			Map<String, Object> paramsMap) {
		Map<String, Object> errMap = new HashMap<>();
		errMap.put(BaseCode.MSG.toString(), msg);
		errMap.put("type", type);
		errorList.add(errMap);
		BufferUtils bufferUtils = new BufferUtils();
		if ("error".equals(type)) {
			bufferUtils.writeRedis(errorList, paramsMap);
		}
	}

	/**
	 * Mq版错误信息写入,
	 * 
	 * @param msg
	 *            错误信息
	 * @param errorList
	 *            错误集合
	 * @param type
	 *            错误类型:
	 *            error-错误,orderExcess-订单超额,address-地址信息,idCard-身份证,overweight-超重,phone-手机号码,member-会员信息..待续
	 * @param paramsMap
	 *            参数
	 */
	public static final void errorInfoMq(String msg, String type, Map<String, Object> paramsMap) {
		List<Map<String, Object>> errorList = new ArrayList<>();
		Map<String, Object> errMap = new HashMap<>();
		errMap.put(BaseCode.MSG.toString(), msg);
		errMap.put("type", type);
		errorList.add(errMap);
		//
		paramsMap.put("type", type);
		BufferUtils bufferUtils = new BufferUtils();
		bufferUtils.writeRedisMq(errorList, paramsMap);
	}
}
