package org.silver.shop.controller.system.tenant;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.tenant.SubMerchantTransaction;
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
 * 商家下子商户controller层
 */
@RequestMapping("subMerchant")
@Controller
public class SubMerchantController {

	@Autowired
	private SubMerchantTransaction subMerchantTransaction;

	@RequestMapping(value = "/addSubMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加子商户信息")
	@RequiresRoles("Manager")
	@ResponseBody
	public String addSubMerchantInfo(String subMerchantInfo, HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isNotEmpty(subMerchantInfo)) {
			try {
				JSONObject json = JSONObject.fromObject(subMerchantInfo);
				return JSONObject.fromObject(subMerchantTransaction.addSubMerchantInfo(json)).toString();
			} catch (Exception e) {
				e.printStackTrace();
				return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数错误!")).toString();
			}
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
	}
}
