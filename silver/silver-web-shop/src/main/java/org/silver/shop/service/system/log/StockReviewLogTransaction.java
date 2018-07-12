package org.silver.shop.service.system.log;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.log.StockReviewLogService;
import org.silver.shop.model.system.organization.Merchant;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class StockReviewLogTransaction {

	@Reference
	private StockReviewLogService stockReviewLogService;
	
	public Map<String,Object> merchantGetLog(String entGoodsNo) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return stockReviewLogService.getLog(merchantId,entGoodsNo);
	}

}
