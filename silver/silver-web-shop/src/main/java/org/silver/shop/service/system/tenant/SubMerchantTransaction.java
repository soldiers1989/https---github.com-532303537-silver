package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.silver.shop.api.system.tenant.SubMerchantService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONObject;

@Service
public class SubMerchantTransaction {

	@Reference
	private SubMerchantService subMerchantService;
	
	//添加子商户信息
	public Map<String,Object> addSubMerchantInfo(JSONObject json) {
		return subMerchantService.addSubMerchantInfo(json);
	}

}
