package org.silver.shop.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisInfoUtils {

	/**
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
	 *            名称
	 */
	public static final void commonErrorInfo(String msg, List<Map<String, Object>> errorList, int totalCount,
			String serialNo, String name,int type) {
		Map<String, Object> errMap = new HashMap<>();
		errMap.put(BaseCode.MSG.toString(), msg);
		errMap.put("type", type);
		errorList.add(errMap);
		BufferUtils bufferUtils = new BufferUtils();
		bufferUtils.writeRedis(errorList, totalCount, serialNo, name);
	}

}
