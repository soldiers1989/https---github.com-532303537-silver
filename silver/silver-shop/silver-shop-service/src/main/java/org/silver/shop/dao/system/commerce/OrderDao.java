package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.organization.Member;

import com.justep.baas.data.Table;

public interface OrderDao {
	/**
	 * 根据实体查询所有数据(带分页查询)
	 * 
	 * @param <T>
	 * @param entity
	 * @param page
	 * @param size
	 * @return list
	 */
	public List<Object> findAll(Class entity, int page, int size);

	/**
	 * 根据实体、列(名)、值查询数据
	 * 
	 * @param entity
	 *            实体名
	 * @param params
	 *            属性
	 * @param page
	 * @param size
	 * @return List
	 */
	public List findByProperty(Class entity, Map<String, Object> params, int page, int size);

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
	 * 待说明
	 * 
	 * @param id
	 * @return
	 */
	public Member findMailboxbyId(long id);

	/**
	 * 统计数据库表中数据数量
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public Long findAllCount(Class entity);

	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @param entity
	 *            实体类名
	 * @return
	 */
	public long findLastId(Class entity);

	/**
	 * 模糊查询数据匹配时间段参数
	 * 
	 * @param entity
	 *            实体类名
	 * @param params
	 *            属性键值对
	 * @param page
	 *            页面属性
	 * @param size
	 *            页面数值
	 * @param startTime
	 *            开始时间 yyyy-dd-mm
	 * @param endTime
	 *            结束时间 yyyy-dd-mm
	 * @return List
	 */
	public List<Object> findBlurryProperty(Class entity, Map<String, Object> params, String startTime,
			String endTime, int page, int size);

	/**
	 * 根据实体、列(名)、值查询数据 倒序
	 * 
	 * @param entity
	 *            实体名
	 * @param params
	 *            属性
	 * @param descParams
	 *            倒序参数
	 * @param page 页数
	 * @param size 数据条数
	 * @return List
	 */
	public List findByPropertyDesc(Class entity, Map<String, Object> params, String descParams, int page,
			int size);

	/**
	 * 根据年份查询当前年份下的流水号总数
	 * @param entity
	 * @param property
	 * @param year
	 * @return
	 */
	public long findSerialNoCount(Class entity,String property, int year);


	/**
	 * 模糊查询总数
	 * @param entity 类
	 * @param params 查询参数
	 * @return
	 */
	public long findByPropertyCount(Class entity,Map<String,Object> params);

	/**
	 * 查询商户下订单信息
	 * @param entity
	 * @param merchantId
	 * @param page
	 * @param size
	 * @return
	 */
	public Table getMerchantOrderInfo( String merchantId, int page, int size);

	/**
	 * 查询超时订单
	 * @param time 时间
	 */
	public List<Object> searchTimOutOrder(Class entity, Map params,String time);
	
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
	long findByPropertyLikeCount(Class entity, Map<String,Object> params,Map<String,Object> blurryMap);
}
