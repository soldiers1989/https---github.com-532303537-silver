package org.silver.shop.controller.system.cross;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.cross.PaymentTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

/**
 * 支付单Controller
 *
 */
@Controller
@RequestMapping(value = "/payment")
public class PaymentController {
	protected static final Logger logger = LogManager.getLogger();

	@Autowired
	private PaymentTransaction paytemTransaction;

	/**
	 * 备案网关异步回馈银盛支付单信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/reNotifyMsg", produces = "application/json; charset=utf-8")
	@ResponseBody
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

	/**
	 * 发起支付单备案
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/sendMpayRecord", produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	public String sendMpayRecord(HttpServletResponse resp, HttpServletRequest req, String tradeNoPack) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<>();
		Map<String, Object> recordMap = new HashMap<>();
		Enumeration<String> itkeys = req.getParameterNames();
		String key = "";
		while (itkeys.hasMoreElements()) {
			key = itkeys.nextElement();
			String value = req.getParameter(key);
			recordMap.put(key, value);
		}
		if (!recordMap.isEmpty()) {
			return JSONObject.fromObject(paytemTransaction.sendMpayRecord(recordMap, tradeNoPack)).toString();
		}
		reqMap.put("status", -3);
		reqMap.put("msg", "缺少支付流水号");
		return JSONObject.fromObject(reqMap).toString();
	}

	/**
	 * 备案网关异步回馈手工支付单信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/rePayNotifyMsg", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String rePayNotifyMsg(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		datasMap.put("status", req.getParameter("status") + "");
		datasMap.put("errMsg", req.getParameter("msg") + "");
		datasMap.put("messageID", req.getParameter("messageID") + "");
		datasMap.put("entPayNo", req.getParameter("entPayNo") + "");
		Map<String, Object> statusMap = paytemTransaction.updatePayRecordInfo(datasMap);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 查询商户支付单信息
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/getMpayRecordInfo", produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	public String getMpayRecordInfo(HttpServletResponse resp, HttpServletRequest req, @RequestParam("page") int page,
			@RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(paytemTransaction.getMpayRecordInfo(req,page,size)).toString();
	}
}
