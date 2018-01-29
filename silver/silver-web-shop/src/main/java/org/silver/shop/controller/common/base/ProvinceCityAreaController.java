package org.silver.shop.controller.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.common.base.ProvinceCityAreaTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 省市区Controller
 */
@Controller
@RequestMapping("/provinceCityArea")
public class ProvinceCityAreaController {

	@Autowired
	private ProvinceCityAreaTransaction provinceCityAreaTransaction;

	/**
	 * 查询省市区三级联动,并通过MAP封装
	 * 
	 * @return
	 */
	@RequestMapping(value = "/findProvinceCityArea", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询省市区三级联动")
	public String findProvinceCityArea(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> statusMap = new HashMap<>();
		List datasList = provinceCityAreaTransaction.findProvinceCityArea();
		if (datasList != null && datasList.size() > 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), datasList);
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getStatus());
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getStatus());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 修改省市区信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/editProvinceCityAreaInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("修改省市区信息")
	@RequiresRoles("Manager")
	public String editProvinceCityAreaInfo(HttpServletResponse response, HttpServletRequest req, int length) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(provinceCityAreaTransaction.editProvinceCityAreaInfo(req,length)).toString();
	}
}
