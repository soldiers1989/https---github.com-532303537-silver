package org.silver.shop.controller.system.organization;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.service.system.organization.ManagerTransaction;
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
	@ResponseBody
	@ApiOperation("管理员查询用户信息")
	@RequiresPermissions("member:findAllmemberInfo")
	public String findAllmemberInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(managerTransaction.findAllmemberInfo(req, page, size)).toString();
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
	@ApiOperation("创建管理员")
	@RequiresPermissions("manager:createManager")
	public String createManager(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("managerName") String managerName, @RequestParam("loginPassword") String loginPassword,
			@RequestParam("managerMarks") int managerMarks, String description, String realName) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (managerName != null && loginPassword != null) {
			return JSONObject.fromObject(
					managerTransaction.createManager(managerName, loginPassword, managerMarks, description, realName))
					.toString();
		} else {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数错误！")).toString();
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
	@ApiOperation("管理员查询所有商户信息")
	@RequiresPermissions("merchant:findAllMerchantInfo")
	public String findAllMerchantInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(managerTransaction.findAllMerchantInfo(req, page, size)).toString();
	}

	/**
	 * 管理员获取商户基本信息
	 * 
	 * @param req
	 * @param response
	 * @param merchantId
	 *            商户Id
	 * @return
	 */
	@RequestMapping(value = "/getMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员获取商户基本信息")
	@RequiresPermissions("merchant:findAllMerchantInfo")
	public String getMerchantInfo(HttpServletRequest req, HttpServletResponse response,
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
	@ApiOperation("管理员修改密码")
	public String updateManagerPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("oldLoginPassword") String oldLoginPassword,
			@RequestParam("newLoginPassword") String newLoginPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (oldLoginPassword != null && newLoginPassword != null) {
			return JSONObject.fromObject(managerTransaction.updateManagerPassword(oldLoginPassword, newLoginPassword)).toString();
		} else {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("密码不能为空！")).toString();
		}
	}

	
	@RequestMapping(value = "/findAllManagerInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("查询所有管理员")
	@ResponseBody
	@RequiresPermissions("manager:findAllManagerInfo")
	public String findAllManagerInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(managerTransaction.findAllManagerInfo()).toString();
	}

	@RequestMapping(value = "/resetManagerPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("重置管理员密码")
	@ResponseBody
	@RequiresPermissions("manager:resetManagerPassword")
	public String resetManagerPassword(HttpServletRequest req, HttpServletResponse response,
			 String managerId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if(StringEmptyUtils.isEmpty(managerId)){
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("管理员id不能为空！")).toString();
		}
		return JSONObject.fromObject(managerTransaction.resetManagerPassword(managerId)).toString();
	}

	/**
	 * 管理员添加商户
	 * 
	 * @param merchantName
	 *            商户名称
	 * @param loginPassword
	 *            登录密码
	 * @param merchantIdCard
	 *            身份证号码
	 * @param merchantIdCardName
	 *            身份证名字
	 * @param recordInfoPack
	 *            第三方商户注册备案信息包(由JSON转成String)
	 * @param type
	 *            1-银盟商户注册,2-第三方商户注册
	 * @param length
	 *            图片长度
	 * @param req
	 * @param response
	 * @return JSON
	 */
	@RequestMapping(value = "/addMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加商户")
	@ResponseBody
	@RequiresPermissions("merchant:addMerchantInfo")
	public String addMerchantInfo(String recordInfoPack, HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Enumeration<String> iskey = req.getParameterNames();
		Map<String, Object> datasMap = new HashMap<>();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		int type = Integer.parseInt(datasMap.get("type") + "");
		int imgLength = Integer.parseInt(datasMap.get("imgLength") + "");
		if (type == 1 || type == 2 && imgLength > 0) {
			return JSONObject.fromObject(managerTransaction.managerAddMerchantInfo(req, datasMap)).toString();
		} else {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("添加商户失败,请求参数错误!")).toString();
		}
	}

	/**
	 * 修改商户信息
	 * 
	 * @param req
	 * @param response
	 * @param length
	 *            参数长度
	 * @return JSON
	 */
	@RequestMapping(value = "/editMerhcnatInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员修改商户信息")
	@ResponseBody
	@RequiresPermissions("merchant:editMerhcnatInfo")
	public String editMerhcnatInfo(HttpServletRequest req, HttpServletResponse response, int length) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (length > 0) {
			return JSONObject.fromObject(managerTransaction.editMerhcnatInfo(req, length)).toString();
		} else {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("参数length错误!")).toString();
		}
	}

	/**
	 * 管理员修改商户业务(图片)信息
	 * 
	 * @param req
	 * @param response
	 * @param length
	 *            图片长度
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @return
	 */
	@RequestMapping(value = "/editMerhcnatBusinessInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员修改商户业务(图片)信息")
	@ResponseBody
	@RequiresPermissions("merchant:editMerhcnatBusinessInfo")
	public String editMerhcnatBusinessInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("imgLength") int imgLength, @RequestParam("merchantId") String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (imgLength > 0) {
			statusMap = managerTransaction.editMerhcnatBusinessInfo(req, imgLength, merchantId);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 管理员查看用户详情
	 * 
	 * @param req
	 * @param response
	 * @param merchantId
	 *            商户Id
	 * @return
	 */
	@RequestMapping(value = "/findMemberDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	// @RequiresPermissions("member:findAllmemberInfo")
	public String findMemberDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("memberId") String memberId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (memberId != null) {
			statusMap = managerTransaction.findMemberDetail(memberId);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	/**
	 * 检查管理员登录
	 */
	@RequestMapping(value = "/checkManagerLogin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查管理员登录")
	// @RequiresRoles("Merchant")
	public String checkManagerLogin(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		Map<String, Object> statusMap = new HashMap<>();
		if (managerInfo != null && currentUser.isAuthenticated()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "管理员已登陆！");
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.PERMISSION_DENIED.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "未登陆,请先登录！");
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 管理员修改用户信息
	 * 
	 * @param req
	 * @param response
	 * @param merchantId
	 *            商户Id
	 * @return
	 */
	@RequestMapping(value = "/managerEditMemberInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员修改用户信息")
	public String managerEditMemberInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = managerTransaction.managerEditMemberInfo(req);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 管理员查看商户备案信息
	 * 
	 * @param req
	 * @param response
	 * @param merchantId
	 *            商户Id
	 * @return
	 */
	@RequestMapping(value = "/findMerchantRecordDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查看商户备案信息")
	@RequiresPermissions("merchant:findMerchantRecordDetail")
	public String findMerchantRecordDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (merchantId != null) {
			statusMap = managerTransaction.findMerchantRecordDetail(merchantId);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	/**
	 * 管理员修改商户备案信息
	 * 
	 * @param req
	 * @param response
	 * @param merchantId
	 *            商户Id
	 * @return
	 */
	@RequestMapping(value = "/editMerchantRecordDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员修改商户备案信息")
	@RequiresPermissions("merchant:editMerchantRecordDetail")
	public String editMerchantRecordDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId,
			@RequestParam("merchantRecordInfo") String merchantRecordInfo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (merchantId != null) {
			statusMap = managerTransaction.editMerchantRecordDetail(merchantId, merchantRecordInfo);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	/**
	 * 管理员删除商户备案信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/managerDeleteMerchantRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员删除商户备案信息")
	@RequiresPermissions("merchant:managerDeleteMerchantRecordInfo")
	public String managerDeleteMerchantRecordInfo(HttpServletRequest req, HttpServletResponse response, long id) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (id > 0) {
			statusMap = managerTransaction.deleteMerchantRecordInfo(id);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	/**
	 * 管理员审核商户
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/managerAuditMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员审核商户")
	@RequiresPermissions("merchant:managerAuditMerchantInfo")
	public String managerAuditMerchantInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantPack") String merchantPack) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);

		return JSONObject.fromObject(managerTransaction.managerAuditMerchantInfo(merchantPack)).toString();
	}

	/**
	 * 管理员重置商户登录密码
	 * 
	 * @param req
	 * @param response
	 * @param managerId
	 *            管理员Id
	 * @return Json
	 */
	@RequestMapping(value = "/resetMerchantLoginPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("超级管理员重置商户登录密码")
	@RequiresPermissions("merchant:resetMerchantLoginPassword")
	public String resetMerchantLoginPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = managerTransaction.resetMerchantLoginPassword(merchantId);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 管理员获取商户业务(详情)信息
	 * 
	 * @param req
	 * @param response
	 * @param merchantId
	 *            商户Id
	 * @return
	 */
	@RequestMapping(value = "/getMerchantBusinessInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员获取商户业务(详情)信息")
	@RequiresPermissions("merchant:getMerchantBusinessInfo")
	public String getMerchantBusinessInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (merchantId != null) {
			statusMap = managerTransaction.getMerchantBusinessInfo(merchantId);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	/**
	 * 临时-管理员更新旧管理员权限信息进权限表中
	 * 
	 * @param req
	 * @param response
	 * @return Json
	 */
	@RequestMapping(value = "/tmpUpdateAuthority", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("临时-管理员更新旧管理员权限信息进权限表中")
	@ResponseBody
	@RequiresRoles("Manager")
	public String tmpUpdateAuthority(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(managerTransaction.tmpUpdateAuthority()).toString();
	}

	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("获取管理员信息")
	@RequiresRoles("Manager")
	// @RequiresPermissions("merchant:getInfo")
	public String getInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		if(managerInfo == null){
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("未知错误！")).toString();
		}
		return JSONObject.fromObject(managerInfo).toString();
	}
}
