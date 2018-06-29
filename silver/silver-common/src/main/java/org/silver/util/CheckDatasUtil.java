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
		List<JSONObject> dataList = new ArrayList<>();
		if (datas != null && !datas.isEmpty()) {
			JSONObject jsonObject = null;
			String key, value, result;
			for (int i = 0; i < datas.size(); i++) {
				jsonObject = (JSONObject) datas.get(i);
				Iterator it = jsonObject.keys();
				while (it.hasNext()) {
					key = it.next() + "";
					value = jsonObject.get(key) + "";
					if (("null".equals(value) || "".equals(value.trim())) && noNullKeys.contains(key)) {
						return ReturnInfoUtils.errorInfo("第" + (i + 1) + "条数据 " + key + " 不能为空!");
					}
					noNullKeys.remove(key);
				}
				dataList.add(jsonObject);
			}
			if (!noNullKeys.isEmpty()) {
				return ReturnInfoUtils.errorInfo(noNullKeys.get(0) + ":不能为空!");
			}
			return ReturnInfoUtils.successDataInfo(dataList);
		}
		return ReturnInfoUtils.errorInfo("非法数据!");
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
				msg = msg.replace("shelfgname", "[shelfgName]商品上架名称");
			} else if (msg.contains("ncadcode")) {
				msg = msg.replace("ncadcode", "[ncadCode]行邮税号");
			} else if (msg.contains("hscode")) {
				msg = msg.replace("hscode", "[HSCode]HS编码");
			} else if (msg.contains("barcode")) {
				msg = msg.replace("barcode", "[barCode]条形码");
			} else if (msg.contains("goodsname")) {
				msg = msg.replace("goodsname", "[GoodsName]商品名称");
			} else if (msg.contains("goodsstyle")) {
				msg = msg.replace("goodsstyle", "[GoodsStyle]商品规格");
			} else if (msg.contains("brand")) {
				msg = msg.replace("brand", "[Brand]品牌");
			} else if (msg.contains("gunit")) {
				msg = msg.replace("gunit", "[gunit]申报计量单位");
			} else if (msg.contains("stdunit")) {
				msg = msg.replace("stdunit", "[stdunit]第一计量单位");
			} else if (msg.contains("regprice")) {
				msg = msg.replace("regprice", "[regprice]单价");
			} else if (msg.contains("giftflag")) {
				msg = msg.replace("giftflag", "[giftflag]是否赠品标识");
			} else if (msg.contains("origincountry")) {
				msg = msg.replace("origincountry", "[OriginCountry]原产国");
			} else if (msg.contains("quality")) {
				msg = msg.replace("quality", "[quality]商品品质及说明");
			} else if (msg.contains("manufactory")) {
				msg = msg.replace("manufactory", "[manufactory]生产厂家或供应商");
			} else if (msg.contains("netwt")) {
				msg = msg.replace("netwt", "[netwt]净重");
			} else if (msg.contains("grosswt")) {
				msg = msg.replace("grosswt", "[grosswt]毛重");
			} else if (msg.contains("ingredient")) {
				msg = msg.replace("ingredient", "[ingredient]成分");
			} else if (msg.contains("eportgoodsno")) {
				msg = msg.replace("eportgoodsno", "[eportGoodsNo]跨境公共平台商品备案申请号");
			} else if (msg.contains("ciqgoodsno")) {
				msg = msg.replace("ciqgoodsno", "[CIQGoodsNo]检验检疫商品备案编号");
			} else if (msg.contains("cusgoodsno")) {
				msg = msg.replace("cusgoodsno", "[CusGoodsNo]海关正式备案编号");
			} else if (msg.contains("entgoodsno")) {
				msg = msg.replace("entgoodsno", "[EntGoodsNo]企业商品自编号");
			} else if (msg.contains("qty")) {
				msg = msg.replace("qty", "[Qty]数量");
			} else if (msg.contains("unit")) {
				msg = msg.replace("unit", "[Unit]计量单位");
			} else if (msg.contains("seq")) {
				msg = msg.replace("seq", "[Seq]商品序号");
			} else if (msg.contains("total")) {
				msg = msg.replace("total", "[Total]商品总价");
			} else if (msg.contains("currcode")) {
				msg = msg.replace("currcode", "[CurrCode]币制");
			}
			return ReturnInfoUtils.errorInfo(msg);
		}
	}

	/**
	 * 根据对应的订单信息,将参数名称修改为对应的中文名称方便阅读
	 * 
	 * @param jsonList
	 * @param noNullKeys
	 * @return Map
	 */
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
			if (msg.contains("entorderno")) {
				msg = msg.replace("entorderno", "[EntOrderNo]订单编号");
			} else if (msg.contains("orderstatus")) {
				msg = msg.replace("orderstatus", "[OrderStatus]电子订单状态");
			} else if (msg.contains("paystatus")) {
				msg = msg.replace("paystatus", "[PayStatus]支付状态");
			} else if (msg.contains("ordergoodtotal")) {
				msg = msg.replace("ordergoodtotal", "[OrderGoodTotal]订单商品总额");
			} else if (msg.contains("ordergoodtotalcurr")) {
				msg = msg.replace("ordergoodtotalcurr", "[OrderGoodTotalCurr]订单商品总额币制");
			} else if (msg.contains("freight")) {
				msg = msg.replace("freight", "[Freight]订单运费");
			} else if (msg.contains("tax")) {
				msg = msg.replace("tax", "[Tax]税款");
			} else if (msg.contains("otherpayment")) {
				msg = msg.replace("otherpayment", "[OtherPayment]抵付金额");
			} else if (msg.contains("actualamountpaid")) {
				msg = msg.replace("actualamountpaid", "[ActualAmountPaid]实际支付金额");
			} else if (msg.contains("recipientname")) {
				msg = msg.replace("recipientname", "[RecipientName]收货人名称");
			} else if (msg.contains("recipientaddr")) {
				msg = msg.replace("recipientaddr", "[RecipientAddr]收货人地址");
			} else if (msg.contains("recipienttel")) {
				msg = msg.replace("recipienttel", "[RecipientTel]收货人电话");
			} else if (msg.contains("recipientcountry")) {
				msg = msg.replace("recipientcountry", "[RecipientCountry]收货人所在国");
			} else if (msg.contains("recipientprovincescode")) {
				msg = msg.replace("recipientprovincescode", "[RecipientProvincesCode]收货人行政区代码");
			} else if (msg.contains("orderdocacount")) {
				msg = msg.replace("orderdocacount", "[OrderDocAcount]下单人账户");
			} else if (msg.contains("orderdocname")) {
				msg = msg.replace("orderdocname", "[OrderDocName]下单人姓名");
			} else if (msg.contains("orderdoctype")) {
				msg = msg.replace("orderdoctype", "[OrderDocType]下单人证件类型");
			} else if (msg.contains("orderdocid")) {
				msg = msg.replace("orderdocid", "[OrderDocId]下单人证件号");
			} else if (msg.contains("orderdoctel")) {
				msg = msg.replace("orderdoctel", "[OrderDocTel]下单人电话");
			} else if (msg.contains("orderdate")) {
				msg = msg.replace("orderdate", "[OrderDate]订单日期");
			} else if (msg.contains("eport")) {
				msg = msg.replace("eport", "[eport]口岸标识");
			} else if (msg.contains("ciqorgcode")) {
				msg = msg.replace("ciqorgcode", "[ciqOrgCode]国检检疫机构编码");
			} else if (msg.contains("customscode")) {
				msg = msg.replace("customscode", "[customsCode]海关关区编码");
			}
			reDataMap.put(BaseCode.MSG.toString(), msg);
			return reDataMap;
		}
	}

}
