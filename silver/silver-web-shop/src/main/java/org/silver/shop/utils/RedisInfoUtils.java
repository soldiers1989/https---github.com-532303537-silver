package org.silver.shop.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;

public class RedisInfoUtils {
	/**
	 * 公共缓存执行方法
	 * 
	 * @param params
	 * @param errl
	 * @param msg
	 * @param msg
	 * 
	 * @param msg
	 *            错误信息
	 * @param errorList
	 *            错误集合
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            批次号
	 * @param name
	 *            名称(标识)
	 * @param type
	 *            类型:1-错误,2-订单超额,3-详细地址信息错误,4-身份证校验不通过,5-超重....待续
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

}
