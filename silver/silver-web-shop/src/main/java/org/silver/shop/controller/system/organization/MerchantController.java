package org.silver.shop.controller.system.organization;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import org.silver.shop.service.system.organization.MerchantTransaction;
import org.silver.shop.utils.RedisInfoUtils;
import org.silver.util.IdcardValidator;
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
 * 商户controller,商户的操作
 */
@Controller
@RequestMapping("/merchant")
public class MerchantController {

	private static final String USER_LOGIN_TYPE = LoginType.MERCHANT.toString();

	@Autowired
	private MerchantTransaction merchantTransaction;

	/**
	 * 商戶登录
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
	@ApiOperation(value = "商户--登录")
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
				statusMap.put(BaseCode.MSG.getBaseCode(), "账户未通过审核或已被禁用,请联系管理员!");
			} catch (AuthenticationException ae) {
				System.out.println(ae.getMessage());
				ae.printStackTrace();
			}
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 商户注册
	 * 
	 * @param account
	 *            账号名
	 * @param loginPassword
	 *            登录密码
	 * @param merchantIdCard
	 *            身份证号码
	 * @param merchantIdCardName
	 *            身份证名称
	 * @param recordInfoPack
	 *            第三方商户注册备案信息包(由JSON转成String)
	 * @param type
	 *            1-银盟商户注册,2-第三方商户注册
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String merchantRegister(HttpServletResponse response, HttpServletRequest req) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		String type = datasMap.get("type") + "";
		String merchantIdCard = datasMap.get("merchantIdCard") + "";
		Map<String, Object> statusMap = new HashMap<>();
		if (!IdcardValidator.isValidatedAllIdcard(merchantIdCard)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("身份证号码错误,请重新输入!")).toString();
		}
		if ("1".equals(type)) {// 1-银盟商户注册
			statusMap = merchantTransaction.merchantRegister(datasMap);
			return JSONObject.fromObject(statusMap).toString();
		} else if ("2".equals(type)) {// 2-第三方商户注册
			statusMap = merchantTransaction.merchantRegister(datasMap);
			return JSONObject.fromObject(statusMap).toString();
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.FORMAT_ERR.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.FORMAT_ERR.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 检查商户名称是否重复
	 * 
	 * @param account
	 *            商户名称
	 * @return Map
	 */
	@RequestMapping(value = "/checkMerchantName", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String checkMerchantName(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("account") String account) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (account != null && !"".equals(account)) {// 判断前台传递的值不为空
			List<Object> reList = merchantTransaction.checkMerchantName(account);
			if (reList.isEmpty()) {// 查询出来的数据为空
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), "商戶名可以使用!");
				return JSONObject.fromObject(statusMap).toString();
			} else {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), "商户名已存在,请重新输入!");
				return JSONObject.fromObject(statusMap).toString();
			}
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 查询商户信息
	 * 
	 * @return Map
	 */
	@RequestMapping(value = "/findMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	public String getMerchantInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		if (merchantInfo != null) {
			// 将session内的商户登录密码清空
			merchantInfo.setLoginPassword("");
			return JSONObject.fromObject(ReturnInfoUtils.successDataInfo(merchantInfo)).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("服务器繁忙!")).toString();
	}

	/**
	 * 修改商户业务信息(营业执照、税务登记证等等图片及编码)
	 * @return
	 */
	@RequestMapping(value = "/editMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("修改商户业务信息")
	public String editMerchantInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject( merchantTransaction.editBusinessInfo(req)).toString();
	}

	/**
	 * 注销商户信息
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户注销")
	// @RequiresRoles("Merchant")
	public String logout(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser != null) {
			try {
				currentUser.logout();
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "商户注销成功,请重新登陆！");
			} catch (Exception e) {
				e.printStackTrace();
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.PERMISSION_DENIED.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "未登陆,请先登录！");
			}
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/editMerchantLoginPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("修改商户登录密码")
	@RequiresRoles("Merchant")
	public String editMerchantLoginPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("oldLoginPassword") String oldLoginPassword,
			@RequestParam("newLoginPassword") String newLoginPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (oldLoginPassword != null && newLoginPassword != null) {
			statusMap = merchantTransaction.editLoginPassword(oldLoginPassword, newLoginPassword);
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@ResponseBody
	@RequestMapping(value = "/checkMerchantRealName", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("检查商户实名")
	@RequiresRoles("Merchant")
	public String checkMerchantRealName() {

		return null;
	}

	/**
	 * 检查商户登录
	 */
	@RequestMapping(value = "/checkMerchantLogin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查商户登录")
	// @RequiresRoles("Merchant")
	public String checkMerchantLogin(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		if (merchantInfo != null && currentUser.isAuthenticated()) {
			return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
		} else {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("未登陆,请先登录！")).toString();
		}
	}

	@RequestMapping(value = "/getMerchantRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户查看海关备案信息")
	@RequiresRoles("Merchant")
	public String getMerchantRecordInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantTransaction.getMerchantRecordInfo()).toString();
	}

	@RequestMapping(value = "/publicMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商城前台公开获取商户信息")
	public String publicMerchantInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("merchantId") String merchantId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("商户Id不能为空!")).toString();
		}
		return JSONObject.fromObject(merchantTransaction.publicMerchantInfo(merchantId)).toString();
	}
	
	@RequestMapping(value = "/setRelatedMember", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("设置代付会员信息")
	@RequiresRoles("Merchant")
	public String setRelatedMember(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("accountName") String accountName, @RequestParam("loginPassword") String loginPassword
			, @RequestParam("payPassword") String payPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantTransaction.setRelatedMember(accountName, loginPassword,payPassword)).toString();
	}

	@RequestMapping(value = "/getRelatedMemberFunds", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询关联的会员储备资金")
	@RequiresRoles("Merchant")
	public String getRelatedMemberFunds(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String,Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key =  isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return JSONObject.fromObject(merchantTransaction.getRelatedMemberFunds(page, size)).toString();
	}
	
	@RequestMapping(value = "/getBusinessInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("获取业务信息")
	public String getBusinessInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(merchantTransaction.getBusinessInfo()).toString();
	}
	
	@RequestMapping(value = "/updateBaseInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户修改基本信息")
	public String updateBaseInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String,Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key =  isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		if(datasMap.isEmpty()){
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("修改参数不能为空！")).toString();
		}
		return JSONObject.fromObject(merchantTransaction.updateBaseInfo(datasMap)).toString();
	}
}
