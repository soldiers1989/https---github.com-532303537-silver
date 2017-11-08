package org.silver.shop.controller.system.commerce;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	protected static final Logger logger = LogManager.getLogger();
	@Autowired
	private OrderTransaction orderTransaction;

	@RequestMapping(value = "/createOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Member")
	@ApiOperation("用户创建订单")
	public String createOrderInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("goodsInfoPack") String goodsInfoPack, @RequestParam("type") int type,
			@RequestParam("recipientId")String recipientId) {
		String originHeader = req.getHeader("Origin");
		String[] iPs = { "http://ym.191ec.com:9528", "http://ym.191ec.com:8080", "http://ym.191ec.com:80",
				"http://ym.191ec.com:8090" };
		if (Arrays.asList(iPs).contains(originHeader)) {
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
			response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Origin", originHeader);
		}
		Map<String, Object> statusMap = orderTransaction.createOrderInfo(goodsInfoPack, type,recipientId);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getMerchantOrderDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户查看订单详情")
	public String getMerchantOrderDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("orderId")String orderId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.getMerchantOrderDetail(orderId);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 备案网关异步回馈订单备案信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/reNotifyMsg", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String reNotifyMsg(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		logger.info("-----备案网关异步回馈订单备案信息---");
		Map<String, Object> datasMap = new HashMap<>();
		datasMap.put("status", req.getParameter("status") + "");
		datasMap.put("errMsg", req.getParameter("errMsg") + "");
		datasMap.put("messageID", req.getParameter("messageID") + "");
		datasMap.put("entOrderNo", req.getParameter("entOrderNo") + "");
		Map<String, Object> statusMap = orderTransaction.updateOrderRecordInfo(datasMap);
		logger.info(JSONObject.fromObject(statusMap).toString());
		return JSONObject.fromObject(statusMap).toString();
	}
	/**
	 * 检查订单商品是否都属于一个海关口岸
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/checkOrderGoodsCustoms", method= RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查订单商品是否都属于一个海关口岸")
	public String checkOrderGoodsCustoms(HttpServletRequest req, HttpServletResponse response,@RequestParam("orderGoodsInfoPack")String orderGoodsInfoPack) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.checkOrderGoodsCustoms(orderGoodsInfoPack);
		return JSONObject.fromObject(statusMap).toString();
	}
	
	
	@RequestMapping(value = "/getMemberOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "获取用户订单信息")
	@RequiresRoles("Member")
	public String getMemberOrderInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.getMemberOrderInfo(page, size);
		return JSONObject.fromObject(statusMap).toString();
	}
	
	@RequestMapping(value = "/getMerchantOrderRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户查看备案订单信息")
	public String getMerchantOrderRecordInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page")int page,@RequestParam("size")int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.getMerchantOrderRecordInfo(page,size);
		return JSONObject.fromObject(statusMap).toString();
	}
	
	public static void main(String[] args) {
		/*Double d= 0.06*1*(119/100d);
		BigDecimal b = new BigDecimal(d); 
		double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		System.out.println("---------->>"+d);
		System.out.println("四舍五入后-------->"+f1);
		
		
		float f = Float.parseFloat(d+"");
		int a = (int) (f * 1000);
		if (a % 10 > 0)
		    f = (a - a % 10 + 10 * 1.0f) / 1000.0f;
		else
		    f = a * 1.0f / 1000.0f;
		System.out.println(f);*/
		String s ="GACNO_20170000415099596709394549";
		System.out.println(s.length());
	}
}
