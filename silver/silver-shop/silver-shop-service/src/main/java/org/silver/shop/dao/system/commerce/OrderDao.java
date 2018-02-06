package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.organization.Member;

import com.justep.baas.data.Table;

public interface OrderDao<T> extends BaseDao{
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
	 * 根据年份查询当前年份下的流水号总数
	 * @param entity
	 * @param property
	 * @param year
	 * @return
	 */
	public long findSerialNoCount(Class entity,String property, int year);

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
	 * 根据商户Id查询商户每日订单报表
	 * @param class1
	 * @param paramsMap
	 * @param page
	 * @param size
	 * @return 
	 */
	public Table getOrderDailyReport(Class<T> class1, Map<String, Object> paramsMap, int page, int size);


	/**
	 * 管理员临时删除测试账号下的所有数据
	 * @return
	 */
	public boolean managerDeleteTestOrder();
}
