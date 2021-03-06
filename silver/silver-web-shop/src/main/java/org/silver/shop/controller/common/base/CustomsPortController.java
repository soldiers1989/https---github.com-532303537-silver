package org.silver.shop.controller.common.base;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.common.base.CustomsPortTransaction;
import org.silver.util.ReturnInfoUtils;
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
	public String findAllCustomsPort(HttpServletRequest req, HttpServletResponse response, Integer page, Integer size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reMap = customsPortTransaction.findAllCustomsPort();
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reMap).toString();
		}
		List<Object> list = (List<Object>) reMap.get(BaseCode.DATAS.toString());
		if (StringEmptyUtils.isNotEmpty(page) && StringEmptyUtils.isNotEmpty(size)) {
			List<Object> newWords = new ArrayList<>();
			int currIdx = (page > 1 ? (page - 1) * size : 0);
			for (int i = 0; i < size && i < list.size() - currIdx; i++) {
				Object word = list.get(currIdx + i);
				newWords.add(word);
			}
			return JSONObject.fromObject(ReturnInfoUtils.successDataInfo(newWords, list.size())).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.successDataInfo(list, list.size())).toString();
	}

	/**
	 * 添加口岸下已开通的 海关及国检名称与编码
	 * 
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
	@RequiresPermissions("customsPort:addCustomsPort")
	public String addCustomsPort(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		int customsPort = Integer.parseInt(params.get("customsPort") + "");
		String customsPortName = params.get("customsPortName") + "";
		if (customsPort == 1 && customsPortName.equals("电子口岸") || customsPort == 2 && customsPortName.equals("智检")) {
			return JSONObject.fromObject(customsPortTransaction.addCustomsPort(params)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("口岸信息错误,请重新输入")).toString();
	}

	/**
	 * 商户查询当前已备案的海关及智检信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/findMerchantCustomsPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户查询当前已备案的海关及智检信息")
	@RequiresRoles("Merchant")
	public String findMerchantCustomsPort(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = null;
		statusMap = customsPortTransaction.findMerchantCustomsPort();
		if (statusMap == null && statusMap.size() <= 0) {
			statusMap = new HashMap<>();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 刪除已开通的 海关及国检名称与编码
	 * 
	 * @return
	 */
	@RequestMapping(value = "/deleteCustomsPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("刪除已开通的 海关及国检名称与编码")
	@RequiresPermissions("customsPort:deleteCustomsPort")
	public String deleteCustomsPort(HttpServletRequest req, HttpServletResponse response, @RequestParam("id") long id) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(customsPortTransaction.deleteCustomsPort(id)).toString();
	}

	/**
	 * 管理员修改已开通的 海关及国检名称与编码
	 * 
	 * @return
	 */
	@RequestMapping(value = "/modifyCustomsPort", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员修改已开通的海关及国检名称与编码")
	@RequiresPermissions("customsPort:modifyCustomsPort")
	public String modifyCustomsPort(HttpServletRequest req, HttpServletResponse response, @RequestParam("id") long id) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		return JSONObject.fromObject(customsPortTransaction.modifyCustomsPort(params)).toString();
	}
}
