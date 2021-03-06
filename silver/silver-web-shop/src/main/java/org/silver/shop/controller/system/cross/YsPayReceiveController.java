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
import org.silver.shop.service.system.tenant.MemberWalletTransaction;
import org.silver.shop.utils.PaySubmitUtils;
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
	@Autowired
	private MemberWalletTransaction memberWalletTransaction;
	
	
	@RequestMapping(value = "/ysPayReceive", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String ysPayReceive(HttpServletRequest req, HttpServletResponse response) {
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

	/**
	 * 当用户选择商品并在银盛支付后的，订单支付回调
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/orderReceive", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String orderReceive(HttpServletRequest req, HttpServletResponse response) {
		Map params = new HashMap<>();
		Enumeration<String> iskeys = req.getParameterNames();
		while (iskeys.hasMoreElements()) {
			String key = iskeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		logger.error("-(1119账号)银盛支付回调参数->" + params.toString());
		if (PaySubmitUtils.verifySign(req, params)) {
			Map<String, Object> reMap = ysPayReceiveTransaction.ysPayReceive(params);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				logger.error("--(1119账号)支付回调信息处理错误-->" + reMap.get(BaseCode.MSG.toString()));
			}
		} else {
			logger.error("-(1119账号)银盛支付回调签名认证不通过！-");
		}
		return "success";
	}
	
	
	@RequestMapping(value = "/fenZhangReceive", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String fenZhangReceive(HttpServletRequest req, HttpServletResponse response) {
		Map params = new HashMap<>();
		Enumeration<String> iskeys = req.getParameterNames();
		while (iskeys.hasMoreElements()) {
			String key = iskeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		logger.error("-(分账)回调参数->" + params.toString());
		if (PaySubmitUtils.verifySign(req, params)) {
			Map<String, Object> reMap = ysPayReceiveTransaction.fenZhangReceive(params);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				logger.error("--(分账)回调信息处理错误-->" + reMap.get(BaseCode.MSG.toString()));
			}
		} else {
			logger.error("-(分账)回调签名认证不通过！-");
		}
		return "success";
	}
	
	/**
	 * 用户调用银盛支付充值货款，成功后回调
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/memberRechargeReceive", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String memberRechargeReceive(HttpServletRequest req, HttpServletResponse response) {
		Map<String,String> params = new HashMap<>();
		Enumeration<String> iskeys = req.getParameterNames();
		while (iskeys.hasMoreElements()) {
			String key = iskeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		logger.error("-会员充值银盛支付回调参数->" + params.toString());
		if (PaySubmitUtils.verifySign(req, params)) {
			Map<String, String> reMap = memberWalletTransaction.memberRechargeReceive(params);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				logger.error("--会员充值银盛支付回调参数-->" + reMap.get(BaseCode.MSG.toString()));
			}
		} else {
			logger.error("-会员充值银盛支付回调参数错误！-");
		}
		return "success";
	}
	
	
	/**
	 * 用户发起资金提现后，银盛返回异步回调
	 * <li>2018-09-05 暂定操作储备资金</li>
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/memberWithdraw", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String memberWithdraw(HttpServletRequest req, HttpServletResponse response) {
		Map datasMap = new HashMap<>();
		Enumeration<String> iskeys = req.getParameterNames();
		while (iskeys.hasMoreElements()) {
			String key = iskeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		logger.error("--用户提现回调参数-->" + datasMap.toString());
		if (ApipaySubmit.verifySign(req, datasMap)) {
			Map<String, Object> reMap = ysPayReceiveTransaction.memberWithdraw(datasMap);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				logger.error("-用户提现失败-->" + reMap.get(BaseCode.MSG.toString()));
			}
		} else {
			System.out.println("--银盛支付回调签名认证不通过！----");
			logger.error("-银盛支付回调签名认证不通过！-");
		}
		return "success";
	}
	
}
