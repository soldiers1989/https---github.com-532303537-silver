package org.silver.shop.api.system.tenant;

import java.util.Map;

import org.silver.shop.model.system.organization.Member;

public interface MemberBankService {

	/**
	 * 添加用户银行卡信息
	 * 
	 * @param memberInfo
	 *            用户信息
	 * @param datasMap
	 *            参数
	 * @return Map
	 */
	public Map<String, Object> addInfo(Member memberInfo, Map<String, Object> datasMap);

	/**
	 * 保存用户银行卡信息
	 * 
	 * @param datasMap
	 *            用户银行卡参数
	 * @return Map
	 */
	public Map<String, Object> saveMemberBank(Map<String, Object> datasMap);

	/**
	 * 用户获取银行卡信息
	 * 
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @param type
	 *            类型：hide-隐藏后信息、display-显示
	 * @return Map
	 */
	public Map<String, Object> getInfo(String type, Map<String, Object> params, int page, int size);

	/**
	 * 用户删除银行卡信息
	 * 
	 * @param memberBankId
	 *            银行卡流水
	 * @return Map
	 */
	public Map<String, Object> deleteInfo(String memberBankId);

	/**
	 * 根据流水id，设置默认结算卡
	 * 
	 * @param memberBankId
	 *            银行卡流水
	 * @param memberId
	 *            用户id
	 * @return Map
	 */
	public Map<String, Object> setDefaultBankCard(String memberBankId, String memberId);

}
