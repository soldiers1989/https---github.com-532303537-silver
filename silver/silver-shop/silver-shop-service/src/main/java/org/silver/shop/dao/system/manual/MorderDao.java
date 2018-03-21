package org.silver.shop.dao.system.manual;

import java.util.List;

import org.silver.shop.dao.BaseDao;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.Table;

@Repository("morderDao")
public interface MorderDao extends BaseDao {

	/**
	 * 
	 * @param merchantId
	 * @param date
	 * @param serialNo
	 * @return
	 */
	public Table getOrderAndOrderGoodsInfo(String merchantId, String date, int serialNo);

	/**
	 * 根据日期筛选商户下手工订单信息关联的所有商品信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return Table
	 */
	public Table getMOrderAndMGoodsInfo(String merchantId, String startDate, String endDate, int page, int size);

	/**
	 * 获取手工订单与对应的商品信息数量
	 * 
	 * @param merchantId
	 * @param startDate
	 * @param endDate
	 * @param page
	 * @param size
	 * @return
	 */
	public long getMOrderAndMGoodsInfoCount(String merchantId, String startDate, String endDate, int page, int size);

	/**
	 * 统计手工订单信息实际支付金额的总额
	 * @param itemList 订单Id集合
	 * @return double
	 */
	public double statisticalManualOrderAmount(List<Object> itemList);
}
