package org.silver.shop.controller.system.organization;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

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
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.service.system.organization.MemberTransaction;
import org.silver.util.DateUtil;
import org.silver.util.JedisUtil;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SendMsg;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import com.alibaba.dubbo.container.Main;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 用户 Controller
 */
@Controller
@RequestMapping("/member")
public class MemberController {
	private static final String USER_LOGIN_TYPE = LoginType.MEMBER.toString();
	@Autowired
	private MemberTransaction memberTransaction;

	/**
	 * 用户注册
	 * 
	 * @param account
	 *            账号
	 * @param loginPass
	 *            登录密码
	 * @param memberIdCardName
	 *            身份证名称
	 * @param memberIdCard
	 *            身份证号码
	 * @param memberTel
	 *            手机号码
	 * @param req
	 * @param response
	 * @return String
	 */
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
	 * @throws IOException
	 */
	@RequestMapping(value = "/memberLogin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户--登录")
	public String memberLogin(@RequestParam("account") String account,
			@RequestParam("loginPassword") String loginPassword, HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (account != null && loginPassword != null) {
			Subject currentUser = SecurityUtils.getSubject();
			// currentUser.logout();
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
		Map<String, Object> statusMap = memberTransaction.getMemberInfo();
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 检查用户登录
	 */
	@RequestMapping(value = "/checkMemberLogin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("检查用户登录")
	// @RequiresRoles("Merchant")
	public String checkMemberLogin(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Member memberInfo = (Member) currentUser.getSession().getAttribute(LoginType.MEMBERINFO.toString());
		if (memberInfo != null && currentUser.isAuthenticated()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "用户已登陆！");

		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.PERMISSION_DENIED.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "未登陆,请先登录！");
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 注销用户信息
	 */
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
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "用户注销成功,请重新登陆！");
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
		Map<String, Object> statusMap = memberTransaction.editShopCartGoodsFlag(goodsInfoPack);
		return JSONObject.fromObject(statusMap).toString();
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
	 * 检查用户账号名是否重复
	 * 
	 * @param account
	 *            用户账号
	 * @return Map
	 */
	@RequestMapping(value = "/checkMemberName", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String checkMemberName(@RequestParam("account") String account, HttpServletRequest req,
			HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = memberTransaction.checkMerchantName(account);
		return JSONObject.fromObject(statusMap).toString();
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
	public String sendMemberRegisterCode(@RequestParam("phone") String phone, HttpServletRequest req,
			HttpServletResponse response) throws ParserConfigurationException, SAXException, IOException {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (phone.length() == 11) {
			JSONObject json = new JSONObject();
			// 获取用户注册保存在缓存中的验证码
			String redisCode = JedisUtil.get("Shop_Key_MemberRegisterCode_" + phone);
			if (StringEmptyUtils.isEmpty(redisCode)) {// redis缓存没有数据
				int code = RandomUtils.getRandom(6);
				SendMsg.sendMsg(phone, "【银盟信息科技有限公司】验证码" + code + ",请在15分钟内按页面提示提交验证码,切勿将验证码泄露于他人!");
				json.put("time", new Date().getTime());
				json.put("code", code);
				System.out.println("-------->>>>>" + code);
				// 将查询出来的省市区放入到redis缓存中
				JedisUtil.setListDatas("Shop_Key_MemberRegisterCode_" + phone, 900, json);
				return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
			} else {
				json = JSONObject.fromObject(redisCode);
				long time = Long.parseLong(json.get("time") + "");
				// 当第一次获取时间与当前时间小于一分钟则认为是频繁获取
				if (time - new Date().getTime() < 50000) {
					return JSONObject.fromObject(ReturnInfoUtils.errorInfo("已获取过验证码,请勿重复获取!")).toString();
				}
			}
		}
		return JSONObject.fromObject(ReturnInfoUtils.errorInfo("手机号码输入不正确,请重新输入!")).toString();
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
	
	public static void main(String[] args) {
		JSONArray json = new JSONArray();
		json.add("SO1804090221615624039");
		json.add("SO1804090221615498001");
		
		json.add("SO1804080221614764603");
		json.add("SO1804080221614651882");
		json.add("SO1804080221614823124");
		json.add("SO1804080221614702206");
		
		json.add("SO1804080221614819380");
		json.add("SO1804090221615480207");
		json.add("SO1804090221615609255");
		json.add("SO1804080221615010168");
		json.add("SO1804080221614702062");
		
		
		json.add("SO1804080221614756808");
		json.add("SO1804090221615589555");
		json.add("SO1804080221614715999");
		System.out.println(json.toString());
	}
}
