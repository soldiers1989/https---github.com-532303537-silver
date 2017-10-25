package org.silver.shop.controller.system.cross;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.shop.service.system.cross.YsPayReceiveTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("yspay-receive")
public class YsPayReceiveController {
	@Autowired
	private YsPayReceiveTransaction ysPayReceiveTransaction;
	
	@RequestMapping("/ysPayReceive")
	public String ysPayReceive(HttpServletRequest req,HttpServletResponse response){
		Map<String,Object> datasMap = new HashMap<>();
		datasMap.put("notify_type", req.getParameter("notify_type")+"");
		datasMap.put("notify_time", req.getParameter("notify_time")+"");
		datasMap.put("out_trade_no", req.getParameter("out_trade_no")+"");
		datasMap.put("total_amount", req.getParameter("total_amount")+"");
		datasMap.put("account_date", req.getParameter("account_date")+"");
		datasMap.put("trade_status", req.getParameter("trade_status")+"");
		datasMap.put("sign", req.getParameter("sign")+"");
		datasMap.put("trade_no", req.getParameter("trade_no")+"");
		datasMap.put("sign_type", req.getParameter("sign_type")+"");
		ysPayReceiveTransaction.ysPayReceive(datasMap);
		return null;
	}
}
