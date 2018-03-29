package org.silver.shop.impl.common.base;

import java.util.List;
import java.util.Map;

import org.silver.shop.api.common.base.CountryService;
import org.silver.shop.dao.common.base.CountryDao;
import org.silver.shop.model.common.base.Country;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = CountryService.class)
public class CountryServiceImpl implements CountryService {

	@Autowired
	private CountryDao countryDao;

	@Override
	public Map<String,Object> findAllCountry() {
		List<Country> reCountryList = countryDao.findByProperty(Country.class, null, 0, 0);
		if (reCountryList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		}else if(!reCountryList.isEmpty()){
			return ReturnInfoUtils.successDataInfo(reCountryList, 0);
		}else{
			return ReturnInfoUtils.errorInfo("暂无国家数据！");
		}
	}
}
