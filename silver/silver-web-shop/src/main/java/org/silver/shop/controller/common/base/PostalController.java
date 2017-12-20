package org.silver.shop.controller.common.base;

import javax.servlet.http.HttpServletResponse;

import org.silver.shop.service.common.base.PostalTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/postal")
public class PostalController {
	
	@Autowired
	private PostalTransaction postalTransaction;
	
	/**
	 * 查询全国邮编
	 * @return
	 */
	@RequestMapping(value = "/getPostal", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询全国邮编")
	public String getPostal(HttpServletResponse response ) {
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Origin", "*");
		return JSONObject.fromObject(PostalTransaction.getPostal()).toString();
	}
}
