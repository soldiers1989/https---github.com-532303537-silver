package org.silver.shop.controller.system.log;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.log.StockReviewLogTransaction;
import org.silver.shop.service.system.log.TradeReceiptLogTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/tradeReviewLog")
public class TradeReceiptLogController {
	@Autowired
	private TradeReceiptLogTransaction tradeReceiptLogTransaction;

	@RequestMapping(value = "/addLog", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	// @RequiresRoles("Merchant")
	@ResponseBody
	@ApiOperation("添加交易记录")
	public String addLog(HttpServletRequest req, HttpServletResponse response) {
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
		return JSONObject.fromObject(tradeReceiptLogTransaction.addLog(datasMap)).toString();
	}

	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Manager")
	@ResponseBody
	@ApiOperation("管理员查询交易记录")
	// @RequiresPermissions("tradeReviewLog:getInfo")
	public String getInfo(HttpServletRequest req, HttpServletResponse response, @RequestParam("page") int page,
			@RequestParam("size") int size) {
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
		return JSONObject.fromObject(tradeReceiptLogTransaction.getInfo(datasMap,page,size)).toString();
	}

}
