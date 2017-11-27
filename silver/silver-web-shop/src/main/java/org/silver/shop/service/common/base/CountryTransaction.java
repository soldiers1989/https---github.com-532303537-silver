package org.silver.shop.service.common.base;

import java.util.List;

import org.silver.shop.api.common.base.CountryService;
import org.silver.shop.model.common.base.Country;
import org.silver.shop.model.common.base.Metering;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
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
	public List findAllCountry() {
		List<Country> countryList = null;
		byte[] redisByte = JedisUtil.get("Shop_Key_Country_List".getBytes(), 3600);
		if (redisByte != null) {
			countryList = (List<Country>) SerializeUtil.toObject(redisByte);
			return countryList;
		} else {
			List<Object> reList = countryService.findAllCountry();
			if (reList != null && !reList.isEmpty()) {
				JedisUtil.set("Shop_Key_Country_List".getBytes(), SerializeUtil.toBytes(reList), 3600);
				return reList;
			}
		}
		return null;
		
	}

}
