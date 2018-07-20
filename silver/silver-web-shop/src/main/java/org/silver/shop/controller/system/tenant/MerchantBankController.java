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
 * 商户银行Controller,商户银行卡信息的操作
 */
@Controller
@RequestMapping("/merchantBank")
public class MerchantBankController {

	@Autowired
	private MerchantBankInfoTransaction merchantBankInfoTransaction;

	@RequestMapping(value = "/managerAddBankInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加商户银行卡信息")
	@ResponseBody
	// @RequiresRoles("Merchant")
	// @RequiresPermissions("merchantBank:managerAddBankInfo")
	public String managerAddBankInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId, @RequestParam("bankProvince") String bankProvince,
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
		return JSONObject.fromObject(merchantBankInfoTransaction.managerAddBankInfo(datasMap)).toString();
	}

	
	@RequestMapping(value = "/getBankInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户获取银行卡信息")
	public String getBankInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantBankInfoTransaction.getBankInfo(page, size)).toString();
	}

	@RequestMapping(value = "/managerGetBankInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("管理员获取银行卡信息")
	// @RequiresPermissions("merchantBank:managerGetBankInfo")
	public String managerGetBankInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size,String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantBankInfoTransaction.managerGetBankInfo(page, size,merchantId)).toString();
	}
	/**
	 * 设置默认银行卡
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/selectMerchantBankInfoDefault", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("设置默认银行卡")
	public String selectMerchantBankInfoDefault(@RequestParam(value = "id") long id) {
		Map<String, Object> statusMap = new HashMap<>();
		if (id > 0) {
			statusMap = merchantBankInfoTransaction.selectMerchantBank(id);
			return JSONObject.fromObject(statusMap).toString();
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.UNKNOWN.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/deleteMerchantBankInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("删除银行卡信息")
	public String deleteMerchantBankInfo(@RequestParam(value = "id") long id) {
		Map<String, Object> statusMap = new HashMap<>();
		if (id > 0) {
			statusMap = merchantBankInfoTransaction.deleteBankInfo(id);
			return JSONObject.fromObject(statusMap).toString();
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.UNKNOWN.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}
}
