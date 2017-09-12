package org.silver.shop.service.system.organization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.model.system.organization.Merchant;
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
		System.out.println(merchantService+"----<"); 
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
		Map<String, Object> statusMap = new HashMap<>();
		// 获取商户ID
		Map<String, Object> reIdMap = merchantService.findOriginalMerchantId();
		String status = reIdMap.get("status") + "";
		if (status.equals("1")) {// 从数据库获取ID成功
			// 获取返回来的商户ID
			String merchantId = reIdMap.get("datas") + "";
			// 商户注册
			statusMap = merchantService.merchantRegister(merchantId, account, loginPassword, merchantIdCard,
					merchantIdCardName, recordInfoPack, type);
			return statusMap;
		}
		statusMap.put("status", StatusCode.NOTICE.getStatus());
		statusMap.put("msg", "注册失败,请检查商户信息是否正确！");
		return statusMap;
	}

	/**
	 * 商户登录
	 * 
	 * @param account
	 * @return
	 */
	public Map<String, Object> merchantLogin(String account, String loginPassword) {
		MD5 md5 = new MD5();
		System.out.println(merchantService+"----------<<<");
		Map<String, Object> datasMap = merchantService.findMerchantBy(account);
		System.out.println("--------------->"+merchantService);
		if (!datasMap.isEmpty()) {
			if ((int) datasMap.get("status") == 1) {
				List<Merchant> merchantList = (List<Merchant>) datasMap.get("datas");
				String name = merchantList.get(0).getMerchantName();
				String loginpas = merchantList.get(0).getLoginPassword();
				String md5Pas = md5.getMD5ofStr(loginPassword);
				if (account.equals(name) && md5Pas.equals(loginpas)) {
					datasMap.clear();
					WebUtil.getSession().setAttribute(LoginType.MERCHANT.toString() + "_info", merchantList.get(0));
					datasMap.put("status", StatusCode.SUCCESS.getStatus());
					datasMap.put("msg", "登录成功");
					return datasMap;
				}
			}

		}
		return null;
	}

	public static void main(String[] args) {
		MD5 md2 = new MD5();
		System.out.println(md2.getMD5ofStr("123"));
	}

}
