package org.silver.shop.impl.common.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.EPortService;
import org.silver.shop.dao.common.base.EPortDao;
import org.silver.shop.model.common.base.City;
import org.silver.shop.model.common.base.EPort;
import org.silver.shop.model.common.base.Province;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;

@Service(interfaceClass = EPortService.class)
public class EPortServiceImpl implements EPortService {

	@Autowired
	private EPortDao ePortDao;

	@Override
	public List<Object> checkEportName(String customsPortName) {
		Map<String, Object> paramsMap = new HashMap<>();
		// key=(表中列名),value=传递过来的值
		paramsMap.put("customsPortName", customsPortName);
		return ePortDao.findByProperty(EPort.class, paramsMap, 1, 1);
	}

	@Override
	public Map<String, Object> addEPort(String customsPort, String customsPortName, String cityCode) {
		Map<String, Object> paramsMap = new HashMap<>();
		List reList = null;
		// 如果为台湾省,香港,澳门三个无市编码的特殊省份
		if (cityCode.equals("710000") || cityCode.equals("810000") || cityCode.equals("820000")) {
			// key=(表中列名),value=传递过来的值
			paramsMap.put("cityCode", cityCode);
			// 则查询省份表
			reList = ePortDao.findByProperty(Province.class, paramsMap, 1, 1);
		} else {
			// key=(表中列名),value=传递过来的值
			paramsMap.put("cityCode", cityCode);
			// 查询城市编码是否正确
			reList = ePortDao.findByProperty(City.class, paramsMap, 1, 1);
		}
		paramsMap.clear();
		if (reList != null && reList.size() > 0) {
			EPort portEntity = new EPort();
			portEntity.setCityCode(cityCode);
			portEntity.setCustomsPort(customsPort);
			portEntity.setCustomsPortName(customsPortName);
			boolean flag = ePortDao.add(portEntity);
			if (flag) {
				paramsMap.clear();
				paramsMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			}
		}
		return paramsMap;
	}

	@Override
	public List<Object> findEPort() {
		return null;
	}
}
