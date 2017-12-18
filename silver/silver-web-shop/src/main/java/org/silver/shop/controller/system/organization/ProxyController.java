package org.silver.shop.controller.system.organization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import org.silver.common.StatusCode;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.organization.Proxy;
import org.silver.shop.service.system.organization.ProxyTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/proxy")
public class ProxyController {
	private static final String USER_LOGIN_TYPE = LoginType.PROXY.toString();

	@Autowired
	private ProxyTransaction proxyTransaction;

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
	@ApiOperation(value = "代理--登录")
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
				statusMap.put(BaseCode.MSG.getBaseCode(), "登录成功");
			} catch (IncorrectCredentialsException ice) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), -1);
				statusMap.put(BaseCode.MSG.getBaseCode(), "账号不存在或密码错误");
			} catch (LockedAccountException lae) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), -1);
				statusMap.put(BaseCode.MSG.getBaseCode(), "账户已被冻结");
			} catch (AuthenticationException ae) {
				System.out.println(ae.getMessage());
				ae.printStackTrace();
			}
		}
		return JSONObject.fromObject(statusMap).toString();
	}
	
	/**
	 * 查询代理信息
	 * 
	 * @return Map
	 */
	@RequestMapping(value = "/findProxyInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Proxy")
	public String findProxyInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Proxy proxyInfo = (Proxy) currentUser.getSession()
				.getAttribute(LoginType.PROXYINFO.toString());
		if (!"".equals(proxyInfo)) {
			// 将session内的商户登录密码清空
			proxyInfo.setLoginPassword("");
			reMap.put(BaseCode.STATUS.getBaseCode(), 1);
			reMap.put(BaseCode.DATAS.getBaseCode(), proxyInfo);
			reMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return JSONObject.fromObject(reMap).toString();
		}
		reMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.LOSS_SESSION.getStatus());
		reMap.put(BaseCode.MSG.getBaseCode(), StatusCode.LOSS_SESSION.getMsg());
		return JSONObject.fromObject(reMap).toString();
	}
	
	@RequestMapping(value = "/getProxyMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("获取代理下商户信息")
	@RequiresRoles("Proxy")
	public String getProxyMerchantInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = proxyTransaction.getProxyMerchantInfo();
		return JSONObject.fromObject(statusMap).toString();
	}
	
	
	
	/**
	 * 检查代理商登录
	 */
	@RequestMapping(value = "/checkProxyLogin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查代理商登录")
	// @RequiresRoles("Merchant")
	public String checkProxyLogin(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Proxy proxyInfo = (Proxy) currentUser.getSession().getAttribute(LoginType.PROXYINFO.toString());
		Map<String, Object> statusMap = new HashMap<>();
		if (proxyInfo != null && currentUser.isAuthenticated()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "已登陆！");
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.PERMISSION_DENIED.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "未登陆,请先登录！");
		}
		return JSONObject.fromObject(statusMap).toString();
	}
}
