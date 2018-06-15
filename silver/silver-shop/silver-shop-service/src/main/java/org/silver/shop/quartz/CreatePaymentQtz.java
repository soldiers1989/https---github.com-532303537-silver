package org.silver.shop.quartz;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.manual.Morder;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 定时任务,扫描商户自助申报的订单,生成支付单信息
 */
public class CreatePaymentQtz {
	/**
	 * 驼峰命名:商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 驼峰命名：订单Id
	 */
	private static final String ORDER_ID = "orderId";
	/**
	 * 口岸
	 */
	private static final String E_PORT = "eport";

	/**
	 * 检验检疫机构代码
	 */
	private static final String CIQ_ORG_CODE = "ciqOrgCode";

	/**
	 * 主管海关代码
	 */
	private static final String CUSTOMS_CODE = "customsCode";
	/**
	 * 计数器
	 */
	private static AtomicInteger counter = new AtomicInteger(0);

	private static Logger logger = LogManager.getLogger(Object.class);

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private PaymentService paymentService;

	/**
	 * 定时任务扫描商户自助申报的订单,未生成支付流水的订单信息
	 */
	public void createPaymentJob() {
		if(counter.get() % 10 == 0){
			System.out.println("---扫描需要生成支付单信息的订单---");
		}
		Map<String, Object> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -3);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		// 设置为24小时制
		params.put("startTime", calendar.getTime());
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		params.put("endTime", calendar.getTime());
		// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
		params.put("order_record_status", 10);
		// 网关接收状态： 0-未发起,1-已发起,2-接收成功,3-接收失败
		params.put("status", 1);
		params.put("tradeNoFlag", " is null");
		try {
			int page = 1;
			int size = 300;
			List<Morder> reOrderList = orderDao.findByPropertyLike(Morder.class, params, null, page, size);
			while (reOrderList != null && !reOrderList.isEmpty()) {
				if (page > 1) {
					reOrderList = orderDao.findByPropertyLike(Morder.class, params, null, page, size);
				}
				if (reOrderList != null && !reOrderList.isEmpty()) {
					for (Morder order : reOrderList) {
						// 保存支付单并且更新订单流水号
						if (!savePayment(order)) {
							logger.error(order.getOrder_id() + "--创建交易流水号失败--");
						}
					}
				}
				page++;
			}
			counter.getAndIncrement();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("--定时扫描待发起备案的订单信息错误--", e);
		}
	}

	/**
	 * 保存支付单信息实体
	 * @param order 订单信息实体类
	 * @return
	 */
	private boolean savePayment(Morder order) {
		if (order == null) {
			return false;
		}
		Map<String, Object> paymentMap = new HashMap<>();
		String merchantId = order.getMerchant_no();
		paymentMap.put(MERCHANT_ID, merchantId);
		int count = SerialNoUtils.getSerialNo("paymentId");
		String tradeNo = SerialNoUtils.createTradeNo("01O", (count + 1), new Date());
		paymentMap.put("tradeNo", tradeNo);
		paymentMap.put(ORDER_ID, order.getOrder_id());
		paymentMap.put("amount", order.getActualAmountPaid());
		paymentMap.put("orderDocName", order.getOrderDocName());
		paymentMap.put("orderDocId", order.getOrderDocId());
		paymentMap.put("orderDocTel", order.getOrderDocTel());
		paymentMap.put("orderDate", order.getOrderDate());
		paymentMap.put("createBy", order.getCreate_by());
		paymentMap.put(E_PORT, order.getEport());
		paymentMap.put(CIQ_ORG_CODE, order.getCiqOrgCode());
		paymentMap.put(CUSTOMS_CODE, order.getCustomsCode());
		paymentMap.put("thirdPartyId", order.getThirdPartyId());
		Map<String, Object> checkInfoMap = new HashMap<>();
		checkInfoMap.put(ORDER_ID, order.getOrder_id());
		checkInfoMap.put("orderDocId", order.getOrderDocId());
		checkInfoMap.put("orderDocName", order.getOrderDocName());
		//申报状态：1-待申报、2-申报中、3-申报成功、4-申报失败、10-申报中(待系统处理)
		paymentMap.put("pay_record_status", 10);
		Map<String, Object> reCheckMap = paymentService.checkPaymentInfo(checkInfoMap);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			logger.error("系统扫描自助申报订单,创建支付单失败->" + reCheckMap.get(BaseCode.MSG.toString()));
			return false;
		} else {
			return paymentService.addEntity(paymentMap)
					&& paymentService.updateOrderPayNo(merchantId, order.getOrder_id(), tradeNo);
		}
	}

}
