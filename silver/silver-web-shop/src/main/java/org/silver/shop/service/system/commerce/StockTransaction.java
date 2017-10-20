package org.silver.shop.service.system.commerce;

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.commerce.StockService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("stockTransaction")
public class StockTransaction {
	
	@Reference
	private StockService stockService;
	
	public Map<String, Object> searchAlreadyRecordGoodsDetails(String warehouseCode, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return stockService.searchAlreadyRecordGoodsDetails(merchantId,warehouseCode,page,size);
	}

	public Map<String,Object> addGoodsStockCount(String warehousCode, String warehousName, String goodsInfoPack) {
		Map<String,Object> dataMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Map<String,Object> datasMap = stockService.addGoodsStockCount(merchantId,merchantName,warehousCode,warehousName,goodsInfoPack);
		return datasMap;
	}

	public Map<String,Object> addGoodsSellCount(String goodsId, int sellCount) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return stockService.addGoodsSellCount(merchantId,merchantName,goodsId,sellCount);
	}

}
