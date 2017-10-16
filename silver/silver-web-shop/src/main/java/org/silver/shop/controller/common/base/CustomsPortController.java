package org.silver.shop.controller.common.base;

import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.common.base.CustomsPortTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 口岸及已开通海关、国检
 */
@Controller
@RequestMapping("/customsPort")
public class CustomsPortController {

	@Autowired
	private CustomsPortTransaction customsPortTransaction;

	/**
	 * 查询所有已开通的口岸及关联的海关
	 * 
	 * @return
	 */
	@RequestMapping(value = "/findAllCustomsPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询已开通的海关及智检")
	public String findAllCustomsPort() {
		Map<String,Object> statusMap = new HashMap<>();
		statusMap = customsPortTransaction.findAllCustomsPort();
		if(statusMap!=null && statusMap.size()>0){
			return JSONObject.fromObject(statusMap).toString();
		}else{
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 添加口岸下已开通的 海关及国检名称与编码
	 * @param provinceName
	 *            省份名称
	 * @param provinceCode
	 *            省份编码
	 * @param cityName
	 *            城市名称
	 * @param cityCode
	 *            城市编码
	 * @param customsPort
	 *            口岸编码：1-电子口岸，2-智检
	 * @param customsPortName
	 *            口岸名称
	 * @param customsCode
	 *            主管海关代码(同仓库编码)
	 * @param customsName
	 *            主管海关代码名称
	 * @param ciqOrgCode
	 *            检验检疫机构代码
	 * @param ciqOrgName
	 *            检验检疫机构名称
	 * @return
	 */
	@RequestMapping(value = "/addCustomsPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("添加已开通的海关及智检")
	public String addCustomsPort(@RequestParam("provinceName") String provinceName,
			@RequestParam("provinceCode") String provinceCode, @RequestParam("cityName") String cityName,
			@RequestParam("cityCode") String cityCode, @RequestParam("customsPort") int customsPort,
			@RequestParam("customsPortName") String customsPortName, @RequestParam("customsCode") String customsCode,
			@RequestParam("customsName") String customsName, @RequestParam("ciqOrgCode") String ciqOrgCode,
			@RequestParam("ciqOrgName") String ciqOrgName) {
		Map<String,Object> statusMap = null;
		statusMap = customsPortTransaction.addCustomsPort(provinceName, provinceCode, cityName, cityCode, customsPort,
				customsPortName, customsCode, customsName, ciqOrgCode, ciqOrgName);
		if(statusMap!=null && statusMap.size()>0){
			return JSONObject.fromObject(statusMap).toString();
		}else{
			statusMap = new HashMap<>();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
}
