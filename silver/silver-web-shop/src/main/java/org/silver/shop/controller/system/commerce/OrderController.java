package org.silver.shop.controller.system.commerce;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 订单Controller
 *
 */
@Controller
@RequestMapping("/order")
public class OrderController {

	@RequestMapping
	@ResponseBody
	
	public String createOrderInfo(){
		
		return null;
	}
}
