package org.silver.shop.controller.system.tenant;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.tenant.OfflineRechargeTransaction;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/offlineRecharge")
public class OfflineRechargeController {

	@Autowired
	private OfflineRechargeTransaction offlineRechargeTransaction;

	@RequestMapping(value = "/merchantApplication", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("商户申请下线钱包充值")
	@ResponseBody
	@RequiresRoles("Merchant")
	// @RequiresPermissions("merchantFee:addServiceFee")
	public String merchantApplication(HttpServletRequest req, HttpServletResponse response) {
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
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("参数不能为空!")).toString();
		}
		return JSONObject.fromObject(offlineRechargeTransaction.merchantApplication(req, datasMap)).toString();
	}

	@RequestMapping(value = "/merchantGetApplication", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("商户查询下线钱包充值信息")
	@ResponseBody
	@RequiresRoles("Merchant")
	// @RequiresPermissions("merchantFee:addServiceFee")
	public String merchantGetApplication(HttpServletRequest req, HttpServletResponse response,
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
		return JSONObject.fromObject(offlineRechargeTransaction.merchantGetApplication(datasMap, page, size))
				.toString();
	}

	@RequestMapping(value = "/managerGetApplication", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员查询下线钱包充值信息")
	@ResponseBody
	@RequiresRoles("Manager")
	// @RequiresPermissions("merchantFee:addServiceFee")
	public String managerGetApplication(HttpServletRequest req, HttpServletResponse response,
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
		return JSONObject.fromObject(offlineRechargeTransaction.managerGetApplication(datasMap, page, size)).toString();
	}

	@RequestMapping(value = "/getApplicationDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("获取钱包线下充值详情")
	@ResponseBody
	@RequiresRoles("Merchant")
	// @RequiresPermissions("merchantFee:addServiceFee")
	public String getApplicationDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("offlineRechargeId") String offlineRechargeId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(offlineRechargeTransaction.getApplicationDetail(offlineRechargeId)).toString();
	}
	
	@RequestMapping(value = "/managerReview", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员初审")
	@ResponseBody
	@RequiresRoles("Merchant")
	// @RequiresPermissions("merchantFee:addServiceFee")
	public String managerReview(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("offlineRechargeId") String offlineRechargeId,int reviewerFlag,String note) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(offlineRechargeTransaction.managerReview(offlineRechargeId,reviewerFlag,note)).toString();
	}
	@RequestMapping(value = "/financialReview", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("财务审核信息")
	@ResponseBody
	//@RequiresRoles("Merchant")
	// @RequiresPermissions("merchantFee:addServiceFee")
	public String financialReview(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("offlineRechargeId") String offlineRechargeId,int reviewerFlag,String note) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(offlineRechargeTransaction.financialReview(offlineRechargeId,reviewerFlag,note)).toString();
	}
}
