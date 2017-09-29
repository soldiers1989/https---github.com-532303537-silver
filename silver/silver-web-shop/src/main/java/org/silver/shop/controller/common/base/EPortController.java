package org.silver.shop.controller.common.base;

import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.common.base.EPortTransaction;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 口岸Controller
 */
@Controller
@RequestMapping("/port")
public class EPortController {
	@Autowired
	private EPortTransaction ePortTransaction;

	/**
	 * 添加开通的口岸
	 * 
	 * @param customsPort
	 *            口岸编码
	 * @param customsPortName
	 *            口岸名称
	 * @param cityCode
	 *            关联城市编码
	 * @return
	 */
	@RequestMapping(value = "/addEPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("添加已开通的口岸")
	public String addEPort(@RequestParam("customsPort") String customsPort,
			@RequestParam("customsPortName") String customsPortName, @RequestParam("cityCode") String cityCode) {
		Map<String, Object> statusMap = new HashMap<>();
		if (customsPort != null && customsPortName != null && cityCode != null) {
			statusMap = ePortTransaction.addEPort(customsPort, customsPortName, cityCode);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.UNKNOWN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.UNKNOWN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 检查口岸名是否重复
	 * 
	 * @param customsPortName
	 *            口岸名称
	 * @return
	 */
	@RequestMapping(value = "/checkEPortName", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查口岸名是否重复")
	public String checkEPortName(@RequestParam("customsPortName") String customsPortName) {
		Map<String, Object> statusMap = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(customsPortName)) {
			statusMap = ePortTransaction.checkEPortName(customsPortName);

		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 查询省市口岸三级联动
	 * @return
	 */
	@RequestMapping(value = "/findEPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询省市口岸三级联动")
	public String findEPort() {
		ePortTransaction.findEPort();
		return null;
	}
}
