package org.silver.shop.controller.system.tenant;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.tenant.SubMerchantTransaction;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商家下子商户controller层
 */
@RequestMapping("subMerchant")
@Controller
public class SubMerchantController {

	@Autowired
	private SubMerchantTransaction subMerchantTransaction;

	@RequestMapping(value = "/addSubMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加子商户信息")
	@RequiresRoles("Manager")
	@ResponseBody
	public String addSubMerchantInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		if (!params.isEmpty()) {
			return JSONObject.fromObject(subMerchantTransaction.addSubMerchantInfo(params)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
	}

	@RequestMapping(value = "/getSubMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员查询所有子商户信息")
	@RequiresRoles("Manager")
	@ResponseBody
	public String getSubMerchantInfo(String subMerchantInfo, HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(subMerchantTransaction.getSubMerchantInfo()).toString();
	}
	
	
	@RequestMapping(value = "/editSubMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员修改子商户信息")
	@RequiresRoles("Manager")
	@ResponseBody
	public String editSubMerchantInfo(String subMerchantInfo, HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		if (!params.isEmpty()) {
			return JSONObject.fromObject(subMerchantTransaction.editSubMerchantInfo(params)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
	}
}
