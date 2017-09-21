package org.silver.shop.controller.system.organization;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
import org.silver.shop.service.system.organization.MerchantTransaction;
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
	public String login(@RequestParam("account") String account, @RequestParam("loginPassword") String loginPassword) {
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
		if (type.equals("1") && account != null && loginPassword != null && merchantIdCardName != null
				&& merchantIdCard != null) {// 1-银盟商户注册
			statusMap = merchantTransaction.merchantRegister(account, loginPassword, merchantIdCard, merchantIdCardName,
					recordInfoPack, type);
			return JSONObject.fromObject(statusMap).toString();
		} else if (type.equals("2") && recordInfoPack != null && account != null && loginPassword != null
				&& merchantIdCardName != null && merchantIdCard != null) {// 2-第三方商户注册
			statusMap = merchantTransaction.merchantRegister(account, loginPassword, merchantIdCard, merchantIdCardName,
					recordInfoPack, type);
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
	public String checkMerchantName(@RequestParam("account") String account) {
		Map<String, Object> statusMap = new HashMap<>();
		if (account != null || !"".equals(account)) {// 判断前台传递的值不为空
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
	public String getMerchantInfo() {
		Map<String, Object> reMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession()
				.getAttribute(LoginType.MERCHANT.toString() + "_info");
		if (!"".equals(merchantInfo)) {
			// 将session内的商户登录密码清空
			merchantInfo.setLoginPassword("");
			reMap.put(BaseCode.STATUS.getBaseCode(), 1);
			reMap.put(BaseCode.DATAS.getBaseCode(), merchantInfo);
			reMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return JSONObject.fromObject(reMap).toString();
		}
		reMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.LOSS_SESSION.getStatus());
		reMap.put(BaseCode.MSG.getBaseCode(), StatusCode.LOSS_SESSION.getMsg());
		return JSONObject.fromObject(reMap).toString();
	}

	/**
	 * 修改商户业务信息(营业执照、税务登记证等等图片及编码)
	 * 
	 * @param merchantInfoPack
	 * @return
	 */
	@RequestMapping(value = "/editMerchantInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("修改商户业务信息")
	public String editMerchantInfo(HttpServletRequest req, HttpServletResponse resp) {
		Map<String, Object> statusMap = new HashMap<>();
		if (req != null) {
			statusMap = merchantTransaction.editBusinessInfo(req);
			return JSONObject.fromObject(statusMap).toString();
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.UNKNOWN.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 注销商户信息
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户注销")
	@RequiresRoles("Merchant")
	public void logout() {
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser != null) {
			currentUser.logout();
		}
	}

	@RequestMapping(value = "/editMerchantLoginPassword", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("修改商户登录密码")
	@RequiresRoles("Merchant")
	public String editMerchantLoginPassword(@RequestParam("loginPassword") String oldLoginPassword,
			@RequestParam("newLoginPassword") String newLoginPassword) {
		Map<String, Object> statusMap = new HashMap<>();
		if (oldLoginPassword != null && newLoginPassword !=null) {
			statusMap = merchantTransaction.editLoginPassword(oldLoginPassword,newLoginPassword);
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
}
