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

	/**
	 * 创建管理员或运营管理员
	 * 
	 * @param req
	 * @param response
	 * @param managerName
	 *            账号名称
	 * @param loginPassword
	 *            登录密码
	 * @param managerMarks
	 *            管理员标识1-超级管理员2-运营管理员
	 * @return
	 */
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

	/**
	 * 管理员查询所有商户信息
	 * 
	 * @param req
	 * @param response
	 * @return String
	 */
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
		Map<String, Object> statusMap = managerTransaction.findAllMerchantInfo(req);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 管理员查看商户详情
	 * 
	 * @param req
	 * @param response
	 * @param merchantId
	 *            商户Id
	 * @return
	 */
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
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	/**
	 * 管理员修改密码
	 * 
	 * @param req
	 * @param response
	 * @param oldLoginPassword
	 *            旧密码
	 * @param newLoginPassword
	 *            新密码
	 * @return String
	 */
	@RequestMapping(value = "/updateManagerPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("修改管理员密码")
	public String updateManagerPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("oldLoginPassword") String oldLoginPassword,
			@RequestParam("newLoginPassword") String newLoginPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (oldLoginPassword != null && newLoginPassword != null) {
			statusMap = managerTransaction.updateManagerPassword(oldLoginPassword, newLoginPassword);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	/**
	 * 管理员修改商户状态
	 * 
	 * @param req
	 * @param response
	 * @param merchantId
	 *            商户Id
	 * @param status
	 *            商户状态：1-启用，2-禁用，3-审核
	 * @return String
	 */
	@RequestMapping(value = "/editMerchantStatus", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("修改商户状态")
	public String editMerchantStatus(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId, @RequestParam("status") int status) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (merchantId != null && status == 1 && status == 2) {
			statusMap = managerTransaction.editMerchantStatus(merchantId, status);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	@RequestMapping(value = "/findAllManagerInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("超级管理员查询所有运营人员信息")
	@ResponseBody
	@RequiresRoles("Manager")
	public String findAllManagerInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = managerTransaction.findAllManagerInfo();
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/resetManagerPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("超级管理员重置运营人员密码")
	@ResponseBody
	@RequiresRoles("Manager")
	public String resetManagerPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("managerId") String managerId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = managerTransaction.resetManagerPassword(managerId);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/managerAddMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加商户")
	@ResponseBody
	@RequiresRoles("Manager")
	public String managerAddMerchantInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = managerTransaction.managerAddMerchantInfo(req);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/editMerhcnatInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员修改商户信息")
	@ResponseBody
	@RequiresRoles("Manager")
	public String editMerhcnatInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("length") int length) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (length > 0) {
			statusMap = managerTransaction.editMerhcnatInfo(req,length);
		}else{
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
}
