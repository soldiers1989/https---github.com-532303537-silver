package org.silver.shop.impl.common.base;

import java.util.List;

import org.silver.shop.api.common.base.CountryService;
import org.silver.shop.dao.common.base.CountryDao;
import org.silver.shop.model.common.base.Country;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = CountryService.class)
public class CountryServiceImpl implements CountryService {

	@Autowired
	private CountryDao countryDao;

	@Override
	public List<Object> findAllCountry() {
		List<Object> reList = countryDao.findAllCountry();
		if (reList != null && reList.size() > 0) {
			return reList;
		}
		return null;
	}
}
