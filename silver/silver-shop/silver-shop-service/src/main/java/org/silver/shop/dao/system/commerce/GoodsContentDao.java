package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.commerce.GoodsContent;

public interface GoodsContentDao {

	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @return
	 */
	public Long findLastId();

	/**
	 * 
	 * @param goods
	 * @return
	 */
	public boolean add(GoodsContent goods);

	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public List<Object> findByProperty(Class entity, Map params, int page, int size);

	/**
	 * 更新实体
	 * @param goodsInfo
	 */
	public boolean update(GoodsContent entity);

	/**
	 * 模糊查询数据匹配时间段参数
	 * 
	 * @param entity 实体类名
	 * @param params 属性键值对
	 * @param page	页面属性
	 * @param size	页面数值
	 * @param startTime 开始时间
	 * @param endTime	结束时间
	 * @return List
	 */
	public List<Object> findBlurryProperty(Class entity,Map params ,String startTime,String endTime,int page,int size);
	
	
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
	public long findSerialNoCount(Class entity,String property,int year);
}
