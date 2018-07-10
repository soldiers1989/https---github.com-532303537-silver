package org.silver.shop.controller.system.tenant;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.tenant.MerchantIdCardCostTransaction;
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
 * 商户身份证费率
 */
@Controller
@RequestMapping("/idCardCost")
public class MerchantIdCardCostController {

	@Autowired
	private MerchantIdCardCostTransaction merchantIdCardCostTransaction;

	@RequestMapping(value = "/addInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加商户实名认证费率")
	//@RequiresRoles("Manager")
	@ResponseBody
	@RequiresPermissions("idCardCost:addInfo")
	public String addInfo(@RequestParam("recipientInfo") String recipientInfo, HttpServletRequest req,
			HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String,Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys= req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key =  isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		if(datasMap.isEmpty()){
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		return JSONObject.fromObject(merchantIdCardCostTransaction.addInfo(datasMap)).toString();
	}

	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员查询商户实名认证费率信息")
	@ResponseBody
	@RequiresPermissions("idCardCost:getInfo")
	public String getInfo(HttpServletRequest req, HttpServletResponse response, String merchantId,int page,int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantIdCardCostTransaction.getInfo(merchantId, page, size)).toString();
	}
	
	@RequestMapping(value = "/updateInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员修改商户实名认证费率信息")
	@ResponseBody
	@RequiresPermissions("idCardCost:updateInfo")
	public String updateInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String,Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys= req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key =  isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		if(datasMap.isEmpty()){
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		return JSONObject.fromObject(merchantIdCardCostTransaction.updateInfo(datasMap)).toString();
	}
}
