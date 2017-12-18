package org.silver.shop.controller.system.tenant;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.tenant.ProxyWalletTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;


/**
 * 
 *
 */
@Controller
@RequestMapping("/proxyWallet")
public class ProxyWalletController {
	@Autowired
	private ProxyWalletTransaction proxyWalletTransaction;
	
	
	@RequestMapping(value = "/getProxyWalletInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("获取代理下钱包信息")
	@RequiresRoles("Proxy")
	public String getProxyWalletInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = proxyWalletTransaction.getProxyWalletInfo();
		return JSONObject.fromObject(statusMap).toString();
	}
	
	/**
	 * 代理商查看钱包记录
	 * @param req
	 * @param response
	 * @param type 查询时间范围 1-三个月内,2-一年内
	 * @param page 页数
	 * @param size 数目
	 * @return String
	 */
	@RequestMapping(value = "/getProxyWalletLog", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Proxy")
	@ResponseBody
	@ApiOperation("代理商查看钱包记录")
	public String getProxyWalletLog(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("type") int type, @RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (type == 1 || type == 2) {
			statusMap = proxyWalletTransaction.getProxyWalletLog(type, page, size);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.toString());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
}
