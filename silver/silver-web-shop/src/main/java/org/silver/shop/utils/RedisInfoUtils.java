package org.silver.shop.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;

public class RedisInfoUtils {
	/**
	 * 公共缓存执行方法
	 * @param msg
	 *            错误信息
	 * @param errorList
	 *            错误集合
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            批次号
	 * @param name
	 *            名称
	 * @param type
	 *            1-错误,2-警告订单超额,3-详细地址信息错误...待续
	 */
	public static final void commonErrorInfo(String msg, List<Map<String, Object>> errorList, int totalCount,
			String serialNo, String name, int type) {
		Map<String, Object> errMap = new HashMap<>();
		errMap.put(BaseCode.MSG.toString(), msg);
		errMap.put("type", type);
		errorList.add(errMap);
		ExcelBufferUtils excelBufferUtils = new ExcelBufferUtils();
		excelBufferUtils.writeRedis(errorList, totalCount, serialNo, name);
	}
}
