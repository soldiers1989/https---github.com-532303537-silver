package org.silver.shop.controller.system.log;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.log.EvaluationLogTransaction;
import org.silver.shop.service.system.log.IdCardCertificationlogsTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@RequestMapping("/idCardCertificationLog")
@Controller
public class IdCardCertificationLogController {
	@Autowired
	private IdCardCertificationlogsTransaction idCardCertificationTransaction;
	
	@RequestMapping(value = "/getlogsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员查询认证日志记录")
	//@RequiresPermissions("idCardCertificationLog:getlogsInfo")
	public String getlogsInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String,Object> datasMap= new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key =  isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		datasMap.remove("page");
		datasMap.remove("size");
		return JSONObject.fromObject(idCardCertificationTransaction.getlogsInfo(datasMap, page, size)).toString();
	}
	
	@RequestMapping(value = "/merchantGetInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户查询身份证认证日志记录")
	public String merchantGetInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String,Object> datasMap= new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key =  isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		datasMap.remove("page");
		datasMap.remove("size");
		return JSONObject.fromObject(idCardCertificationTransaction.merchantGetInfo(datasMap, page, size)).toString();
	}
}
