package org.silver.shop.service.system.organization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.FileUpLoadService;
import org.silver.util.MD5;
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
	public Map<String, Object> merchantRegister(String account, String loginPassword, String merchantIdCard,
		String merchantIdCardName, String recordInfoPack, String type) {
		// 获取商户ID
		Map<String, Object> reIdMap = merchantService.findOriginalMerchantId();
		String status = reIdMap.get(BaseCode.STATUS.getBaseCode()) + "";
		if (status.equals("1")) {// 从数据库获取ID成功
			// 获取返回来的商户ID
			String merchantId = reIdMap.get(BaseCode.DATAS.getBaseCode()) + "";
			// 商户注册
			return merchantService.merchantRegister(merchantId, account, loginPassword, merchantIdCard,merchantIdCardName, recordInfoPack, type);
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
			// 判断查询出的账号密码与前台登录的账号密码是否一致
			if (account.equals(name) && md5Pas.equals(loginpas)) {
				Subject currentUser = SecurityUtils.getSubject();
				// 获取商户登录时,shiro存入在session中的数据
				Merchant merchantInfo = (Merchant) currentUser.getSession()
						.getAttribute(LoginType.MERCHANTINFO.toString());
				if (merchantInfo == null) {
					WebUtil.getSession().setAttribute(LoginType.MERCHANTINFO.toString(), reList.get(0));
				}
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
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String path = "E:/STSworkspace/apache-tomcat-7.0.57/webapps/UME/img/" + merchantName + "/";
		// 海关注册编码
		String customsregistrationCode = req.getParameter("merchantCustomsregistrationCode");
		// 组织机构编码
		String organizationCode = req.getParameter("merchantOrganizationCode");
		// 报检注册编码
		String checktheRegistrationCode = req.getParameter("merchantChecktheRegistrationCode");
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, path, ".jpg", true, 800, 800, null);
		// 获取文件上传后的文件名称
		List<Object> imglist = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
		// 获取前台传递过来的图片数量
		int imgLength = Integer.parseInt(req.getParameter("imgLength"));
		// 创建一个与前台图片数量一样长度的数组
		int[] array = new int[imgLength];
		for (int i = 0; i < imgLength; i++) {
			array[i] = Integer.parseInt(req.getParameter("img[" + i + "]") + "");
		}
		Map<String, Object> datasMap = merchantService.editBusinessInfo(merchantInfo, imglist, array,
				customsregistrationCode, organizationCode, checktheRegistrationCode);
		boolean reFlag = (boolean) datasMap.get(BaseCode.STATUS.getBaseCode());
		if (reFlag) {
			datasMap.put(BaseCode.STATUS.getBaseCode(), 1);
			datasMap.put(BaseCode.MSG.getBaseCode(), "更新成功,待审核！");
			Merchant reMerchantInfo = (Merchant) datasMap.get("datas");
			// 更新session中的实体数据
			WebUtil.getSession().setAttribute(LoginType.MERCHANTINFO.toString(), reMerchantInfo);
			return datasMap;
		}
		datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
		datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
		return datasMap;
	}

	// 修改登录密码
	public Map<String, Object> editLoginPassword(String oldLoginPassword, String newLoginPassword) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String account = merchantInfo.getMerchantName();
		// 验证输入的原密码是否能登录
		Map<String, Object> reMap = merchantLogin(account, oldLoginPassword);
		if (reMap != null) {
			String status = reMap.get(BaseCode.STATUS.getBaseCode()) + "";
			if (status.equals("1")) {
				datasMap = merchantService.updateLoginPassword(merchantInfo, newLoginPassword);
			} else {
				datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
				datasMap.put(BaseCode.MSG.getBaseCode(), "原登录密码输入错误");
			}
		}
		return datasMap;
	}
}
