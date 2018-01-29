package org.silver.shop.service.common.base;

import java.util.Map;

import org.silver.shop.api.common.base.IdCardService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class IdCardTransaction {
	
	@Reference
	private IdCardService idCardService;
	
	public Map<String,Object> getAllIdCard(int page, int size, String idName,String idNumber, int type) {
		return idCardService.getAllIdCard(idName,idNumber,page,size,type);
	}

	
	public Map<String,Object> editIdCardInfo(long id, String idName, String idNumber, int type) {
		return idCardService.editIdCardInfo(id,idName,idNumber,type);
	}


	public Map<String,Object> firstUpdateIdCardInfo() {
		return idCardService.firstUpdateIdCardInfo();
	}

}
