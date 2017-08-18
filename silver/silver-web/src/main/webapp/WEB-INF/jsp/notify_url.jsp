
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.Connection"%>

<%@page import="org.slf4j.Logger"%>
<%@ page import="org.slf4j.LoggerFactory"%>
<%
	/* *
	 功能：银盛服务器异步通知页面
	 版本：3.0
	 日期：2016-04-21
	*/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="org.silver.yspay.*"%>
<%
	Logger logger = LoggerFactory.getLogger(getClass());
	logger.debug("开始接受异步返回通知");
	String ip = request.getHeader("x-forwarded-for");
	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("Proxy-Client-IP");
	}
	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("WL-Proxy-Client-IP");
	}
	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getRemoteAddr();
	}
	System.out.println("异步通知发起地址：" + ip);
	

	request.setCharacterEncoding("UTF-8");

	//获取银盛POST过来反馈信息
	Map<String, String> params = new HashMap<String, String>();
	Map requestParams = request.getParameterMap();
	for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
		String name = (String) iter.next();
		String[] values = (String[]) requestParams.get(name);
		String valueStr = "";
		for (int i = 0; i < values.length; i++) {
			valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
		}
		System.out.println("name：" + name + " value:" + valueStr);
		params.put(name, valueStr);
	}

	//交易目前所处的状态
	String trade_status = request.getParameter("trade_status");

	//验证 params  验签
	boolean paramsResult = ApipaySubmit.verifySign(request, params);

	if (paramsResult) {//验证成功
		//////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("异步通知验证签名成功");
		logger.debug("paramsResult：" + paramsResult);
		//如果状态为TRADE_FINISHED
		if (trade_status.equals("TRADE_FINISHED")) {
			//判断该笔订单是否在商户网站中已经做过处理
			//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
			//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
			//如果有做过处理，不执行商户的业务程序
			out.println("success");//请不要修改或删除
			//注意：
			//退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知

			//如果状态为TRADE_SUCCESS
		} else if (trade_status.equals("TRADE_SUCCESS")) {
			//判断该笔订单是否在商户网站中已经做过处理
			//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
			//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
			//如果有做过处理，不执行商户的业务程序
			out.println("success");//请不要修改或删除
			//注意：
			//付款完成后，支付宝系统发送该交易状态通知
		} else {
			out.println("fail");//请不要修改或删除
		}
		//收到回执更新数据库
		logger.debug("开始处理异步返回的交易流水并存入数据库：");
		String entNo = request.getParameter("out_trade_no");//平台订单业务流水
		String orderNo = request.getParameter("trade_no");//交易流水
		logger.debug("平台订单业务流水：" + entNo);
		logger.debug("交易流水：" + orderNo);
		

		
		//////////////////////////////////////////////////////////////////////////////////////////
	} else {//验证失败
		out.println("fail");
		System.out.println("异步通知验证签名失败");
	}
%>
