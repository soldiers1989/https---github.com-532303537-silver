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

import net.sf.json.JSONArray;

/**
 * 商品备案Transaction
 */
@Service("goodsRecordTransaction")
public class GoodsRecordTransaction {
	@Reference
	private GoodsRecordService goodsRecordService;

	public List<Object> findMerchantGoodsBaseInfo(int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		List datasList = goodsRecordService.findGoodsBaseInfo(merchantName, page, size);
		if (datasList != null && datasList.size() > 0) {
			return datasList;
		}

		return null;
	}

	public List<Object> getMerchantGoodsRecordInfo(String goodsIdPack) {
		System.out.println(goodsIdPack);
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		System.out.println("-------->"+merchantInfo);
		datasMap = goodsRecordService.getGoodsRecordInfo(merchantName,goodsIdPack);
		if (datasMap != null && datasMap.size() > 0) {
			List reList = (List) datasMap.get(BaseCode.DATAS.toString());
			return reList;
		}
		return null;
	}
}
