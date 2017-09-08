package org.silver.shop.controller.system.organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.StatusCode;
import org.silver.shop.service.system.organization.MerchantTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 商户controller层
 */
@Controller
@RequestMapping("/merchant")
public class MerchantController {

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
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "商户--登录")
	public Map<String, Object> login(@RequestParam("account") String account,
			@RequestParam("loginPassword") String loginPassword) {
		Map<String, Object> statusMap = new HashMap<>();
		if (account != null && loginPassword != null) {
			statusMap = null;
		}

		return statusMap;
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
}
