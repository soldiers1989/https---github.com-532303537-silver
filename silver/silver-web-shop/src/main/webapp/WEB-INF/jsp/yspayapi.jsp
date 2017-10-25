
<%
	/* *
	 *功能：即时到账接口接入页
	 *版本：3.0
	 *日期：2016-04-21
	 */
%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="org.silver.shop.controller.system.cross.*"%>

<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>银盛纯网关接口</title>
</head>
<body>
	<%
		request.setCharacterEncoding("UTF-8");

			///////////////////银盛请求公用参数//

			//接口名称方法
			String method = (String)request.getAttribute("method");
			System.out.println("method :   " +method);
			System.out.println("接口名称方法 :   " +method);
			//商户号
			String partner_id = (String)request.getAttribute("partner_id");
			//发送时间
			String timestamp = (String)request.getAttribute("timestamp");
			//字符集
			String charset = (String)request.getAttribute("charset");
			//签名类型
			String sign_type = (String)request.getAttribute("sign_type");

			//银盛支付服务器主动通知商户网站里指定的页面http路径
			String notify_url = (String)request.getAttribute("notify_url");

			//String notify_url = "http://10.211.52.49:8081/create_direct_pay_by_user-JAVA/notify_url.jsp";
			//银盛支付处理完请求后，当前页面自动跳转到商户网站里指定页面的http路径
			String return_url = (String)request.getAttribute("return_url");
			//版本号
			String version = (String)request.getAttribute("version");

			//即时到账请求参数
			//银盛支付合作商户网站唯一订单号
			String out_trade_no = (String)request.getAttribute("out_trade_no");
			//商品的标题/交易标题/订单标题/订单关键字等
			String subject = (String)request.getAttribute("subject");
			//该笔订单的资金总额
			String total_amount = (String)request.getAttribute("total_amount");
			//收款方银盛支付用户号
			String seller_id = (String)request.getAttribute("seller_id");
			//收款方银盛支付客户名
			String seller_name = (String)request.getAttribute("seller_name");
			//设置未付款交易的超时时间，一旦超时，该笔交易就会自动被关闭
			String timeout_express = (String)request.getAttribute("timeout_express");
			//业务代码
			String business_code = (String)request.getAttribute("business_code");
			//公用回传参数
			String extra_common_param = (String)request.getAttribute("extra_common_param");
			//备注
			//String remark = (String)request.getAttribute("remark");
			
			//直连银行信息
			//直联模式使用,锁定指定的支付方式，目前支持internetbank:网银 quickpay 快捷
			//String pay_mode = (String)request.getAttribute("pay_mode");
			//和paymode配合使用,若填写了则直接锁定该银行支付
			String bank_type = (String)request.getAttribute("bank_type");
			//付款方银行账户类型，bank_type非空时，此处必填corporate :对公账户;personal:对私账户
			String bank_account_type = (String)request
			.getAttribute("bank_account_type");
			//支持卡类型, bank_type非空时，此处必填debit:借记卡;credit:信用卡
			String support_card_type = (String)request
			.getAttribute("support_card_type");
			
			//银行行号
			String bank_account_no = (String)request.getAttribute("bank_account_no");
			
			//银行账号用户名
			String bank_account_name = (String)request.getAttribute("bank_account_name");
			
			////////////////////把请求参数打包成一个map
			Map<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("method", method);
			paramsMap.put("partner_id", partner_id);
			paramsMap.put("timestamp", timestamp);
			paramsMap.put("charset", charset);
			paramsMap.put("sign_type", sign_type);
			paramsMap.put("notify_url", notify_url);
			paramsMap.put("return_url", return_url);
			paramsMap.put("version", version);

			paramsMap.put("out_trade_no", out_trade_no);
			paramsMap.put("subject", subject);
			paramsMap.put("total_amount", total_amount);
			paramsMap.put("seller_id", seller_id);
			paramsMap.put("seller_name", seller_name);
			paramsMap.put("timeout_express", timeout_express);
			paramsMap.put("business_code", business_code);
			paramsMap.put("extra_common_param", extra_common_param);
			//paramsMap.put("remark", remark);
			//paramsMap.put("pay_mode", pay_mode);
			paramsMap.put("bank_type", bank_type);
			paramsMap.put("bank_account_type", bank_account_type);
			paramsMap.put("support_card_type", support_card_type);
			paramsMap.put("bank_account_no", bank_account_no);
			//判断为加急代付就进入拼接加急代付的提交地址,其他的则进去拼接网关的地址
			if(((String)request.getAttribute("method")).equals("ysepay.df.single.quick.accept") ||
					((String)request.getAttribute("method")).equals("ysepay.dsf.bill.downloadurl.get") ||
					((String)request.getAttribute("method")).equals("ysepay.df.single.query") ||
					((String)request.getAttribute("method")).equals("ysepay.df.single.normal.accept") ){
				String html = ApipaySubmit.buildRequestdf(request, paramsMap, "post",
				"确认");
				out.println(html);
				
			}else{
				String html = ApipaySubmit.buildRequest(request, paramsMap, "post",
						"确认");
				out.println(html);
			}
	%>
	
</body>
</html>
