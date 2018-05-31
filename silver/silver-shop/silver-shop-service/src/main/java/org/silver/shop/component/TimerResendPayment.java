package org.silver.shop.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.impl.system.cross.PaymentServiceImpl;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.manual.PaymentCallBack;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 跟随项目启动扫描支付单第三方回执
 *
 */
public class TimerResendPayment implements InitializingBean {

	private static Logger logger = LogManager.getLogger(TimerResendPayment.class);
	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private PaymentServiceImpl paymentServiceImpl;

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("支付回调定时器启动-----");
		//reminder();
	}

	public void reminder() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				resendPayment();
			}
			// 项目启动后90秒开始扫描， 暂定循环为60秒启动一次
		}, 90000, 60000);
	}

	/**
	 * 通过操纵缓存实现一个支付单重返10次
	 */
	private void resendPayment() {
		try {
			System.out.println("--扫描支付单返回次数10次以下与返回状态为FALSE--");
			Map<String, Object> params = new HashMap<>();
			params.put("resendStatus", "FALSE");
			List<PaymentCallBack> paymentList = paymentDao.getFailPaymentInfo(PaymentCallBack.class, params, 1, 200);
			if (paymentList != null && !paymentList.isEmpty()) {
				for (PaymentCallBack paymentCallBack : paymentList) {
					// 根据交易流水号/订单Id/商户Id查询支付单信息
					params.clear();
					params.put("trade_no", paymentCallBack.getTradeNo());
					params.put("morder_id", paymentCallBack.getOrderId());
					params.put("merchant_no", paymentCallBack.getMerchantId());
					List<Mpay> rePaymentList = paymentDao.findByProperty(Mpay.class, params, 0, 0);
					if (rePaymentList != null && !rePaymentList.isEmpty()) {
						paymentServiceImpl.rePaymentInfo(rePaymentList.get(0));
					}
				}
			}
		} catch (Exception e) {
			logger.error("----扫描回调支付单错误-----", e);
		}
	}
}
