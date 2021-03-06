package org.silver.shop.impl.common.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.RedisKey;
import org.silver.shop.api.common.base.ProvinceCityAreaService;
import org.silver.shop.dao.common.base.ProvinceCityAreaDao;
import org.silver.shop.dao.common.base.impl.ProvinceCityAreaDaoImpl;
import org.silver.shop.model.common.base.Area;
import org.silver.shop.model.common.base.City;
import org.silver.shop.model.common.base.Province;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
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
		List<Object> provinceList = new ArrayList<>();
		Map<String, Map<String, Map<String, Object>>> provinceMap = new HashMap<>();
		Map<String, Map<String, Object>> cityMap = null;
		Map<String, Object> areaMap = null;
		// 查询省市区
		Table table = provinceCityAreaDao.findAllProvinceCityArea();
		if (table != null && !table.getRows().isEmpty()) {
			List<Row> lr = table.getRows();
			for (int i = 0; i < lr.size(); i++) {
				areaMap = new HashMap<>();
				String areaCode = lr.get(i).getValue("areaCode") + "";
				String areaName = lr.get(i).getValue("areaName") + "";
				String cityCode = lr.get(i).getValue("cityCode") + "";
				String cityName = lr.get(i).getValue("cityName") + "";
				String provinceCode = lr.get(i).getValue("provinceCode") + "";
				String provinceName = lr.get(i).getValue("provinceName") + "";
				if (provinceMap.get(provinceName + "_" + provinceCode) != null) {
					String cityKey = provinceMap.get(provinceName + "_" + provinceCode).get(cityName + "_" + cityCode)
							+ "";
					if (StringEmptyUtils.isNotEmpty(cityKey)) {
						provinceMap.get(provinceName + "_" + provinceCode).get(cityName + "_" + cityCode).put(areaCode,
								areaName);
					} else {
						areaMap.put(areaCode, areaName);
						provinceMap.get(provinceName + "_" + provinceCode).put(cityName + "_" + cityCode, areaMap);

					}
				} else {// 省份不存在时
					provinceMap = new HashMap<>();
					if (StringEmptyUtils.isNotEmpty(areaCode)) {
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
			// 将查询出来的省市区放入到redis缓存中
			JedisUtil.setListDatas("SHOP_KEY_PROVINCE_CITY_AREA_LIST", 86400, provinceList);
			return ReturnInfoUtils.successDataInfo(provinceList);
		} else {
			return ReturnInfoUtils.errorInfo("查询省市区信息失败,服务器繁忙!");
		}
	}


	/**
	 * 获取所有(邮编)省市区四表信息,并进行对应的等级封装
	 * 
	 * @return Map
	 */
	@Override
	public Map<String, Object> getAllProvinceCityAreaPostal() {
		Map<String, Object> item = new HashMap<>();
		// 查询省市区
		Table table = provinceCityAreaDao.findAllProvinceCityArePostal();
		if (table != null && !table.getRows().isEmpty()) {
			com.alibaba.fastjson.JSONArray jsonObject = Transform.tableToJson(table).getJSONArray("rows");
			if (jsonObject != null && !jsonObject.isEmpty()) {
				for (int i = 0; i < jsonObject.size(); i++) {
					JSONObject provinceCityArea = JSONObject.fromObject(jsonObject.get(i));
					// 由于取出来是row数据,所以需要截取字符串
					// 查询省市区封装成Map集合key=区域编码,value=省份编码+省份名称#城市编码_城市名称#区域编码_区域名称
					item.put(replace(provinceCityArea.getString("areaCode")),
							replace(provinceCityArea.getString("provinceCode")) + "_"
									+ replace(provinceCityArea.getString("provinceName")) + "#"
									+ replace(provinceCityArea.getString("cityCode")) + "_"
									+ replace(provinceCityArea.getString("cityName")) + "#"
									+ replace(provinceCityArea.getString("areaCode")) + "_"
									+ replace(provinceCityArea.getString("areaName")));
				}
				// 将查询出来的数据放入到缓存中,由于查询省市区超时故而将缓冲时间延长至五天
				JedisUtil.set(RedisKey.SHOP_KEY_PROVINCE_CITY_AREA_POSTAL_MAP.getBytes(), SerializeUtil.toBytes(item),
						(3600 * 24) * 7);
			}
		} else {
			return ReturnInfoUtils.errorInfo("查询省市区信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successDataInfo(item);
	}

	/**
	 * 通用替换多表查询字符串
	 * 
	 * @param str
	 * @return
	 */
	private String replace(String str) {
		if (StringEmptyUtils.isEmpty(str)) {
			return "";
		}
		return str.replace("{\"value\":\"", "").replace("\"}", "");
	}

	@Override
	public Map<String, Object> editProvinceCityAreaInfo(Map<String, Object> params, int flag) {
		switch (flag) {
		case 1:
			Map<String, Object> provinceMap = editProvince(JSONObject.fromObject(params));
			if (!"1".equals(provinceMap.get(BaseCode.STATUS.toString()))) {
				return provinceMap;
			}
			break;
		case 2:
			Map<String, Object> cityMap = editCity(JSONObject.fromObject(params));
			if (!"1".equals(cityMap.get(BaseCode.STATUS.toString()))) {
				return cityMap;
			}
			break;
		case 3:
			Map<String, Object> areaMap = editArea(JSONObject.fromObject(params));
			if (!"1".equals(areaMap.get(BaseCode.STATUS.toString()))) {
				return areaMap;
			}
			break;
		default:
			return ReturnInfoUtils.errorInfo("标识错误！");
		}
		getProvinceCityArea();
		getAllProvinceCityAreaPostal();
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 修改区域信息
	 * 
	 * @param datas
	 * @return Map
	 */
	private Map<String, Object> editArea(JSONObject datas) {
		Map<String, Object> params = new HashMap<>();
		String oldAreaCode = datas.get("oldCode") + "";
		params.put("areaCode", oldAreaCode);
		List<Area> reAreaList = provinceCityAreaDao.findByProperty(Area.class, params, 1, 1);
		if (reAreaList != null && !reAreaList.isEmpty()) {
			Area area = reAreaList.get(0);
			area.setAreaCode(datas.get("newCode") + "");
			area.setAreaName(datas.get("name") + "");
			area.setCityCode(datas.get("parentCode") + "");
			if (!provinceCityAreaDao.update(area)) {
				return ReturnInfoUtils.errorInfo("更新区域信息失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("区域信息未找到,请核对信息!");
	}

	/**
	 * 修改城市信息
	 * 
	 * @param datas
	 * @return
	 */
	private Map<String, Object> editCity(JSONObject datas) {
		Map<String, Object> params = new HashMap<>();
		String oldCityCode = datas.get("oldCode") + "";
		params.put("cityCode", oldCityCode);
		List<City> reCityList = provinceCityAreaDao.findByProperty(City.class, params, 1, 1);
		if (reCityList != null && !reCityList.isEmpty()) {
			City city = reCityList.get(0);
			city.setCityCode(datas.get("newCode") + "");
			city.setCityName(datas.get("name") + "");
			city.setProvinceCode(datas.get("parentCode") + "");
			if (!provinceCityAreaDao.update(city)) {
				return ReturnInfoUtils.errorInfo("更新城市信息失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("城市信息未找到,请核对信息!");
	}

	/**
	 * 修改省份
	 * 
	 * @param datas
	 * @return
	 */
	private Map<String, Object> editProvince(JSONObject datas) {
		Map<String, Object> params = new HashMap<>();
		String oldProvinceCode = datas.get("oldCode") + "";
		params.put("provinceCode", oldProvinceCode);
		List<Province> rePronvinceList = provinceCityAreaDao.findByProperty(Province.class, params, 1, 1);
		if (rePronvinceList != null && !rePronvinceList.isEmpty()) {
			Province province = rePronvinceList.get(0);
			province.setProvinceCode(datas.get("newCode") + "");
			province.setProvinceName(datas.get("name") + "");
			if (!provinceCityAreaDao.update(province)) {
				return ReturnInfoUtils.errorInfo("更新省份信息失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("省份信息未找到,请核对信息!");
	}
}
