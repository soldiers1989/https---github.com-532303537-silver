package org.silver.shop.service.system.commerce;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.commerce.WarehousService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class WarehousTransaction {

	@Reference
	public WarehousService warehousService;
	
	public Map<String,Object> getWarehousInfo(int page,int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return warehousService.getWarehousInfo(merchantId,page,size);
	}

	//管理员查询商户仓库
	public Map<String,Object> getInfo(int page, int size) {
		return warehousService.getWarehousInfo(null,page,size);
	}
}	
