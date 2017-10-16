package org.silver.shop.impl.common.base;

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
	public Map<String, Object> addEPort(String customsPort, String customsPortName, String cityCode, String cityName,
			String provinceCode, String provinceName) {
		Map<String, Object> paramsMap = new HashMap<>();
		List<Object> reList = null;
		// 如果为台湾省,香港,澳门三个无市区编码的特殊省份
		if (provinceCode.equals("710000") || provinceCode.equals("810000") || provinceCode.equals("820000")) {
			// key=(表中列名),value=传递过来的值
			paramsMap.put("provinceCode", provinceCode);
			// 查询省份表
			reList = ePortDao.findByProperty(Province.class, paramsMap, 1, 1);
		} else {
			paramsMap.put("cityCode", cityCode);
			// 查询城市编码是否正确
			reList = ePortDao.findByProperty(City.class, paramsMap, 1, 1);
		}
		paramsMap.clear();
		if (reList != null && reList.size() > 0) {
			EPort portEntity = new EPort();
			portEntity.setCustomsPort(Integer.valueOf(customsPort));
			portEntity.setCustomsPortName(customsPortName);
			portEntity.setCityCode(cityCode);
			portEntity.setCityName(cityName);
			portEntity.setProvinceCode(provinceCode);
			portEntity.setProvinceName(provinceName);
			boolean flag = ePortDao.add(portEntity);
			if (flag) {
				paramsMap.clear();
				paramsMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			}
		}
		return paramsMap;
	}

	@Override
	public Map<String, Object> findAllEPort() {
		Map<String, Object> datasMap = new HashMap<>();
		List<Object> reList = ePortDao.findAll(EPort.class, 0, 0);
		if (reList != null && reList.size() > 0) {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.DATAS.toString(), reList);
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
		}
		return datasMap;
	}

	@Override
	public Map<String, Object> editEPotInfo(long id, String customsPort, String customsPortName, String cityCode,
			String cityName, String provinceCode, String provinceName) {
		Map<String, Object> paramsMap = new HashMap<>();
		List<Object> reList = null;
		// key=(表中列名),value=传递过来的值
		paramsMap.put("id", id);
		reList = ePortDao.findByProperty(EPort.class, paramsMap, 1, 1);
		paramsMap.clear();
		if (reList != null && reList.size() > 0) {
			EPort eportInfo = (EPort) reList.get(0);
			eportInfo.setCustomsPort(Integer.valueOf(customsPort.trim()));
			eportInfo.setCustomsPortName(customsPortName);
			eportInfo.setCityCode(cityCode);
			eportInfo.setCityName(cityName);
			eportInfo.setProvinceCode(provinceCode);
			eportInfo.setProvinceName(provinceName);
			boolean flag = ePortDao.update(eportInfo);
			if (flag) {
				paramsMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			}
		}
		return paramsMap;
	}

}
