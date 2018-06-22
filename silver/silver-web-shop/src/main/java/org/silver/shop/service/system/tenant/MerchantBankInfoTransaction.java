package org.silver.shop.service.system.tenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
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
	public Map<String,Object> findMerchantBankInfo(int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return merchantBankInfoService.findMerchantBankInfo( merchantInfo.getMerchantId(), page, size);
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
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		flag = merchantBankInfoService.addMerchantBankInfo(merchantInfo, bankName, bankAccount, defaultFalg);
		return flag;
	}

	/**
	 * 设置默认银行卡
	 * 
	 * @param id
	 * @return
	 */
	public Map<String, Object> selectMerchantBank(long id) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		boolean flag = merchantBankInfoService.selectMerchantBank(id, merchantId);
		if (flag) {
			datasMap.put(BaseCode.STATUS.toString(), 1);
			datasMap.put(BaseCode.MSG.getBaseCode(), "设置成功!");
			return datasMap;
		}
		datasMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
		datasMap.put(BaseCode.MSG.getBaseCode(), "参数错误,修改失败!");
		return datasMap;
	}

	/**
	 * 删除商户银行卡信息
	 * 
	 * @param id
	 */
	public Map<String, Object> deleteBankInfo(long id) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		boolean reFlag = merchantBankInfoService.deleteMerchantBankInfo(id, merchantId);
		if (reFlag) {
			datasMap.put(BaseCode.STATUS.toString(), 1);
			datasMap.put(BaseCode.MSG.getBaseCode(), "删除成功!");
			return datasMap;
		}
		datasMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
		datasMap.put(BaseCode.MSG.getBaseCode(), "参数错误,删除失败!");
		return datasMap;
	}
	
	
}
