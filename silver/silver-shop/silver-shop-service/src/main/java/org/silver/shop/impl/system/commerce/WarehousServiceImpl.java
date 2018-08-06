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
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = WarehousService.class)
public class WarehousServiceImpl implements WarehousService {

	@Autowired
	private WarehousDao warehousDao;

	@Override
	public Map<String, Object> getWarehousInfo(String merchantId, int page, int size) {
		List<Object> cacheList = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		if(StringEmptyUtils.isNotEmpty(merchantId)){
			params.put("merchantId", merchantId);
		}
		params.put("deleteFlag", 0);
		List<WarehouseContent> reList = warehousDao.findByProperty(WarehouseContent.class, params, page, size);
		long totalCount = warehousDao.findByPropertyCount(WarehouseContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				WarehouseContent warehouse =  reList.get(i);
				params.put("warehouseCode", warehouse.getWarehouseCode());
				long count = warehousDao.findByPropertyCount(StockContent.class, params);
				warehouse.setReMark(String.valueOf(count));
				cacheList.add(warehouse);
			}
			return ReturnInfoUtils.successDataInfo(reList, totalCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据！");
		}
	}

}
