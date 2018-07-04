package org.silver.shop.controller.system.tenant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
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

	
	@RequestMapping(value = "/addRecipientInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("用户添加收货地址信息")
	@RequiresRoles("Member")
	@ResponseBody
	public String addRecipientInfo(@RequestParam("recipientInfo") String recipientInfo, HttpServletRequest req,
			HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
	/*	if (recipientInfo != null && StringEmptyUtils.isNotEmpty(recipientInfo)) {
			return JSONObject.fromObject(recipientTransaction.addRecipientInfo(recipientInfo)).toString();
		}*/
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
	}
}
