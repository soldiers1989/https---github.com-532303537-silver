package org.silver.shop.api.system.log;

import java.util.Map;

import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;

public interface MemberWalletLogService {

	/**
	 * 添加用户钱包流水日志
	  * @param datasMap
	 * <li>memberWalletId(用户钱包id)</li>
	 * <li>memberName(用户名称)</li>
	 * <li>serialNo(交易流水号或订单编号)</li>
	 * <li>serialName(交易名称)</li>
	 * <li>beforeChangingBalance(变更之前钱包余额)</li>
	 * <li>amount(金额)</li>
	 * <li>afterChangeBalance(变更之后钱包余额)</li>
	 * <li>type( 类型：1-佣金、2-充值、3-提现、4-缴费、5-购物)</li>
	 * <li>status(状态：success-交易成功、failure-交易失败)</li>
	 * <li>flag(进出帐标识：in-进账,out-出账)</li>
	 * <li>targetWalletId(目标(来源)钱包Id)</li>
	 * <li>targetName(目标(来源)名称)</li>
	 * @return Map
	 */
	public Map<String,Object> addWalletLog(Map<String, Object> params);

	/**
	 * 用户查看钱包记录信息
	 * @param memberInfo 用户信息
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @param type 
	 * @param page 页数
	 * @param size 数目
	 * @return Map
	 */
	public Map<String, Object> getInfo(Member memberInfo, String startDate, String endDate, int type, int page, int size);

}
