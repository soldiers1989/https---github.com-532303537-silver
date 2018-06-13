package org.silver.shop.api.common.base;

import java.util.List;
import java.util.Map;

public interface CustomsPortService {

	/**
	 * 添加口岸下已开通的 海关及国检名称与编码
	 * 
	 * @param params
	 *            参数
	 * @param managerName
	 *            管理员名称
	 * @param managerId
	 *            管理员Id
	 * @return boolean
	 */
	public Map<String, Object> addCustomsPort(Map<String, Object> params, String managerId, String managerName);

	/**
	 * 查询所有口岸下已开通的 海关及国检名称与编码
	 * 
	 * @return
	 */
	public Map<String, Object> findAllCustomsPort();

	/**
	 * 商户查询当前已备案的海关及智检信息
	 * 
	 * @param merchantId
	 * @param merchantName
	 * @return
	 */
	public Map<String, Object> findMerchantCustomsPort(String merchantId, String merchantName);

	/**
	 * 管理员根据ID删除口岸信息
	 * 
	 * @param id
	 * @return
	 */
	public boolean deleteCustomsPort(long id);

	/**
	 * 管理员修改已开通口岸信息
	 * 
	 * @param managerId
	 *            管理员Id
	 * @param managerName
	 *            管理员名称
	 * @param params
	 *            修改信息包
	 * @return Map
	 */
	public Map<String, Object> modifyCustomsPort(String managerId, String managerName, Map<String, Object> params);

	/**
	 * 根据国检名称,国检编码校验国检检疫信息是否正确
	 * 
	 * @param ciqOrgName
	 *            国检名称
	 * @param ciqOrgCode
	 *            国检编码
	 * @return boolean
	 */
	public boolean checkCCIQ(String ciqOrgName, String ciqOrgCode);

	/**
	 * 根据国检编码校验国检检疫信息是否正确
	 * 
	 * @param ciqOrgCode
	 *            国检编码
	 * @return boolean
	 */
	public boolean checkCCIQ(String ciqOrgCode);

	/**
	 * 根据海关关区名称,海关关区编码校验海关关区信息是否正确
	 * 
	 * @param customsName
	 *            海关关区名称
	 * @param customsCode
	 *            海关关区编码
	 * @return boolean
	 */
	public boolean checkGAC(String customsName, String customsCode);

	/**
	 * 根据海关关区编码校验海关关区信息是否正确
	 * 
	 * @param customsCode
	 *            海关关区编码
	 * @return boolean
	 */
	public boolean checkGAC(String customsCode);
}
