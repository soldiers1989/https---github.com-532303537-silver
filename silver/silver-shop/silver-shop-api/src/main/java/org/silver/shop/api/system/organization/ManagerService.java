package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

public interface ManagerService {

	
	public List<Object> findManagerBy(String account);

	/**
	 * 管理员查询用户信息
	 * @param size 
	 * @param page 
	 * @param datasMap 
	 * @return
	 */
	public Map<String, Object> findAllmemberInfo(int page, int size, Map<String, Object> datasMap);

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
	 * @param datasMap 
	 * @param size 
	 * @param page 
	 * @return
	 */
	public Map<String, Object> findAllMerchantInfo(Map<String, Object> datasMap, int page, int size);

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
	 * 查询所有管理员信息
	 * @return
	 */
	public Map<String, Object> findAllManagerInfo();

	/**
	 * 超级管理员重置运营人员密码
	 * @param managerId
	 * @param managerName
	 * @return
	 */
	public Map<String, Object> resetManagerPassword(String managerId, String managerName);

	/**
	 * 管理员修改商户信息
	 * @param managerId 管理员Id
	 * @param managerName 管理员名称
	 * @param arrStr 参数
	 * @return Map
	 */
	public Map<String, Object> editMerhcnatInfo(String managerId, String managerName, String[] arrStr);

	/**
	 * 管理员修改商户业务信息
	 * @param managerId
	 * @param managerName
	 * @param imglist
	 * @param arrayStr
	 * @return
	 */
	public Map<String, Object> editMerhcnatBusinessInfo(String managerId, String managerName, List<Object> imglist,
			String[] arrayStr,String merchantId);

	/**
	 * 添加商户业务信息(图片)
	 * @param merchantId  商户Id
	 * @param arrayStr 参数下标
	 * @param imglist 图片List
	 * @return Map
	 */
	public Map<String, Object> addMerchantBusinessInfo(String merchantId, String[] arrayInt, List<Object> imglist);

	/**
	 * 管理员查询用户详情
	 * @param memberId
	 * @return
	 */
	public Map<String, Object> findMemberDetail(String memberId);

	/**
	 * 管理员编辑用户信息
	 * @param managerId
	 * @param managerName
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> managerEditMemberInfo(String managerId, String managerName,
			Map<String, Object> datasMap);

	/**
	 * 管理员查看商户备案信息
	 * @param merchantId 商户Id
	 * @return Map
	 */
	public Map<String, Object> findMerchantRecordDetail(String merchantId);

	/**
	 * 管理员修改商户备案信息
	 * @param managerId
	 * @param managerName
	 * @param merchantId
	 * @param merchantRecordInfo
	 * @return
	 */
	public Map<String, Object> editMerchantRecordDetail(String managerId, String managerName, String merchantId,
			String merchantRecordInfo);

	/**
	 * 管理员删除商户备案信息
	 * @param id 
	 * @return Map
	 */
	public Map<String, Object> deleteMerchantRecordInfo(long id);

	/**
	 * 管理员审核商户
	 * @param managerId 管理员Id
	 * @param managerName 管理员名称
	 * @param json 数据名称
	 * @return Map
	 */
	public Map<String, Object> managerAuditMerchantInfo(String managerId, String managerName, JSONArray json);

	/**
	 * 管理员重置商户密码
	 * @param merchantId 商户Id
	 * @param managerId 管理员Id
	 * @param managerName 管理员名称
	 * @return Map
	 */
	public Map<String, Object> resetMerchantLoginPassword(String merchantId, String managerId, String managerName);

	
}
