package org.silver.shop.api.common.base;

import java.util.Map;

public interface IdCardService {

	/**
	 * 获取所有身份证信息
	 * @param page 页数
	 * @param size 数目
	 * @param datasMap 
	 * 			查询参数
	 * @return Map
	 */
	public Map<String,Object> getAllIdCard( int page, int size, Map<String,Object> datasMap);

	/**
	 * 管理员修改身份证信息
	 * @param id 
	 * @param idName 身份证名字
	 * @param idNumber 身份证号码
	 * @param type 类型：1-未验证,2-手工验证,3-海关认证,4-第三方认证,5-错误
	 * @return Map
	 */
	public Map<String,Object> editIdCardInfo(long id, String idName, String idNumber, int type);

	/**
	 * 去重
	 * @return
	 */
	public Map<String, Object> deleteDuplicateIdCardInfo();

	public Object temPush();

	/**
	 * 姓名与身份证号码+手机号码(三要素)验证、发送身份证验证请求
	 * @param idName 姓名
	 * @param idCard 身份证号码
	 * @param phone 手机号码
	 * @return Map
	 */
	public Map<String, Object> sendIdCardPhoneCertification(String idName, String idCard, String phone);
	
}
