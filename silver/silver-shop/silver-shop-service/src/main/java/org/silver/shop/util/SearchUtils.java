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

import com.alibaba.dubbo.container.Main;

/**
 * 商城通用检索类
 *
 */
public final class SearchUtils {

	/**
	 * 手工订单生成的支付流水号字段名称
	 */
	private static final String TRADENO = "trade_no";

	/**
	 * 兼容另一种命名-开始时间
	 */
	private static final String STARTTIME = "startTime";

	/**
	 * 兼容另一种命名-结束时间
	 */
	private static final String ENDTIME = "endTime";

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
					/*
					 * Calendar cal = Calendar.getInstance();
					 * cal.setTime(DateUtil.parseDate(value + "")); Date
					 * startDate = cal.getTime();
					 */
					paramMap.put(key, DateUtil.parseDate2(value + ""));
				}
				break;
			case "endDate":
				if (StringEmptyUtils.isNotEmpty(value)) {
					/*
					 * Calendar cal = Calendar.getInstance();
					 * cal.setTime(DateUtil.parseDate(value + ""));
					 * cal.set(Calendar.HOUR, 23); cal.set(Calendar.MINUTE, 59);
					 * cal.set(Calendar.SECOND, 59);
					 * cal.set(Calendar.MILLISECOND, 999); Date endDate =
					 * cal.getTime();
					 */
					paramMap.put(key, DateUtil.parseDate2(value + ""));
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
					paramMap.put(key, value.trim());
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
			case STARTTIME:
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, DateUtil.parseDate2(value));
				}
				break;
			case ENDTIME:
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, DateUtil.parseDate2(value));
				}
				break;
			case "type":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, Integer.parseInt(value));
				}
				break;

			case "action":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "serialNo":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "entGoodsNo":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "merchantStatus":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value.trim());
				}
				break;
			case "del_flag":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, Integer.parseInt(value));
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

	/**
	 * 通用手工订单信息检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalMOrderSearch(Map<String, Object> datasMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> lm = new ArrayList<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "order_id":
				paramMap.put(key, value);
				break;
			case TRADENO:
				paramMap.put(key, value);
				break;
			case "waybill":
				paramMap.put(key, value);
				break;
			case STARTTIME:
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case ENDTIME:
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "tradeNoFlag":
				if ("1".equals(value)) {
					// 支付流水为空
					paramMap.put(key, " IS NULL");
				} else if ("2".equals(value)) {
					// 支付流水不为空
					paramMap.put(key, " IS NOT NULL ");
				}
				break;
			case "order_record_status":
				paramMap.put(key, Integer.parseInt(value));
				break;
			case "merchant_no":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value.trim());
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

	/**
	 * 通用手工支付单信息检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalMPaymentSearch(Map<String, Object> datasMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> lm = new ArrayList<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "order_id":
				paramMap.put(key, value);
				break;
			case TRADENO:
				paramMap.put(key, value);
				break;
			case "waybill":
				paramMap.put(key, value);
				break;
			case "startTime":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "endTime":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "pay_record_status":
				paramMap.put(key, Integer.parseInt(value));
				break;
			case "morder_id":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value);
				}
				break;
			case "merchant_no":
				if (StringEmptyUtils.isNotEmpty(value)) {
					paramMap.put(key, value.trim());
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

	/**
	 * 通用代理商检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalAgentSearch(Map<String, Object> datasMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> lm = new ArrayList<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "agentName":
				paramMap.put(key, value);
				break;
			case "agentStatus":
				paramMap.put(key, value);
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

	/**
	 * 商家商家后台订单通用检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalMerchantOrderSearch(Map<String, Object> datasMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> viceParams = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> lm = new ArrayList<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "entOrderNo":
				paramMap.put(key, value);
				viceParams.put("order_id", value);
				break;
			case "createDate":
				paramMap.put(key, value);
				viceParams.put("OrderDate", value);
				break;
			case "endDate":
				paramMap.put(key, value);
				viceParams.put("OrderDate", value);
				break;
			case "merchantId":
				paramMap.put(key, value);
				viceParams.put("merchant_no", value);
				break;
		/*	case "deleteFlag":
				paramMap.put(key, value);
				viceParams.put("del_flag", value);
				break;*/
			default:
				break;
			}
		}
		statusMap.put("param", paramMap);
		statusMap.put("viceParams", viceParams);
		statusMap.put("error", lm);
		return statusMap;
	}
}
