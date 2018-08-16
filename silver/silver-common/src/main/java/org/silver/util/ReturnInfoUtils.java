package org.silver.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;

/**
 * 通用信息返回工具类
 */
public class ReturnInfoUtils {

	/**
	 * 错误返回信息,注：该信息为单条错误信息返回,并且错误信息在msg键中
	 * 
	 * @param message
	 *            提示信息
	 * @return Map
	 */
	public static final Map<String, Object> errorInfo(String message) {
		if (StringEmptyUtils.isEmpty(message)) {
			return errorInfo("未知错误！");
		}
		Map<String, Object> map = new HashMap<>();
		map.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		map.put(BaseCode.MSG.toString(), message);
		return map;
	}

	/**
	 * 错误返回信息带datas参数,注：该信息为错误信息与带参返回,并且错误信息在msg键中
	 * 
	 * @param message
	 *            提示信息
	 * @param datas
	 *            参数信息
	 * @return Map
	 */
	public static final Map<String, Object> errorInfo(String message, Object datas) {
		if (StringEmptyUtils.isEmpty(message)) {
			return errorInfo("未知错误,返回错误信息失败!");
		}
		Map<String, Object> map = new HashMap<>();
		map.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		map.put(BaseCode.MSG.toString(), message);
		map.put(BaseCode.DATAS.toString(), datas);
		return map;
	}

	/**
	 * 成功返回信息
	 * 
	 * @return Map
	 */
	public static final Map<String, Object> successInfo() {
		Map<String, Object> map = new HashMap<>();
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		map.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return map;
	}

	/**
	 * 返回成功信息,带Datas数据与总数
	 * 
	 * @param datas
	 *            返回参数
	 * @param count
	 *            总数
	 * @return Map
	 */
	public static final Map<String, Object> successDataInfo(Object datas, long count) {
		if (datas == null || count < 0) {
			return errorInfo("未知错误,返回错误信息失败!");
		}
		Map<String, Object> map = new HashMap<>();
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		map.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		map.put(BaseCode.DATAS.toString(), datas);
		map.put(BaseCode.TOTALCOUNT.toString(), count);
		return map;
	}

	/**
	 * 返回成功信息,单带Data数据
	 * 
	 * @param datas
	 *            返回参数
	 * @return Map
	 */
	public static final Map<String, Object> successDataInfo(Object datas) {
		if (datas == null) {
			return successInfo();
		}
		Map<String, Object> map = new HashMap<>();
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		map.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		map.put(BaseCode.DATAS.toString(), datas);
		return map;
	}

	/**
	 * 多个错误信息返回工具类,由于是多操作,故而有错也算作操作成功提示信息 注：通过List集合存放Map(错误信息)返回,并且错误信息在ERROR键中
	 * 
	 * @param errorList
	 *            错误信息集合
	 * @return Map
	 */
	public static final Map<String, Object> errorInfo(List<Map<String, Object>> errorList) {
		if (errorList == null) {
			return errorInfo("未知错误,返回错误信息失败!");
		} else if (errorList.isEmpty()) {
			return successInfo();
		}
		Map<String, Object> map = new HashMap<>();
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		map.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		map.put(BaseCode.ERROR.toString(), errorList);
		return map;
	}

	/**
	 * 多个错误信息返回工具类,由于是多操作,故而有错也算作操作成功提示信息,带总条数参数
	 * 注：通过List集合存放Map(错误信息)返回,并且错误信息在ERROR键中
	 * 
	 * @param errorList
	 *            错误信息集合
	 * @param totalCount
	 *            总条数
	 * @return Map
	 */
	public static final Map<String, Object> errorInfo(List<Map<String, Object>> errorList, int totalCount) {
		if (errorList == null) {
			return errorInfo("未知错误,返回错误信息失败!");
		} else if (errorList.isEmpty()) {
			return successInfo();
		}
		Map<String, Object> map = new HashMap<>();
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		map.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		map.put(BaseCode.ERROR.toString(), errorList);
		map.put(BaseCode.TOTALCOUNT.toString(), totalCount);
		return map;
	}
	
	/**
	 * 服务器报错返回信息
	 * @return Map
	 */
	public static final Map<String, Object> warnInfo() {
		Map<String, Object> map = new HashMap<>();
		map.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		map.put(BaseCode.MSG.toString(), "查询失败，服务器繁忙！");
		return map;
	}
	
}
