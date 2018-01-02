package org.silver.shop.controller.system.tenant;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.tenant.MerchantWalletTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商户钱包Controller
 */
@Controller
@RequestMapping("/merchantWallet")
public class MerchantWalletController {

	@Autowired
	private MerchantWalletTransaction merchantWalletTransaction;

	@RequestMapping(value = "/merchantWalletRecharge", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	@ApiOperation("商户往钱包充值")
	public String merchantWalletRecharge(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("money") Double money) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (money != null && money > 0) {
			statusMap = merchantWalletTransaction.merchantWalletRecharge(money);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.toString());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getMerchantWallet", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	@ApiOperation("商户查看钱包余额")
	public String getMerchantWallet(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		statusMap = merchantWalletTransaction.getMerchantWallet();
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 商户查看钱包记录
	 * 
	 * @param req
	 * @param response
	 * @param type
	 *            查询时间范围 1-三个月内,2-一年内,3-查询今天
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return String
	 */
	@RequestMapping(value = "/getMerchantWalletLog", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	@ApiOperation("商户查看钱包记录")
	public String getMerchantWalletLog(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("type") int type, @RequestParam("page") int page, @RequestParam("size") int size,
			@RequestParam("timeLimit") int timeLimit) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (timeLimit == 1 || timeLimit == 2) {
			statusMap = merchantWalletTransaction.getMerchantWalletLog(type, page, size, timeLimit);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.toString());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
}
