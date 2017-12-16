package org.silver.shop.service.system.manual;

import java.util.Map;

import org.silver.shop.api.system.manual.YMorderService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("yMorderBusiness")
public class YMorderBusiness {

	@Reference
	private YMorderService yMorderService;
	
	 
	public Map<String, Object> doBusiness(String merchant_no, String out_trade_no, String amount, String notify_url,
			String extra_common_param, String client_sign, String timestamp) {
	
		return yMorderService.doBusiness( merchant_no, out_trade_no, amount,  notify_url,
				 extra_common_param,  client_sign, timestamp);
	}

	public Map<String, Object> doCallBack(String order_id, String trade_no, String trade_status) {
		// TODO Auto-generated method stub
		return yMorderService.doCallBack(order_id,trade_no,trade_status);
	}

}
