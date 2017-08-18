package org.silver.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.silver.sys.api.NSEportService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("checkDatasService")
public class CheckDatasService {

	@Reference
	private NSEportService nSEportService;
	
	public Map<String, Object> checkDatas(HttpServletRequest req) {
		Map<String, Object> statusMap = new HashMap<String, Object>();
		String entName = req.getParameter("entName");// 公司名
		String list = req.getParameter("list");// 需备案的数据列表
		String typeStr = req.getParameter("type");// 需要报关的操作类型 // 0：商品备案，1：订单备案，2：支付备案											
		int type = 0;
		if (typeStr != null && list != null) {
			JSONArray j = JSONArray.fromObject(list);
			try {
				type = Integer.parseInt(typeStr);
				switch (type) {
				case 0:
					statusMap = checkGoodsDatas(j, statusMap);
					System.out.println(statusMap);
					if((int)statusMap.get("status")!=1){
						return statusMap;
					}
					
					nSEportService.createEportXML(type,statusMap.get("datas"));
					
					
					break;
				case 1:
                    break;
				case 2:
					break;
				default:
					statusMap.put("status", -4);
					statusMap.put("msg", "未知的业务类型");
					return statusMap;
				}

				nSEportService.saveEportData();
				//nSEportService.pushEportData();
				
			} catch (NumberFormatException e) {
				statusMap.put("status", -2);
				statusMap.put("msg", "错误的type类型");
				return statusMap;
			}
		}
		statusMap.put("status", -3);
		statusMap.put("msg", "非法请求");
		return statusMap;
	}

	/**
	 * 备案数据校验 
	 * @param records 
	 * @param map
	 * @return 效验结果码  1 成功     其他 失败并返回错误提示
	 */
	private Map<String, Object> checkGoodsDatas(JSONArray records, Map<String, Object> map) {
		JSONObject record = null;
		String result = "";
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		System.out.println(records.get(0));
		for (int i = 0; i < records.size(); i++) {
			record = records.getJSONObject(i);
			Map<String, Object> hashMap = new HashMap<String, Object>();
			String Seq=record.get("Seq")+"";
			if (" ".equals(Seq) || Seq.isEmpty()||"null".equals(Seq)) {
				result = result + "Seq:的值不能为空;";

			} else {
				hashMap.put("Seq", Seq);
			}
			String Gcode = record.get("EntGoodsNo")+"";// 商品货号
			if (" ".equals(Gcode) || Gcode.isEmpty()) {
				result = result + "Gcode:的值不能为空;";

			} else {
				hashMap.put("EntGoodsNo", Gcode);
			}
			String Gname = record.get("ShelfGName")+"";// 商品名称
			if (" ".equals(Gname) || Gname.isEmpty()) {
				result = result + "Gname:的值不能为空;";

			} else {
				hashMap.put("ShelfGName", Gname);
			}
			String Spec = record.get("Spec")+"";// 规格型号
			if (" ".equals(Spec) || Spec.isEmpty()) {
				result = result + "Spec:的值不能为空;";

			} else {
				hashMap.put("Spec", Spec);
			}
			String Hscode = record.get("HSCode")+"";// 商品HS编码
			if (" ".equals(Hscode) || Hscode.isEmpty()||"null".equals(Hscode)) {
				result = result + "Hscode:的值不能为空;";

			} else {
				hashMap.put("HSCode", Hscode);
			}
			String Unit = record.get("Unit")+"";// 计量单位(最小)
			if (" ".equals(Unit) || Unit.isEmpty()) {
				result = result + "Unit:的值不能为空;";

			} else {
				hashMap.put("Unit", Unit);
			}
			String Brand = record.get("Brand")+"";
			if (" ".equals(Brand) || Brand.isEmpty()||"null".equals(Brand)) {
				result = result + "Brand:的值不能为空;";

			} else {
				hashMap.put("Brand", Brand);
			}
			String AssemCountry = record.get("AssemCountry")+"";
			System.out.println(AssemCountry+"--->AssemCountry");
			if (" ".equals(AssemCountry) || AssemCountry.isEmpty()||"null".equals(AssemCountry)) {
				result = result + "AssemCountry:的值不能为空;";

			} else {
				hashMap.put("AssemCountry", AssemCountry);
			}
			String GoodsBarcode = record.get("GoodsBarcode")+"";// 商品条形码
			hashMap.put("GoodsBarcode", GoodsBarcode);
			String GoodsDesc = record.get("GoodsDesc")+"";// 商品描述
			hashMap.put("GoodsDesc", GoodsDesc);
			String Remark = record.get("Remark")+"";// 备注
			hashMap.put("Remark", Remark);
			String ComName = record.get("Manufactory")+"";// 生产厂家
			hashMap.put("ComName", ComName);
			String Manufactureradd = record.get("Manufactureraddr")+"";// 生产厂家地址（食品类商品必填）
			hashMap.put("Manufactureradd", Manufactureradd);
			String Ingredient = record.get("Ingredient")+"";// 成分 为空时默认“无”
			if (Ingredient.isEmpty()) {
				hashMap.put("Ingredient", "无");
			} else {
				hashMap.put("Ingredient", Ingredient);
			}
			String Additiveflag = record.get("Additiveflag")+"";// 超范围使用食品添加剂,为空时默认“无”
			if (Additiveflag.isEmpty()) {
				hashMap.put("Additiveflag", "无");
			} else {
				hashMap.put("Additiveflag", Additiveflag);
			}
			String Poisonflag = record.get("Poisonflag")+"";// 含有毒害物质,为空时默认“无”
			if (Poisonflag.isEmpty()) {
				hashMap.put("Poisonflag", "无");
			} else {
				hashMap.put("Poisonflag", Poisonflag);
			}
			list.add(hashMap);
		}
		if (result.isEmpty()) {
			map.put("status", 1);
			map.put("msg", "校验报文完成");
			map.put("datas", list);
			return map;
		}
		map.put("status", 1);
		map.put("msg", "不合法的报文");
		map.put("err", result);
		map.put("datas", list);
		return map;
	}

}
