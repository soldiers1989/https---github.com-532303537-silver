package org.silver.shop.api.system.tenant;

import java.util.List;
import java.util.Map;

/**
 *	商户银行卡信息Service层 
 *
 */
public interface MerchantBankInfoService {
	/**
	 * 查询商户银行卡信息
	 * @param dataMap
	 * @return
	 */
	public List<Object> findMerchantBankInfo(Map dataMap,int page,int size);
	
	/**
	 * 添加商户银行信息
	 * @param bankAccount 
	 * @param bankName 
	 * @param bankName
	 * @param bankAccount
	 * @return
	 */
	public boolean addMerchantBankInfo(Object entity, String bankName, String bankAccount,int defaultFalg);
	
	/**
	 * 商户选择默认银行卡
	 * @param id 自增ID(唯一)
	 * @param merchantId (商户ID)
	 * @return
	 */
	public boolean selectMerchantBank(long id,String merchantId);

	
	/**
	 * 商户删除银行卡信息
	 * @param id 自增ID(唯一)
	 * @param merchantId (商户ID)
	 * @return 
	 */
	public boolean deleteMerchantBankInfo(long id, String merchantId);
}
