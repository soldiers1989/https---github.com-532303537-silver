package org.silver.shop.dao.system.manual;

import org.silver.shop.dao.BaseDao;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.Table;

@Repository("morderDao")
public interface MorderDao extends BaseDao {

	public Table getOrderAndOrderGoodsInfo(String merchantId,String date,int serialNo);
}
