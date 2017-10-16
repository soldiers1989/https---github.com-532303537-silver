package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.util.JedisUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;

@Service
public class CustomsPortTransaction {

	@Reference
	private CustomsPortService customsPortService;

	//添加口岸下已开通的 海关及国检名称与编码
	public Map<String, Object> addCustomsPort(String provinceName, String provinceCode, String cityName,
			String cityCode, int customsPort, String customsPortName, String customsCode, String customsName,
			String ciqOrgCode, String ciqOrgName) {
		Map<String,Object> reMap = new HashMap<>();
		System.out.println("----》"+customsPort);
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
		Map<String,Object> datasMap = new HashMap<>();
		String redisList = JedisUtil.get("shop_port_AllCustomsPort");
		if(StringEmptyUtils.isEmpty(redisList)){
			 datasMap = customsPortService.findAllCustomsPort();
			if(datasMap!=null && datasMap.size()>0){
				String status = datasMap.get(BaseCode.STATUS.toString())+"";
				if(status.equals("1")){
					List<Object> dateList = (List<Object>) datasMap.get(BaseCode.DATAS.toString());
					// 将查询出来的口岸放入到redis缓存中
					JedisUtil.setListDatas("shop_port_AllCustomsPort", 3600, dateList);
					return datasMap;
				}
			}
		}else{
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.DATAS.toString(), JSONArray.fromObject(redisList));
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return datasMap;
		}
		return null;
	}
}
