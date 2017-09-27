package org.silver.shop.impl.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.common.base.ProvinceCityAreaService;
import org.silver.shop.dao.common.base.ProvinceCityAreaDao;
import org.silver.shop.dao.common.base.impl.ProvinceCityAreaDaoImpl;
import org.silver.shop.model.common.base.Area;
import org.silver.shop.model.common.base.City;
import org.silver.shop.model.common.base.Province;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = ProvinceCityAreaService.class)
public class ProvinceCityAreaServiceImpl implements ProvinceCityAreaService {

	@Autowired
	private ProvinceCityAreaDao provinceCityAreaDao;

	@Override
	public Map<String, HashMap<String, Object>> getProvinceCityArea() {
		Map<String, HashMap<String, Object>> provinceCityArea = new HashMap<>();
		Map<String, Object> provinceMap = null;
		Map<String, Object> cityMap = null;
		Map<String, Object> areaMap = null;
		//查询省市区
		List<Object> datasList = provinceCityAreaDao.findAllCountry();
		String province = "";
		String city = "";
		for (int i = 0; i < datasList.size(); i++) {
			List list = JSONArray.fromObject(datasList.get(i));
			areaMap = new HashMap<>();
			String areaCode = list.get(0) + "";
			String areaName = list.get(1) + "";
			String cityCode = list.get(2) + "";
			String cityName = list.get(3) + "";
			String provinceCode = list.get(4) + "";
			String provinceName = list.get(5) + "";
			if(!province.equals(provinceCode) ){
				provinceMap = new HashMap<>();
				provinceMap.put("provinceName"+i, provinceName);
				provinceMap.put("provinceCode"+i, provinceCode);
				province = provinceMap.get("provinceCode"+i)+"";
				provinceCityArea.put("province" + i, (HashMap<String, Object>) provinceMap);
			}
			if(!city.equals(cityCode) ){
				cityMap = new HashMap<>();
				cityMap.put("cityName"+i, cityName);
				cityMap.put("cityCode"+i, cityCode);
				provinceMap.put("city" + i, cityMap);
				city = cityMap.get("cityCode"+i)+"";
			}
			areaMap.put("areaName" + i, areaName);
			areaMap.put("areaCode" + i, areaCode);
			cityMap.put("area" + i, areaMap);
		}
		return provinceCityArea;
	}

}
