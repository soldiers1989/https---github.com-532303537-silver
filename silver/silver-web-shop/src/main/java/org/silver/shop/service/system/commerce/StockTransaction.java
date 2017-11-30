package org.silver.shop.service.system.commerce;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
	
	//搜索该仓库下已经备案成功的备案商品信息
	public Map<String, Object> searchAlreadyRecordGoodsDetails(String warehouseCode, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return stockService.searchAlreadyRecordGoodsDetails(merchantId,warehouseCode,page,size);
	}

	public Map<String,Object> addGoodsStockCount(String warehouseCode, String warehouseName, String goodsInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return stockService.addGoodsStockCount(merchantId,merchantName,warehouseCode,warehouseName,goodsInfoPack);
	}

	//
	public Map<String,Object> addGoodsSellCount(String goodsId, int sellCount) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return stockService.addGoodsSellCount(merchantId,merchantName,goodsId,sellCount);
	}

	//获取商品所有库存信息
	public Map<String, Object> getGoodsStockInfo(int page, int size,String warehouseCode) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return stockService.getGoodsStockInfo(merchantId,merchantName,page,size,warehouseCode);
	}

	//商户批量与单个商品上/下架状态修改
	public Map<String, Object> merchantSetGoodsSellAndStopSelling(String goodsInfoPack,int type) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return stockService.setGoodsSellAndStopSelling(merchantId,merchantName,goodsInfoPack,type);
	}

	//商户批量与单个修改商品库存与上架数量
	public Map<String, Object> merchantSetGoodsStorageAndSellCount(String goodsInfoPack, int type) { 
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return stockService.setGoodsStorageAndSellCount(merchantId,merchantName,goodsInfoPack,type);
	}

	public Map<String, Object> searchGoodsStockInfo(HttpServletRequest req, int page, int size) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return stockService.searchGoodsStockInfo(merchantId, merchantName, datasMap,page,size);
	}

	//商户批量与单个修改商品售卖价或市场价
	public Map<String, Object> merchantSetGoodsSalePriceAndMarketPrice(String goodsInfoPack, int type) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return stockService.merchantSetGoodsSalePriceAndMarketPrice(merchantId,merchantName,goodsInfoPack,type);
	}


}
