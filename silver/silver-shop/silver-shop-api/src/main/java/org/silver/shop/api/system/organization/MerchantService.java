package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;

/**
 * 商户service层
 */
public interface MerchantService {

	/**
	 * 保存商戶基本信息与备案信息
	 * 
	 * @param Map
	 *            参数
	 * @return Map
	 */
	public Map<String, Object> merchantRegister(Map<String, Object> datasMap);

	/**
	 * 检查商户名是否重复
	 * 
	 * @param dataMap
	 *            key=(表中列名称),value=(查询参数)
	 * @return List
	 */
	public List<Object> checkMerchantName(Map dataMap);

	/**
	 * 根据商户名查询商户数据
	 * 
	 * @param account
	 * @return List
	 */
	public List<Object> findMerchantBy(String account);

	/**
	 * 获取商户自增ID后,自编ID
	 * 
	 * @return Map
	 */
	public Map<String, Object> findOriginalMerchantId();

	/**
	 * 保存商户对应的电商平台名称(及编码)
	 * 
	 * @param entity 商户备案信息实体类
	 * @param type
	 *            1-银盟商户注册,2-第三方商户注册
	 * @return boolean
	 */
	public boolean addMerchantRecordInfo(MerchantRecordInfo entity, String type);

	/**
	 * 修改商户业务信息(图片)
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param imglist
	 *            上传后的图片名称
	 * @param array
	 *            上传图片的下标
	 * @param customsregistrationCode
	 *            海关注册编码
	 * @param organizationCode
	 *            组织机构编码
	 * @param checktheRegistrationCode
	 *            报检注册编码
	 * @param merchantName
	 * 			商户名称
	 * @return Map
	 */
	public Map<String, Object> editBusinessInfo(String merchantInfo, List<Object> imglist, int[] array,
			String customsregistrationCode, String organizationCode, String checktheRegistrationCode,
			String merchantName);

	/**
	 * 更新商户登录密码
	 * 
	 * @param merchantInfo
	 *            商户实体类
	 * @param newLoginPassword
	 *            新登录密码
	 * @param Map
	 */
	public Map<String, Object> updateLoginPassword(Merchant merchantInfo, String newLoginPassword);

	/**
	 * 根据商户Id,获取商户备案信息
	 * @param merchantId 商户Id
	 * @return Map 
	 */
	public Map<String, Object> getMerchantRecordInfo(String merchantId);

	/**
	 * 商城前台公开接口，用户查询商品时获取商户信息
	 * @param merchantId 商户Id
	 * @return
	 */
	public Map<String, Object> publicMerchantInfo(String merchantId);

	/**
	 * 根据商户Id获取商户对应的权限信息
	 * @param merchantId 商户Id
	 * @return 
	 */
	public Map<String, Object> getMerchantAuthority(String merchantId);

	/**
	 * 设置商户关联的用户账号
	 * @param merchantId 商户Id
	 * @param accountName 用户账号
	 * @param loginPassword  用户登录密码
	 * @param payPassword  支付密码
	 * @param payPassword 
	 * @return Map
	 */
	public Map<String, Object> setRelatedMember(String merchantId, String merchantName, String accountName, String loginPassword, String payPassword );

	/**
	 * 获取关联的用户资金信息
	 * @param merchantId
	 * @param size 
	 * @param page 
	 * @return
	 */
	public Map<String, Object> getRelatedMemberFunds(String merchantId, int page, int size);

	/**
	 * 根据商户id、获取商户业务信息
	 * @param merchantId 商户id
	 * @return Map
	 */
	public Map<String, Object> getBusinessInfo(String merchantId);

	/**
	 * 商户修改基本信息
	 * @param merchantId 商户id
	 * @param datasMap 
	 * @param merchantName 商户名称 
	 * @return Map
	 */
	public Map<String, Object> updateBaseInfo(String merchantId, String merchantName, Map<String, Object> datasMap);

	/**
	 * 查询商户信息
	 * @param params 查询参数
	 * @param size 
	 * @param page 
	 * @return
	 */
	public Map<String, Object> getMerchantInfo(Map<String, Object> params, int page, int size);

	/**
	 * 根据用户id重新设置登录密码
	 * @param merchantId 商户id
	 * @param newPassword 新的登录密码
	 * @return 
	 */
	public Map<String, Object> resetLoginPwd(String merchantId, String newPassword);

}
