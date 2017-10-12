package org.silver.shop.impl.common.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.ProvinceCityAreaService;
import org.silver.shop.dao.common.base.ProvinceCityAreaDao;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

@Service(interfaceClass = ProvinceCityAreaService.class)
public class ProvinceCityAreaServiceImpl implements ProvinceCityAreaService {

	@Autowired
	private ProvinceCityAreaDao provinceCityAreaDao;

	@Override
	public Map<String, Object> getProvinceCityArea() {
		Map<String, Object> statusMap = new HashMap<>();
		List<Object> provinceList = new ArrayList<>();
		Map<String, Map<String, Map<String, Object>>> provinceMap = new HashMap<>();
		Map<String, Map<String, Object>> cityMap = null;
		Map<String, Object> areaMap = null;
		// 查询省市区
		Table table = provinceCityAreaDao.findAllProvinceCityArea();
		// System.out.println(Transform.tableToJson(table));
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
				} else {//省份不存在时
					provinceMap = new HashMap<>();
					if (areaCode != null && !areaCode.trim().equals("null")) {
						cityMap = new HashMap<>();
						areaMap.put(areaCode, areaName);
						cityMap.put(cityName + "_" + cityCode, areaMap);
					} else {//当省份下没有城市时
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

}