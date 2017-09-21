package org.silver.shop.service.common.base;

import java.util.List;

import org.silver.shop.api.common.base.CountryService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

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
		List reList = countryService.findAllCountry();
		if (reList != null && reList.size() > 0) {
			return reList;
		}
		return null;
	}

}
