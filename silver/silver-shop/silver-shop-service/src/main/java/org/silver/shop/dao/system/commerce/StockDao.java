package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

import com.justep.baas.data.Table;

public interface StockDao {
	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public Table getWarehousGoodsInfo(String merchantId ,String warehouseCode, int page, int size);
	
	
	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public List<Object> findByProperty(Class entity, Map params, int page, int size);
	
	/**
	 * 将实体类实例化
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean add(Object entity);
	
	
	/**
	 * 根据实体类更新数据库信息
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean update(Object entity);
	
	/**
	 * 模糊查询总数
	 * 
	 * @param entity
	 *            类
	 * @param params
	 *            查询参数
	 * @return
	 */
	public long findByPropertyCount(Class entity, Map<String, Object> params);
	
	/**
	 * 根据实体、列(名)、值模糊查询数据
	 * @param entity
	 *            实体名
	 * @param params 主参数
	 * @param OrMap Or条件查询参数
	 * @param page 页数
	 * @param size 数目
	 * @return List 
	 */
	public List<Object> findByPropertyOr(Class entity, Map<String, Object> params,Map<String,List<Object>> orMap ,int page, int size);

	
	/**
	 * 根据实体、列(名)、值模糊查询数据
	 * 
	 * @param entity
	 *            实体名
	 * @param params 主参数
	 * @param blurryMap 模糊查询参数
	 * @param page 页数
	 * @param size 数目
	 * @return List 
	 */
	public List<Object> findByPropertyLike(Class entity, Map<String, Object> params,Map<String,Object> blurryMap ,int page, int size);

	/**
	 * 模糊查询总数
	 * @param entity 类
	 * @param params 主参数
	 * @param blurryMap 模糊查询参数 
	 * @return
	 */
	long findByPropertyLikeCount(Class entity, Map<String,Object> params,Map<String,Object> blurryMap);
}
