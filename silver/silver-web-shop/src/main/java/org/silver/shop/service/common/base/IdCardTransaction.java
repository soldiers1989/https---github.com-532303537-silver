package org.silver.shop.service.common.base;

import java.util.Map;

import org.silver.shop.api.common.base.IdCardService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class IdCardTransaction {
	
	@Reference
	private IdCardService idCardService;
	
	public Map<String,Object> getAllIdCard(int page, int size, Map<String,Object>datasMap) {
		return idCardService.getAllIdCard(page,size,datasMap);
	}

	
	public Map<String,Object> editIdCardInfo(long id, String idName, String idNumber, int type) {
		return idCardService.editIdCardInfo(id,idName,idNumber,type);
	}

	public Map<String,Object> deleteDuplicateIdCardInfo() {
		return idCardService.deleteDuplicateIdCardInfo();
	}


	public Object temPush() {
		return idCardService.temPush();
	}
}
