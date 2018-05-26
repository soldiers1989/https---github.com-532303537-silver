package org.silver.shop.service.system.log;

import java.util.Map;

import org.silver.shop.api.system.log.EvaluationLogService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class EvaluationLogTransaction {

	@Reference
	private EvaluationLogService evaluationLogService;
	
	public Map<String,Object> getlogsInfo(Map<String, Object> datasMap, int page, int size) {
		return evaluationLogService.getlogsInfo(datasMap,page,size);
	}

	public Map<String,Object> tempLogs() {
		return evaluationLogService.tempLogs();
	}
}
