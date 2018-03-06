package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.silver.shop.api.system.tenant.MerchantFeeService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MerchantFeeTransaction {

	@Reference
	private MerchantFeeService merchantFeeService;
	
	//管理员添加
	public Map<String,Object> addMerchantServiceFee(Map<String, Object> params) {
		return merchantFeeService.addMerchantServiceFee(params);
	}

}
