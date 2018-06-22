package org.silver.shop.api.system.log;

import java.util.Map;

public interface MerchantWalletLogService {

	/**
	 * 添加商户钱包日志
	 * 
	 * @param datasMap
	 *            参数:merchantId(商户Id)、walletId(钱包Id)、balance(余额)、type(类型:1-佣金、2-充值、3-提现、4-缴费、5-购物)、status(
	 *            状态：success-交易成功、fail-交易失败)、amount(金额)、flag(进出帐标识：in-进账,out-出账)、targetWalletId(目标钱包Id)、targetName(目标名称)
	 * 
	 * @exception 注：余额需填写改动前的,因为方法内部会根据flag标识进行加减！
	 * @return Map
	 */
	public Map<String, Object> addWalletLog(Map<String, Object> datasMap);

	/**
	 * 查看商户钱包日志记录
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param type
	 *            类型1-佣金、2-充值、3-提现、4-缴费、5-购物
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @return
	 */
	public Map<String, Object> getWalletLog(String merchantId, int type, int page, int size, String startDate,
			String endDate);
}
