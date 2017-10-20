package org.silver.shop.controller.system.commerce;

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.commerce.WarehousTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 仓库
 *
 */
@Controller
@RequestMapping("/warehous")
public class WarehousController {
	@Autowired
	private WarehousTransaction warehousTransaction;
	
	@RequestMapping(value = "/searchAlreadyRecordGoodsDetails", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询商户下仓库")
	@RequiresRoles("Merchant")
	public String getWarehousInfo(){
		Map<String, Object> statusMap = warehousTransaction.getWarehousInfo();
		return JSONObject.fromObject(statusMap).toString();
	}
	
}