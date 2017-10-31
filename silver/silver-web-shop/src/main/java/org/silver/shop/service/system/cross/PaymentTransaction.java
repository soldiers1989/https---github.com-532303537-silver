package org.silver.shop.service.system.cross;

import java.util.Map;

import org.silver.shop.api.system.cross.PaymentService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service
public class PaymentTransaction {

	@Reference
	private PaymentService paymentService;

	public  Map<String, Object> updatePaymentInfo(Map<String, Object> datasMap) {
		return paymentService.updatePaymentStatus(datasMap);
	}

}
