package org.silver.shop.dao.system.organization;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;


public interface ManagerDao<T>  extends BaseDao<T>{

	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @return long
	 */
	public long findLastId();

}
