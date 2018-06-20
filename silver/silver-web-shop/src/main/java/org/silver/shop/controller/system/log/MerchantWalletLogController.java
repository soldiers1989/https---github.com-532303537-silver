package org.silver.shop.controller.system.log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.log.MmerchantWalletLogTransaction;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商户钱包
 */
@Controller
@RequestMapping("/merchantWalletLog")
public class MerchantWalletLogController {

	@Autowired
	private MmerchantWalletLogTransaction merchantWalletLogTransaction;

	/**
	 * 商户查看钱包记录
	 */
	@RequestMapping(value = "/getWalletLog", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	@ApiOperation("商户查看钱包记录")
	public String getWalletLog(HttpServletRequest req, HttpServletResponse response, @RequestParam("type") int type,
			@RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("startDate") String startDate,
			@RequestParam("endDate") String endDate) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if(StringEmptyUtils.isNotEmpty(startDate) && StringEmptyUtils.isNotEmpty(endDate)){
			return JSONObject.fromObject(merchantWalletLogTransaction.getMerchantWalletLog(startDate,endDate, type, page, size)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("参数不能为空!")).toString();
	}

}
