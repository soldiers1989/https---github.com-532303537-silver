package org.silver.shop.controller.system.organization;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.service.system.organization.MerchantTransaction;
import org.silver.shop.shiro.MerchantRealm;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商户controller层
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
	public String login(@RequestParam("account") String account, @RequestParam("loginPassword") String loginPassword
			)  {
		Map<String, Object> statusMap = new HashMap<>();
		if (account != null && loginPassword != null) {
			Subject currentUser = SecurityUtils.getSubject();
			currentUser.logout();
			if (!currentUser.isAuthenticated()) {
				CustomizedToken customizedToken = new CustomizedToken(account, loginPassword, USER_LOGIN_TYPE);
				customizedToken.setRememberMe(false);
				try {
					currentUser.login(customizedToken);
					statusMap.put("status", 1);
					statusMap.put("msg", "登录成功");
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
		statusMap.put("status", -1);
		statusMap.put("msg", "账号不存在或密码错误");
		//resp.getWriter().println(JSONObject.fromObject(statusMap).toString());
		 return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 商户注册
	 * 
	 * @param account
	 * @param loginPassword
	 * @param merchantIdCardName
	 * @param merchantIdCard
	 * @param recordInfoPack
	 *            第三方商户注册备案信息包(前台打包好,由JSON转成String)
	 * @param type
	 *            1-银盟商户注册,2-第三方商户注册
	 * @return
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String merchantRegister(@RequestParam("account") String account,
			@RequestParam("loginPassword") String loginPassword,
			@RequestParam("merchantIdCardName") String merchantIdCardName,
			@RequestParam("merchantIdCard") String merchantIdCard,
			@RequestParam("recordInfoPack") String recordInfoPack, @RequestParam("type") String type) {
		Map<String, Object> statusMap = new HashMap<>();
		if (type.equals("1")) {// 1-银盟商户注册
			if (account != null && loginPassword != null && merchantIdCardName != null && merchantIdCard != null) {
				statusMap = merchantTransaction.merchantRegister(account, loginPassword, merchantIdCard,
						merchantIdCardName, recordInfoPack, type);
			}
		} else if (type.equals("2")) {// 2-第三方商户注册
			if (!recordInfoPack.isEmpty() && account != null && loginPassword != null && merchantIdCardName != null
					&& merchantIdCard != null) {
				statusMap = merchantTransaction.merchantRegister(account, loginPassword, merchantIdCard,
						merchantIdCardName, recordInfoPack, type);
			}
		}
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
	// @RequiresRoles(value = { "Merchant" })
	public String checkMerchantName(@RequestParam("account") String account) {
		Map<String, Object> statusMap = new HashMap<>();
		if (account != null || !"".equals(account)) {// 判断前台传递的值不为空
			List<Object> reList = merchantTransaction.checkMerchantName(account);
			if (reList.isEmpty() || reList.size() <= 0) {// 查询出来的数据为空
				statusMap.put("status", StatusCode.SUCCESS.getStatus());
				statusMap.put("msg", "商戶名可以使用!");
				return JSONObject.fromObject(statusMap).toString();
			} else {
				statusMap.put("status", StatusCode.UNKNOWN.getStatus());
				statusMap.put("msg", "商户名已存在,请重新输入!");
				return JSONObject.fromObject(statusMap).toString();
			}
		}
		statusMap.put("Status", StatusCode.NOTICE.getStatus());
		statusMap.put("msg", StatusCode.NOTICE.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/findMerchantGoods")
	@ResponseBody
	@RequiresRoles("Merchant")
	public Map<String, Object> findMerchantGoods() {
		MerchantRealm merchantRealm = new MerchantRealm();
		merchantRealm.getName();
		Subject currentUser = SecurityUtils.getSubject();
		Merchant m = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT.toString() + "_info");
		System.out.println("-->" + m);
		return null;
	}
}
