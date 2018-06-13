package org.silver.shop.dao.system.organization;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface MerchantDao extends BaseDao {

	/**
	 * 查询数据库表中最后一条记录的自增ID
	 * 
	 * @return long
	 */
	public long findLastId();

	/**
	 * 根据商户Id查询商户关联的用户资金
	 * 
	 * @param merchantId
	 *            商户Id 
	 *@param memberId 用户Id
	 * @param size
	 * @param page
	 */
	public Table getRelatedMemberFunds(String merchantId, String memberId, int page, int size);

}
