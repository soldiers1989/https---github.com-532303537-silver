package org.silver.shop.controller.system.commerce;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.controller.system.cross.DirectPayConfig;
import org.silver.shop.service.system.commerce.OrderTransaction;
import org.silver.util.DateUtil;
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
			@RequestParam("recipientId") String recipientId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.createOrderInfo(goodsInfoPack, type, recipientId);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getMerchantOrderDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户查看订单详情")
	public String getMerchantOrderDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entOrderNo") String entOrderNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.getMerchantOrderDetail(entOrderNo);
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
	@RequestMapping(value = "/checkOrderGoodsCustoms", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查订单商品是否都属于一个海关口岸")
	public String checkOrderGoodsCustoms(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("orderGoodsInfoPack") String orderGoodsInfoPack) {
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
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.getMerchantOrderRecordInfo(page, size);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getMemberOrderDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Member")
	@ApiOperation("用户查看订单详情")
	public String getMemberOrderDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entOrderNo") String entOrderNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = orderTransaction.getMemberOrderDetail(entOrderNo);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/searchMerchantOrderInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("根据指定信息搜索商户订单信息")
	@RequiresRoles("Merchant")
	public String searchMerchantOrderInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (page > 0 && size > 0) {
			statusMap = orderTransaction.searchMerchantOrderInfo(req, page, size);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
	
	@RequestMapping(value = "/getMerchantOrderReport", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户查询订单每日报表")
	@RequiresRoles("Merchant")
	public String getMerchantOrderReport(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size, String startDate,
			String endDate) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (page >= 0 && size >= 0) {
			statusMap = orderTransaction.getMerchantOrderReport( page, size,startDate,endDate);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
	
	/**
	 *  提供第四方下单入口
	 * @param req
	 * @param resp
	 * @param merchant_cus_no 商户编号
	 * @param out_trade_no 交易订单号
	 * @param amount 交易金额
	 * @param return_url 回调URL
	 * @param notify_url 银盛URl
	 * @param extra_common_param  支付人姓名
	 * @param client_sign 签名
	 * @param timestamp 时间戳
	 * @param errBack
	 * @return
	 */
	@RequestMapping("/enter")
	public String dopay(HttpServletRequest req, HttpServletResponse resp, String merchant_cus_no, String out_trade_no,
			String amount, String return_url, String notify_url, String extra_common_param, String client_sign,
			String timestamp,String errBack) {
		if (merchant_cus_no != null && out_trade_no != null && amount != null && client_sign != null && timestamp != null
				&& notify_url != null) {
			Map<String, Object> reqMap = orderTransaction.doBusiness(merchant_cus_no, out_trade_no, amount, notify_url,
					extra_common_param, client_sign, timestamp);
			if (!"1".equals(reqMap.get("status")) ) {
				System.out.println("---->>"+reqMap);
				req.setAttribute("msg", reqMap.get("msg")+"<a href=\""+errBack+"\">"+"点击返回</a>");
				return "ympay-err";
			}
			System.out.println("--------验证通过,准备向银盛发起--------------------");
			req.setAttribute("method", "ysepay.online.directpay.createbyuser");
			req.setAttribute("partner_id", DirectPayConfig.PLATFORM_PARTNER_NO);
			req.setAttribute("timestamp", DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss"));
			req.setAttribute("charset", DirectPayConfig.DEFAULT_CHARSET);
			req.setAttribute("sign_type", DirectPayConfig.SIGN_ALGORITHM);
			// request.setAttribute("sign", userName);
			req.setAttribute("notify_url", "http://ym.191ec.com/silver-web-shop/yspay-receive/ysPayReceive");
			req.setAttribute("return_url", return_url);
			req.setAttribute("version", DirectPayConfig.VERSION);
			req.setAttribute("out_trade_no", reqMap.get("order_id"));// 商户订单号
			req.setAttribute("subject", "即时到账");
			req.setAttribute("total_amount", amount);// 支付总金额
			req.setAttribute("seller_id", DirectPayConfig.PLATFORM_PARTNER_NO);
			req.setAttribute("seller_name", DirectPayConfig.PLATFORM_PARTNER_NAME);
			req.setAttribute("timeout_express", "1h");
			req.setAttribute("business_code", "01000010");
			req.setAttribute("extra_common_param", extra_common_param);// 支付人姓名
			// request.setAttribute("pay_mode", "internetbank");
			req.setAttribute("bank_type", "");
			req.setAttribute("bank_account_type", "");
			req.setAttribute("support_card_type", "");
			req.setAttribute("bank_account_no", "");
			return "yspayapi";
		}
		req.setAttribute("msg", "下单参数有误");
		return "ympay-err";
	}
}
