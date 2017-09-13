package org.silver.shop.service.system.tenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.tenant.MerchantBankInfoService;
import org.silver.shop.model.system.organization.Merchant;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("merchantBankInfoTransaction")
public class MerchantBankInfoTransaction {

	@Reference
	private MerchantBankInfoService merchantBankInfoService;

	/**
	 * 获取商户银行卡信息并存入Map
	 * 
	 * @return Map
	 */
	public List<Object> findMerchantBankInfo(int page, int size) {
		Map<String, Object> dataMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession()
				.getAttribute(LoginType.MERCHANT.toString() + "_info");
		// key=(表中列名),value=传递过来的值
		dataMap.put("merchantId", merchantInfo.getMerchantId());
		List<Object> reList = merchantBankInfoService.findMerchantBankInfo(dataMap, page, size);
		if (!reList.isEmpty()) {
			dataMap.clear();
			return reList;
		}
		return reList;
	}

	/**
	 * 添加商戶银行卡信息
	 * 
	 * @param bankName
	 * @param bankAccount
	 * @param type
	 *            默认选择：1-默认选中,2-备用
	 * @return
	 */
	public boolean addMerchantBankInfo(String bankName, String bankAccount, int defaultFalg) {
		boolean flag = false;
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession()
				.getAttribute(LoginType.MERCHANT.toString() + "_info");
		flag = merchantBankInfoService.addMerchantBankInfo(merchantInfo, bankName, bankAccount, defaultFalg);
		return flag;
	}
	
	public boolean selectMerchantBank(){
		
		return true;
	}
}
