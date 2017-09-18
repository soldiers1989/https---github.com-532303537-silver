package org.silver.shop.service.system.organization;

import java.util.ArrayList;
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

import net.sf.json.JSONObject;

/**
 * 商戶Transaction(事物)层
 */
@Service("merchantTransaction")
public class MerchantTransaction {
	private static final String STATUS = "status";

	@Autowired
	private MerchantService merchantService;
	@Autowired
	private FileUpLoadService fileUpLoadService;

	/**
	 * 检查商户名称是否重复
	 * 
	 * @param account
	 *            商户名称
	 * @return Map
	 */
	public List<Object> checkMerchantName(String account) {
		Map<String, Object> dataMap = new HashMap<>();
		// key=(表中列名),value=传递过来的值
		dataMap.put("merchantName", account);
		return merchantService.checkMerchantName(dataMap);
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
	 * @param eport
	 *            1-广州电子口岸(目前只支持BC业务) 2-南沙智检(支持BBC业务)
	 */
	public Map<String, Object> merchantRegister(String account, String loginPassword, String merchantIdCard,
			String merchantIdCardName, String recordInfoPack, String type) {
		Map<String, Object> datasMap = new HashMap<>();
		// 获取商户ID
		Map<String, Object> reIdMap = merchantService.findOriginalMerchantId();
		String status = reIdMap.get(STATUS) + "";
		if (status.equals("1")) {// 从数据库获取ID成功
			// 获取返回来的商户ID
			String merchantId = reIdMap.get("datas") + "";
			// 商户注册
			datasMap = merchantService.merchantRegister(merchantId, account, loginPassword, merchantIdCard,
					merchantIdCardName, recordInfoPack, type);
			return datasMap;
		}
		datasMap.put(STATUS, StatusCode.NOTICE.getStatus());
		datasMap.put("msg", "注册失败,请检查商户信息是否正确！");
		return datasMap;
	}

	/**
	 * 商户登录
	 * 
	 * @param account
	 * @return
	 */
	public Map<String, Object> merchantLogin(String account, String loginPassword) {
		MD5 md5 = new MD5();
		Map<String, Object> datasMap = merchantService.findMerchantBy(account);
		if (!datasMap.isEmpty()) {
			if ((int) datasMap.get(STATUS) == 1) {// 查询商户成功
				List<Merchant> merchantList = (List<Merchant>) datasMap.get("datas");
				String name = merchantList.get(0).getMerchantName();
				String loginpas = merchantList.get(0).getLoginPassword();
				String md5Pas = md5.getMD5ofStr(loginPassword);
				// 判断查询出的账号密码与前台登录的账号密码是否一致
				if (account.equals(name) && md5Pas.equals(loginpas)) {
					datasMap.clear();
					WebUtil.getSession().setAttribute(LoginType.MERCHANT.toString() + "_info", merchantList.get(0));
					datasMap.put(STATUS, StatusCode.SUCCESS.getStatus());
					datasMap.put("msg", "登录成功");
					return datasMap;
				}
			}

		}
		return null;
	}

	/**
	 * 修改商户业务信息
	 * 
	 * @param req
	 * @return
	 */
	public Map<String, Object> editBusinessInfo(HttpServletRequest req) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession()
				.getAttribute(LoginType.MERCHANT.toString() + "_info");
		// 海关注册编码
		String customsregistrationCode = req.getParameter("merchantCustomsregistrationCode");
		// 组织机构编码
		String organizationCode = req.getParameter("merchantOrganizationCode");
		// 报检注册编码
		String checktheRegistrationCode = req.getParameter("merchantChecktheRegistrationCode");
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, "/opt/www/img/merchant/business/", ".jpg",
				true, 800, 800, null);
		System.out.println("-0---->" + JSONObject.fromObject(imgMap).toString());
		//获取文件上传后的文件名称
		List<Object> imglist = (List) imgMap.get("datas");
		//获取前台传递过来的图片数量
		int imgLength = Integer.parseInt(req.getParameter("imgLength"));
		//创建一个与前台图片数量一样长度的数组
		int[] array = new int[imgLength];
		for (int i = 0; i < imgLength; i++) {
			array[i] = Integer.parseInt(req.getParameter("img[" + i + "]") + "");
		}
		datasMap = merchantService.editBusinessInfo(merchantInfo, imglist, array, customsregistrationCode,
				organizationCode, checktheRegistrationCode);
		boolean reFlag = (boolean) datasMap.get(STATUS);
		if (reFlag) {
			datasMap.put(STATUS, 1);
			datasMap.put("msg", "更新成功,待审核！");
			Merchant reMerchantInfo = (Merchant) datasMap.get("datas");
			// 更新session中的实体数据
			WebUtil.getSession().setAttribute(LoginType.MERCHANT.toString() + "_info", reMerchantInfo);
			return datasMap;
		}
		datasMap.put(STATUS, StatusCode.WARN.getStatus());
		datasMap.put("msg", StatusCode.WARN.getMsg());
		return datasMap;
	}

	public static void main(String[] args) {
		int[] array = new int[0];
	}
}
