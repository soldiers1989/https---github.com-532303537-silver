package org.silver.shop.controller.system.tenant;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.tenant.MerchantFeeTransaction;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商户口岸手续费
 */
@Controller
@RequestMapping("/merchantFee")
public class MerchantFeeController {

	@Autowired
	private MerchantFeeTransaction merchantFeeTransaction;

	@RequestMapping(value = "/addServiceFee", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加商户口岸服务费率")
	@ResponseBody
	@RequiresPermissions("merchantFee:addServiceFee")
	public String addServiceFee(HttpServletRequest req, HttpServletResponse response) {
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
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("添加商户服务费参数不能为空!")).toString();
		}
		return JSONObject.fromObject(merchantFeeTransaction.addMerchantServiceFee(params)).toString();
	}

	@RequestMapping(value = "/getMerchantServiceFee", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("商户获取口岸服务费信息")
	@ResponseBody
	@RequiresRoles("Merchant")
	public String getMerchantServiceFee(HttpServletRequest req, HttpServletResponse response, String merchantId,
			String type) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("商户Id不能为空!")).toString();
		}
		if (StringEmptyUtils.isEmpty(type)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("商户口岸费率类型不能为空!")).toString();
		}
		return JSONObject.fromObject(merchantFeeTransaction.getMerchantServiceFee(merchantId, type)).toString();
	}

	@RequestMapping(value = "/updateServiceFee", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员修改商户口岸服务费信息")
	@ResponseBody
	@RequiresRoles("Manager")
	@RequiresPermissions("merchantFee:updateServiceFee")
	public String updateServiceFee(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantFeeId") String merchantFeeId, @RequestParam("customsPort") int customsPort,
			@RequestParam("customsPortName") String customsPortName, @RequestParam("customsName") String customsName,
			@RequestParam("customsCode") String customsCode, @RequestParam("ciqOrgName") String ciqOrgName,
			@RequestParam("ciqOrgCode") String ciqOrgCode, @RequestParam("platformFee") double platformFee,
			@RequestParam("type") String type, @RequestParam("status") String status) {
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
		return JSONObject.fromObject(merchantFeeTransaction.editMerchantServiceFee(params)).toString();
	}

	@RequestMapping(value = "/getServiceFee", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员获取商户口岸服务费信息")
	@ResponseBody
	@RequiresPermissions("merchantFee:getServiceFee")
	public String getServiceFee(HttpServletRequest req, HttpServletResponse response, String merchantId, int page,
			int size) {
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
		return JSONObject.fromObject(merchantFeeTransaction.getServiceFee(datasMap, page, size)).toString();
	}

	/**
	 * 商户自助申报订单时进行口岸费率统计总和
	 * @param req
	 * @param response
	 * @return 
	 */
	@RequestMapping(value = "/getCustomsFee", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("商户获取口岸服务费信息")
	@ResponseBody
	@RequiresRoles("Merchant")
	public String getCustomsFee(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantFeeTransaction.getCustomsFee()).toString();
	}
}
