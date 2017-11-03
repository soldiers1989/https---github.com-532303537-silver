package org.silver.shop.service.common.base;

import java.util.List;

import org.silver.shop.api.common.base.CountryService;
import org.silver.util.JedisUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;

/**
 * 国家 Transaction
 */
@Service("countryTransaction")
public class CountryTransaction {
	@Reference
	private CountryService countryService;

	/**
	 * 查询所有国家
	 * 
	 * @return
	 */
	public List<Object> findAllCountry() {
		// 获取在redis中的所有国家代码
		String redisList = JedisUtil.get("Shop_Key_Country_List");
		if (StringEmptyUtils.isEmpty(redisList)){
			List<Object> reList = countryService.findAllCountry();
			if (reList != null && reList.size() > 0) {
				JedisUtil.set("Shop_Key_Country_List", 3600, reList);
				return reList;
			}
		}
		return JSONArray.fromObject(redisList);
	}

}
