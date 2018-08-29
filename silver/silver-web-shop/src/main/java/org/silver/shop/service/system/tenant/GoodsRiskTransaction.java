package org.silver.shop.service.system.tenant;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.GoodsRiskService;
import org.silver.shop.model.system.organization.Manager;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class GoodsRiskTransaction {

	@Reference
	private GoodsRiskService goodsRiskService;
	
	public Map<String,Object> getInfo(Map<String, Object> datasMap, int page, int size) {
		return goodsRiskService.getInfo(datasMap,page,size);
	}

	public Object tmpUpdate() {
		return goodsRiskService.tmpUpdate();
	}
	
	//更新商品风控价格
	public Map<String,Object> updateInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		return goodsRiskService.updateInfo(datasMap,managerInfo);
	}
	
}
