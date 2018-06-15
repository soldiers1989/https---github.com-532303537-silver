package org.silver.shop.controller.system.commerce;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.controller.system.cross.DirectPayConfig;
import org.silver.shop.service.system.commerce.OrderTransaction;
import org.silver.util.DateUtil;
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
import net.sf.json.JSONObject;

/**
 * 订单Controller
 *
 */
@Controller
@RequestMapping("/order")
public class OrderController {
	protected static final Logger logger = LogManager.getLogger();
	@Autowired
	private OrderTransaction orderTransaction;

	@RequestMapping(value = "/createOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Member")
	@ApiOperation("用户创建订单")
	public String createOrderInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("goodsInfoPack") String goodsInfoPack, @RequestParam("type") int type,
			@RequestParam("recipientId") String recipientId) {

		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.createOrderInfo(goodsInfoPack, type, recipientId);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getMerchantOrderDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户查看订单详情")
	public String getMerchantOrderDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entOrderNo") String entOrderNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.getMerchantOrderDetail(entOrderNo);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 备案网关异步回馈订单备案状态
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/reNotifyMsg", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String reNotifyMsg(HttpServletRequest req, HttpServletResponse response) {
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
		return JSONObject.fromObject(orderTransaction.updateOrderRecordInfo(datasMap)).toString();
	}

	/**
	 * 用户下单时，检查订单商品是否都属于一个海关口岸
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/checkOrderGoodsCustoms", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查订单商品是否都属于一个海关口岸")
	public String checkOrderGoodsCustoms(HttpServletRequest req, HttpServletResponse response,
			String orderGoodsInfoPack, String recipientId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(recipientId)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("收货人地址Id不能为空!")).toString();
		}
		if (StringEmptyUtils.isEmpty(orderGoodsInfoPack)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单商品信息包不能为空!")).toString();
		}
		return JSONObject.fromObject(orderTransaction.checkOrderGoodsCustoms(orderGoodsInfoPack, recipientId))
				.toString();
	}

	@RequestMapping(value = "/getMemberOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "获取用户订单信息")
	@RequiresRoles("Member")
	// @RequiresPermissions("")
	public String getMemberOrderInfo(HttpServletRequest req, HttpServletResponse response, int page, int size) {
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
		datasMap.remove("page");
		datasMap.remove("size");
		return JSONObject.fromObject(orderTransaction.getMemberOrderInfo(page, size, datasMap)).toString();
	}

	@RequestMapping(value = "/getMerchantOrderRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户查看备案订单信息")
	public String getMerchantOrderRecordInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.getMerchantOrderRecordInfo(page, size);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getMemberOrderDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Member")
	@ApiOperation("用户查看订单详情")
	public String getMemberOrderDetail(HttpServletRequest req, HttpServletResponse response, String entOrderNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isNotEmpty(entOrderNo)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单Id不能为空!")).toString();
		}
		return JSONObject.fromObject(orderTransaction.getMemberOrderDetail(entOrderNo)).toString();
	}

	@RequestMapping(value = "/searchMerchantOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("根据指定信息搜索商户订单信息")
	@RequiresRoles("Merchant")
	public String searchMerchantOrderInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (page > 0 && size > 0) {
			statusMap = orderTransaction.searchMerchantOrderInfo(req, page, size);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getMerchantOrderReport", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户查询订单报表")
	@RequiresRoles("Merchant")
	public String getMerchantOrderReport(HttpServletRequest req, HttpServletResponse response, String startDate,
			String endDate) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(orderTransaction.getMerchantOrderReport(startDate, endDate)).toString();
	}

	@RequestMapping(value = "/getManualOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询所有手工订单信息")
	@RequiresPermissions("manualOrder:getManualOrderInfo")
	public String getManualOrderInfo(HttpServletRequest req, HttpServletResponse response, int page, int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.getManualOrderInfo(page, size, req);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/managerGetOrderReport", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询订单报表")
	@RequiresPermissions("orderReport:managerGetOrderReport")
	public String managerGetOrderReport(HttpServletRequest req, HttpServletResponse response, String startDate,
			String endDate, String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(orderTransaction.managerGetOrderReport(startDate, endDate, merchantId)).toString();
	}

	@RequestMapping(value = "/memberDeleteOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Member")
	@ApiOperation("用户删除订单信息")
	public String memberDeleteOrderInfo(HttpServletRequest req, HttpServletResponse response, String entOrderNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isNotEmpty(entOrderNo)) {
			return JSONObject.fromObject(orderTransaction.memberDeleteOrderInfo(entOrderNo)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单Id不能为空!")).toString();
	}

	@RequestMapping(value = "/getAgentOrderReport", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Agent")
	@ApiOperation("代理商查询旗下商户订单报表信息")
	public String getAgentOrderReport(HttpServletRequest req, HttpServletResponse response,
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
			return JSONObject.fromObject(orderTransaction.getAgentOrderReport(datasMap)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
	}

	/**
	 * 管理员获取已移除到历史记录(删除)表中的订单及订单商品信息
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/getAlreadyDelOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员获取已删除的订单信息")
	@RequiresPermissions("manualOrder:getAlreadyDelOrderInfo")
	public String getAlreadyDelOrderInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
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
		datasMap.remove("page");
		datasMap.remove("size");
		return JSONObject.fromObject(orderTransaction.getAlreadyDelOrderInfo(datasMap, page, size)).toString();
	}

	/**
	 * 第三方商城平台传递订单信息入口
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/thirdPartyBusiness", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("第三方商城平台传递订单信息入口")
	public String thirdPartyBusiness(HttpServletRequest req, HttpServletResponse response) {
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
		if (StringEmptyUtils.isEmpty(datasMap.get("datas"))) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单信息不能为空,请核对信息!")).toString();
		}
		if (StringEmptyUtils.isEmpty(datasMap.get("thirdPartyId"))) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("第三方订单Id(唯一标识)不能为空,请核对信息!")).toString();
		}
		return JSONObject.fromObject(orderTransaction.thirdPartyBusiness(datasMap)).toString();
	}

	/**
	 * 公开性第三方商城平台 查询订单信息入口
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getThirdPartyInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("公开性第三方商城平台 查询订单信息入口")
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

		if (StringEmptyUtils.isEmpty(datasMap.get("thirdPartyId"))) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单第三方业务Id不能为空,请核对信息!")).toString();
		}
		return JSONObject.fromObject(orderTransaction.getThirdPartyInfo(datasMap)).toString();
	}

	/**
	 * 推送订单时,根据订单号校验订单是否全部属于一个口岸
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/checkOrderPort", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	public String checkOrderPort(HttpServletResponse resp, HttpServletRequest req) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Enumeration<String> iskeys = req.getParameterNames();
		List<String> orderIDs = new ArrayList<>();
		String key = "";
		while (iskeys.hasMoreElements()) {
			key = iskeys.nextElement();
			String value = req.getParameter(key).trim();
			orderIDs.add(value);
		}
		if (!orderIDs.isEmpty()) {
			return JSONObject.fromObject(orderTransaction.checkOrderPort(orderIDs)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单Id不能为空!")).toString();
	}

	@RequestMapping(value = "/managerGetOrderReportInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("新-管理员查询订单报表信息")
	@RequiresRoles("Manager")
	// @RequiresPermissions("orderReport:managerGetOrderReportInfo")
	public String managerGetOrderReportInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate,String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(orderTransaction.managerGetOrderReportInfo(startDate, endDate,merchantId)).toString();
	}

	@RequestMapping(value = "/managerGetOrderReportDetails", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("新-管理员查询订单报表详情")
	@RequiresRoles("Manager")
	// @RequiresPermissions("orderReport:managerGetOrderReportInfo")
	public String managerGetOrderReportDetails(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate,
			String merchantId) {
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
		return JSONObject.fromObject(orderTransaction.managerGetOrderReportDetails(params)).toString();
	}

	public static void main(String[] args) {
		Map<String, Object> item = new HashMap<>();
		List<JSONObject> orderGoodsList = new ArrayList<>();
		JSONObject order = new JSONObject();
		JSONObject goods = new JSONObject();
		JSONObject goods2 = new JSONObject();
		order.element("EntOrderNo", "TEST1234");
		order.element("OrderStatus", "1");
		order.element("PayStatus", "1");
		order.element("OrderGoodTotal", 2100);
		order.element("OrderGoodTotalCurr", "142");
		order.element("Freight", 0);
		order.element("Tax", 0);
		order.element("OtherPayment", "1");
		order.element("ActualAmountPaid", 210);
		order.element("RecipientName", "收货人姓名");
		order.element("RecipientAddr", "收货人地址");
		order.element("RecipientID", "");
		order.element("RecipientTel", "13812345678");
		order.element("RecipientCountry", "116");
		order.element("RecipientProvincesCode", "110000");
		order.element("OrderDocAcount", "下单人账号");
		order.element("OrderDocName", "下单人先生");
		order.element("OrderDocType", "1");
		order.element("OrderDocId", "37021119770918101X");
		order.element("OrderDocTel", "13812345678");
		order.element("OrderDate", "订单日期");
		order.element("eport", "1");
		order.element("ciqOrgCode", "440300");
		order.element("customsCode", "5165");

		goods.element("Seq", 1);
		goods.element("EntGoodsNo", "TEst321");
		goods.element("CIQGoodsNo", "*");
		// goods.element("BarCode");
		goods.element("CusGoodsNo", "*");
		goods.element("GoodsName", "商品名称");
		goods.element("GoodsStyle", "商品规格");
		goods.element("OriginCountry", "142");
		goods.element("Qty", 1);
		goods.element("HSCode", "HS编码");
		goods.element("Unit", "110");
		goods.element("Price", 110);
		goods.element("Total", (1 * 110));
		goods.element("CurrCode", "142");
		goods.element("Brand", "品牌");
		orderGoodsList.add(goods);
		goods2.element("Seq", 1);
		goods2.element("EntGoodsNo", "TEst2");
		goods2.element("CIQGoodsNo", "*");
		// goods.element("BarCode");
		goods2.element("CusGoodsNo", "*");
		goods2.element("GoodsName", "商品名称");
		goods2.element("GoodsStyle", "商品规格");
		goods2.element("OriginCountry", "142");
		goods2.element("Qty", 1);
		goods2.element("HSCode", "HS编码");
		goods2.element("Unit", "110");
		goods2.element("Price", "100");
		goods2.element("Total", (1 * 100));
		goods2.element("CurrCode", "142");
		goods2.element("Brand", "品牌");
		orderGoodsList.add(goods2);
		order.element("orderGoodsList", orderGoodsList);

		item.put("datas", order);
		item.put("thirdPartyId", "a");
		item.put("merchantId", "MerchantId_00001");
		System.out.println("------->>"
				+ YmHttpUtil.HttpPost("http://localhost:8080/silver-web-shop/order/thirdPartyBusiness", item));
	}
}
