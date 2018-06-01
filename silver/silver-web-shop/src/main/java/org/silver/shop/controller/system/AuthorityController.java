package org.silver.shop.controller.system;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.AuthorityTransaction;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
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
 * 权限Controller
 */
@Controller
@RequestMapping("/authority")
public class AuthorityController {

	@Autowired
	private AuthorityTransaction authorityTransaction;

	/**
	 * 管理员添加权限信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/addAuthorityInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员添加权限信息")
	public String addAuthorityInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("type") String type, @RequestParam("groupName") String groupName,
			@RequestParam("firstName") String firstName, @RequestParam("firstCode") String firstCode,
			@RequestParam("secondName") String secondName, @RequestParam("secondCode") String secondCode,
			@RequestParam("thirdName") String thirdName, @RequestParam("thirdCode") String thirdCode) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		if (datasMap.isEmpty()) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("权限信息不能为空!")).toString();
		}
		return JSONObject.fromObject(authorityTransaction.addAuthorityInfo(datasMap)).toString();
	}

	/**
	 * 管理员查询所有权限字典信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getAuthorityInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员查询所有权限字典信息")
	public String getAuthorityInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(authorityTransaction.getAuthorityInfo()).toString();
	}

	/**
	 * 管理员针对对应的用户组查询权限信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getUserAuthorityInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员获取用户所有权限")
	public String getUserAuthorityInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("userId") String userId, @RequestParam("groupName") String groupName) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(userId)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("用户Id不能为空!")).toString();
		}
		if (StringEmptyUtils.isEmpty(groupName)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("分组名称不能为空!")).toString();
		}
		return JSONObject.fromObject(authorityTransaction.getUserAuthorityInfo(userId, groupName)).toString();
	}

	/**
	 * 管理员设置角色权限信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/setAuthorityInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员设置角色权限信息")
	public String setAuthorityInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("type") String type, @RequestParam("userId") String userId,
			@RequestParam("authorityPack") String authorityPack) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		if (datasMap.isEmpty()) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		return JSONObject.fromObject(authorityTransaction.setAuthorityInfo(datasMap)).toString();
	}

	/**
	 * 管理员修改权限字典信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/editAuthorityInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员修改权限字典信息")
	public String editAuthorityInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("authorityId") String authorityId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return JSONObject.fromObject(authorityTransaction.editAuthorityInfo(datasMap)).toString();
	}
}
