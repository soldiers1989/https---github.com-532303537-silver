package org.silver.shop.service.system.cross;

import java.util.Map;

import org.silver.shop.api.system.cross.YsPayService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("ysPayTransaction")
public class YsPayTransaction {

	@Reference
	private YsPayService ysPayService;
	
	public Map<String,Object> checkOrderInfo(String memberId, String orderId){
		return ysPayService.checkOrderInfo(memberId,orderId);
	}
}
