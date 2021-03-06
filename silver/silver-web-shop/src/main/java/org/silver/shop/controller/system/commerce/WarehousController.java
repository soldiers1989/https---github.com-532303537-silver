package org.silver.shop.controller.system.commerce;

import java.io.File;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.config.FenZhangConfig;
import org.silver.shop.service.system.commerce.WarehousTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 仓库
 *
 */
@Controller
@RequestMapping("/warehous")
public class WarehousController {
	@Autowired
	private WarehousTransaction warehousTransaction;

	@RequestMapping(value = "/getWarehousInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户查询仓库")
	@RequiresRoles("Merchant")
	public String getWarehousInfo(@RequestParam("page") int page, @RequestParam("size") int size,
			HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(warehousTransaction.getWarehousInfo(page, size)).toString();
	}

	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询商户仓库")
	@RequiresRoles("Manager")
	public String getInfo(HttpServletRequest request,
			HttpServletResponse response) {
		String originHeader = request.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		ServletContext servletContext = request.getServletContext();
		
		String realPath = servletContext.getRealPath("/") + "WEB-INF/classes/credentials";
		String partnerCert = FenZhangConfig.PATH_PARTER_PKCS12;
		File f = new File(realPath + partnerCert);
		System.out.println("-f->>"+f.getPath());
		return realPath;
	}
}
