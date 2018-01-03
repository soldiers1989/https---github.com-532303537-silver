package org.silver.shop.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.util.DateUtil;
import org.silver.util.StringEmptyUtils;

/**
 * 商城通用检索类
 *
 */
public final class SearchUtils {
	/**
	 * 通用检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalSearch(Map<String, Object> datasMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> lm = new ArrayList<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			switch (key) {
			case "goodsName":
				if (StringEmptyUtils.isNotEmpty(value)) {
					blurryMap.put(key, "%" + value + "%");
				}
				break;
			case "spareGoodsFirstTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "spareGoodsSecondTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "spareGoodsThirdTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "goodsFirstTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "goodsSecondTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "goodsThirdTypeId":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "startDate":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(DateUtil.parseDate(value + ""));
					Date startDate = cal.getTime();
					paramMap.put(key, startDate);
				}
				break;
			case "endDate":
				if (StringEmptyUtils.isNotEmpty(value)) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(DateUtil.parseDate(value + ""));
					cal.set(Calendar.HOUR, 23);
					cal.set(Calendar.MINUTE, 59);
					cal.set(Calendar.SECOND, 59);
					cal.set(Calendar.MILLISECOND, 999);
					Date endDate = cal.getTime();
					paramMap.put(key, endDate);
				}
				break;
			case "status":
				try {
					if (StringEmptyUtils.isEmpty(value)) {
						paramMap.remove(key);
					} else {
						int status = Integer.parseInt(value);
						paramMap.put(key, status);
					}
				} catch (Exception e) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "status参数错误,请重新输入!");
					return statusMap;
				}
				break;
			case "entOrderNo":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "sellFlag":
				int sellFlag = 0;
				try {
					sellFlag = Integer.parseInt(value);
				} catch (Exception e) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "上/下架标识参数错误,请重新输入!");
					return statusMap;
				}
				if (sellFlag > 0) {
					paramMap.put(key, sellFlag);
				}
				break;

			case "customsPort":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "customsCode":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "ciqOrgCode":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "warehouseCode":
				if (StringEmptyUtils.isNotEmpty(value)) {
					int one = value.indexOf('_');
					int two = value.indexOf('_', one + 1);
					// 截取MerchantId_00030_|5165| 第二个下划线后4位数为仓库码
					String code = value.substring(two + 1);
					paramMap.put(key, code);
				}
				break;
			case "merchantName":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "memberName":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "recordFlag":
				try {
					if (StringEmptyUtils.isEmpty(value)) {
						paramMap.remove(key);
					} else {
						int status = Integer.parseInt(value);
						paramMap.put(key, status);
					}
				} catch (Exception e) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "recordFlag参数错误,请重新输入!");
					return statusMap;
				}
				break;
			case "order_record_status":
				if (StringEmptyUtils.isNotEmpty(value) && Integer.parseInt(value) > 0) {
					paramMap.put("order_record_status", Integer.parseInt(value));
				}
				break;
			case "startTime":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put("startTime", DateUtil.parseDate2(value));
				}
				break;
			case "endTime":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put("endTime", DateUtil.parseDate2(value));
				}
				break;

			case "order_id":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "trade_no":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			default:
				break;
			}
		}
		statusMap.put("param", paramMap);
		statusMap.put("blurry", blurryMap);
		statusMap.put("error", lm);
		return statusMap;
	}
}
