package org.silver.shop.controller.common.base;

import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.common.base.ProvinceCityAreaTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	@RequestMapping(value = "findProvinceCityArea", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询省市区三级联动")
	public String findProvinceCityArea() {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, HashMap<String, Object>> datasMap = provinceCityAreaTransaction.findProvinceCityArea();
		if (datasMap != null && datasMap.size() > 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), datasMap);
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getStatus());
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getStatus());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
}
