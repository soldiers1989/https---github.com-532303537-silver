package org.silver.shop.impl.common.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.ProvinceCityAreaService;
import org.silver.shop.dao.common.base.ProvinceCityAreaDao;
import org.silver.shop.dao.common.base.impl.ProvinceCityAreaDaoImpl;
import org.silver.shop.model.common.base.Area;
import org.silver.shop.model.common.base.City;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.common.base.Province;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONObject;

@Service(interfaceClass = ProvinceCityAreaService.class)
public class ProvinceCityAreaServiceImpl implements ProvinceCityAreaService {

	@Autowired
	private ProvinceCityAreaDao provinceCityAreaDao = new ProvinceCityAreaDaoImpl();

	@Override
	public Map<String, Object> getProvinceCityArea() {
		Map<String, Object> statusMap = new HashMap<>();
		List<Object> provinceList = new ArrayList<>();
		Map<String, Map<String, Map<String, Object>>> provinceMap = new HashMap<>();
		Map<String, Map<String, Object>> cityMap = null;
		Map<String, Object> areaMap = null;
		// 查询省市区
		Table table = provinceCityAreaDao.findAllProvinceCityArea();
		if (table != null && table.getRows().size() > 0) {
			List<Row> lr = table.getRows();
			for (int i = 0; i < lr.size(); i++) {
				areaMap = new HashMap<>();
				String areaCode = lr.get(i).getValue("areaCode") + "";
				String areaName = lr.get(i).getValue("areaName") + "";
				String cityCode = lr.get(i).getValue("cityCode") + "";
				String cityName = lr.get(i).getValue("cityName") + "";
				String provinceCode = lr.get(i).getValue("provinceCode") + "";
				String provinceName = lr.get(i).getValue("provinceName") + "";
				if (provinceMap != null && provinceMap.get(provinceName + "_" + provinceCode) != null) {
					String cityKey = provinceMap.get(provinceName + "_" + provinceCode).get(cityName + "_" + cityCode)
							+ "";
					if (cityKey != null && !cityKey.equals("null")) {
						cityMap = new HashMap<>();
						provinceMap.get(provinceName + "_" + provinceCode).get(cityName + "_" + cityCode).put(areaCode,
								areaName);
					} else {
						areaMap.put(areaCode, areaName);
						provinceMap.get(provinceName + "_" + provinceCode).put(cityName + "_" + cityCode, areaMap);

					}
				} else {// 省份不存在时
					provinceMap = new HashMap<>();
					if (areaCode != null && !areaCode.trim().equals("null")) {
						cityMap = new HashMap<>();
						areaMap.put(areaCode, areaName);
						cityMap.put(cityName + "_" + cityCode, areaMap);
					} else {// 当省份下没有城市时
						cityMap = new HashMap<>();
						areaMap.put("", "");
						cityMap.put("", areaMap);
					}
					provinceMap.put(provinceName + "_" + provinceCode, cityMap);
					provinceList.add(provinceMap);
				}
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), provinceList);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		}
		return statusMap;
	}

	@Override
	public Map<String, Object> getProvince() {
		Map<String, Object> reMap = new HashMap<>();
		List<Object> dataList = provinceCityAreaDao.findAll(Province.class, 0, 0);
		if (dataList != null && dataList.size() > 0) {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reMap.put(BaseCode.DATAS.toString(), dataList);
			reMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			reMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return reMap;
	}

	@Override
	public Map<String, Object> getCity() {
		Map<String, Object> reMap = new HashMap<>();
		List<Object> dataList = provinceCityAreaDao.findAll(City.class, 0, 0);
		if (dataList != null && dataList.size() > 0) {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reMap.put(BaseCode.DATAS.toString(), dataList);
			reMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			reMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return reMap;
	}

	@Override
	public Map<String, Object> getArea() {
		Map<String, Object> reMap = new HashMap<>();
		List<Object> dataList = provinceCityAreaDao.findAll(Area.class, 0, 0);
		if (dataList != null && dataList.size() > 0) {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reMap.put(BaseCode.DATAS.toString(), dataList);
			reMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			reMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return reMap;
	}

	@Override
	public Map<String, Object> getProvinceCityArea2() {
		Map<String, Object> reMap = new HashMap<>();
		Map<String, Object> datasMap = new HashMap<>();
		Map<String, Object> province = null;
		byte[] redisByte = JedisUtil.get("Shop_Key_Province_Map".getBytes());
		if (redisByte != null) {
			province = (Map<String, Object>) SerializeUtil.toObject(redisByte);
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			datasMap.put(BaseCode.DATAS.toString(), JSONObject.fromObject(province));
			return datasMap;
		} else {
			// 查询省市区
			Table table = provinceCityAreaDao.findAllProvinceCityArePostal();
			if (table != null && !table.getRows().isEmpty()) {
				com.alibaba.fastjson.JSONArray jsonObject = Transform.tableToJson(table).getJSONArray("rows");
				reMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				reMap.put(BaseCode.DATAS.toString(), jsonObject);
				reMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
				if (jsonObject != null && !jsonObject.isEmpty()) {
					Map<String, Object> item = new HashMap<>();
					for (int i = 0; i < jsonObject.size(); i++) {
						JSONObject provinceCityArea = JSONObject.fromObject(jsonObject.get(i));
						// 由于取出来是row数据,所以需要截取字符串
						item.put(provinceCityArea.getString("areaCode").replace("{\"value\":\"", "").replace("\"}", ""),
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
					// 将查询出来的数据放入到缓存中,由于查询省市区超时故而将缓冲时间延长至五天
					JedisUtil.set("Shop_Key_Province_Map".getBytes(), SerializeUtil.toBytes(item), 86400 );
					datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
					datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
					datasMap.put(BaseCode.DATAS.toString(), item);
					return datasMap;
				}
			} else {
				reMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
				reMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			}
		}
		return reMap;
	}

}
