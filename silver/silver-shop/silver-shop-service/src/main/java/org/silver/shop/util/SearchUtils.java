package org.silver.shop.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;

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
	 * 下划线命名商户Id
	 */
	private static final String MERCHANT_NO = "merchant_no";

	/**
	 * 下划线命名订单Id
	 */
	private static final String ORDER_ID = "order_id";

	/**
	 * 下划线命名删除标识
	 */
	private static final String DEL_FLAG = "del_flag";
	/**
	 * 参数名称
	 */
	private static final String PARAM = "param";

	/**
	 * 通用手工订单信息检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalMOrderSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为空!");
		}
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> orList = new ArrayList<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case ORDER_ID:
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
			case MERCHANT_NO:
				paramMap.put(key, value.trim());
				break;
			case DEL_FLAG:
				paramMap.put(key, Integer.parseInt(value));
				break;
			case "RecipientTel":// 收货人电话
				paramMap.put(key, value.trim());
				break;
			case "OrderDocId":// 下单人身份证号码
				paramMap.put(key, value.trim());
				break;
			case "thirdPartyId":// 第三方订单系统标识
				paramMap.put(key, value.trim());
				break;
			case "eport":// 口岸
				paramMap.put(key, value.trim());
				break;
			case "ciqOrgCode":// 国检检疫机构代码
				paramMap.put(key, value.trim());
				break;
			case "customsCode":// 海关关区代码
				paramMap.put(key, value.trim());
				break;
			case "order_record_status":
				Map<String, Object> orMap = null;
				if ("2".equals(value)) {
					// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
					orMap = new HashMap<>();
					orMap.put("order_record_status", 2);
					orList.add(orMap);
					orMap = new HashMap<>();
					orMap.put("order_record_status", 10);
					orList.add(orMap);
				} else {
					paramMap.put(key, Integer.parseInt(value));
				}
				break;
			default:
				break;
			}
		}
		statusMap.put(PARAM, paramMap);
		statusMap.put("blurry", blurryMap);
		statusMap.put("orList", orList);
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
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
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数集合不能为null");
		}
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case ORDER_ID:
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
				paramMap.put(key, value);
				break;
			case MERCHANT_NO:
				paramMap.put(key, value.trim());
				break;
			case DEL_FLAG:
				paramMap.put(key, Integer.parseInt(value.trim()));
				break;
			case "thirdPartyId":// 第三方业务Id
				paramMap.put(key, value.trim());
				break;
			case "eport":// 口岸
				paramMap.put(key, value.trim());
				break;
			case "ciqOrgCode":// 国检检疫机构代码
				paramMap.put(key, value.trim());
				break;
			case "customsCode":// 海关关区代码
				paramMap.put(key, value.trim());
				break;
			default:
				break;
			}
		}
		statusMap.put(PARAM, paramMap);
		statusMap.put("blurry", blurryMap);
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
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
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为空!");
		}
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
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
			case "agentId":
				paramMap.put(key, value);
				break;
			case "agentStatus":
				paramMap.put(key, value);
				break;
			default:
				break;
			}
		}
		statusMap.put(PARAM, paramMap);
		statusMap.put("blurry", blurryMap);
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
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
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为null");
		}

		Map<String, Object> viceParams = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
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
				viceParams.put(ORDER_ID, value);
				break;
			case "startDate":
				doDate(paramMap, viceParams, key, value);
				break;
			case "endDate":
				doDate(paramMap, viceParams, key, value);
				break;
			case "merchantId":
				paramMap.put(key, value);
				viceParams.put(MERCHANT_NO, value);
				break;
			case "orderTradingStatus":
				paramMap.put(key, value);
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put("viceParams", viceParams);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	private static void doDate(Map<String, Object> paramMap, Map<String, Object> viceParams, String key, String value) {
		paramMap.put(key, value);
		viceParams.put(key, value);
	}

	/**
	 * 商城商家后台库存通用检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalStockSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为null");
		}

		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "startDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "endDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "sellFlag":
				int sellFlag = 0;
				try {
					sellFlag = Integer.parseInt(value);
				} catch (Exception e) {
					return ReturnInfoUtils.errorInfo("上/下架标识参数格式错误！");
				}
				if (sellFlag > 0) {
					paramMap.put(key, sellFlag);
				}
				break;
			case "merchantId":
				paramMap.put(key, value);
				break;
			case "entGoodsNo":
				blurryMap.put(key, "%" + value + "%");
				//paramMap.put(key, value);
				break;
			case "warehouseCode":
				paramMap.put(key, value);
				break;
			case "goodsName":
				blurryMap.put(key, "%" + value + "%");
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put("blurry", blurryMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	/**
	 * 商城商家后台订单导入错误日志通用检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalOrderImplLogSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为空!");
		}
		Map<String, Object> blurryMap = null;
		Map<String, Object> paramMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "startDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "endDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "type":
				paramMap.put(key, value);
				break;
			case "operatorId":
				paramMap.put(key, value);
				break;
			case "action":
				paramMap.put(key, value);
				break;
			case "serialNo":
				paramMap.put(key, value);
				break;
			case "blurryStr":
				blurryMap = new HashMap<>();
				blurryMap.put("action", value);
				break;
			case "readingSign":
				paramMap.put(key, Integer.valueOf(value));
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put("blurry", blurryMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	/**
	 * 商城后台备案商品信息通用检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalRecordGoodsSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为空!");
		}

		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "startDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "endDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "goodsName":
				blurryMap.put(key, "%" + value + "%");
				break;
			case "status":
				try {
					int status = Integer.parseInt(value);
					paramMap.put(key, status);
				} catch (Exception e) {
					return ReturnInfoUtils.errorInfo("status参数错误,请重新输入!");
				}
				break;
			case "recordFlag":
				try {
					int recordFlag = Integer.parseInt(value);
					paramMap.put(key, recordFlag);
				} catch (Exception e) {
					return ReturnInfoUtils.errorInfo("recordFlag参数错误,请重新输入!");
				}
				break;
			case "customsPort":
				paramMap.put(key, value);
				break;
			case "spareGoodsFirstTypeId":
				paramMap.put(key, value);
				break;
			case "spareGoodsSecondTypeId":
				paramMap.put(key, value);
				break;
			case "spareGoodsThirdTypeId":
				paramMap.put(key, value);
				break;
			case "barCode":
				paramMap.put(key, value);
				break;
			case "entGoodsNo":
				paramMap.put(key, value);
				break;
			case "goodsMerchantId":
				paramMap.put(key, value);
				break;
			case "imageFlag":
				if ("1".equals(value)) {
					paramMap.put(key, " IS NULL ");
				} else if ("2".equals(value)) {
					paramMap.put(key, " IS NOT NULL ");
				}
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put("blurry", blurryMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	/**
	 * 商城前端用户通用订单检索方法
	 * 
	 * @param datasMap
	 *            参数Map
	 * @return Map
	 */
	public static final Map<String, Object> universalMemberOrderSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为空!");
		}
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "startDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "endDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "goodsName":
				blurryMap.put(key, "%" + value + "%");
				break;
			case "entOrderNo":
				paramMap.put(key, value);
				break;
			default:
				break;
			}
		}
		statusMap.put(PARAM, paramMap);
		statusMap.put("blurry", blurryMap);
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	/**
	 * 通用检索商户口岸费率
	 * 
	 * @param datasMap
	 * @return
	 */
	public static Map<String, Object> universalMerchantFeeSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数错误,请重新输入!");
		}
		Map<String, Object> paramMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "merchantId":
				paramMap.put(key, value);
				break;
			case "type":
				paramMap.put(key, value);
				break;
			case "platformFee":
				paramMap.put(key, Double.parseDouble(value));
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	/**
	 * 通用检索商户口岸费率
	 * 
	 * @param datasMap
	 * @return
	 */
	public static Map<String, Object> universalMemberSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数错误,请重新输入!");
		}
		Map<String, Object> paramMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "memberId":
				paramMap.put(key, value);
				break;
			case "memberTel":
				paramMap.put(key, value);
				break;
			case "memberIdCardName":
				paramMap.put(key, value);
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	/**
	 * 通用检索商户钱包工具类
	 * 
	 * @param datasMap
	 * @return
	 */
	public static Map<String, Object> universalMerchantWalletSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数错误,请重新输入!");
		}
		Map<String, Object> paramMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "merchantId":
				paramMap.put(key, value);
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	/**
	 * 通用检索商户身份证实名认证记录工具类
	 * 
	 * @param datasMap
	 * @return
	 */
	public static Map<String, Object> universalIdCardCertificationlogsSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为null");
		}
		Map<String, Object> paramMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "merchantId":
				paramMap.put(key, value);
				break;
			case "startDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "endDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "idNumber":
				paramMap.put(key, value);
				break;
			case "name":
				paramMap.put(key, value);
				break;
			case "tollFlag":
				try {
					paramMap.put(key, Integer.parseInt(value));
				} catch (Exception e) {
					return ReturnInfoUtils.errorInfo("收费标识错误!");
				}
				break;
			default:
				break;
			}
		}
		Map<String, Object> item = new HashMap<>();
		item.put(PARAM, paramMap);
		item.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return item;
	}

	/**
	 * 通用检索身份证实名库工具类
	 * 
	 * @param datasMap
	 * @return
	 */
	public static Map<String, Object> universalIdCardSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为null");
		}
		Map<String, Object> paramMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "merchantId":
				paramMap.put(key, value);
				break;
			case "startDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "endDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "idNumber":
				paramMap.put(key, value);
				break;
			case "name":
				paramMap.put(key, value);
				break;
			case "type":
				try {
					paramMap.put(key, Integer.parseInt(value));
				} catch (Exception e) {
					return ReturnInfoUtils.errorInfo("类型参数格式错误!");
				}
				break;
			case "status":
				paramMap.put(key, value);
				break;
			case "certifiedNo":
				paramMap.put(key, value);
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	public static Map<String, Object> universalOfflineRechargeSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为null");
		}
		Map<String, Object> paramMap = new HashMap<>();
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "applicantId":
				paramMap.put(key, value);
				break;
			case "reviewerType":
				paramMap.put(key, value);
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	/**
	 * 通用检索商户信息
	 * 
	 * @param datasMap
	 * @return
	 */
	public static Map<String, Object> universalMerchantSearch(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数错误,请重新输入!");
		}
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> blurryMap = null;
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "merchantName":
				blurryMap = new HashMap<>();
				blurryMap.put(key, "%" + value + "%");
				break;
			case "merchantStatus":
				paramMap.put(key, value.trim());
				break;
			case "startDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			case "endDate":
				paramMap.put(key, DateUtil.parseDate2(value));
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put("blurry", blurryMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}

	/**
	 * 通用检索商品风控信息
	 * 
	 * @param datasMap
	 * @return
	 */
	public static Map<String, Object> goodsRiskControl(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数错误,请重新输入!");
		}
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> blurryMap = null;
		Iterator<String> isKey = datasMap.keySet().iterator();
		while (isKey.hasNext()) {
			String key = isKey.next().trim();
			String value = datasMap.get(key) + "".trim();
			// value值为空时不需要添加检索参数
			if (StringEmptyUtils.isEmpty(value)) {
				continue;
			}
			switch (key) {
			case "goodsName":
				blurryMap = new HashMap<>();
				blurryMap.put(key, "%" + value + "%");
				break;
			case "hsCode":
				paramMap.put(key, value.trim());
				break;
			case "goodsStyle":
				paramMap.put(key, value.trim());
				break;
			case "goodsBrand":
				paramMap.put(key, value.trim());
				break;
			case "regPrice":
				paramMap.put(key, Double.parseDouble(value));
				break;
			default:
				break;
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put(PARAM, paramMap);
		map.put("blurry", blurryMap);
		map.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return map;
	}
	
}
