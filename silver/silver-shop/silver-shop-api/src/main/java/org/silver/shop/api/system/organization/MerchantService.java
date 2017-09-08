package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.RecordInfo;

/**
 * 商户service层
 */
public interface MerchantService {

	/**
	 * 商戶基本信息实例化
	 * 
	 * @return boolean
	 */
	public boolean merchantRegister(Merchant entity);

	/**
	 * 检查商户名是否重复
	 * 
	 * @param dataMap key=(表中列名称),value=(查询参数)
	 * @return List
	 */
	public List<Object> checkMerchantName(Map dataMap);

	/**
	 * 根据商户名查询商户数据
	 * 
	 * @param account
	 * @return
	 */
	public String findMerchantBy(String account);

	/**
	 * 查询数据库表中的商户自增长ID
	 * 
	 * @return long
	 */
	public Long findOriginalMerchantId();

	/**
	 * 保存商户对应的电商平台名称(及编码)
	 * 
	 * @param entity
	 * @param type  1-银盟商户注册,2-第三方商户注册
	 * @return
	 */
	public boolean addMerchantRecordInfo(RecordInfo entity, String type);
}
