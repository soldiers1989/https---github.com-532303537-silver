package org.silver.shop.service.system.commerce;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;


/**
 * 商品备案Transaction
 */
@Service("goodsRecordTransaction")
public class GoodsRecordTransaction {
	@Reference
	private GoodsRecordService goodsRecordService;

	//查询商户下商品基本信息
	public List<Object> findMerchantGoodsBaseInfo(int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		List<Object> datasList = goodsRecordService.findGoodsBaseInfo(merchantName, page, size);
		if (datasList != null && datasList.size() > 0) {
			return datasList;
		}
		return null;
	}

	//商户选择商品基本信息后,根据商品ID与商品名查询已发起备案的商品信息
	public List<Object> getMerchantGoodsRecordInfo(String goodsInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		Map<String, Object> datasMap = goodsRecordService.getGoodsRecordInfo(merchantName,goodsInfoPack);
		if (datasMap != null && datasMap.size() > 0) {
			return (List<Object>)datasMap.get(BaseCode.DATAS.toString());
		}
		return null;
	}

	public Map<String,Object> merchantSendGoodsRecord(String eport,String customsCode,String ciqOrgCode,String recordGoodsInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		Map<String, Object> datasMap = goodsRecordService.merchantSendGoodsRecord(merchantName,merchantId,recordGoodsInfoPack,eport,customsCode,ciqOrgCode);
		return datasMap;
	}
}
