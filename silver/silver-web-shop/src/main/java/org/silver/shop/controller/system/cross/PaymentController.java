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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.cross.PaymentTransaction;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 支付单Controller
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
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("支付流水号信息不能为空!")).toString();
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
		return JSONObject.fromObject(paytemTransaction.updatePayRecordInfo(datasMap)).toString();
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
	public String getMerchantPaymentReport(HttpServletRequest req, HttpServletResponse response, String startDate,
			String endDate) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(paytemTransaction.getMerchantPaymentReport(startDate, endDate)).toString();
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
	public String createMpayByOID(HttpServletResponse resp, HttpServletRequest req,
			 String orderID,  String memberId) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Enumeration<String> itkeys = req.getParameterNames();
		List<String> orderIDs = new ArrayList<>();
		String key = "";
		while (itkeys.hasMoreElements()) {
			key = itkeys.nextElement();
			if (!"memberId".equals(key)) {
				String value = req.getParameter(key).trim();
				orderIDs.add(value);
			}
		}
		if (!orderIDs.isEmpty()) {
			return JSONObject.fromObject(paytemTransaction.groupCreateMpay(orderIDs, memberId)).toString();
		}

		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("支付单Id不能为空!")).toString();
	}

	@RequestMapping(value = "/managerGetPaymentReport", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询支付单报表")
	@RequiresPermissions("paymentReport:managerGetPaymentReport")
	public String managerGetPaymentReport(HttpServletRequest req, HttpServletResponse response, String startDate,
			String endDate, String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(paytemTransaction.managerGetPaymentReport(startDate, endDate, merchantId))
				.toString();
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
	@ApiOperation("管理员查询商户手工支付单信息")
	@RequiresPermissions("manualPayment:managerGetMpayInfo")
	public String managerGetMpayInfo(HttpServletResponse resp, HttpServletRequest req, @RequestParam("page") int page,
			@RequestParam("size") int size) {
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
	@RequiresPermissions("manualPayment:managerEditMpayInfo")
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

	@RequestMapping(value = "/getAgentPaymentReport", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Agent")
	@ApiOperation("代理商查询旗下所有商户支付单报表信息")
	public String getAgentPaymentReport(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		datasMap.put("startDate", startDate);
		datasMap.put("endDate", endDate);
		if (!datasMap.isEmpty()) {
			return JSONObject.fromObject(paytemTransaction.getAgentPaymentReport(datasMap)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
	}

	/**
	 * 管理员隐藏(对于商户=删除)手工支付单信息
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/managerHideMpayInfo", produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresPermissions("manualPayment:managerHideMpayInfo")
	@ApiOperation("管理员隐藏(对于商户=删除)手工支付单信息")
	public String managerHideMpayInfo(HttpServletResponse resp, HttpServletRequest req,
			@RequestParam("paymentIdPack") String paymentIdPack) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		JSONArray jsonArray = null;
		try {
			jsonArray = JSONArray.fromObject(paymentIdPack);
		} catch (Exception e) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数格式错误!")).toString();
		}
		if (jsonArray.isEmpty()) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		return JSONObject.fromObject(paytemTransaction.managerHideMpayInfo(jsonArray)).toString();
	}

	/**
	 * 根据支付单流水号校验订单是否全部属于一个口岸
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/checkPaymentPort", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	public String checkPaymentPort(HttpServletResponse resp, HttpServletRequest req) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Enumeration<String> iskeys = req.getParameterNames();
		List<String> tradeNos = new ArrayList<>();
		String key = "";
		while (iskeys.hasMoreElements()) {
			key = iskeys.nextElement();
			String value = req.getParameter(key).trim();
			tradeNos.add(value);
		}
		if (!tradeNos.isEmpty()) {
			return JSONObject.fromObject(paytemTransaction.checkPaymentPort(tradeNos)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("支付单Id不能为空!")).toString();
	}

	/**
	 * 公开性第三方商城平台 查询支付单信息入口
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getThirdPartyInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("公开性第三方商城平台 查询支付单信息入口")
	public String getThirdPartyInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		if (StringEmptyUtils.isEmpty(datasMap.get("merchantId"))) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("商户Id不能为空,请核对信息!")).toString();
		}
		/*
		 * if(StringEmptyUtils.isEmpty(datasMap.get("thirdPartyId")) ){ return
		 * JSONObject.fromObject(ReturnInfoUtils.errorInfo(
		 * "支付单第三方业务Id不能为空,请核对信息!")).toString(); }
		 */
		return JSONObject.fromObject(paytemTransaction.getThirdPartyInfo(datasMap)).toString();
	}

	@RequestMapping(value = "/managerGetPaymentReportInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("新-管理员查询支付单报表信息")
	@RequiresRoles("Manager")
	// @RequiresPermissions("orderReport:managerGetOrderReportInfo")
	public String managerGetPaymentReportInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate,String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		if (params.isEmpty()) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		return JSONObject.fromObject(paytemTransaction.managerGetPaymentReportInfo(params)).toString();
	}
	
	@RequestMapping(value = "/managerGetPaymentReportDetails", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("新-管理员查询支付单报表详情")
	@RequiresRoles("Manager")
	// @RequiresPermissions("orderReport:managerGetOrderReportInfo")
	public String managerGetPaymentReportDetails(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		if (params.isEmpty()) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		return JSONObject.fromObject(paytemTransaction.managerGetPaymentReportDetails(params)).toString();
	}
	
	public static void main(String[] args) {
		Map<String, Object> item = new HashMap<>();
		// YM180125052191327
		// YM180125052181629
		// YM180125052176708
		// item.put("a", "YM180125052209119");
		// 2
		item.put("b", "01O180206003352760");
		// 1
		item.put("c", "01O180507014605478");
		System.out.println("------->>"
				+ YmHttpUtil.HttpPost("http://localhost:8080/silver-web-shop/payment/checkPaymentPort", item));
	}
}
