package org.silver.shop.dao.system.organization;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;

public interface MerchantDao<T> extends BaseDao<T>{
	
	/**
	 * 检查商户名是否存在
	 * @param entity 实体类
	 * @param account 商户名称
	 * @return list
	 */
	public List<Object> checkMerchantName(Class<T> entity, Map params,int page , int size);
	
	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * @return long
	 */
	public Long findLastId();
	
	/**
	 * 保存商户基本信息
	 * @param entity
	 * @return
	 */
	public boolean saveMerchantContent(Object entity);
	
	/**
	 * 保存商户对应的备案信息
	 * @param entity
	 * @return
	 */
	public boolean savenMerchantRecordInfo(Object entity);
}
