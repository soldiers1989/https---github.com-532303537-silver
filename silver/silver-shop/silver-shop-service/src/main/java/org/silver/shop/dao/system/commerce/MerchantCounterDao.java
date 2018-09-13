package org.silver.shop.dao.system.commerce;

import java.util.Map;

import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface MerchantCounterDao extends BaseDao{

	
	/**
	 * 根据商户ID查询商户能入专柜的商品信息
	 * @param merchantId 商户id
	 * @param datasMap 查询参数
	 * @param page 页数
	 * @param size 数目
	 * @return 
	 */
	public Table getEnteringTheCabinetGoods(String merchantId, Map<String, Object> datasMap, int page, int size);

}
