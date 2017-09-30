package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class CustomsPortTransaction {

	@Reference
	private CustomsPortService customsPortService;

	//添加口岸下已开通的 海关及国检名称与编码
	public Map<String, Object> addCustomsPort(String provinceName, String provinceCode, String cityName,
			String cityCode, String customsPort, String customsPortName, String customsCode, String customsName,
			String ciqOrgCode, String ciqOrgName) {
		Map<String,Object> reMap = new HashMap<>();
		
		reMap = customsPortService.addCustomsPort(provinceName, provinceCode, cityName, cityCode, customsPort,
				customsPortName, customsCode, customsName, ciqOrgCode, ciqOrgName);
		if(reMap!=null && reMap.size()>0){
			String status = reMap.get(BaseCode.STATUS.toString())+"";
			if(status.equals("1")){
				return reMap;
			}
		}
		return null;
	}

	
	public Map<String, Object> findAllCustomsPort() {
		Map<String,Object> datasMap = customsPortService.findAllCustomsPort();
		if(datasMap!=null && datasMap.size()>0){
			String status = datasMap.get(BaseCode.STATUS.toString())+"";
			if(status.equals("1")){
				return datasMap;
			}
		}
		return null;
	}
}
