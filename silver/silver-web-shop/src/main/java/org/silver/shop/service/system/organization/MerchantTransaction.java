package org.silver.shop.service.system.organization;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.RecordInfo;
import org.silver.util.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;

/**
 * 商戶Transaction(事物)层
 */
@Service("merchantTransaction")
public class MerchantTransaction {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Reference
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
		Date dateTime = new Date();
		Merchant merchant = new Merchant();
		RecordInfo recordInfo = new RecordInfo();
		MD5 md5 = new MD5();
		// 扫描获取数据库表中的商户自增长ID
		Long merchantCount = merchantService.findOriginalMerchantId();
		// 得出的总数上+1
		Long count = merchantCount + 1;
		if (count < 1) {// 判断数据库查询出数据如果小于1,则中断程序,告诉异常
			statusMap.put("status", StatusCode.WARN.getStatus());
			statusMap.put("msg", StatusCode.WARN.getMsg());
			return statusMap;
		}
		String str = count + "";
		// 当商户ID没有5位数时,前面补0
		while (str.length() < 5) {
			str = "0" + str;
		}
		str = "MerchantId_" + str;
		boolean merchantFlag = false;
		boolean recordFlag = false;
		if (type.equals("1")) {// 1-银盟商户注册
			merchant.setMerchantId(str);
			merchant.setMerchantCusNo("YM_" + str);
			merchant.setMerchantName(account);
			merchant.setLoginPassword(md5.getMD5ofStr(loginPassword));
			merchant.setMerchantIdcard(merchantIdCard);
			merchant.setMerchantIdCardName(merchantIdCardName);
			merchant.setMerchantStatus("3");// 商户状态：1-启用，2-禁用，3-审核
			merchant.setCreateBy(account);
			merchant.setCreateDate(dateTime);
			merchant.setDeletFlag(0);// 删除标识:0-未删除,1-已删除
			recordInfo.setMerchantId(count.toString());
			recordInfo.setCreateBy(account);
			recordInfo.setCreateDate(dateTime);
			recordInfo.setDeletFlag(0);// 删除标识:0-未删除,1-已删除
			// 商戶基本信息实例化
			merchantFlag = merchantService.merchantRegister(merchant);
			if (merchantFlag) {
				// 保存商户对应的电商平台名称(及编码)
				recordFlag = merchantService.addMerchantRecordInfo(recordInfo, "1");
				if (recordFlag) {// 数据库添加完成后,返回成功信息
					statusMap.put("status", StatusCode.SUCCESS.getStatus());
					statusMap.put("msg", "注册成功！");
					return statusMap;
				}
			}
		} else {// 2-第三方商户注册
			merchant.setMerchantId(str);
			merchant.setMerchantCusNo("TP_" + str);
			merchant.setMerchantName(account);
			merchant.setLoginPassword(md5.getMD5ofStr(loginPassword));
			merchant.setMerchantIdcard(merchantIdCard);
			merchant.setMerchantIdCardName(merchantIdCardName);
			merchant.setMerchantStatus("3");// 商户状态：1-启用，2-禁用，3-审核
			merchant.setCreateBy(account);
			merchant.setCreateDate(dateTime);
			merchant.setDeletFlag(0);// 删除标识:0-未删除,1-已删除
			// 商戶基本信息实例化
			merchantFlag = merchantService.merchantRegister(merchant);
			if (merchantFlag) {
				JSONArray jsonList = null;
				try {
					jsonList = JSONArray.fromObject(recordInfoPack);
				} catch (Exception e) {
					logger.debug("商户备案参数不正确！");
					e.getStackTrace();
					statusMap.put("status", StatusCode.NOTICE.getStatus());
					statusMap.put("msg", "注册失败,请检查商户备案信息是否正确！");
					return statusMap;
				}
				Map<String, Object> packMap = null;
				// 取出前台打包好的商户备案信息
				for (int x = 0; x < jsonList.size(); x++) {
					packMap = new HashMap<>();
					packMap = (Map) jsonList.get(x);
					String eport = packMap.get("eport") + "";
					String ebEntNo = packMap.get("ebEntNo") + "";
					String ebEntName = packMap.get("ebEntName") + "";
					String ebpEntNo = packMap.get("ebpEntNo") + "";
					String ebpEntName = packMap.get("ebpEntName") + "";
					recordInfo.setMerchantId(str);
					recordInfo.setEport(Integer.valueOf(eport));// 1-广州电子口岸
																// 2-南沙智检
					recordInfo.setEbEntNo(ebEntNo);
					recordInfo.setEbEntName(ebEntName);
					recordInfo.setEbpEntNo(ebpEntNo);
					recordInfo.setEbpEntName(ebpEntName);
					recordInfo.setCreateBy(account);
					recordInfo.setCreateDate(dateTime);
					recordInfo.setDeletFlag(0);// 删除标识:0-未删除,1-已删除
					// 保存商户对应的电商平台名称(及编码)
					recordFlag = merchantService.addMerchantRecordInfo(recordInfo, "2");
					if (!recordFlag) {
						if (Integer.valueOf(eport) == 0) {
							eport = "电子口岸";
						} else if (Integer.valueOf(eport) == 1) {
							eport = "南沙智检";
						}
						statusMap.put("status", StatusCode.NOTICE.getStatus());
						statusMap.put("msg", eport + "商户备案信息错误,保存失败！");
						return statusMap;
					}
				}
				statusMap.put("status", StatusCode.SUCCESS.getStatus());
				statusMap.put("msg", "注册成功！");
				return statusMap;
			}
		}
		statusMap.put("status", StatusCode.NOTICE.getStatus());
		statusMap.put("msg", "注册失败,请检查商户信息是否正确！");
		return statusMap;
	}

	public Map<String, Object> merchantLogin(String account) {
		Map<String, Object> dataMap = new HashMap<>();
		// key=(表中列名),value=传递过来的值
		dataMap.put("merchantName", account);
		return null;
	}

	public static void main(String[] args) {
		MD5 md2 = new MD5();
		System.out.println(md2.getMD5ofStr("abc"));
		System.out.println(md2.getMD5ofStr("7692DCDC19E41E66C6AE2DE54A696B25"));
	}

}
