package org.silver.shop.service.system.log;

import java.util.Map;

import org.silver.shop.api.system.log.IdCardCertificationlogsService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class IdCardCertificationlogsTransaction {

	@Reference
	private IdCardCertificationlogsService idCardCertificationService;
	
	public Object getlogsInfo(Map<String, Object> datasMap, int page, int size) {
		return idCardCertificationService.getlogsInfo(datasMap,page,size);
	}

	public Object merchantGetInfo(Map<String, Object> datasMap, int page, int size) {
		return idCardCertificationService.getlogsInfo(datasMap,page,size);
	}

}
