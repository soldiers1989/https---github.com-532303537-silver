package org.silver.shop.service.system.cross;

import java.util.Map;

import org.silver.shop.api.system.cross.ReportsService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class ReportsTransaction {

	@Reference
	private ReportsService reportsService;
	
	public Map<String,Object> getSynthesisReportDetails(Map<String, Object> params) {
		return reportsService.getSynthesisReportDetails(params);
	}
}
