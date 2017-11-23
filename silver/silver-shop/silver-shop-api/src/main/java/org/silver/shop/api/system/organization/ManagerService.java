package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;

public interface ManagerService {

	
	public List<Object> findManagerBy(String account);

	/**
	 * 管理员查询用户信息
	 * @return
	 */
	public Map<String, Object> findAllmemberInfo();

	/**
	 * 创建管理员
	 * @param managerName 管理员账号名
	 * @param loginPassword 登陆密码
	 * @param managerMarks 管理员标识1-超级管理员2-运营管理员
	 * @param reManagerName 当前管理员姓名
	 * @return
	 */
	public Map<String, Object> createManager(String managerName, String loginPassword, int managerMarks,
			String reManagerName);

	/**
	 * 管理员查询所有商户信息
	 * @return
	 */
	public Map<String, Object> findAllMerchantInfo();

	/**
	 * 管理查询商户详情
	 * @param managerName 管理员名称
	 * @param merchantId 商户Id
	 * @return Map
	 */
	public Map<String, Object> findMerchantDetail(String managerName, String merchantId);

	/**
	 * 修改密码
	 * @param managerId  管理员Id
	 * @param managerName 管理员名称
	 * @param loginPassword 登录密码
	 * @return Map
	 */
	public Map<String, Object> updateManagerPassword(String managerId, String managerName, String oldLoginPassword,String newLoginPassword);

	/**
	 * 修改商户状态
	 * @params merchantId 商户Id
	 * @param managerId 管理员Id
	 * @param managerName 管理员名称
	 * @param status  商户状态：1-启用，2-禁用，3-审核
	 * @return Map
	 */
	public Map<String, Object> editMerchantStatus(String merchantId,String managerId, String managerName, int status);

	/**
	 * 修改备案商品状态
	 * @param managerId 管理员Id
	 * @param managerName 管理员名称
	 * @param entGoodsNo 商品备案Id
	 * @param status 0-未备案，1-备案中，2-备案成功，3-备案失败
	 * @return Map
	 */
	public Map<String, Object> editGoodsRecordStatus(String managerId, String managerName, String entGoodsNo,int status);

	

}
