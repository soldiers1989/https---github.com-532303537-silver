package org.silver.shop.dao.system.organization;


import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface MemberDao extends BaseDao{
	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @return long
	 */
	public long findLastId();

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
	 * 关联表查询当前用户下订单及商品
	 * @param memberId
	 * @param page
	 * @param size
	 * @return
	 */
	public Table findOrderInfo(String memberId, int page,int size);
}
