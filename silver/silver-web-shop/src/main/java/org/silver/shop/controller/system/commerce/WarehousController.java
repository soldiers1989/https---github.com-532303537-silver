package org.silver.shop.controller.system.commerce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
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
	@ApiOperation("查询商户下仓库")
	@RequiresRoles("Merchant")
	public String getWarehousInfo(@RequestParam("page")int page,@RequestParam("size")int size,
			HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = warehousTransaction.getWarehousInfo(page,size);
		return JSONObject.fromObject(statusMap).toString();
	}	
	
	public static void main(String[] args) {
		 Calendar todayEnd = Calendar.getInstance();  
	        todayEnd.set(Calendar.HOUR, 23);  
	        todayEnd.set(Calendar.MINUTE, 59);  
	        todayEnd.set(Calendar.SECOND, 59);  
	        todayEnd.set(Calendar.MILLISECOND, 999);  
	        todayEnd.getTime();
	        System.out.println("---------->>>>>>"+todayEnd.getTime());
	}
}
