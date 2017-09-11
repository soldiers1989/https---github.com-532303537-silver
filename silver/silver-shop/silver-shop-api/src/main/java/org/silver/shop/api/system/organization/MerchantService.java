package org.silver.shop.api.system.organization;

import java.util.List;
import java.util.Map;
import org.silver.shop.model.system.tenant.RecordInfo;

/**
 * 商户service层
 */
public interface MerchantService {

	/**
	 * 保存商戶基本信息与备案信息
	 * 
	 * @return Map
	 */
	public Map<String,Object> merchantRegister(String merchantId,String account, String loginPassword, String merchantIdCard,
			String merchantIdCardName, String recordInfoPack, String type);

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
	 * 获取数据库查询自增ID后,自编ID
	 * @return Map
	 */
	public Map<String, Object> findOriginalMerchantId();

	/**
	 * 保存商户对应的电商平台名称(及编码)
	 * 
	 * @param entity
	 * @param type  1-银盟商户注册,2-第三方商户注册
	 * @return
	 */
	public boolean addMerchantRecordInfo(RecordInfo entity, String type);
}
