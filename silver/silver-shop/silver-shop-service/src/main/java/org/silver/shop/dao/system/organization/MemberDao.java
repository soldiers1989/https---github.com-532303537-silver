package org.silver.shop.dao.system.organization;

import java.util.List;
import java.util.Map;

import com.justep.baas.data.Table;

public interface MemberDao {
	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @return long
	 */
	public long findLastId();

	/**
	 * 保存商户信息
	 * 
	 * @param entity
	 * @return
	 */
	public boolean add(Object entity);

	/**
	 * 根据实体,及Map键值对查询数据 key=(表中列名称),value=(查询参数)
	 * 
	 * @param entity
	 * @param params
	 * @param page
	 * @param size
	 */
	public List<Object> findByProperty(Class entity, Map params, int page, int size);

	/**
	 * 根据实体更新数据
	 * 
	 * @param entity
	 */
	public boolean update(Object entity);
	
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
	/**
	 * 根据实体类删除信息
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public boolean delete(Object entity);
	
	/**
	 * 模糊查询总数
	 * @param entity 类
	 * @param params 查询参数
	 * @return
	 */
	public long findByPropertyCount(Class entity,Map params);
	
	
	/**
	 * 关联表查询当前用户下订单及商品
	 * @param memberId
	 * @param page
	 * @param size
	 * @return
	 */
	public Table findOrderInfo(String memberId, int page,int size);
}
