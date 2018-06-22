package org.silver.shop.api.system.tenant;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.tenant.RecipientContent;

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
	 * @param memberId 用户Id
	 * @param memberName 用户名称
	 * @return Map
	 */
	public Map<String, Object> getMemberRecipientInfo(String memberId, String memberName);


	/**
	 * 根据用户Id删除用户收货地址信息
	 * @param recipientId 收货地址Id
	 * @param memberId   
	 * @return Map
	 */
	public Map<String, Object> deleteMemberRecipientInfo(String memberId,String memberName,String recipientId);


	/**
	 * 修改收货人地址信息
	 * @param recipientInfoPack
	 * @param updateBy 更新人
	 * @return
	 */
	public Map<String, Object> modifyRecipientInfo(String recipientInfoPack, String updateBy);

	/**
	 * 根据收货地址信息流水Id 查询对应的收货地址信息
	 * 
	 * @param recipientId
	 *            收获地址信息流水Id
	 * @return Map 键datas=参数
	 */
	public Map<String, Object> getRecipientInfo(String recipientId);

	/**
	 * 保存收货人信息实体类
	 * 
	 * @param cacheList
	 *            缓存收货人信息实体集合
	 * @return Map
	 */
	 public Map<String, Object> saveRecipientContent(List<RecipientContent> cacheList);

}
