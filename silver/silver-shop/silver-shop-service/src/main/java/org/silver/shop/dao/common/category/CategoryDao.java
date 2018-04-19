package org.silver.shop.dao.common.category;

import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface CategoryDao<T> extends BaseDao<T> {

	/**
	 * 查询商品所有类型
	 * 
	 * @return list
	 */
	public Table findAllCategory();

	/**
	 * 查询第二级商品类型
	 * 
	 * @return
	 */
	public Table searchSecondCategory();

	/**
	 * 更新商品备案信息中第二级商品类型
	 * 
	 * @param firstId
	 *            关联的第一级商品类型Id
	 * @param firstTypeName
	 *            第一级商品类型名称
	 * @param secondId
	 *            第二级商品类型Id
	 * @param goodsSecondTypeName
	 *            第二级商品类型名称
	 * @param managerName 
	 * @return boolean
	 */
	public boolean updateGoodsRecordDetailSecondCategory(int firstId, String firstTypeName, long secondId,
			String goodsSecondTypeName, String managerName);

	/**
	 * 更新商品基本信息中第二级商品类型
	 * 
	 * @param firstId
	 *            关联的第一级商品类型Id
	 * @param firstTypeName第一级商品类型名称
	 * @param secondId
	 *            第二级商品类型Id
	 * @param goodsSecondTypeName
	 *            第二级商品类型名称
	 */
	public boolean updateGoodsBaseInfoSecondCategory(int firstId, String firstTypeName, long secondId,
			String goodsSecondTypeName, String managerName);

}
