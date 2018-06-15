package org.silver.shop.impl.system.commerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.WarehousService;
import org.silver.shop.dao.system.commerce.WarehousDao;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.commerce.WarehouseContent;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = WarehousService.class)
public class WarehousServiceImpl implements WarehousService {

	@Autowired
	private WarehousDao warehousDao;

	@Override
	public Map<String, Object> getWarehousInfo(String merchantId, String merchantName, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		List<Object> cacheList = new ArrayList<>();
		params.put("merchantId", merchantId);
		params.put("deleteFlag", 0);
		List<Object> reList = warehousDao.findByProperty(WarehouseContent.class, params, page, size);
		long totalCount = warehousDao.findByPropertyCount(WarehouseContent.class, params);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				WarehouseContent warehouse = (WarehouseContent) reList.get(i);
				params.put("warehouseCode", warehouse.getWarehouseCode());
				long count = warehousDao.findByPropertyCount(StockContent.class, params);
				warehouse.setReMark(String.valueOf(count));
				cacheList.add(warehouse);
			}
			return ReturnInfoUtils.successDataInfo(reList, totalCount);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

}
