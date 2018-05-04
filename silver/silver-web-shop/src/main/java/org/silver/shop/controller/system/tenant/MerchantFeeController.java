package org.silver.shop.controller.system.tenant;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.tenant.MerchantFeeTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商户平台手续费
 */
@Controller
@RequestMapping("/merchantFee")
public class MerchantFeeController {

	@Autowired
	private MerchantFeeTransaction merchantFeeTransaction;

	@RequestMapping(value = "/addServiceFee", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加商户服务费")
	@ResponseBody
	@RequiresRoles("Manager")
	public String addServiceFee(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId, @RequestParam("provinceName") String provinceName,
			@RequestParam("provinceCode") String provinceCode, @RequestParam("cityName") String cityName,
			@RequestParam("cityCode") String cityCode, @RequestParam("customsPort") int customsPort,
			@RequestParam("customsPortName") String customsPortName, @RequestParam("customsName") String customsName,
			@RequestParam("customsCode") String customsCode, @RequestParam("ciqOrgName") String ciqOrgName,
			@RequestParam("ciqOrgCode") String ciqOrgCode, @RequestParam("managerName") String managerName,
			@RequestParam("platformFee") double platformFee, @RequestParam("type") String type,
			@RequestParam("status") String status) {
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
		return JSONObject.fromObject(merchantFeeTransaction.addMerchantServiceFee(params)).toString();
	}

	@RequestMapping(value = "/getMerchantServiceFee", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员获取商户口岸服务费信息")
	@ResponseBody
	@RequiresRoles("Manager")
	public String getMerchantServiceFee(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantFeeTransaction.getMerchantServiceFee(merchantId)).toString();
	}

	@RequestMapping(value = "/editServiceFee", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员修改商户口岸服务费信息")
	@ResponseBody
	@RequiresRoles("Manager")
	public String editServiceFee(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantFeeId") String merchantFeeId, @RequestParam("provinceName") String provinceName,
			@RequestParam("provinceCode") String provinceCode, @RequestParam("cityName") String cityName,
			@RequestParam("cityCode") String cityCode, @RequestParam("customsPort") int customsPort,
			@RequestParam("customsPortName") String customsPortName, @RequestParam("customsName") String customsName,
			@RequestParam("customsCode") String customsCode, @RequestParam("ciqOrgName") String ciqOrgName,
			@RequestParam("ciqOrgCode") String ciqOrgCode, @RequestParam("managerName") String managerName,
			@RequestParam("platformFee") double platformFee, @RequestParam("type") String type,
			@RequestParam("status") String status) {
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
	@ApiOperation("商户获取口岸服务费信息")
	@ResponseBody
	@RequiresRoles("Manager")
	public String getServiceFee(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId, @RequestParam("type") String type) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantFeeTransaction.getServiceFee(merchantId,type)).toString();
	}
}
