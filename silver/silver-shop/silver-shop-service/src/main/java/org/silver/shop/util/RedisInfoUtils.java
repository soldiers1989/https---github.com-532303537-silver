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
	 *            1-错误,2-订单超额,3-详细地址信息错误,4-身份证校验不通过,5-超重,6-更新(已备案成功)手动订单商品..待续
	 * @param paramsMap
	 *            参数
	 */
	public static final void commonErrorInfo(String msg, List<Map<String, Object>> errorList, int type,
			Map<String, Object> paramsMap) {
		Map<String, Object> errMap = new HashMap<>();
		errMap.put(BaseCode.MSG.toString(), msg);
		errMap.put("type", type);
		errorList.add(errMap);
		BufferUtils bufferUtils = new BufferUtils();
		if (type == 1) {
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
	 *            1-错误,2-订单超额,3-详细地址信息错误,4-身份证校验不通过,5-超重,6-更新(已备案成功)手动订单商品..待续
	 * @param paramsMap
	 *            参数
	 */
	public static final void errorInfoMq(String msg, int type, Map<String, Object> paramsMap) {
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
