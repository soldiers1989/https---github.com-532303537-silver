package org.silver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CheckDatasUtil {

	/**
	 * 校验JSONArray中的键不能为空
	 * 
	 * @param datas
	 *            参数
	 * @param noNullKeys
	 *            不能为空的Key
	 * @return Map
	 */
	public static Map<String, Object> checkData(JSONArray datas, List<String> noNullKeys) {
		Map<String, Object> map = new HashMap<>();
		List<JSONObject> dataList = new ArrayList<>();
		if (datas != null && datas.size() > 0) {
			JSONObject jsonObject = null;
			String key, value, result;
			for (int i = 0; i < datas.size(); i++) {
				jsonObject = (JSONObject) datas.get(i);
				Iterator it = jsonObject.keys();
				while (it.hasNext()) {
					key = it.next() + "";
					value = jsonObject.get(key) + "";
					if (("null".equals(value) || "".equals(value.trim())) && noNullKeys.contains(key)) {
						map.put("status", -1);
						map.put("msg", "第" + (i + 1) + "条数据 " + key + " 不能为空");
						return map;
					}
					noNullKeys.remove(key);
				}
				dataList.add(jsonObject);
			}
			if (noNullKeys.size() > 0) {
				map.put("status", -2);
				map.put("msg", noNullKeys.get(0) + ":不能为空");
				return map;
			}
			map.put("status", 1);
			map.put("datas", dataList);
			map.put("msg", "数据校验通过");
			return map;
		}
		map.put("status", -3);
		map.put("msg", "非法数据");
		return map;
	}

	/**
	 * 将校验的商品信息结果进行可读性修改
	 * 
	 * @param jsonList
	 *            参数
	 * @param noNullKeys
	 *            不可空参数
	 * @return Map 修改后的Msg
	 */
	public static final Map<String, Object> changeMsg(JSONArray jsonList, List<String> noNullKeys) {
		Map<String, Object> reDataMap = CheckDatasUtil.checkData(jsonList, noNullKeys);
		String msg = "";
		if ("1".equals(reDataMap.get(BaseCode.STATUS.toString()))) {
			return reDataMap;
		} else {
			msg = reDataMap.get(BaseCode.MSG.toString()) + "";
			msg = msg.replace("条数据", "个商品");
			// 将所有key值转换为小写,实现通用
			msg = msg.toLowerCase();
			if (msg.contains("shelfgname")) {
				msg = msg.replace("shelfgname", "商品上架名称");
			} else if (msg.contains("ncadcode")) {
				msg = msg.replace("ncadcode", "行邮税号");
			} else if (msg.contains("hscode")) {
				msg = msg.replace("hscode", "HS编码");
			} else if (msg.contains("goodsname")) {
				msg = msg.replace("goodsname", "商品名称");
			} else if (msg.contains("goodsstyle")) {
				msg = msg.replace("goodsstyle", "商品规格");
			} else if (msg.contains("brand")) {
				msg = msg.replace("brand", "品牌");
			} else if (msg.contains("gunit")) {
				msg = msg.replace("gunit", "申报计量单位");
			} else if (msg.contains("stdunit")) {
				msg = msg.replace("stdunit", "第一计量单位");
			} else if (msg.contains("regprice")) {
				msg = msg.replace("regprice", "单价");
			} else if (msg.contains("giftflag")) {
				msg = msg.replace("giftflag", "是否赠品标识");
			} else if (msg.contains("origincountry")) {
				msg = msg.replace("origincountry", "原产国");
			} else if (msg.contains("quality")) {
				msg = msg.replace("quality", "商品品质及说明");
			} else if (msg.contains("manufactory")) {
				msg = msg.replace("manufactory", "生产厂家或供应商");
			} else if (msg.contains("netwt")) {
				msg = msg.replace("netwt", "净重");
			} else if (msg.contains("grosswt")) {
				msg = msg.replace("grosswt", "毛重");
			} else if (msg.contains("ingredient")) {
				msg = msg.replace("ingredient", "成分");
			} else if (msg.contains("eportgoodsno")) {
				msg = msg.replace("eportgoodsno", "跨境公共平台商品备案申请号");
			} else if (msg.contains("ciqgoodsno")) {
				msg = msg.replace("ciqgoodsno", "检验检疫商品备案编号");
			} else if (msg.contains("cusgoodsno")) {
				msg = msg.replace("cusgoodsno", "海关正式备案编号");
			} else if (msg.contains("entgoodsno")) {
				msg = msg.replace("entgoodsno", "企业商品自编号");
			} else if (msg.contains("qty")) {
				msg = msg.replace("qty", "数量");
			} else if (msg.contains("unit")) {
				msg = msg.replace("unit", "计量单位");
			} else if (msg.contains("seq")) {
				msg = msg.replace("seq", "商品序号");
			} else if (msg.contains("total")) {
				msg = msg.replace("total", "商品总价");
			} else if (msg.contains("currcode")) {
				msg = msg.replace("currcode", "币制");
			}
			reDataMap.put(BaseCode.MSG.toString(), msg);
			return reDataMap;
		}
	}

	public static Map<String, Object> changeOrderMsg(JSONArray jsonList, List<String> noNullKeys) {
		Map<String, Object> reDataMap = CheckDatasUtil.checkData(jsonList, noNullKeys);
		String msg = "";
		if ("1".equals(reDataMap.get(BaseCode.STATUS.toString()))) {
			return reDataMap;
		} else {
			msg = reDataMap.get(BaseCode.MSG.toString()) + "";
			 msg = msg.replace("第1条数据", "");
			// 将所有key值转换为小写,实现通用
			msg = msg.toLowerCase();
			if (msg.contains("EntOrderNo")) {
				msg = msg.replace("EntOrderNo", "订单编号");
			} else if (msg.contains("OrderStatus")) {
				msg = msg.replace("OrderStatus", "电子订单状态");
			} else if (msg.contains("PayStatus")) {
				msg = msg.replace("PayStatus", "支付状态");
			} else if (msg.contains("OrderGoodTotal")) {
				msg = msg.replace("OrderGoodTotal", "订单商品总额");
			} else if (msg.contains("OrderGoodTotalCurr")) {
				msg = msg.replace("OrderGoodTotalCurr", "订单商品总额币制");
			} else if (msg.contains("Freight")) {
				msg = msg.replace("Freight", "订单运费");
			} else if (msg.contains("Tax")) {
				msg = msg.replace("Tax", "税款");
			} else if (msg.contains("OtherPayment")) {
				msg = msg.replace("OtherPayment", "抵付金额");
			} else if (msg.contains("ActualAmountPaid")) {
				msg = msg.replace("ActualAmountPaid", "实际支付金额");
			} else if (msg.contains("RecipientName")) {
				msg = msg.replace("RecipientName", "收货人名称");
			} else if (msg.contains("RecipientAddr")) {
				msg = msg.replace("RecipientAddr", "收货人地址");
			} else if (msg.contains("RecipientTel")) {
				msg = msg.replace("RecipientTel", "收货人电话");
			} else if (msg.contains("RecipientCountry")) {
				msg = msg.replace("RecipientCountry", "收货人所在国");
			} else if (msg.contains("RecipientProvincesCode")) {
				msg = msg.replace("RecipientProvincesCode", "收货人行政区代码");
			} else if (msg.contains("OrderDocAcount")) {
				msg = msg.replace("OrderDocAcount", "下单人账户");
			} else if (msg.contains("OrderDocName")) {
				msg = msg.replace("OrderDocName", "下单人姓名");
			} else if (msg.contains("OrderDocType")) {
				msg = msg.replace("OrderDocType", "下单人证件类型");
			} else if (msg.contains("OrderDocId")) {
				msg = msg.replace("OrderDocId", "下单人证件号");
			} else if (msg.contains("OrderDocTel")) {
				msg = msg.replace("OrderDocTel", "下单人电话");
			} else if (msg.contains("OrderDate")) {
				msg = msg.replace("OrderDate", "订单日期");
			}
			reDataMap.put(BaseCode.MSG.toString(), msg);
			return reDataMap;
		}
	}

}
