package org.silver.shop.dao.system.cross;

import java.util.Map;

import org.silver.shop.dao.BaseDao;

import com.justep.baas.data.Table;

public interface ReportsDao extends BaseDao{
	
	/**
	 * 根据日期、商户id查询对应的身份证实名认证报表数据
	 * @param params
	 * @return
	 */
	public Table getIdCardDetails(Map<String, Object> params);

	/**
	 * 获取身份证认证报表详情
	 * @param params 
	 * @return
	 */
	public Table getIdCardCertificationDetails(Map<String, Object> params);
}
