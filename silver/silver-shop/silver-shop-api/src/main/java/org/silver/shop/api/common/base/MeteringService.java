package org.silver.shop.api.common.base;

import java.util.List;

public interface MeteringService {

	/**
	 * 查询所有计量单位
	 * @return list
	 */
	public List<Object> findAllMetering();

}
