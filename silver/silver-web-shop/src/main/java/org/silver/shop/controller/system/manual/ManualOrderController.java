package org.silver.shop.controller.system.manual;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.manual.ManualOrderTransaction;
import org.silver.shop.service.system.manual.MdataService;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 
 */
@Controller
@RequestMapping("/manualOrder")
public class ManualOrderController {

	@Autowired
	private ManualOrderTransaction manualOrderTransaction;
	@Autowired
	private MdataService mdataService;

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
		return JSONObject.fromObject(manualOrderTransaction.excelImportOrder(req)).toString();
	}

	/**
	 * 2018-06-14 商户自助申报订单
	 * 
	 * @param resp
	 * @param req
	 * 
	 * @return
	 */
	@RequestMapping(value = "/selfReportOrder", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ApiOperation("商户自助申报订单")
	@ResponseBody
	public String selfReportOrder(HttpServletResponse resp, HttpServletRequest req,
			@RequestParam("orderNoPack") String orderNoPack) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		//
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> itkeys = req.getParameterNames();
		String key = "";
		while (itkeys.hasMoreElements()) {
			key = itkeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		params.remove("orderNoPack");
		// 用于区分商户自主申报订单
		params.put("pushType", "selfReportOrder");
		return JSONObject.fromObject(mdataService.sendMorderRecord(params, orderNoPack)).toString();
	}
	
	@RequestMapping(value = "/updateManualOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ApiOperation("商户修改手工订单信息")
	@ResponseBody
	public String updateManualOrderInfo(HttpServletResponse resp, HttpServletRequest req) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		//
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> itkeys = req.getParameterNames();
		while (itkeys.hasMoreElements()) {
			String key = itkeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return JSONObject.fromObject(manualOrderTransaction.updateManualOrderInfo(datasMap)).toString();
	}
	
	@RequestMapping(value = "/updateManualOrderGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ApiOperation("商户修改手工订单商品信息")
	@ResponseBody
	public String updateManualOrderGoodsInfo(HttpServletResponse resp, HttpServletRequest req) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		//
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> itkeys = req.getParameterNames();
		while (itkeys.hasMoreElements()) {
			String key = itkeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return JSONObject.fromObject(manualOrderTransaction.updateManualOrderGoodsInfo(datasMap)).toString();
	}
	
	public static void main(String[] args) {
		Map<String,Object> item = new HashMap<>();
		item.put("order_code", "76861711609");
		String reString = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/waybill/queryOrderStatus", item);
		System.out.println("------[>>"+reString);
	}
}
