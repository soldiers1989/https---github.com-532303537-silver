package org.silver.shop.task;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.shop.component.ManualOrderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import net.sf.json.JSONArray;

/**
 * 商户订单申报时,进行钱包金额清算
 */
public class MerchantWalletTollTask implements Callable<Object> {
	
	private static Logger logger = LogManager.getLogger(Object.class);
	private String merchantId;// 商户Id
	private JSONArray orderList;// 订单集合
	private ManualOrderInterceptor orderInterceptor;
	private JSONArray idCardList;// 身份证订单id集合
	
	public MerchantWalletTollTask(JSONArray orderList, String merchantId, ManualOrderInterceptor orderInterceptor, JSONArray idCardList) {
		this.merchantId = merchantId;
		this.orderList = orderList;
		this.orderInterceptor = orderInterceptor;
		this.idCardList = idCardList;
	}

	@Override
	public Map<String, Object> call() {
		try {
			long startTime = System.currentTimeMillis();
			Map<String, Object> reTollMap = orderInterceptor.merchantWalletToll(orderList, merchantId,idCardList);
			long endTime = System.currentTimeMillis();
			logger.error("---商户实名认证与订单申报手续费---结果--耗时-" + (endTime - startTime) + "ms;==>>>" + reTollMap.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("--商户实名认证与订单申报手续费--错误--", e);
		}
		return null;
	}
}
