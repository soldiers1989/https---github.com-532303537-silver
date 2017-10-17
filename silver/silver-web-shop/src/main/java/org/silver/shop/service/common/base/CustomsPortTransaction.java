package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
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
		Map<String,Object>	reMap = customsPortService.addCustomsPort(provinceName, provinceCode, cityName, cityCode, customsPort,
				customsPortName, customsCode, customsName, ciqOrgCode, ciqOrgName);
		if(reMap!=null && reMap.size()>0){
			String status = reMap.get(BaseCode.STATUS.toString())+"";
			if(status.equals("1")){
				return reMap;
			}
		}
		return null;
	}

	//查询所有已开通的口岸及关联的海关
	public Map<String, Object> findAllCustomsPort() {
		Map<String,Object> datasMap = new HashMap<>();
		List<CustomsPort> customsPortList = null;
		byte[] redisByte = JedisUtil.get("shop_port_AllCustomsPort".getBytes(), 3600);
		if (redisByte != null) {
			customsPortList = (List<CustomsPort>) SerializeUtil.toObject(redisByte);
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.DATAS.toString(), JSONArray.fromObject(customsPortList));
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return datasMap;
		} else {// 缓存中没有数据,重新访问数据库读取数据
			datasMap = customsPortService.findAllCustomsPort();
			if (!datasMap.get(BaseCode.STATUS.toString()).equals("1")) {
				return datasMap;
			}
			customsPortList = (List<CustomsPort>) datasMap.get(BaseCode.DATAS.toString());
			// 将查询出来的口岸数据放入缓存中
			JedisUtil.set("shop_port_AllCustomsPort".getBytes(), SerializeUtil.toBytes(customsPortList), 3600);
			return datasMap;
		}
	}
}
