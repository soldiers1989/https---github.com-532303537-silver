package org.silver.shop.controller.common.base;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	 * 添加开通的口岸
	 * @param customsPort 口岸编码
	 * @param customsPortName 口岸名称
	 * @param cityCode 城市编码
	 * @param cityName 城市中文名称
	 * @param provinceCode 省份编码
	 * @param provinceName 省份中文名称
	 * @return String
	 */
	@RequestMapping(value = "/addEPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("添加已开通的口岸")
	public String addEPort(@RequestParam("customsPort") String customsPort,
			@RequestParam("customsPortName") String customsPortName, @RequestParam("cityCode") String cityCode,
			@RequestParam("cityName") String cityName, @RequestParam("provinceCode") String provinceCode,
			@RequestParam("provinceName") String provinceName) {
		Map<String, Object> statusMap = new HashMap<>();
		if (customsPort != null && customsPortName != null) {
			statusMap = ePortTransaction.addEPort(customsPort, customsPortName, cityCode, cityName, provinceCode,
					provinceName);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.UNKNOWN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.UNKNOWN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	

	/**
	 * 查询省市下关联的口岸
	 * 
	 * @return
	 */
	@RequestMapping(value = "/findEPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询省市下关联的口岸")
	public String findEPort(HttpServletRequest req,HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		statusMap = ePortTransaction.findEPort();
		if (statusMap != null) {
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.UNKNOWN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.UNKNOWN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
	
	/**
	 * 修改口岸
	 * @param id 数据库流水ID
	 * @param customsPort 口岸编码
	 * @param customsPortName 口岸名称
	 * @param cityCode 城市编码
	 * @param cityName 城市名称
	 * @param provinceCode 省份编码
	 * @param provinceName 省份名字
	 * @return
	 */
	@RequestMapping(value = "/editEPot", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("修改口岸")
	public String editEPot(@RequestParam("id")long id,@RequestParam("customsPort") String customsPort,@RequestParam("customsPortName")String customsPortName,
			@RequestParam("cityCode")String cityCode,@RequestParam("cityName")String cityName,@RequestParam("provinceCode")String provinceCode,
			@RequestParam("provinceName")String provinceName){
		Map<String,Object> stautsMap = new HashMap<>();
		if(StringEmptyUtils.isNotEmpty(id)){
			stautsMap = ePortTransaction.editEPot(id,customsPort,customsPortName,cityCode,cityName,provinceCode,provinceName);
		}else{
			stautsMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
			stautsMap.put(BaseCode.MSG.toString(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(stautsMap).toString();
	}
}
