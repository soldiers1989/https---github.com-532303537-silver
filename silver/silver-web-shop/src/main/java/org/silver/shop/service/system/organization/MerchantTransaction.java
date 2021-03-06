package org.silver.shop.service.system.organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.EmailUtils;
import org.silver.util.FileUpLoadService;
import org.silver.util.JedisUtil;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 商戶Transaction(事物)层
 */
@Service("merchantTransaction")
public class MerchantTransaction {

	@Autowired
	private MerchantService merchantService;
	@Autowired
	private FileUpLoadService fileUpLoadService;

	// 检查商户名称是否重复
	public List<Object> checkMerchantName(String account) {
		Map<String, Object> dataMap = new HashMap<>();
		// key=(表中列名),value=传递过来的值
		dataMap.put("merchantName", account);
		return merchantService.checkMerchantName(dataMap);
	}

	// 商戶注册
	public Map<String, Object> merchantRegister(Map<String, Object> datasMap) {
		// 获取商户ID
		Map<String, Object> reIdMap = merchantService.findOriginalMerchantId();
		String status = reIdMap.get(BaseCode.STATUS.getBaseCode()) + "";
		if (status.equals("1")) {// 从数据库获取ID成功
			// 获取返回来的商户ID
			String merchantId = reIdMap.get(BaseCode.DATAS.getBaseCode()) + "";
			datasMap.put("merchantId", merchantId);
			// 商户注册
			return merchantService.merchantRegister(datasMap);
		}
		return reIdMap;
	}

	// 商户登录
	public Map<String, Object> merchantLogin(String account, String loginPassword) {
		MD5 md5 = new MD5();
		Map<String, Object> datasMap = new HashMap<>();
		List<Object> reList = merchantService.findMerchantBy(account);
		if (reList != null && !reList.isEmpty()) {// 商户数据不为空
			Merchant merchant = (Merchant) reList.get(0);
			String name = merchant.getMerchantName();
			String loginpas = merchant.getLoginPassword();
			String md5Pas = md5.getMD5ofStr(loginPassword);
			// 商户状态：1-启用，2-禁用，3-审核
			String status = merchant.getMerchantStatus();
			// 判断查询出的账号密码与前台登录的账号密码是否一致
			if (account.equals(name) && md5Pas.equals(loginpas)) {
				// 判断帐号是否通过审核
				if ("2".equals(status) || "3".equals(status)) {
					// 抛出 帐号锁定异常
					throw new LockedAccountException();
				}
				WebUtil.getSession().setAttribute(LoginType.MERCHANT_INFO.toString(), reList.get(0));
				datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
				datasMap.put(BaseCode.MSG.getBaseCode(), "登录成功");
				return datasMap;
			}
		}
		return null;
	}

	// 修改商户业务信息(图片及编码)
	public Map<String, Object> editBusinessInfo(HttpServletRequest req) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		// String path = "E:/STSworkspace/apache-tomcat-7.0.57/webapps/UME/img/"
		// + merchantId + "/";
		String path = "/opt/www/img/merchant/" + merchantId + "/";

		// 海关注册编码
		String customsregistrationCode = req.getParameter("merchantCustomsregistrationCode");
		// 组织机构编码
		String organizationCode = req.getParameter("merchantOrganizationCode");
		// 报检注册编码
		String checktheRegistrationCode = req.getParameter("merchantChecktheRegistrationCode");
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, path, ".jpg", false, 800, 800, null);
		if (!"1".equals(imgMap.get(BaseCode.STATUS.toString()) + "")) {
			return imgMap;
		}
		// 获取文件上传后的文件名称
		List<Object> imglist = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
		// 获取前台传递过来的图片数量
		int imgLength = Integer.parseInt(req.getParameter("imgLength"));
		// 创建一个与前台图片数量一样长度的数组
		int[] array = new int[imgLength];
		for (int i = 0; i < imgLength; i++) {
			array[i] = Integer.parseInt(req.getParameter("img[" + i + "]") + "");
		}
		Map<String, Object> reMap = merchantService.editBusinessInfo(merchantId, imglist, array,
				customsregistrationCode, organizationCode, checktheRegistrationCode, merchantName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			// Merchant reMerchantInfo = (Merchant) reMap.get("datas");
			// 更新session中的实体数据
			// WebUtil.getSession().setAttribute(LoginType.MERCHANTINFO.toString(),
			// reMerchantInfo);
			return reMap;
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	// 修改登录密码
	public Map<String, Object> editLoginPassword(String oldLoginPassword, String newLoginPassword) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String account = merchantInfo.getMerchantName();

		// 验证输入的原密码是否能登录
		Map<String, Object> reMap = merchantLogin(account, oldLoginPassword);
		if (reMap != null) {
			String status = reMap.get(BaseCode.STATUS.getBaseCode()) + "";
			if (status.equals("1")) {
				return merchantService.updateLoginPassword(merchantInfo, newLoginPassword);
			}
		}
		return ReturnInfoUtils.errorInfo("原登录密码输入错误!");
	}

	// 获取商户备案信息
	public Map<String, Object> getMerchantRecordInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return merchantService.getMerchantRecordInfo(merchantId);
	}

	//
	public Map<String, Object> publicMerchantInfo(String merchantId) {
		return merchantService.publicMerchantInfo(merchantId);
	}

	public List<String> getMerchantAuthority() {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String redisKey = "Shop_Key_Merchant_Authority_List_" + merchantId;
		byte[] redisByte = JedisUtil.get(redisKey.getBytes());
		// if (redisByte != null && redisByte.length > 0) {
		// return (List<String>) SerializeUtil.toObject(redisByte);
		// } else {
		Map<String, Object> reMap = merchantService.getMerchantAuthority(merchantId);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return null;
		}
		List<String> item = (List<String>) reMap.get(BaseCode.DATAS.toString());
		JedisUtil.set(redisKey.getBytes(), SerializeUtil.toBytes(item), 3600);
		return item;
		// }
	}

	// 管理员设置商户关联的用户信息
	public Map<String, Object> setRelatedMember(String accountName, String loginPassword, String payPassword) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return merchantService.setRelatedMember(merchantInfo.getMerchantId(), merchantInfo.getMerchantName(),
				accountName, loginPassword, payPassword);
	}

	//
	public Map<String, Object> getRelatedMemberFunds(int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return merchantService.getRelatedMemberFunds(merchantId, page, size);
	}

	public Map<String, Object> getBusinessInfo() {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return merchantService.getBusinessInfo(merchantId);
	}

	public Map<String, Object> updateBaseInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();

		Map<String, Object> reUpdateMap = merchantService.updateBaseInfo(merchantId, merchantName, datasMap);
		if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
			return reUpdateMap;
		}
		currentUser.getSession().setAttribute(LoginType.MERCHANT_INFO.toString(),
				reUpdateMap.get(BaseCode.DATAS.toString()));
		return ReturnInfoUtils.successInfo();
	}

	public Map<String, Object> checkMerchant(String phone, String merchantName) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantPhone", phone);
		params.put("merchantName", merchantName);
		Map<String,Object> reMap = merchantService.getMerchantInfo(params, 1, 1);
		if("-1".equals(reMap.get(BaseCode.ERROR_CODE.toString()))){
			return ReturnInfoUtils.errorInfo("登录账户或手机号码错误！");
		}else{
			return reMap;
		}
	}

	public Map<String,Object> resetLoginPwd(String merchantId ,String loginPassword) {
		return merchantService.resetLoginPwd(merchantId, loginPassword);
	}
}
