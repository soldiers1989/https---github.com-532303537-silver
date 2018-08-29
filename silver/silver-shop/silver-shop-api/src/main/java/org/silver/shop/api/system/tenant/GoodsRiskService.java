package org.silver.shop.api.system.tenant;

import java.util.Map;

import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.organization.Manager;

public interface GoodsRiskService {

	/**
	 * 查询风控信息
	 * @param datasMap 查询参数
	 * @param size 数目 
	 * @param page 页数
	 * @return Map
	 */
	public Map<String, Object> getInfo(Map<String, Object> datasMap, int page, int size);

	/**
	 * 
	 * @return
	 */
	public Object tmpUpdate();

	/**
	 * 更新风控信息
	 * @param datasMap 
	 * @param managerInfo 管理员信息
	 * @return Map
	 */
	public Map<String, Object> updateInfo(Map<String, Object> datasMap, Manager managerInfo);

	
	/**
	 * 根据已备案商品信息，添加新的商品风控信息
	 * @param goods 商品备案信息
	 */
	public void addGoodsRiskControlContent(GoodsRecordDetail goods);

}
