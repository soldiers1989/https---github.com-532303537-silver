package org.silver.shop.controller.system.tenant;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.tenant.ManagerWalletTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/managerWallet")
public class ManagerWalletController {

	@Autowired
	private ManagerWalletTransaction managerWalletTransaction;

	/**
	 * 管理员查询所有商户钱包信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getMerchantWalletInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询所有商户钱包信息")
	@RequiresPermissions("merchantWallet:getMerchantWalletInfo")
	public String getMerchantWalletInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(managerWalletTransaction.getMerchantWalletInfo(page, size, req)).toString();
	}
	
	
	/**
	 * 管理员给商户钱包加钱
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/updateMerchantWalletAmount", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresPermissions("merchantWallet:updateMerchantWalletAmount")
	@ApiOperation("管理员给商户钱包充值")
	public String updateMerchantWalletAmount(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId ,@RequestParam("amount") double amount)  {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(managerWalletTransaction.updateMerchantWalletAmount(merchantId, amount)).toString();
	}
}
