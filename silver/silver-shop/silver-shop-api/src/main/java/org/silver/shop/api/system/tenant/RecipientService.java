package org.silver.shop.api.system.tenant;

import java.util.Map;

public interface RecipientService {

	/**
	 * 添加用户收获地址信息
	 * @param memberId 用户ID
	 * @param memberName 用户名称
	 * @param recipientInfo 用户收货地址信息包
	 * @return
	 */
	public Map<String,Object> addRecipientInfo(String memberId, String memberName, String recipientInfo);

	
	/**
	 * 获取用户收货地址信息
	 * @param memberId
	 * @param memberName
	 * @return
	 */
	public Map<String, Object> getMemberRecipientInfo(String memberId, String memberName);


	/**
	 * 根据用户Id删除用户收货地址信息
	 * @param recipientId  s
	 * @return
	 */
	public Map<String, Object> deleteMemberRecipientInfo(String memberId,String memberName,String recipientId);

}
