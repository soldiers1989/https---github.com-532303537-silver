package org.silver.shop.service.system.commerce;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.commerce.MerchantCounterService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.ReturnInfoUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class MerchantCounterTransaction {

	@Reference
	private MerchantCounterService merchantCounterService;
	
	//
	public Map<String,Object> getInfo(Map<String, Object> datasMap, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		datasMap.put("merchantId", merchantInfo.getMerchantId());
		return merchantCounterService.getInfo(datasMap,page,size);
	}

	//
	public Object getGoodsInfo(Map<String, Object> datasMap, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return merchantCounterService.getGoodsInfo(merchantInfo.getMerchantId(),datasMap,page,size);
	}

	//添加专柜信息
	public Map<String,Object> addCounterInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return merchantCounterService.addCounterInfo(merchantInfo,datasMap);
	}

	//商户专柜添加商品信息
	public Map<String,Object> addGoodsInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return merchantCounterService.addGoodsInfo(merchantInfo,datasMap);
	}

	public Map<String,Object> counterInfo(String counterId) {
		return merchantCounterService.counterInfo(counterId);
	}

	//查询专柜商品信息
	public Map<String,Object> counterGoods(Map<String, Object> datasMap, int page, int size) {
		
		return merchantCounterService.getGoodsInfo("",datasMap, page,size);
	}
	
}
