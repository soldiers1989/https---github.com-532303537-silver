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

	@RequestMapping(value = "/addMerchantServiceFee", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加商户服务费")
	@ResponseBody
	@RequiresRoles("Manager")
	public String addMerchantServiceFee(HttpServletRequest req, HttpServletResponse response) {
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
}
