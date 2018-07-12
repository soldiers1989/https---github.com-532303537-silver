package org.silver.shop.api.system.log;

import java.util.Map;

public interface StockReviewLogService {

	/**
	 * 获取库存审核日志记录
	 * @param merchantId 商户id
	 * @param entGoodsNo 商品自编号
	 * @return Map
	 */
	public Map<String, Object> getLog(String merchantId, String entGoodsNo);

}
