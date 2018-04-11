package org.silver.shop.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;

public class RedisInfoUtils {
	/**
	 * 公共缓存执行方法
	 * 
	 * @param msg
	 *            错误信息
	 * @param errorList
	 *            错误集合
	 * @param type
	 *            error-错误,orderExcess-订单超额,address-地址信息,idCard-身份证,overweight-超重,phone-手机号码,member-会员信息..待续
	 * @param params
	 */
	public static final void commonErrorInfo(String msg, List<Map<String, Object>> errl, String type,
			Map<String, Object> params) {
		Map<String, Object> err = new HashMap<>();
		err.put(BaseCode.MSG.toString(), msg);
		err.put("type", type);
		errl.add(err);
		ExcelBufferUtils excelBufferUtils = new ExcelBufferUtils();
		//
		if ("error".equals(type)) {
			excelBufferUtils.writeRedis(errl, params);
		}
	}

	/**
	 * Mq版错误信息写入,
	 * 
	 * @param msg
	 *            信息
	 * @param type
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
		ExcelBufferUtils excelBufferUtils = new ExcelBufferUtils();
		excelBufferUtils.writeRedisMq(errorList, paramsMap);
	}
}
