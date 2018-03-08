package org.silver.util;

import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;

/**
 * 通用信息返回工具类
 *
 */
public class ReturnInfoUtils {
	private ReturnInfoUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * 错误返回信息
	 * 
	 * @param msg
	 *            提示信息
	 * @return Map
	 */
	public static final Map<String, Object> errorInfo(String msg) {
		Map<String, Object> statusMap = new HashMap<>();
		if (StringEmptyUtils.isEmpty(msg)) {
			return null;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		statusMap.put(BaseCode.MSG.toString(), msg);
		return statusMap;
	}

	/**
	 * 成功返回信息
	 * 
	 * @return Map
	 */
	public static final Map<String, Object> successInfo() {
		Map<String, Object> statusMap = new HashMap<>();
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	/**
	 * 返回成功信息,并带Data数据
	 * 
	 * @param Datas
	 *            返回参数
	 * @param count
	 *            总数
	 * @return Map
	 */
	public static final Map<String, Object> successDataInfo(Object datas, long count) {
		Map<String, Object> statusMap = new HashMap<>();
		if (datas == null || count < 0) {
			return null;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put(BaseCode.DATAS.toString(), datas);
		statusMap.put(BaseCode.TOTALCOUNT.toString(), count);
		return statusMap;
	}
}
