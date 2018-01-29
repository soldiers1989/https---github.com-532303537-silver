package org.silver.shop.service.common.base;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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

	/**
	 * 查询省市区(不封装对应三级联动)
	 * 
	 * @return
	 */
	public Object getProvinceCityArea() {
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
			datasMap = provinceCityAreaService.getProvinceCityArea2();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				return datasMap;
			}
		}
		return datasMap;
	}

	public Object editProvinceCityAreaInfo(HttpServletRequest req, int length) {
		String[] strArr = new String[length];
		Enumeration<String> isKey = req.getParameterNames();
		int i = 0 ;
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			strArr[i] = req.getParameter(key);
			i++;
		}	
		return provinceCityAreaService.editProvinceCityAreaInfo(strArr);
	}
}
