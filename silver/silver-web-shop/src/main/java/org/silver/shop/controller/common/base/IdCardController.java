package org.silver.shop.controller.common.base;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.common.base.IdCardTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/idCard")
public class IdCardController {

	@Autowired
	private IdCardTransaction idCardTransaction;

	/**
	 * 管理员查询所有身份证信息
	 * 
	 * @return Json
	 */
	@RequestMapping(value = "/getAllIdCard", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询所有身份证信息")
	@RequiresRoles("Manager")
	public String getAllIdCard(HttpServletRequest req, HttpServletResponse response, @RequestParam("page") int page,
			@RequestParam("size") int size, String idName, String idNumber,int type) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(idCardTransaction.getAllIdCard(page, size, idName, idNumber,type)).toString();
	}

	/**
	 * 管理员修改身份证信息
	 * 
	 * @return Json
	 */
	@RequestMapping(value = "/editIdCardInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员修改身份证")
	@RequiresRoles("Manager")
	public String editIdCardInfo(HttpServletRequest req, HttpServletResponse response, @RequestParam("id") long id,
			@RequestParam("idName") String idName, @RequestParam("idNumber") String idNumber,
			@RequestParam("type") int type) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(idCardTransaction.editIdCardInfo(id, idName, idNumber, type)).toString();
	}

	/**
	 * 管理员首次更新身份证数据
	 * 
	 * @return Json
	 */
	@RequestMapping(value = "/firstUpdateIdCardInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员首次更新身份证数据")
	@RequiresRoles("Manager")
	public String firstUpdateIdCardInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(idCardTransaction.firstUpdateIdCardInfo()).toString();
	}
}