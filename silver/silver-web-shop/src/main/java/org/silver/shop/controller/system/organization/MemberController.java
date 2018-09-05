package org.silver.shop.controller.system.organization;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.RedisKey;
import org.silver.common.StatusCode;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.service.system.organization.MemberTransaction;
import org.silver.shop.utils.CusAccessObjectUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.PhoneUtils;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SendMsg;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import com.google.code.kaptcha.Constants;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 用户 Controller
 */
@Controller
@RequestMapping("/member")
public class MemberController {

	/**
	 * 用户查询登录密码时缓存的手机号码
	 */
	private static final String RETRIEVE_LOGIN_PASSWORD_PHONE = "RETRIEVE_LOGIN_PASSWORD_PHONE";

	/**
	 * 用户重置登录密码时缓存UUID
	 */
	private static final String RETRIEVE_LOGIN_PASSWORD_UUID = "RETRIEVE_LOGIN_PASSWORD_UUID";

	/**
	 * 用户查询登录密码时缓存的用户id
	 */
	private static final String RETRIEVE_LOGIN_PASSWORD_MEMBER_ID = "RETRIEVE_LOGIN_PASSWORD_MEMBER_ID";
	/**
	 * 用户修改(更新)登录密码时缓存UUID
	 */
	private static final String UPDATE_LOGIN_PASSWORD_UUID = "UPDATE_LOGIN_PASSWORD_UUID";

	/**
	 * 用户设置支付密码时缓存UUID
	 */
	private static final String SET_PAYMENT_PASSWORD_UUID = "SET_PAYMENT_PASSWORD_UUID";

	/**
	 * 用户重置交易密码时缓存UUID
	 */
	private static final String RETRIEVE_PAYMENT_PASSWORD_UUID = "RETRIEVE_PAYMENT_PASSWORD_UUID";

	/**
	 * 用户重置交易密码时缓存身份证号码
	 */
	private static final String RETRIEVE_PAYMENT_PASSWORD_ID_NUMBER = "RETRIEVE_PAYMENT_PASSWORD_ID_NUMBER";

	/**
	 * 用户登录信息
	 */
	private static final String USER_LOGIN_TYPE = LoginType.MEMBER.toString();

	@Autowired
	private MemberTransaction memberTransaction;

	@RequestMapping(value = "/memberRegister", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户--注册")
	public String memberRegister(@RequestParam("account") String account, @RequestParam("loginPass") String loginPass,
			@RequestParam("memberIdCardName") String memberIdCardName,
			@RequestParam("memberIdCard") String memberIdCard, @RequestParam("memberTel") String memberTel,
			String verificationCode, HttpServletRequest req, HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Origin", "*");
		boolean result = verificationCode.matches("[0-9]+");
		if (account != null && loginPass != null && memberIdCardName != null && memberIdCard != null
				&& memberTel != null && result) {
			Map<String, Object> statusMap = memberTransaction.memberRegister(account, loginPass, memberIdCardName,
					memberIdCard, memberTel, Integer.parseInt(verificationCode));
			return JSONObject.fromObject(statusMap).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("参数错误,请重新输入!")).toString();
	}

	/**
	 * 用户登录
	 * 
	 * @param account
	 *            账号
	 * @param loginPassword
	 *            登录密码
	 * @return
	 */
	@RequestMapping(value = "/memberLogin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户--登录")
	public String memberLogin(@RequestParam("account") String account,
			@RequestParam("loginPassword") String loginPassword, String captcha, HttpServletRequest req,
			HttpServletResponse response) {
		String ipAddress = CusAccessObjectUtil.getIpAddress(req);
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String captchaCode = String.valueOf(session.getAttribute(Constants.KAPTCHA_SESSION_KEY));
		if (StringEmptyUtils.isEmpty(captcha) || !captcha.equalsIgnoreCase(captchaCode)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误,请重新输入！")).toString();
		}
		Subject currentUser = SecurityUtils.getSubject();
		// currentUser.logout();
		// 将IP地址拼接在账号变量中传递到登陆方法
		CustomizedToken customizedToken = new CustomizedToken(account + "_" + ipAddress, loginPassword,
				USER_LOGIN_TYPE);
		customizedToken.setRememberMe(false);
		try {
			currentUser.login(customizedToken);
			return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
		} catch (IncorrectCredentialsException ice) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("你输入的密码和账户名不匹配!")).toString();
		} catch (LockedAccountException lae) {
			// 账号锁定(冻结)错误
			// 多次输入密码错误，请 30 分钟之后再次尝试登录！
			//
			Long ttl = JedisUtil.ttl("SHOP_LOGIN_MEMBER_ERROR_COUNT_INT" + lae.getMessage() + "_" + ipAddress);
			int m = (ttl.intValue() % 3600) / 60;
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("多次输入密码错误，请 " + m + " 分钟之后再次尝试登录！")).toString();
		} catch (AuthenticationException ae) {
			// 账号认证失败错误
			// ae.printStackTrace();
			return passwordErrorInfo(ae.getMessage(), ipAddress);
		}
	}

	/**
	 * 当用户同一个Ip地址同一账号登陆多次密码失败后
	 * 
	 * @param memberId
	 *            商户Id
	 * @param ipAddress
	 *            IP地址
	 * @return
	 */
	private String passwordErrorInfo(String memberId, String ipAddress) {
		// 根据缓存中的KEY获取用户登陆次数
		String redis = JedisUtil.get("SHOP_LOGIN_MEMBER_ERROR_COUNT_INT" + memberId + "_" + ipAddress);
		if (StringEmptyUtils.isNotEmpty(redis)) {
			int count = Integer.parseInt(redis);
			if ((3 - count) <= 0) {
				return JSONObject.fromObject(ReturnInfoUtils.errorInfo("多次输入密码错误，请 15 分钟之后再次尝试登录！")).toString();
			} else {
				return JSONObject.fromObject(ReturnInfoUtils.errorInfo("密码错误,您还可以尝试" + (3 - count) + "次!")).toString();
			}
		} else {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("登录失败,请联系客服人员!")).toString();
		}
	}

	@RequestMapping(value = "/getMemberInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "获取用户信息")
	@RequiresRoles("Member")
	public String getMemberInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(memberTransaction.getInfo()).toString();
	}

	/**
	 * 检查用户登录
	 */
	@RequestMapping(value = "/checkMemberLogin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查用户登录")
	public String checkMemberLogin(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		if (memberInfo != null && currentUser.isAuthenticated()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "用户已登陆！");
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.PERMISSION_DENIED.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "未登陆,请先登录！");
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户注销")
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
				return JSONObject.fromObject(ReturnInfoUtils.errorInfo("注销成功,请重新登陆！")).toString();
			} catch (Exception e) {
				e.printStackTrace();
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.PERMISSION_DENIED.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "未登陆,请先登录！");
			}
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 用户设置购物车商品标识(暂定)
	 * 
	 * @param req
	 * @param response
	 * @param goodsId
	 * @param flag
	 *            用户商品选中标识1-为选择,2-已选择
	 * @return
	 */
	@RequestMapping(value = "/editShopCartGoodsFlag", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户设置购物车商品标识")
	@RequiresRoles("Member")
	public String editShopCartGoodsFlag(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("goodsInfoPack") String goodsInfoPack) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(memberTransaction.editShopCartGoodsFlag(goodsInfoPack)).toString();
	}

	@RequestMapping(value = "/getMemberWalletInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户获取钱包信息")
	@RequiresRoles("Member")
	public String getMemberWalletInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = memberTransaction.getMemberWalletInfo();
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 检查用户注册信息是否重复
	 * 
	 * @param datas
	 *            参数
	 * @param type
	 *            类型：memberName(用户名称),memberTel(手机号码),memberIdCard(身份证号)
	 * @return Map
	 */
	@RequestMapping(value = "/checkRegisterInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String checkRegisterInfo(String datas, String type, HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(type) || StringEmptyUtils.isEmpty(datas)) {
			return JSONObject.fromObject("请求参数不能为空!").toString();
		}
		return JSONObject.fromObject(memberTransaction.checkRegisterInfo(datas, type)).toString();
	}

	/**
	 * 发送用户注册时手机验证码
	 * 
	 * @param phone
	 *            手机号码
	 * @return Map
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@RequestMapping(value = "/sendMemberRegisterCode", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String sendMemberRegisterCode(String phone, HttpServletRequest req, HttpServletResponse response,
			String captcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(phone) || StringEmptyUtils.isEmpty(captcha)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		HttpSession session = req.getSession();
		String captchaCode = String.valueOf(session.getAttribute(Constants.KAPTCHA_SESSION_KEY));
		if (PhoneUtils.isPhone(phone) && captcha.equalsIgnoreCase(captchaCode)) {
			Map<String, Object> reMsgMap = SendMsg.sendVerificationCode(phone, RedisKey.SHOP_KEY_MEMBER_REGISTER_CODE);
			if (!"1".equals(reMsgMap.get(BaseCode.STATUS.toString()))) {
				return JSONObject.fromObject(reMsgMap).toString();
			}
			return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("手机号码或验证码错误,请重新输入!")).toString();
	}

	@RequestMapping(value = "/batchRegisterMember", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "管理员批量注册会员")
	@RequiresRoles("Manager")
	public String batchRegisterMember(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("orderIdPack") String orderIdPack) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(orderIdPack)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单Id不能为空!")).toString();
		}
		JSONArray jsonArr = null;
		try {
			jsonArr = JSONArray.fromObject(orderIdPack);
		} catch (Exception e) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单Id参数格式错误!")).toString();
		}
		return JSONObject.fromObject(memberTransaction.batchRegisterMember(jsonArr)).toString();
	}

	@RequestMapping(value = "/realName", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户身份证--实名认证")
	@RequiresRoles("Member")
	public String realName(HttpServletRequest req, HttpServletResponse response, String captcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String captchaCode = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
		if (StringEmptyUtils.isEmpty(captcha) || !captcha.equalsIgnoreCase(captchaCode)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误！")).toString();
		}
		return JSONObject.fromObject(memberTransaction.realName()).toString();
	}

	@RequestMapping(value = "/updateLoginPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户修改登陆密码")
	@RequiresRoles("Member")
	public String updateLoginPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("loginPassword") String loginPassword, String captcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		if (req.getSession(false) == null) {
			System.out.println("Session has been invalidated!");
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("操作超时！")).toString();
		}
		String key = String.valueOf(session.getAttribute(UPDATE_LOGIN_PASSWORD_UUID));
		if (StringEmptyUtils.isEmpty(key)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("系统繁忙！")).toString();
		}
		return JSONObject.fromObject(memberTransaction.updateLoginPassword(loginPassword)).toString();
	}

	@RequestMapping(value = "/editInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户修改信息")
	@RequiresRoles("Member")
	public String editInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("memberId") String memberId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(memberId)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("用户Id不能为空!")).toString();
		}
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		datasMap.put("memberId", memberId);
		return JSONObject.fromObject(memberTransaction.editInfo(datasMap)).toString();
	}

	/**
	 * 用户修改登陆密码时发送手机验证码
	 * 
	 * @param phone
	 *            手机号码
	 * @return Map
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@RequestMapping(value = "/sendUpdateLoginPasswordCaptchaCode", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Member")
	public String sendUpdateLoginPasswordCaptchaCode(HttpServletRequest req, HttpServletResponse response,
			String captcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		if (memberInfo == null) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("登录超时，请重新登录！")).toString();
		}
		HttpSession session = req.getSession();
		String captchaCode = String.valueOf(session.getAttribute(Constants.KAPTCHA_SESSION_KEY));
		if (captcha.equalsIgnoreCase(captchaCode)) {
			Map<String, Object> reMsgMap = SendMsg.sendVerificationCode(memberInfo.getMemberTel(),
					RedisKey.SHOP_KEY_MEMBER_UPDATE_LOGIN_PASSWORD_CODE);
			if (!"1".equals(reMsgMap.get(BaseCode.STATUS.toString()))) {
				return JSONObject.fromObject(reMsgMap).toString();
			}
			return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误！")).toString();

	}

	/**
	 * 用户设置新的登陆密码前,验证手机验证码是否正确
	 * 
	 * @param phone
	 *            手机号码
	 * @return Map
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@RequestMapping(value = "/updateLoginPasswordVerifyIdentidy", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Member")
	@ResponseBody
	public String updateLoginPasswordVerifyIdentidy(HttpServletRequest req, HttpServletResponse response,
			String smsCaptcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		if (memberInfo == null) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("登录超时，请重新登录！")).toString();
		}
		String redis = JedisUtil.get(RedisKey.SHOP_KEY_MEMBER_UPDATE_LOGIN_PASSWORD_CODE + memberInfo.getMemberTel());

		if (StringEmptyUtils.isNotEmpty(redis)) {
			JSONObject json = null;
			try {
				json = JSONObject.fromObject(redis);
			} catch (Exception e) {
				return JSONObject.fromObject(ReturnInfoUtils.errorInfo("缓存信息错误！")).toString();
			}
			// 判断前台传递的验证码是否与发送至手机的一致
			if (smsCaptcha.trim().equals(json.get("code") + "")) {
				// 以秒为单位，即在没有活动5分钟后，session将失效
				session.setMaxInactiveInterval(5 * 60);
				UUID key = UUID.randomUUID();
				session.setAttribute(UPDATE_LOGIN_PASSWORD_UUID, key);
				return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
			} else {
				return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误,请重新输入!")).toString();
			}
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("未知错误!")).toString();
	}

	@RequestMapping(value = "/setPaymentPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户设置支付密码")
	@RequiresRoles("Member")
	public String setPaymentPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("paymentPassword") String paymentPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String key = (String) session.getAttribute(SET_PAYMENT_PASSWORD_UUID);
		if (StringEmptyUtils.isEmpty(key)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("操作超时！")).toString();
		}
		return JSONObject.fromObject(memberTransaction.setPaymentPassword(paymentPassword)).toString();
	}

	@RequestMapping(value = "/setPayPassSendCaptchaCode", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户设置支付密码时-发送短信验证码")
	@RequiresRoles("Member")
	public String setPayPassSendCaptchaCode(HttpServletRequest req, HttpServletResponse response, String captcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(captcha)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		if (memberInfo == null) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("登录超时！")).toString();
		}
		HttpSession session = req.getSession();
		String captchaCode = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
		if (captcha.equalsIgnoreCase(captchaCode)) {
			Map<String, Object> reMsgMap = SendMsg.sendVerificationCode(memberInfo.getMemberTel(),
					RedisKey.SHOP_KEY_MEMBER_SET_PAYMENT_PASSWORD_CODE);
			if (!"1".equals(reMsgMap.get(BaseCode.STATUS.toString()))) {
				return JSONObject.fromObject(reMsgMap).toString();
			}
			// 以秒为单位，即在没有活动5分钟后，session将失效
			session.setMaxInactiveInterval(5 * 60);
			session.setAttribute(SET_PAYMENT_PASSWORD_UUID, UUID.randomUUID().toString());
			return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("手机号码或验证码错误,请重新输入!")).toString();
	}

	@RequestMapping(value = "/setPayPassVerifyIdentidy", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户设置支付密码-验证身份信息")
	public String setPayPassVerifyIdentidy(HttpServletRequest req, HttpServletResponse response, String smsCaptcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		if (memberInfo == null) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("登录超时！")).toString();
		}
		String redis = JedisUtil.get(RedisKey.SHOP_KEY_MEMBER_SET_PAYMENT_PASSWORD_CODE + memberInfo.getMemberTel());
		if (StringEmptyUtils.isNotEmpty(redis)) {
			JSONObject json = null;
			try {
				json = JSONObject.fromObject(redis);
			} catch (Exception e) {
				return JSONObject.fromObject(ReturnInfoUtils.errorInfo("缓存信息错误！")).toString();
			}
			JedisUtil.del(RedisKey.SHOP_KEY_MEMBER_SET_PAYMENT_PASSWORD_CODE + memberInfo.getMemberTel());
			// 判断前台传递的验证码是否与发送至手机的一致
			if (smsCaptcha.trim().equals(json.get("code") + "")) {
				return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
			}
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("短信验证码无效！")).toString();
	}

	/**
	 * 用户修改支付密码时，发送短信验证码
	 * 
	 * @param phone
	 *            手机号码
	 * @return Map
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@RequestMapping(value = "/sendUpdatePayPasswordCaptchaCode", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String sendUpdatePayPasswordCaptchaCode(HttpServletRequest req, HttpServletResponse response,
			String captcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(captcha)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("请求参数不能为空!")).toString();
		}
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		if (memberInfo == null) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("登录超时！")).toString();
		}
		HttpSession session = req.getSession();
		String captchaCode = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
		if (captcha.equalsIgnoreCase(captchaCode)) {
			Map<String, Object> reMsgMap = SendMsg.sendVerificationCode(memberInfo.getMemberTel(),
					RedisKey.SHOP_KEY_MEMBER_UPDATE_PAYMENT_PASSWORD_CODE);
			if (!"1".equals(reMsgMap.get(BaseCode.STATUS.toString()))) {
				return JSONObject.fromObject(reMsgMap).toString();
			}
			return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("手机号码或验证码错误,请重新输入!")).toString();
	}

	@RequestMapping(value = "/updatePayPasswordVerification", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("更新支付密码前---进行短信验证码与登录密码验证")
	@RequiresRoles("Member")
	public String updatePayPasswordVerification(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("loginPassword") String loginPassword, String smsCaptcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		MD5 md5 = new MD5();
		Subject currentUser = SecurityUtils.getSubject();
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBER_INFO.toString());
		String redisCode = JedisUtil
				.get(RedisKey.SHOP_KEY_MEMBER_UPDATE_PAYMENT_PASSWORD_CODE + memberInfo.getMemberTel());
		if (StringEmptyUtils.isNotEmpty(redisCode)) {
			JSONObject json = JSONObject.fromObject(redisCode);
			String code = json.get("code") + "";
			if (code.equals(smsCaptcha)) {// 验证-短信验证码
				if (memberInfo.getLoginPass().equals(md5.getMD5ofStr(loginPassword))) {// 验证用户登录密码
					return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
				} else {
					return JSONObject.fromObject(ReturnInfoUtils.errorInfo("登录密码错误！")).toString();
				}
			}
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("短信验证码错误！")).toString();
	}

	@RequestMapping(value = "/updatePaymentPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户设置支付密码")
	@RequiresRoles("Member")
	public String updatePaymentPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("paymentPassword") String paymentPassword, String captcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String captchaCode = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
		if (StringEmptyUtils.isEmpty(captcha) || !captcha.equalsIgnoreCase(captchaCode)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误！")).toString();
		}
		return JSONObject.fromObject(memberTransaction.setPaymentPassword(paymentPassword)).toString();
	}

	@RequestMapping(value = "/retrieveLoginPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户找回登录密码-根据账号名称查询账号是否存在")
	public String retrieveLoginPassword(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("accountName") String accountName, String captcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String captchaCode = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
		if (StringEmptyUtils.isEmpty(captcha) || !captcha.equalsIgnoreCase(captchaCode)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误！")).toString();
		}
		Map<String, Object> reMap = memberTransaction.retrieveLoginPassword(accountName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reMap).toString();
		}
		Member member = (Member) reMap.get(BaseCode.DATAS.toString());
		String tel = member.getMemberTel();
		session.setAttribute(RETRIEVE_LOGIN_PASSWORD_PHONE, tel);
		session.setAttribute(RETRIEVE_LOGIN_PASSWORD_MEMBER_ID, member.getMemberId());
		// 切割手机号码
		String telTop = tel.substring(0, 3);
		String telEnd = tel.substring(tel.length() - 4, tel.length());
		String newPhone = telTop + "****" + telEnd;
		Map<String, Object> map = new HashMap<>();
		map.put("phone", newPhone);
		String smsKey = UUID.randomUUID().toString();
		map.put("smsKey", smsKey);
		session.setAttribute("SMS_KEY", smsKey);
		return JSONObject.fromObject(ReturnInfoUtils.successDataInfo(map)).toString();
	}

	@RequestMapping(value = "/resetPwdSendVerifyCode", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户重置登录密码-发送短信验证码")
	public String resetPwdSendVerifyCode(HttpServletRequest req, HttpServletResponse response, String key) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String smsKey = (String) session.getAttribute("SMS_KEY");
		if (StringEmptyUtils.isEmpty(smsKey) || !smsKey.equalsIgnoreCase(key)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误！")).toString();
		}
		String phone = session.getAttribute(RETRIEVE_LOGIN_PASSWORD_PHONE) + "";
		Map<String, Object> reMsgMap = SendMsg.sendVerificationCode(phone,
				RedisKey.SHOP_KEY_MEMBER_RESET_LOGIN_PASSWORD_CODE);
		if (!"1".equals(reMsgMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reMsgMap).toString();
		}
		return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
	}

	@RequestMapping(value = "/resetLoginPasswordVerifyIdentidy", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户重置登录密码-验证身份信息")
	public String resetLoginPasswordVerifyIdentidy(HttpServletRequest req, HttpServletResponse response,
			String smsCaptcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String phone = session.getAttribute(RETRIEVE_LOGIN_PASSWORD_PHONE) + "";
		
		Map<String, Object> reRedisMap = SendMsg.checkRedisInfo(RedisKey.SHOP_KEY_MEMBER_RESET_LOGIN_PASSWORD_CODE,
				phone, smsCaptcha);
		if (!"1".equals(reRedisMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reRedisMap).toString();
		}
		session.setMaxInactiveInterval(5 * 60);
		session.setAttribute(RETRIEVE_LOGIN_PASSWORD_UUID, UUID.randomUUID().toString());
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("短信验证码无效！")).toString();
	}

	@RequestMapping(value = "/resetLoginPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户重置登录密码")
	public String resetLoginPassword(HttpServletRequest req, HttpServletResponse response, String loginPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String key = (String) session.getAttribute(RETRIEVE_LOGIN_PASSWORD_UUID);
		if (StringEmptyUtils.isEmpty(key)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("操作超时！")).toString();
		}
		//获取用户第一步操作中用户id
		String memberId = String.valueOf(session.getAttribute(RETRIEVE_LOGIN_PASSWORD_MEMBER_ID));
		return JSONObject.fromObject(memberTransaction.resetPassword(memberId, loginPassword)).toString();
	}

	@RequestMapping(value = "/updatePhoneSendVerifyCode", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户更换手机号码-发送短信验证码")
	public String updatePhoneSendVerifyCode(HttpServletRequest req, HttpServletResponse response, String oldPhone,
			String idNumber, String captcha, String newPhone) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String captchaCode = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
		if (StringEmptyUtils.isEmpty(captcha) || !captcha.equalsIgnoreCase(captchaCode)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误！")).toString();
		}
		if (!PhoneUtils.isPhone(newPhone)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("新手机号码错误！", "502")).toString();
		}
		Map<String, Object> reMap = memberTransaction.getOldPhone(oldPhone, idNumber);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reMap).toString();
		}
		return JSONObject.fromObject(SendMsg.sendVerificationCode(newPhone, RedisKey.SHOP_KEY_MEMBER_UPDATE_PHONE_CODE))
				.toString();
	}

	@RequestMapping(value = "/updatePhone", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户更换手机号码")
	public String updatePhone(HttpServletRequest req, HttpServletResponse response, String smsCaptcha, String oldPhone,
			String idNumber, String newPhone) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reRedisMap = SendMsg.checkRedisInfo(RedisKey.SHOP_KEY_MEMBER_UPDATE_PHONE_CODE, newPhone,
				smsCaptcha);
		if (!"1".equals(reRedisMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reRedisMap).toString();
		}
		return JSONObject.fromObject(memberTransaction.updatePhone(oldPhone, idNumber, newPhone)).toString();
	}

	@RequestMapping(value = "/updatePayPwd", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户修改交易密码")
	public String updatePayPwd(HttpServletRequest req, HttpServletResponse response, String newPayPassword,
			String oldPayPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(memberTransaction.updatePayPwd(newPayPassword, oldPayPassword)).toString();
	}

	@RequestMapping(value = "/resetPayPwdSendVerifyCode", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户重置交易密码-发送短信验证码")
	public String updatePayPwdSendVerifyCode(HttpServletRequest req, HttpServletResponse response, String idNumber,
			String phone, String captcha) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String captchaCode = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
		if (StringEmptyUtils.isEmpty(captcha) || !captcha.equalsIgnoreCase(captchaCode)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("验证码错误！")).toString();
		}
		Map<String, Object> reCheckMap = memberTransaction.getOldPhone(phone ,idNumber);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reCheckMap).toString();
		}
		return JSONObject
				.fromObject(SendMsg.sendVerificationCode(phone, RedisKey.SHOP_KEY_MEMBER_RESET_PAYMENT_PASSWORD_CODE))
				.toString();
	}

	@RequestMapping(value = "/resetPayPwdVerifyIdentidy", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户重置支付密码-验证身份信息")
	public String updatePayPwdVerifyIdentidy(HttpServletRequest req, HttpServletResponse response, String smsCaptcha,
			String idNumber, String phone) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		Map<String, Object> reCheckMap = memberTransaction.getOldPhone(phone ,idNumber);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reCheckMap).toString();
		}
		Map<String, Object> reRedisMap = SendMsg.checkRedisInfo(RedisKey.SHOP_KEY_MEMBER_RESET_PAYMENT_PASSWORD_CODE,
				phone, smsCaptcha);
		if (!"1".equals(reRedisMap.get(BaseCode.STATUS.toString()))) {
			return JSONObject.fromObject(reRedisMap).toString();
		}
		// 以秒为单位，即在没有活动5分钟后，session将失效
		session.setMaxInactiveInterval(5 * 60);
		session.setAttribute(RETRIEVE_PAYMENT_PASSWORD_UUID, UUID.randomUUID().toString());
		session.setAttribute(RETRIEVE_PAYMENT_PASSWORD_ID_NUMBER, idNumber);
		
		return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
	}

	@RequestMapping(value = "/resetPayPwd", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户重置交易密码")
	public String resetPayPwd(HttpServletRequest req, HttpServletResponse response, String newPayPassword) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		HttpSession session = req.getSession();
		String uuid = session.getAttribute(RETRIEVE_PAYMENT_PASSWORD_UUID) + "";
		if (StringEmptyUtils.isEmpty(uuid)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("操作超时！")).toString();
		}
		String idNumber = session.getAttribute(RETRIEVE_PAYMENT_PASSWORD_ID_NUMBER) + "";
		return JSONObject.fromObject(memberTransaction.resetPayPwd(newPayPassword,idNumber)).toString();
	}

}
