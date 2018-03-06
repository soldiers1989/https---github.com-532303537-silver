package org.silver.shop.impl.system.tenant;

import java.util.Map;

import org.silver.shop.api.system.tenant.MerchantFeeService;
import org.silver.shop.dao.system.tenant.MerchantFeeDao;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantFeeService.class)
public class MerchantFeeServiceImpl implements MerchantFeeService {

	@Autowired
	private MerchantFeeDao merchantFeeDao;
	
	
	@Override
	public Map<String, Object> addMerchantServiceFee(Map<String, Object> params) {
		if (params != null) {
			params.get("");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

}
