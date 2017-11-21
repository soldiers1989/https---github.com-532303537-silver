package org.silver.shop.controller.system.organization;

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
import org.silver.shop.service.system.organization.ManagerTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 管理员
 */
@Controller
@RequestMapping("/manager")
public class ManagerController {
	private static final String USER_LOGIN_TYPE = LoginType.MANAGER.toString();

	@Autowired
	private ManagerTransaction managerTransaction;

	@RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "管理员--登录")
	public String login(@RequestParam("account") String account, @RequestParam("loginPassword") String loginPassword,
			HttpServletRequest req, HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> statusMap = new HashMap<>();
		if (account != null && loginPassword != null) {
			Subject currentUser = SecurityUtils.getSubject();
			currentUser.logout();
			if (!currentUser.isAuthenticated()) {
				CustomizedToken customizedToken = new CustomizedToken(account, loginPassword, USER_LOGIN_TYPE);
				customizedToken.setRememberMe(false);
				try {
					currentUser.login(customizedToken);
					statusMap.put(BaseCode.STATUS.getBaseCode(), 1);
					statusMap.put(BaseCode.MSG.getBaseCode(), "登录成功");
					return JSONObject.fromObject(statusMap).toString();
				} catch (IncorrectCredentialsException ice) {
					System.out.println("账号/密码不匹配！");
				} catch (LockedAccountException lae) {
					System.out.println("账户已被冻结！");
				} catch (AuthenticationException ae) {
					System.out.println(ae.getMessage());
					ae.printStackTrace();
				}
			}
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), -1);
		statusMap.put(BaseCode.MSG.getBaseCode(), "账号不存在或密码错误");
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/findAllmemberInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员查询所有用户信息")
	@ResponseBody
	@RequiresRoles("Manager")
	public String findAllmemberInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = managerTransaction.findAllmemberInfo();
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/createManager", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("创建管理员或运营管理员")
	public String createManager(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("managerName") String managerName, @RequestParam("loginPassword") String loginPassword,
			@RequestParam("managerMarks") int managerMarks) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (managerName != null && loginPassword != null && managerMarks == 1 || managerMarks == 2) {
			statusMap = managerTransaction.createManager(managerName, loginPassword, managerMarks);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	@RequestMapping(value = "/findAllMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员查询所有商户信息")
	public String findAllMerchantInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = managerTransaction.findAllMerchantInfo();
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/findMerchantDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员查询商户详情")
	public String findMerchantDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (merchantId != null) {
			 statusMap = managerTransaction.findMerchantDetail(merchantId);
			 return JSONObject.fromObject(statusMap).toString();
		}else{
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}
	
	@RequestMapping(value = "/updateManagerPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("修改管理员密码")
	public String updateManagerPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("oldLoginPassword")String  oldLoginPassword,@RequestParam("newLoginPassword") String newLoginPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (oldLoginPassword != null && newLoginPassword !=null ) {
			 statusMap = managerTransaction.updateManagerPassword(oldLoginPassword,newLoginPassword);
			 return JSONObject.fromObject(statusMap).toString();
		}else{
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}
}
