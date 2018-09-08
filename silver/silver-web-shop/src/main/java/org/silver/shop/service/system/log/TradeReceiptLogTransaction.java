package org.silver.shop.service.system.log;

import java.util.Map;

import org.silver.shop.api.system.log.TradeReceiptLogService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class TradeReceiptLogTransaction {

	@Reference
	private TradeReceiptLogService tradeReceiptLogService;
	
	public Map<String,Object> addLog(Map<String,Object> datasMap) {
		return tradeReceiptLogService.addLog(datasMap);
	}

	//
	public Map<String,Object> getInfo(Map<String, Object> datasMap, int page, int size) {
		
		return tradeReceiptLogService.getInfo(datasMap, page, size);
	}

}
