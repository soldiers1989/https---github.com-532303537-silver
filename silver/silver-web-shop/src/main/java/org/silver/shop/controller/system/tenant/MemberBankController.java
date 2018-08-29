package org.silver.shop.controller.system.tenant;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.tenant.MemberBankTransaction;
import org.silver.shop.service.system.tenant.MerchantBankInfoTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 用户银行卡Controller
 */
@Controller
@RequestMapping("/memberBank")
public class MemberBankController {

	@Autowired
	private MemberBankTransaction memberBankTransaction;

	@RequestMapping(value = "/addInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("添加银行卡信息")
	@ResponseBody
	@RequiresRoles("Member")
	// @RequiresPermissions("merchantBank:managerAddBankInfo")
	public String addInfo(HttpServletRequest req, HttpServletResponse response, @RequestParam("bankProvince") String bankProvince,
			@RequestParam("bankCity") String bankCity, @RequestParam("bankName") String bankName,
			@RequestParam("bankAccountNo") String bankAccountNo,
			@RequestParam("bankAccountName") String bankAccountName,
			@RequestParam("bankAccountType") String bankAccountType, @RequestParam("bankCardType") String bankCardType,
			@RequestParam("defaultFlag") int defaultFlag) {
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
		return JSONObject.fromObject(memberBankTransaction.addInfo(datasMap)).toString();
	}

	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Member")
	@ApiOperation("商户获取银行卡信息")
	public String getInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(memberBankTransaction.getInfo(page, size)).toString();
	}
	
}
