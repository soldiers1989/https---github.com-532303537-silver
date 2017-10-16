package org.silver.shop.impl.common.base;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.dao.common.base.CustomsPortDao;
import org.silver.shop.model.common.base.CustomsPort;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = CustomsPortService.class)
public class CustomsPortServiceImpl implements CustomsPortService {

	@Autowired
	private CustomsPortDao customsPortDao;
	
	@Override
	public Map<String, Object> addCustomsPort(String provinceName, String provinceCode, String cityName,
			String cityCode, int customsPort, String customsPortName, String customsCode, String customsName,
			String ciqOrgCode, String ciqOrgName) {
		Map<String,Object> statusMap = new HashMap<>();
		Date date = new Date();
		CustomsPort customsInfo =  new CustomsPort();
		customsInfo.setProvince(provinceName);
		customsInfo.setProvinceCode(provinceCode);
		
		customsInfo.setCity(cityName);
		customsInfo.setCityCode(cityCode);
		
		customsInfo.setCustomsPort(customsPort);
		customsInfo.setCustomsPortName(customsPortName);
		
		customsInfo.setCustomsName(customsName);
		customsInfo.setCustomsCode(customsCode);
		
		customsInfo.setCiqOrgCode(ciqOrgCode);
		customsInfo.setCiqOrgName(ciqOrgName);
		
		customsInfo.setCreateDate(date);
		customsInfo.setDeleteFlag(0);
		boolean flag= customsPortDao.add(customsInfo);
		if(flag){
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		}else{
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return statusMap;
	}

	@Override
	public Map<String,Object> findAllCustomsPort() {
		Map<String,Object> reMap = new HashMap<>();
		List<Object> dataList = customsPortDao.findAll(CustomsPort.class,0,0);
		if(dataList!=null && dataList.size()>0){
			reMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reMap.put(BaseCode.DATAS.toString(), dataList);
			reMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		}else{
			reMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			reMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return reMap; 
	}

}
