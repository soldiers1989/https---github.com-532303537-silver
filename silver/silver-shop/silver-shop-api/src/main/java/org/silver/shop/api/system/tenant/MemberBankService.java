package org.silver.shop.api.system.tenant;

import java.util.Map;

import org.silver.shop.model.system.organization.Member;

public interface MemberBankService {

	/**
	 * 添加用户银行卡信息
	 * @param memberInfo 用户信息
	 * @param datasMap 参数
	 * @return Map
	 */
	public Map<String, Object> addInfo(Member memberInfo, Map<String, Object> datasMap);

	/**
	 * 用户获取银行卡信息
	 * @param memberInfo 用户信息
	 * @param page 页数
	 * @param size 数目
	 * @return Map
	 */
	public Map<String, Object> getInfo(Member memberInfo, int page, int size);

}
