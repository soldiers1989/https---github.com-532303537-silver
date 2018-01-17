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
	 * @param datas 参数
	 * @param noNullKeys 不能为空的Key
	 * @return Map
	 */
	public static Map<String, Object> checkData(JSONArray datas, List<String> noNullKeys ) {
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
							map.put("msg", "第" + (i+1) + "条数据 " + key + " 不能为空");
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
	 * 将校验的结果进行可读性修改
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
			if (msg.contains("shelfGName")) {
				msg = msg.replace("shelfGName", "商品上架名称");
			} else if (msg.contains("ncadCode")) {
				msg = msg.replace("ncadCode", "行邮税号");
			} else if (msg.contains("hsCode")) {
				msg = msg.replace("hsCode", "HS编码");
			} else if (msg.contains("goodsName")) {
				msg = msg.replace("goodsName", "商品名称");
			} else if (msg.contains("goodsStyle")) {
				msg = msg.replace("goodsStyle", "商品规格");
			} else if (msg.contains("brand")) {
				msg = msg.replace("brand", "品牌");
			} else if (msg.contains("gUnit")) {
				msg = msg.replace("gUnit", "申报计量单位");
			} else if (msg.contains("stdUnit")) {
				msg = msg.replace("stdUnit", "第一计量单位");
			} else if (msg.contains("regPrice")) {
				msg = msg.replace("regPrice", "单价");
			} else if (msg.contains("giftFlag")) {
				msg = msg.replace("giftFlag", "是否赠品标识");
			} else if (msg.contains("originCountry")) {
				msg = msg.replace("originCountry", "原产国");
			} else if (msg.contains("quality")) {
				msg = msg.replace("quality", "商品品质及说明");
			} else if (msg.contains("manufactory")) {
				msg = msg.replace("manufactory", "生产厂家或供应商");
			} else if (msg.contains("netWt")) {
				msg = msg.replace("netWt", "净重");
			} else if (msg.contains("grossWt")) {
				msg = msg.replace("grossWt", "毛重");
			} else if (msg.contains("ingredient")) {
				msg = msg.replace("ingredient", "成分");
			} else if (msg.contains("eportGoodsNo")) {
				msg = msg.replace("eportGoodsNo", "跨境公共平台商品备案申请号");
			} else if (msg.contains("ciqGoodsNo")) {
				msg = msg.replace("ciqGoodsNo", "检验检疫商品备案编号");
			} else if (msg.contains("cusGoodsNo")) {
				msg = msg.replace("cusGoodsNo", "海关正式备案编号");
			} else if (msg.contains("entGoodsNo")) {
				msg = msg.replace("entGoodsNo", "企业商品自编号");
			}
			reDataMap.put(BaseCode.MSG.toString(), msg);
			return reDataMap;
		}
	}
}
