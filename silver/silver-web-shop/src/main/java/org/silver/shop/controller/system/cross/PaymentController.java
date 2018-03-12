package org.silver.shop.controller.system.cross;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.cross.PaymentTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
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
	 * 发起手工支付单备案
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
		datasMap.put("msg", req.getParameter("msg") + "");
		datasMap.put("messageID", req.getParameter("messageID") + "");
		datasMap.put("entPayNo", req.getParameter("entPayNo") + "");
		Map<String, Object> statusMap = paytemTransaction.updatePayRecordInfo(datasMap);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 查询商户手工支付单信息
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
		return JSONObject.fromObject(paytemTransaction.getMpayRecordInfo(req, page, size)).toString();
	}

	@RequestMapping(value = "/getMerchantPaymentReport", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户查询支付单报表")
	@RequiresRoles("Merchant")
	public String getMerchantPaymentReport(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size, String startDate, String endDate) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (page >= 0 && size >= 0) {
			statusMap = paytemTransaction.getMerchantPaymentReport(page, size, startDate, endDate);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/********************************** 模拟生成支付信息 *********************************/

	/**
	 * 根据订单号生成支付单（支持批量）
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/createMpayByOID", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	public String createMpayByOID(HttpServletResponse resp, HttpServletRequest req, int length) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<>();
		Enumeration<String> itkeys = req.getParameterNames();
		List<String> orderIDs = new ArrayList<>();
		String key = "";
		while (itkeys.hasMoreElements()) {
			key = itkeys.nextElement();
			String value = req.getParameter(key).trim();
			orderIDs.add(value);
		}
		orderIDs.remove(length);
		if (!orderIDs.isEmpty()) {
			return JSONObject.fromObject(paytemTransaction.groupCreateMpay(orderIDs)).toString();
		}
		reqMap.put("status", -3);
		reqMap.put("msg", "缺少订单编号，生成失败");
		return JSONObject.fromObject(reqMap).toString();
	}

	@RequestMapping(value = "/managerGetPaymentReport", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询支付单报表")
	@RequiresRoles("Manager")
	public String managerGetPaymentReport(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size, String startDate, String endDate,
			String merchantName) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (page >= 0 && size >= 0) {
			statusMap = paytemTransaction.managerGetPaymentReport(page, size, startDate, endDate, merchantName);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 管理员查询所有商户手工支付单信息
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/managerGetMpayInfo", produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	public String managerGetMpayRecordInfo(HttpServletResponse resp, HttpServletRequest req,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		return JSONObject.fromObject(paytemTransaction.managerGetMpayInfo(params, page, size)).toString();
	}

	/**
	 * 管理员修改商户手工支付单信息
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/managerEditMpayInfo", produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	public String managerEditMpayInfo(HttpServletResponse resp, HttpServletRequest req) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		return JSONObject.fromObject(paytemTransaction.managerEditMpayInfo(params)).toString();
	}
}
