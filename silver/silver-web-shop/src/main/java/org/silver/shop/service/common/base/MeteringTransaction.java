package org.silver.shop.service.common.base;

import java.util.List;

import org.silver.shop.api.common.base.MeteringService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

/**
 * 计量单位 Transaction
 *
 */
@Service("meteringTransaction")
public class MeteringTransaction {

	@Reference
	private MeteringService meteringService;
	
	public List findMetering(){
		List reList = meteringService.findAllMetering();
		if(reList !=null && reList.size()>0){
			return reList;
		}
		return null;
	}
	
}
