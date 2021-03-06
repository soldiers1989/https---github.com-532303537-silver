package org.silver.shop.controller.system.log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.log.MemberWalletLogTransaction;
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
 * 用户钱包交易记录
 */
@Controller
@RequestMapping("/memberWalletLog")
public class MemberWalletLogController {

	@Autowired
	private MemberWalletLogTransaction memberWalletLogTransaction;

	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Member")
	@ResponseBody
	@ApiOperation("用户查看钱包记录")
	public String getInfo(HttpServletRequest req, HttpServletResponse response, @RequestParam("type") int type,
			@RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("startDate") String startDate,
			@RequestParam("endDate") String endDate) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if(StringEmptyUtils.isNotEmpty(startDate) && StringEmptyUtils.isNotEmpty(endDate)){
			return JSONObject.fromObject(memberWalletLogTransaction.getInfo(startDate,endDate, type, page, size)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("参数不能为空!")).toString();
	}

}
