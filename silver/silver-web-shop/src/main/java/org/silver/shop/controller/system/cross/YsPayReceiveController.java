package org.silver.shop.controller.system.cross;

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
	private static Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
	@Autowired
	private YsPayReceiveTransaction ysPayReceiveTransaction;

	@RequestMapping(value = "/ysPayReceive", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String ysPayReceive(HttpServletRequest req, HttpServletResponse response) {
		Map datasMap = new HashMap<>();
		datasMap.put("notify_type", req.getParameter("notify_type") + "");
		
		datasMap.put("notify_time", req.getParameter("notify_time") + "");		
		datasMap.put("out_trade_no", req.getParameter("out_trade_no") + "");
		datasMap.put("total_amount", req.getParameter("total_amount") + "");
		
		datasMap.put("account_date", req.getParameter("account_date") + "");
		datasMap.put("trade_status", req.getParameter("trade_status") + "");
		datasMap.put("sign", req.getParameter("sign") + "");
		
		datasMap.put("trade_no", req.getParameter("trade_no") + "");
		
		datasMap.put("sign_type", req.getParameter("sign_type") + "");
		logger.error(datasMap.toString());
		Map<String, Object> statusMap=new HashMap<>(); 
		if(ApipaySubmit.verifySign(req, datasMap)){
			 statusMap = ysPayReceiveTransaction.ysPayReceive(datasMap);
		}
		if(!"1".equals(statusMap.get(BaseCode.STATUS.toString()))){
			logger.error("------支付回调信息处理错误------");
			logger.error(statusMap.toString());
		}
		return "success";
	}
}
