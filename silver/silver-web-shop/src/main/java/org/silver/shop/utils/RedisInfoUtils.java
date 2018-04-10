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
	 *            类型:1-错误,2-订单超额,3-详细地址信息错误,4-身份证错误,5-超重,6-收货人手机号码超过使用次数...待续
	 * @param params
	 */
	public static final void commonErrorInfo(String msg, List<Map<String, Object>> errl, int type,
			Map<String, Object> params) {
		Map<String, Object> err = new HashMap<>();
		err.put(BaseCode.MSG.toString(), msg);
		err.put("type", type);
		errl.add(err);
		ExcelBufferUtils excelBufferUtils = new ExcelBufferUtils();
		//
		if (type == 1) {
			excelBufferUtils.writeRedis(errl, params);
		}
	}

	/**
	 * Mq版错误信息写入,
	 * 
	 * @param msg
	 *            信息
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
		ExcelBufferUtils excelBufferUtils = new ExcelBufferUtils();
		excelBufferUtils.writeRedisMq(errorList, paramsMap);
	}
}
