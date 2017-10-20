package org.silver.sys.api;

import java.util.Map;

/**
 * 商户--商品备案查询
 * @author Administrator
 *
 */
public interface CommodityInquiryService {

	/**
	 * 
	 * @param tenantNo
	 * @param type
	 * @param page
	 * @param size
	 * @return
	 */
	public Map<String, Object> findAllRecordsByAppkey(String tenantNo, int type,int page,int size);
}
