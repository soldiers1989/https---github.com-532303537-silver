package org.silver.shop.controller.system.cross;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.cross.ReportsTransaction;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商城统计报表controller
 */
@Controller
@RequestMapping(value = "/reports")
public class ReportsController {
	
	@Autowired
	private ReportsTransaction reportsTransaction;
	
	@RequestMapping(value = "/getSynthesisReportDetails", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("新-管理员查询综合报表详情")
	@RequiresRoles("Manager")
	// @RequiresPermissions("report:getSynthesisReportDetails")
	public String getSynthesisReportDetails(HttpServletRequest req, HttpServletResponse response, String merchantId) {
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
		if (datasMap.isEmpty()) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		return JSONObject.fromObject(reportsTransaction.getSynthesisReportDetails(datasMap)).toString();
	}
	
	@RequestMapping(value = "/merchantGetSynthesisReportDetails", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("新-商户查询综合报表详情")
	@RequiresRoles("Merchant")
	// @RequiresPermissions("report:getSynthesisReportDetails")
	public String merchantGetSynthesisReportDetails(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
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
		return JSONObject.fromObject(reportsTransaction.merchantGetSynthesisReportDetails(params)).toString();
	}
	
	@RequestMapping(value = "/merchantGetIdCardCertification", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户查询身份证认证报表信息")
	@RequiresRoles("Merchant")
	// @RequiresPermissions("report:getSynthesisReportDetails")
	public String merchantGetIdCardCertification(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
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
		return JSONObject.fromObject(reportsTransaction.merchantGetIdCardCertification(params)).toString();
	}
	
	@RequestMapping(value = "/getIdCardCertification", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询身份证认证报表信息")
	@RequiresRoles("Manager")
	// @RequiresPermissions("report:getIdCardCertification")
	public String getIdCardCertification(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
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
		return JSONObject.fromObject(reportsTransaction.getIdCardCertification(params)).toString();
	}
	
	@RequestMapping(value = "/tmpUpdate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("临时更新7月至今数据")
	//@RequiresRoles("Manager")
	// @RequiresPermissions("report:getSynthesisReportDetails")
	public String tmpUpdate(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
	
		return JSONObject.fromObject(reportsTransaction.tmpUpdate()).toString();
	}
}


