package org.silver.shop.service.common.base;

import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.common.base.ProvinceCityAreaService;
import org.silver.util.JedisUtil;
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
		String redisList = JedisUtil.get("shop_province_provinceCityArea");
		if (StringEmptyUtils.isEmpty(redisList)) {// redis缓存没有数据
			datasMap = provinceCityAreaService.getProvinceCityArea();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if (status.equals("1")) {
				datasList = (List)datasMap.get(BaseCode.DATAS.toString());
				// 将查询出来的省市区放入到redis缓存中
				JedisUtil.setListDatas("shop_province_provinceCityArea", 3600, datasList);
			}
		} else {
			// redis缓存中已有数据,直接返回数据
			return JSONArray.fromObject(redisList);
		}
		return JSONArray.fromObject(datasList);
	}

}
