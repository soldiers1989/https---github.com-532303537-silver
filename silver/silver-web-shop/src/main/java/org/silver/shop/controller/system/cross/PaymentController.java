package org.silver.shop.controller.system.cross;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.shop.service.system.cross.PaymentTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import net.sf.json.JSONObject;

/**
 * 支付Controller
 *
 */
@Controller
@RequestMapping(value = "/payment")
public class PaymentController {
	protected static final Logger logger = LogManager.getLogger();

	@Autowired
	private PaymentTransaction paytemTransaction;
	/**
	 * 备案网关异步回馈备案商品信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/reNotifyMsg", produces = "application/json; charset=utf-8")
	public String reNotifyMsg(HttpServletRequest req, HttpServletResponse response) {
		logger.info("-----备案网关异步回馈支付单信息---");
		Map<String, Object> datasMap = new HashMap<>();
		datasMap.put("status", req.getParameter("status") + "");
		datasMap.put("errMsg", req.getParameter("errMsg") + "");
		datasMap.put("messageID", req.getParameter("messageID") + "");
		datasMap.put("entPayNo", req.getParameter("entPayNo") + "");
		Map<String, Object> statusMap = paytemTransaction.updatePaymentInfo(datasMap);
		logger.info(JSONObject.fromObject(statusMap).toString());
		return JSONObject.fromObject(statusMap).toString();
	}
}
