package org.silver.shop.api.common.base;

import java.util.Map;

public interface IdCardService {

	/**
	 * 获取所有身份证信息
	 * @param idName 姓名
	 * @param idNumber 身份证号码
	 * @param page 页数
	 * @param size 数目
	 * @param type  类型：1-未验证,2-手工验证,3-海关认证,4-第三方认证,5-错误
	 * @return Map
	 */
	public Map<String,Object> getAllIdCard(String idName, String idNumber, int page, int size, String type);

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
	
}
