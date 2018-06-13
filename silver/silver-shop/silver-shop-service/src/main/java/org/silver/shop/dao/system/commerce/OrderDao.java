package org.silver.shop.dao.system.commerce;

import java.util.List;
import java.util.Map;

import org.silver.shop.dao.BaseDao;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.organization.Member;

import com.justep.baas.data.Table;

public interface OrderDao extends BaseDao {

	/**
	 * 查询商户下订单信息
	 * 
	 * @param entity
	 * @param merchantId
	 * @param page
	 * @param size
	 * @return
	 */
	public Table getMerchantOrderInfo(String merchantId, int page, int size);

	/**
	 * 查询超时订单
	 * 
	 * @param time
	 *            时间
	 */
	public List<Object> searchTimOutOrder(Class entity, Map params, String time);

	/**
	 * 根据商户Id查询商户每日订单报表
	 * 
	 * @param paramsMap
	 *            查询参数
	 * @return Table
	 */
	public Table getOrderDailyReport(Map<String, Object> paramsMap);

	/**
	 * 管理员临时删除测试账号下的所有数据
	 * 
	 * @return
	 */
	public boolean managerDeleteTestOrder();

	/**
	 * 商城商家后台并集查询所有手工订单及商城订单
	 * 
	 * @return List
	 */
	public List<OrderRecordContent> merchantuUnionOrderInfo(Class entity, Map<String, Object> paramsMap,
			Map<String, Object> viceParams, int page, int size);

	/**
	 * 商城商家后台并集查询所有手工订单及商城订单统计总数
	 * 
	 * @param entity
	 *            实体类
	 * @param paramMap
	 * @param viceParams
	 * @return
	 */
	public long merchantuUnionOrderCount(Class<OrderRecordContent> entity, Map<String, Object> paramMap,
			Map<String, Object> viceParams);

	/**
	 * 代理商获取订单报表信息
	 * 
	 * @param datasMap
	 * @return
	 */
	public Table getAgentOrderReport(Map<String, Object> datasMap);

	/**
	 * 获取每日订单报表信息
	 * @param paramsMap
	 * @return
	 */
	public Table getOrderDailyReportInfo(Map<String, Object> paramsMap);

	/**
	 * 获取商户订单报表详情
	 * @param params
	 * @return
	 */
	public Table getOrderDailyReportetDetails(Map<String, Object> params);

}
