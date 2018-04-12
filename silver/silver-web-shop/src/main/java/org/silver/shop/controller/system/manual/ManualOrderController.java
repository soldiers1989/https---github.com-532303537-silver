package org.silver.shop.controller.system.manual;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.manual.ManualOrderTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

/**
 * 
 */
@Controller
@RequestMapping("/manualOrder")
public class ManualOrderController {
	
	@Autowired
	private ManualOrderTransaction manualOrderTransaction;
	
	
	/**
	 * excel批量导入手工订单 暂只支持有国宗、企邦(将作为对外统一模板)
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/excelImportOrder", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	public String excelImportOrder(HttpServletResponse resp, HttpServletRequest req) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = manualOrderTransaction.excelImportOrder(req);
		return JSONObject.fromObject(reqMap).toString();
	}
	
	private static AtomicInteger counter = new AtomicInteger(0);
	
	public static void main(String[] args) {
		for(int i =0 ;i <10 ; i ++){
			System.out.println(counter);
			System.out.println("自增数--->"+counter.getAndIncrement());
		}
	}
}
