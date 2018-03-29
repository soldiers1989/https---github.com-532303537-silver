package org.silver.shop.service.common.base;

import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CountryService;
import org.silver.shop.model.common.base.Country;
import org.silver.shop.model.common.base.Metering;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
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
	public Map<String, Object> findAllCountry() {
		List<Country> countryList = null;
		byte[] redisByte = JedisUtil.get("Shop_Key_Country_List".getBytes(), 3600);
		if (redisByte != null) {
			countryList = (List<Country>) SerializeUtil.toObject(redisByte);
			return ReturnInfoUtils.successDataInfo(countryList, 0);
		} else {
			Map<String, Object> item = countryService.findAllCountry();
			if (!"1".equals(item.get(BaseCode.STATUS.toString()))) {
				return item;
			}
			JedisUtil.set("Shop_Key_Country_List".getBytes(),
					SerializeUtil.toBytes(item.get(BaseCode.DATAS.toString())), 3600);
			return item;
		}
	}

	public boolean checkCountryCode(String code) {
		CountryTransaction countryTransaction = new CountryTransaction();
		Map<String, Object> reMap = countryTransaction.findAllCountry();
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return false;
		}
		List<Country> reList = (List<Country>) reMap.get(BaseCode.DATAS.toString());
		for (int i = 0; i < reList.size(); i++) {
			Country country = reList.get(i);
			if (code.equals(country.getCountryCode())) {
				return true;
			}
		}
		return false;
	}
}
