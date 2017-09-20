package org.silver.shop.impl.common.base;

import java.util.List;

import org.silver.shop.api.common.base.CountryService;
import org.silver.shop.dao.common.base.CountryDao;
import org.silver.util.JedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = CountryService.class)
public class CountryServiceImpl implements CountryService {

	@Autowired
	private CountryDao countryDao;

	@Override
	public List<Object> findAllCountry() {
		// 获取在redis中的所有国家代码
		String redisList = JedisUtil.get("Shop_country_AllCountry");
		if(redisList==null || "".equals(redisList.trim()) ){
			List<Object> reList = countryDao.findAllCountry();
			if (reList != null && reList.size() > 0) {
				JedisUtil.set("Shop_country_AllCountry", 3600, reList);
				return reList;
			}
		}
		return JSONArray.fromObject(redisList);
	}

}
