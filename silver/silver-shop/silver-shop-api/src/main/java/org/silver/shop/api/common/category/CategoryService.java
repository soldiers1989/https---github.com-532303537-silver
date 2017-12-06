package org.silver.shop.api.common.category;

import java.util.Map;


public interface CategoryService {
	/**
	 * 查询所有商品类型,并进行对应的级联封装到Map
	 * @return
	 */
	public Map<String, Object>  findGoodsType();

	/**
	 * 管理员添加商品类型
	 * @param managerId
	 * @param managerName
	 * @param paramMap
	 * @return
	 */
	public Map<String, Object> addGoodsCategory(String managerId, String managerName, Map<String, Object> paramMap);

	/**
	 * 管理员删除商品类型
	 * @param managerId 管理员Id
	 * @param managerName 管理员名称
	 * @param paramMap
	 * @return
	 */
	public Map<String, Object> deleteGoodsCategory(String managerId, String managerName, Map<String, Object> paramMap);

	/**
	 * 管理员修改商品类型
	 * @param managerId 管理员Id
	 * @param managerName 管理员名称
	 * @param paramMap
	 * @return
	 */
	public Map<String, Object> editGoodsCategory(String managerId, String managerName, Map<String, Object> paramMap);

	/**
	 * 管理员查询商品类型详情
	 * @param type type 1-第一级,2-第二级,3-第三级
	 * @param id 类型Id
	 * @return
	 */
	public Map<String, Object> getCategoryInfo(int type, String id);

	/**
	 * 根据等级查询商品类型
	 * @param type
	 * @return
	 */
	public Map<String, Object> searchCategoryInfo(int type);
}
