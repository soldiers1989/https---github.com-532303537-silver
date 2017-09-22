package org.silver.shop.controller.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.common.base.MeteringTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 计量单位Controller
 *
 */
@Controller
@RequestMapping(value="/metering")
public class MeteringController {

	@Autowired
	private MeteringTransaction meteringTransaction;
	
	@RequestMapping(value="/findAllMetering", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询所有计量单位")
	public String findAllMetering(){
		Map<String,Object> statusMap = new HashMap<>();
		List datasList= meteringTransaction.findMetering();
		if (datasList != null && datasList.size() > 0) {
			statusMap.put(BaseCode.STATUS.getBaseCode(),StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.getBaseCode(), datasList);
			statusMap.put(BaseCode.MSG.getBaseCode(),StatusCode.SUCCESS.getMsg());
		}else{
			statusMap.put(BaseCode.STATUS.getBaseCode(),StatusCode.UNKNOWN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(),StatusCode.UNKNOWN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
}
