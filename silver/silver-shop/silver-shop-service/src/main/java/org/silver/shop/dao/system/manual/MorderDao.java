package org.silver.shop.dao.system.manual;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.shop.model.system.manual.Morder;
import org.springframework.stereotype.Repository;

import com.justep.baas.data.Table;

@Repository("morderDao")
public interface MorderDao extends BaseDao {

	/**
	 * 根据订单日期、商户Id、批次号查询订单及订单商品信息
	 * @param merchantId 商户Id
	 * @param date 日期
	 * @param serialNo 批次号
	 * @return Table
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
	 * 统计未发起过备案的手工订单实际支付金额的总额
	 * @param itemList 订单Id集合 
	 * @return double 总金额
	 */
	public double statisticalManualOrderAmount(List<Object> itemList);

	/**
	 * 根据下单人身份证号码,查询日期下身份证号码出现的次数
	 * @param orderDocId 身份号码
	 * @param endDate 开始时间
	 * @param startDate  结束时间
	 * @return  Table
	 */
	public Table getIdCardCount(String orderDocId, String startDate, String endDate);

	/**
	 * 根据收货人手机号码,查询日期货人手机号码出现的次数
	 * @param recipientTel 收货人手机号码
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return Table
	 */
	public Table getPhoneCount(String recipientTel, String startDate, String endDate);

	/**
	 * 根据集合中Map key-orderNo,value -订单Id ,通过SQL IN 操作符统一查询批量的订单信息
	 * @param itemList 订单Id集合
	 * @return List
	 */
	public List<Morder> findByPropertyIn(List<Map<String,Object>> itemList);
	
	
}
