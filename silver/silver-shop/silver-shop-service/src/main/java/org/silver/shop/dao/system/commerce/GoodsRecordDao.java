package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

import com.justep.baas.data.Table;

public interface GoodsRecordDao<T> {

	/**
	 * 查询商户商品基本信息,并倒序
	 * 
	 * @param params
	 *            查询参数
	 * @param descParam
	 *            倒序参数
	 * @param page
	 *            页数
	 * @param size
	 *            数据条数
	 * @return List
	 */
	public List<T> findGoodsBaseInfo(Map<String, Object> params, String descParam, int page, int size);

	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * 
	 * @param entity
	 *            实体类Class
	 * @param params
	 *            查询参数
	 * @param page
	 *            页数
	 * @param size
	 *            数据条数
	 */
	public List<T> findByProperty(Class entity, Map params, int page, int size);

	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @return Long
	 */
	public Long findLastId();

	/**
	 * 根据实体、列(名)、值查询数据 倒序
	 * 
	 * @param entity
	 *            实体名
	 * @param params
	 *            属性
	 * @param descParams
	 *            倒序参数
	 * @param page
	 *            页数
	 * @param size
	 *            数据条数
	 * @return List
	 */
	public List<T> findByPropertyDesc(Class entity, Map<String, Object> params, String descParams, int page,
			int size);

	/**
	 * 根据年份查询当前年份下的流水号总数
	 * 
	 * @param entity
	 *            实体类Class
	 * @param property
	 *            查询表中列销属性名
	 * @param year
	 *            年份
	 * @return String
	 */
	public long findSerialNoCount(Class entity, String property, int year);

	/**
	 * 将实体类实例化
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean add(Object entity);

	/**
	 * 根据实体类删除信息
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean delete(Object entity);

	/**
	 * 根据实体类更新数据库信息
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean update(Object entity);

	/**
	 * 更新商品备案信息状态
	 * 
	 * @param tableName
	 *            更新表名
	 * @param merchantIdColumnName
	 *            商户ID表中的列名
	 * @param merchantId
	 *            商户ID
	 * @param goodsSerialNo
	 *            商城商品流水号
	 * @param status
	 *            状态码：
	 * @return boolean
	 */
	public boolean updateGoodsRecordStatus(String tableName, String merchantIdColumnName, String merchantId,
			String goodsSerialNo, int status);

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
	 * 
	 * @param entity
	 *            实体名
	 * @param params 主参数
	 * @param blurryMap 模糊查询参数
	 * @param page 页数
	 * @param size 数目
	 * @return List 
	 */
	public List<Object> findByPropertyLike(Class entity, Map<String, Object> params,Map blurryMap ,int page, int size);

	
	/**
	 * 模糊查询总数
	 * @param entity 类
	 * @param params 主参数
	 * @param blurryMap 模糊查询参数 
	 * @return
	 */
	public long findByPropertyLikeCount(Class entity, Map<String,Object> params,Map<String,Object> blurryMap);
	
	/**
	 * 
	 * @param page
	 * @param size
	 * @return
	 */
	public Table findByRecordInfo(String merchantId ,int page,int size);
	
	
	/**
	 * 模糊商户备案信息查询数据
	 * 
	 * @param entity
	 *            实体名
	 * @param params 主参数
	 * @param blurryMap 模糊查询参数
	 * @param page 页数
	 * @param size 数目
	 * @return List 
	 */
	public Table findByRecordInfoLike(Class entity, Map<String, Object> params,Map blurryMap ,int page, int size);

}
