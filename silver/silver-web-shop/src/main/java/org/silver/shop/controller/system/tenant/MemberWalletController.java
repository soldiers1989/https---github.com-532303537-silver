package org.silver.shop.controller.system.tenant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.tenant.ManagerWalletTransaction;
import org.silver.shop.service.system.tenant.MemberWalletTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 用户钱包
 */
@Controller
@RequestMapping("/memberWallet")
public class MemberWalletController {

	@Autowired
	private MemberWalletTransaction memberWalletTransaction;

	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户查询钱包信息")
	@RequiresRoles("Member")
	//@RequiresPermissions("merchantWallet:getMerchantWalletInfo")
	public String getInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(memberWalletTransaction.getInfo()).toString();
	}
	
	
}
