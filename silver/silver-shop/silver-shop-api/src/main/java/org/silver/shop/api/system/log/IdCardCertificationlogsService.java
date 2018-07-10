package org.silver.shop.api.system.log;

import java.util.Map;


public interface IdCardCertificationlogsService {

	/**
	 * 查询身份证实名认证记录
	 * @param datasMap 查询条件
	 * @param page 页数
	 * @param size 数目
	 * @return
	 */
	public Map<String,Object> getlogsInfo(Map<String, Object> datasMap, int page, int size);

}
