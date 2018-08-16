package org.silver.shop.api.system.log;

import java.util.Map;

import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;

public interface MemberWalletLogService {

	/**
	 * 添加用户钱包流水日志
	 * @param params 参数
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
