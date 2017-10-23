package org.silver.shop.controller.system.commerce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.commerce.OrderTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 订单Controller
 *
 */
@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderTransaction orderTransaction;

	@RequestMapping(value = "/createOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Member")
	@ApiOperation("用户创建订单")
	public String createOrderInfo(HttpServletRequest req ,HttpServletResponse response,@RequestParam("goodsInfoPack") String goodsInfoPack,@RequestParam("type")int type
			,@RequestParam("totalPrice")double totalPrice) {
		String originHeader = req.getHeader("Origin");
		String[] iPs = { "http://ym.191ec.com:9528", "http://ym.191ec.com:8080", "http://ym.191ec.com:80",
				"http://ym.191ec.com:8090" };
		if (Arrays.asList(iPs).contains(originHeader)) {
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
			response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Origin", originHeader);
		}
		 Map<String, Object> statusMap = orderTransaction.createOrderInfo(goodsInfoPack,type,totalPrice);
		return JSONObject.fromObject(statusMap).toString();
	}

	/*public static void main(String[] args) {
		List<Object> list = new ArrayList<>();
		Map<String, Object> params = null;
		for (int i = 0; i < 2; i++) {
			params = new HashMap<>();
			params.put("goodsId", "YM_20170000515084016841518754");
			params.put("count", 1);
			list.add(params);
		}
		System.out.println(JSONObject.fromObject(params).toString());

	}*/
	public static void main(String[] args) {
		List<Object> ListS = new ArrayList<>();
		ListS.add("aaaa");
		System.out.println(ListS.contains("aaaa"));

	}
}
