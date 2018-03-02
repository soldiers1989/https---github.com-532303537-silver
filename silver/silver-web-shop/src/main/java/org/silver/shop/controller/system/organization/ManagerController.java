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
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
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
	public String findAllmemberInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = managerTransaction.findAllmemberInfo(req, page, size);
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
	public String findAllMerchantInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = managerTransaction.findAllMerchantInfo(req, page, size);
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
	 * 超级管理员查询所有运营人员信息
	 * 
	 * @param req
	 * @param response
	 * @return JSON
	 */
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

	/**
	 * 超级管理员重置运营人员密码
	 * 
	 * @param req
	 * @param response
	 * @param managerId
	 *            管理员Id
	 * @return Json
	 */
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
	@RequestMapping(value = "/managerAddMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("管理员添加商户")
	@ResponseBody
	@RequiresRoles("Manager")
	public String managerAddMerchantInfo(@RequestParam("merchantName") String merchantName,
			@RequestParam("loginPassword") String loginPassword,
			@RequestParam("merchantIdCardName") String merchantIdCardName,
			@RequestParam("merchantIdCard") String merchantIdCard, String recordInfoPack,
			@RequestParam("type") int type, @RequestParam("imgLength") int imgLength, String phone,HttpServletRequest req,
			HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (type == 1 || type == 2 && imgLength > 0) {
			statusMap = managerTransaction.managerAddMerchantInfo(merchantName, loginPassword, merchantIdCard,
					merchantIdCardName, recordInfoPack, type, imgLength, req,phone);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
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
			statusMap = managerTransaction.editMerhcnatInfo(req, length);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
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
	@RequiresRoles("Manager")
	public String editMerhcnatBusinessInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("imgLength") int imgLength, @RequestParam("merchantId") String merchantId,
			@RequestParam("merchantName") String merchantName) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (imgLength > 0) {
			statusMap = managerTransaction.editMerhcnatBusinessInfo(req, imgLength, merchantId, merchantName);
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
	@RequiresRoles("Manager")
	@ApiOperation("管理员查询用户详情")
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
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
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
	@RequiresRoles("Manager")
	@ApiOperation("管理员查看商户备案信息")
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
	@RequiresRoles("Manager")
	@ApiOperation("管理员修改商户备案信息")
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
	@RequiresRoles("Manager")
	@ApiOperation("管理员删除商户备案信息")
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
	@RequiresRoles("Manager")
	@ApiOperation("管理员审核商户")
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
	@ApiOperation("超级管理员重置运营人员密码")
	@ResponseBody
	@RequiresRoles("Manager")
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
}
