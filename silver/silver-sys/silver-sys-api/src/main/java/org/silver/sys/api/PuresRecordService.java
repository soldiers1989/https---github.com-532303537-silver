package org.silver.sys.api;

import java.util.Map;

public interface PuresRecordService {

	/**
	 * 钱包充值
	 * @param serialNo     业务编号
	 * @param walletNo     钱包编号
	 * @param tenantNo     商户编号
	 * @param acceptanceNo 支付企业响应号
	 * @param money        金额
	 * @param status       业务受理状态
	 * @return
	 */
	public Map<String,Object> walletRecharge(String serialNo,String walletNo,String tenantNo,String acceptanceNo,double money,int status);
	/**
	 * 提现
	 * @param serialNo     业务编号
	 * @param walletNo     钱包编号
	 * @param tenantNo     商户编号
	 * @param acceptanceNo 支付企业响应号
	 * @param money        金额
	 * @param bank         银行名称
	 * @param bankAccount  银行账户
	 * @param status       业务受理状态
	 * @return
	 */
	public Map<String,Object> withdrawDeposit(String serialNo,String walletNo,String tenantNo,String acceptanceNo,double money,String bank,String bankAccount,int status);
}
