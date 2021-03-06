package org.silver.shop.controller.system.organization;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.service.system.organization.AgentTransaction;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 代理商control层
 *
 */
@RequestMapping("agent")
@Controller
public class AgentController {

	private static final String USER_LOGIN_TYPE = LoginType.AGENT.toString();

	@Autowired
	private AgentTransaction agentTransaction;

	/**
	 * 管理员添加代理商
	 * 
	 * @param response
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/addBaseInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation(value = "管理员添加代理商基本信息")
	public String addBaseInfo(HttpServletResponse response, HttpServletRequest req,
			@RequestParam("agentName") String agentName, @RequestParam("loginPassword") String loginPassword,
			@RequestParam("goodsRecordCommissionRate") double goodsRecordCommissionRate,
			@RequestParam("orderCommissionRate") double orderCommissionRate,
			@RequestParam("paymentCommissionRate") double paymentCommissionRate) {
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
		if (!params.isEmpty()) {
			return JSONObject.fromObject(agentTransaction.addAgentBaseInfo(params)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("参数不能为空!")).toString();
	}

	/**
	 * 代理商登录
	 * 
	 * @param account
	 *            账号
	 * @param loginPassword
	 *            登录密码
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "代理商--登录")
	public String login(@RequestParam("account") String account, @RequestParam("loginPassword") String loginPassword,
			HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (account != null && loginPassword != null) {
			Subject currentUser = SecurityUtils.getSubject();
			CustomizedToken customizedToken = new CustomizedToken(account, loginPassword, USER_LOGIN_TYPE);
			customizedToken.setRememberMe(false);
			try {
				currentUser.login(customizedToken);
				statusMap.put(BaseCode.STATUS.getBaseCode(), 1);
				statusMap.put(BaseCode.MSG.getBaseCode(), "登录成功!");
			} catch (IncorrectCredentialsException ice) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), -1);
				statusMap.put(BaseCode.MSG.getBaseCode(), "账号不存在或密码错误");
			} catch (LockedAccountException lae) {
				return JSONObject.fromObject(ReturnInfoUtils.errorInfo("账户未通过审核或已被禁用,请联系管理员!")).toString();
			} catch (AuthenticationException ae) {
				System.out.println(ae.getMessage());
				ae.printStackTrace();
			}
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 检查代理商是否登录
	 */
	@RequestMapping(value = "/checkLogin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查代理商是否登录")
	public String checkLogin(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.AGENT_INFO.toString());
		if (managerInfo != null && currentUser.isAuthenticated()) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("代理商已登陆！")).toString();
		} else {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("未登陆,请先登录！")).toString();
		}
	}

	@RequestMapping(value = "/getAllAgentInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员查询所有代理商信息")
	@ResponseBody
	@RequiresRoles("Manager")
	public String getAllAgentInfo(HttpServletRequest req, HttpServletResponse response, @RequestParam("page") int page,
			@RequestParam("size") int size) {
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
		return JSONObject.fromObject(agentTransaction.getAllAgentInfo(params, page, size)).toString();
	}

	@RequestMapping(value = "/setAgentSubMerchant", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员为代理商设置子商户")
	@ResponseBody
	@RequiresRoles("Manager")
	public String setAgentSubMerchant(HttpServletRequest req, HttpServletResponse response) {
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
		return JSONObject.fromObject(agentTransaction.setAgentSubMerchant(params)).toString();
	}

	@RequestMapping(value = "/getSubMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员查询代理商下所有子商户信息")
	@ResponseBody
	@RequiresRoles("Manager")
	public String getSubMerchantInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(agentTransaction.getSubMerchantInfo()).toString();
	}

	/**
	 * 注销代理商信息
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("代理商注销")
	// @RequiresRoles("Merchant")
	public String logout(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser != null) {
			try {
				currentUser.logout();
				return JSONObject.fromObject(ReturnInfoUtils.errorInfo("注销成功,请重新登陆！")).toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("未登陆,请先登录！")).toString();
	}
	
	public static void main(String[] args) {
		String regex = "^(?:(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9])).{6,20}$";
		String str = "123456aAsc";
		if (!str.matches(regex)) {
		System.out.println("----->>>>");
		}else{
			System.out.println("--包");
		}
		UUID uuid = UUID.randomUUID();
		System.out.println("----->>"+uuid);
	}
}
