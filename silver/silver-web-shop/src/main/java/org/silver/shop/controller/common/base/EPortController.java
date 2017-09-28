package org.silver.shop.controller.common.base;

import java.util.HashMap;
import java.util.Map;

import org.silver.shop.service.common.base.EPortTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

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
	 * @param customsPort 口岸编码
	 * @param customsPortName 口岸名称
	 * @param cityCode  关联城市编码
	 * @return
	 */
	@RequestMapping(value = "/addEPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("添加已开通的口岸")
	public String addEPort(@RequestParam("customsPort") String customsPort,
			@RequestParam("customsPortName") String customsPortName, @RequestParam("cityCode") String cityCode) {
		Map<String, Object> statusMap = new HashMap<>();
		if (customsPortName != null && customsPortName != null && cityCode != null) {

		}
		return null;
	}
}
