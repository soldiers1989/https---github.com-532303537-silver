package org.silver.shop.impl.system.commerce;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.WarehousService;
import org.silver.shop.dao.system.commerce.WarehousDao;
import org.silver.shop.model.system.commerce.Warehous;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = WarehousService.class)
public class WarehousServiceImpl implements WarehousService {

	@Autowired
	private WarehousDao warehousDao;
	
	@Override
	public Map<String, Object> getWarehousInfo(String merchantId) {
		Map<String,Object> statusMap = new HashMap<>();
		Map<String,Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("deleteFlag", 0);
		List<Object> reList = warehousDao.findByProperty(Warehous.class, params, 0, 0);
		if(reList == null){
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}else{
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		}
		return statusMap;
	}

}
