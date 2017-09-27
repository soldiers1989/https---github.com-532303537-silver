package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.Map;

import org.silver.shop.api.common.base.ProvinceCityAreaService;
import org.silver.util.JedisUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

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
	 * @return
	 */
	public Map<String, HashMap<String, Object>> findProvinceCityArea() {
		String redisMap = JedisUtil.get("shop_province_provinceCityArea");
		if (redisMap == null || "".equals(redisMap.trim())) {
			Map<String, HashMap<String, Object>> datasMap = provinceCityAreaService.getProvinceCityArea();
			//将查询出来的省市区放入到redis缓存中
			JedisUtil.set("shop_province_provinceCityArea", 3600, datasMap);
			return datasMap;
		}
		return JSONObject.fromObject(redisMap);
	}

}
