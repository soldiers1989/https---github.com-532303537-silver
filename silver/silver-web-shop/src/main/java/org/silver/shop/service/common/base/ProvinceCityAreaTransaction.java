package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.ProvinceCityAreaService;
import org.silver.shop.model.common.base.Area;
import org.silver.shop.model.common.base.City;
import org.silver.shop.model.common.base.Province;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 省市区 Transaction
 */
@Service("provinceCityAreaTransaction")
public class ProvinceCityAreaTransaction {

	@Reference
	private ProvinceCityAreaService provinceCityAreaService;

	/**
	 * 查询省市区三级联动
	 * 
	 * @return
	 */
	public List<Object> findProvinceCityArea() {
		List<Object> datasList = null;
		Map<String, Object> datasMap = null;
		String redisList = JedisUtil.get("Shop_Key_ProvinceCityArea_Map");
		if (StringEmptyUtils.isEmpty(redisList)) {// redis缓存没有数据
			datasMap = provinceCityAreaService.getProvinceCityArea();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if (status.equals("1")) {
				datasList = (List) datasMap.get(BaseCode.DATAS.toString());
				// 将查询出来的省市区放入到redis缓存中
				JedisUtil.setListDatas("Shop_Key_ProvinceCityArea_Map", 3600, datasList);
			}
		} else {
			// redis缓存中已有数据,直接返回数据
			return JSONArray.fromObject(redisList);
		}
		return JSONArray.fromObject(datasList);
	}

	public Object getProvince() {
		Map<String, Object> datasMap = new HashMap<>();
		List<Province> provinceList = null;
		byte[] redisByte = JedisUtil.get("Shop_Key_Province_Map".getBytes(), 3600);
		if (redisByte != null) {
			provinceList = (List<Province>) SerializeUtil.toObject(redisByte);
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			datasMap.put(BaseCode.DATAS.toString(), JSONObject.fromObject(provinceList));
			return datasMap;
		} else {
			datasMap = provinceCityAreaService.getProvince();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				List<Object> reList = (List) datasMap.get(BaseCode.DATAS.getBaseCode());
				if (reList != null && !reList.isEmpty()) {
					// 将查询出来的数据放入到缓存中
					JedisUtil.set("Shop_Key_Province_Map".getBytes(), SerializeUtil.toBytes(reList), 3600);
				}
			}
			return datasMap;
		}
	}

	public Object getCity() {
		Map<String, Object> datasMap = new HashMap<>();
		List<City> citylList = null;
		byte[] redisByte = JedisUtil.get("Shop_Key_City_Map".getBytes(), 3600);
		if (redisByte != null) {
			citylList = (List<City>) SerializeUtil.toObject(redisByte);
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			datasMap.put(BaseCode.DATAS.toString(), JSONObject.fromObject(citylList));
			return datasMap;
		} else {
			datasMap = provinceCityAreaService.getCity();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				List<Object> reList = (List) datasMap.get(BaseCode.DATAS.getBaseCode());
				if (reList != null && !reList.isEmpty()) {
					// 将查询出来的数据放入到缓存中
					JedisUtil.set("Shop_Key_City_Map".getBytes(), SerializeUtil.toBytes(reList), 3600);
				}
			}
			return datasMap;
		}
	}

	public Object getArea() {
		Map<String, Object> datasMap = new HashMap<>();
		List<Area> areaList = null;
		byte[] redisByte = JedisUtil.get("Shop_Key_Area_Map".getBytes(), 3600);
		if (redisByte != null) {
			areaList = (List<Area>) SerializeUtil.toObject(redisByte);
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			datasMap.put(BaseCode.DATAS.toString(), JSONObject.fromObject(areaList));
			return datasMap;
		} else {
			datasMap = provinceCityAreaService.getArea();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				List<Object> reList = (List) datasMap.get(BaseCode.DATAS.getBaseCode());
				if (reList != null && !reList.isEmpty()) {
					// 将查询出来的数据放入到缓存中
					JedisUtil.set("Shop_Key_Area_Map".getBytes(), SerializeUtil.toBytes(reList), 3600);
				}
			}
			return datasMap;
		}
	}

	/**
	 * 查询省市区(不封装对应三级联动)
	 * 
	 * @return
	 */
	public Object getProvinceCityArea() {
		Map<String, Object> datasMap = new HashMap<>();
		Map<String, Object> province = null;
		byte[] redisByte = JedisUtil.get("Shop_Key_Province_Map".getBytes(), 3600);
		if (redisByte != null) {
			province = (Map<String, Object>) SerializeUtil.toObject(redisByte);
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			datasMap.put(BaseCode.DATAS.toString(), JSONObject.fromObject(province));
			return datasMap;
		} else {
			datasMap = provinceCityAreaService.getProvinceCityArea2();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				com.alibaba.fastjson.JSONArray jsonObject = (com.alibaba.fastjson.JSONArray) datasMap
						.get(BaseCode.DATAS.getBaseCode());
				if (jsonObject != null && !jsonObject.isEmpty()) {
					Map<String, Object> item = new HashMap<>();
					for (int i = 0; i < jsonObject.size(); i++) {
						JSONObject provinceCityArea = JSONObject.fromObject(jsonObject.get(i));
						// 由于取出来是row数据,所以需要截取字符串
						item.put(
								provinceCityArea.getString("areaCode").replace("{\"value\":\"", "").replace("\"}",
										""),
								provinceCityArea.getString("provinceCode").replace("{\"value\":\"", "").replace("\"}",
										"")
										+ "_"
										+ provinceCityArea.getString("provinceName").replace("{\"value\":\"", "")
												.replace("\"}", "")
										+ "#"
										+ provinceCityArea.getString("cityCode").replace("{\"value\":\"", "")
												.replace("\"}", "").replace("{\"value\":\"", "").replace("\"}", "")
										+ "_"
										+ provinceCityArea.getString("cityName").replace("{\"value\":\"", "")
												.replace("\"}", "")
										+ "#"
										+ provinceCityArea.getString("areaCode").replace("{\"value\":\"", "")
												.replace("\"}", "")
										+ "_" + provinceCityArea.getString("areaName").replace("{\"value\":\"", "")
												.replace("\"}", ""));

					}
					// 将查询出来的数据放入到缓存中
					JedisUtil.set("Shop_Key_Province_Map".getBytes(), SerializeUtil.toBytes(item), 3600);
					datasMap.put(BaseCode.DATAS.toString(), item);
				}
			}
		}
		return datasMap;
	}

}
