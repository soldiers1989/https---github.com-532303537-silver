package org.silver.shop.controller.system.tenant;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.tenant.MerchantBusinessTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@RequestMapping("/merchantBusiness")
@Controller
public class MerchantBusinessController {

	@Autowired
	private MerchantBusinessTransaction merchantBusinessTransaction;

	@RequestMapping(value = "/addInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加商户对应的业务信息")
	@ResponseBody
	@RequiresRoles("Manager")
	// @RequiresPermissions("merchantBank:managerAddBankInfo")
	public String addInfo(HttpServletRequest req, HttpServletResponse response, String merchantId, String businessType,
			String pushType, String idCardVerifySwitch) {
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
		return JSONObject.fromObject(merchantBusinessTransaction.addInfo(datasMap)).toString();
	}

	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员查询商户业务信息")
	@ResponseBody
	@RequiresRoles("Manager")
	// @RequiresPermissions("merchantBank:managerAddBankInfo")
	public String getInfo(HttpServletRequest req, HttpServletResponse response, String merchantId) {
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
		return JSONObject.fromObject(merchantBusinessTransaction.getInfo(datasMap)).toString();
	}
}
