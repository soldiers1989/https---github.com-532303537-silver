package org.silver.shop.dao.system.tenant;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.shop.model.system.tenant.MerchantBankInfo;

public interface MerchantBankInfoDao extends BaseDao {
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
	 * 将实体类中的数据实例化
	 * 
	 * @param entity
	 * @return
	 */
	public boolean add(MerchantBankInfo entity);

	/**
	 * 根据实体更新数据
	 * 
	 * @param entity
	 * @return
	 */
	public boolean update(Class entity);
	
	/**
	 * 根据实体删除数据
	 * @param entity
	 * @return
	 */
	public boolean delete(Class entity);
}
