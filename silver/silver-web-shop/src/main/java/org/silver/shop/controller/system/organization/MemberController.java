package org.silver.shop.controller.system.organization;

import java.io.IOException;
import java.util.Arrays;
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
import org.silver.shop.service.system.organization.MemberTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
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

	@RequestMapping(value = "/memberRegister", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("用户--注册")
	public String memberRegister(@RequestParam("account") String account, @RequestParam("loginPass") String loginPass,
			@RequestParam("memberIdCardName") String memberIdCardName,
			@RequestParam("memberIdCard") String memberIdCard) {
		Map<String, Object> statusMap = new HashMap<>();
		if (account != null && loginPass != null && memberIdCardName != null && memberIdCard != null) {
			statusMap = memberTransaction.memberRegister(account, loginPass, memberIdCardName, memberIdCard);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.FORMAT_ERR.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
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
	@RequestMapping(value = "/memberlogin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户--登录")
	public String memberlogin(@RequestParam("account") String account,
			@RequestParam("loginPassword") String loginPassword, HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		String[] iPs = { "http://ym.191ec.com:9528", "http://ym.191ec.com:8080", "http://ym.191ec.com:80",
				"http://ym.191ec.com:8090" };
		if (Arrays.asList(iPs).contains(originHeader)) {
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
			response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Origin", originHeader);
		}
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

	@RequestMapping(value = "/getMemberInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "获取用户信息")
	@RequiresRoles("Member")
	public String getMemberInfo() {
		Map<String, Object> statusMap = memberTransaction.getMemberInfo();
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/addGoodsToshopcart", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用戶添加商品至购物车")
	@RequiresRoles("Member")
	public String addGoodsToShopCart(@RequestParam("goodsId") String goodsId, @RequestParam("count") int count) {
		Map<String, Object> statusMap = memberTransaction.addGoodsToShopCart(goodsId, count);
		return JSONObject.fromObject(statusMap).toString();
	}

}