package org.silver.shop.controller.system.tenant;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.tenant.RecipientTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 用户收货地址
 *
 */
@Controller
@RequestMapping("/recipient")
public class RecipientController {

	@Autowired
	private RecipientTransaction recipientTransaction;

	@RequestMapping(value = "/addRecipientInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("用户添加收货地址信息")
	@RequiresRoles("Member")
	@ResponseBody
	public String addRecipientInfo(@RequestParam("recipientInfo") String recipientInfo,HttpServletRequest req,HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> stautsMap = new HashMap<>();
		if (recipientInfo != null) {
			stautsMap = recipientTransaction.addRecipientInfo(recipientInfo);
			return JSONObject.fromObject(stautsMap).toString();
		}
		stautsMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
		stautsMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		return JSONObject.fromObject(stautsMap).toString();
	}

	@RequestMapping(value = "/getMemberRecipientInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("用户查询收货地址信息")
	@RequiresRoles("Member")
	@ResponseBody
	public String getMemberRecipientInfo(HttpServletRequest req,HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> stautsMap = recipientTransaction.getMemberRecipientInfo();
		return JSONObject.fromObject(stautsMap).toString();
	}

	public static void main(String[] args) {
		JSONArray jsonList = new JSONArray();
		Map<String, Object> params = new HashMap<>();
		params.put("recipientName", "杨戬");
		params.put("recipientCardId", "身份证985");
		params.put("recipientTel", "1862001xxxx");
		params.put("recipientCountryName", "景德镇");
		params.put("recProvincesName", "北京市");
		params.put("recCityName", "市辖区");
		params.put("recAreaName", "密云区");
		params.put("recipientCountryCode", "142");
		params.put("recProvincesCode", "110000");
		params.put("recCityCode", "110100");
		params.put("recAreaCode", "110101");
		params.put("recipientAddr", "地址01");
		params.put("notes", "收货地址测试");
		jsonList.add(params);
		JSONArray jsonList2 = JSONArray.fromObject(jsonList.toString());
		System.out.println(jsonList2);
	}
}
