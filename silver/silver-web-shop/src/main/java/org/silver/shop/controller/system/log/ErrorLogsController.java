package org.silver.shop.controller.system.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.shop.service.system.log.ErrorLogsTransaction;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

/**
 * 错误日志
 */
@Controller
@RequestMapping("/errorLogs")
public class ErrorLogsController {

	@Autowired
	private ErrorLogsTransaction errorLogsTransaction;

	/**
	 * 商户错误日志记录
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/addErrorLogs", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String addErrorLogs(HttpServletRequest req, HttpServletResponse response,
			List<Map<String, Object>> errorList, int totalCount, String serialNo,String action) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if(errorList !=null && totalCount >=0&& StringEmptyUtils.isNotEmpty(serialNo)&&  StringEmptyUtils.isNotEmpty(action)){
			return JSONObject.fromObject(errorLogsTransaction.addErrorLogs(errorList,totalCount,serialNo,action)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数出错,请核对信息!")).toString();
	}
}
