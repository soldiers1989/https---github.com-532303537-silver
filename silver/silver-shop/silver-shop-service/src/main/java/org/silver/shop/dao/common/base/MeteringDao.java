package org.silver.shop.dao.common.base;

import java.util.List;

public interface MeteringDao {

	/**
	 * 查询所有计量单位
	 * @return
	 */
	public List<Object> findMetering();
}
