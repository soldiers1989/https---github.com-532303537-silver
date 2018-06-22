package org.silver.shop.controller.system.cross;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.service.system.cross.YsPayReceiveTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 支付完成后,回调信息处理Controller
 *
 */
@Controller
@RequestMapping("yspay-receive")
public class YsPayReceiveController {

	/**
	 * 线程计数器
	 */
	private int counter = 0;

	private static Logger logger = LogManager.getLogger();

	@Autowired
	private YsPayReceiveTransaction ysPayReceiveTransaction;

	@RequestMapping(value = "/ysPayReceive", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String ysPayReceive(HttpServletRequest req, HttpServletResponse response) {
		System.out.println("--支付回调计数器-->" + counter++);
		Map params = new HashMap<>();
		Enumeration<String> iskeys = req.getParameterNames();
		while (iskeys.hasMoreElements()) {
			String key = iskeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		logger.error("-银盛支付回调参数->" + params.toString());
		if (ApipaySubmit.verifySign(req, params)) {
			Map<String, Object> reMap = ysPayReceiveTransaction.ysPayReceive(params);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				logger.error("--支付回调信息处理错误-->" + reMap.get(BaseCode.MSG.toString()));
			}
		} else {
			System.out.println("--银盛支付回调签名认证不通过！----");
			logger.error("-银盛支付回调签名认证不通过！-");
		}
		return "success";
	}

	/**
	 * 银盟发起钱包余额充值后,银盛支付成功后回调
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/walletRecharge", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String walletRecharge(HttpServletRequest req, HttpServletResponse response) {
		Map params = new HashMap<>();
		Enumeration<String> iskeys = req.getParameterNames();
		while (iskeys.hasMoreElements()) {
			String key = iskeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		logger.error("--钱包充值回调信息参数-->" + params.toString());
		if (ApipaySubmit.verifySign(req, params)) {
			Map<String, Object> reMap = ysPayReceiveTransaction.walletRechargeReceive(params);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				logger.error("--钱包充值失败-->" + reMap.get(BaseCode.MSG.toString()));
			}
		} else {
			System.out.println("--银盛支付回调签名认证不通过！----");
			logger.error("-银盛支付回调签名认证不通过！-");
		}
		return "success";
	}

	/**
	 * 银盟发起商户资金代付后,银盛支付成功回调
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/dfReceive", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String dfReceive(HttpServletRequest req, HttpServletResponse response) {
		Map params = new HashMap<>();
		Enumeration<String> iskeys = req.getParameterNames();
		while (iskeys.hasMoreElements()) {
			String key = iskeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		logger.error("--商户资金结算回调参数-->" + params.toString());
		if (ApipaySubmit.verifySign(req, params)) {
			Map<String, Object> reMap = ysPayReceiveTransaction.dfReceive(params);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				logger.error("-商户资金结算失败-->" + reMap.get(BaseCode.MSG.toString()));
			}
		} else {
			System.out.println("--银盛支付回调签名认证不通过！----");
			logger.error("-银盛支付回调签名认证不通过！-");
		}
		return "success";
	}
}
