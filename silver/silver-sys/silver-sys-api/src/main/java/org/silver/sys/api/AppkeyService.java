package org.silver.sys.api;

import java.util.Map;

public interface AppkeyService {
    /**
     * 创建appkey
     * @param app_name 应用名
     * @param user_name 用户名
     * @param user_mobile 用户联系电话
     * @return  map  appkey,appsecret
     */
	public Map<String,String> createRecord(String app_name,String user_name,String user_mobile,String user_id,String company_name,String website);
	/**
	 * 认证签名
	 * @param appKey  应用appkey
	 * @param clientSign 客户端签名
	 * @param listStr    客户端数据   (json串数据)
	 * @param timestamp  时间戳
	 * @return 1 校验通过        -1.。。  其它错误提示
	 */
	public Map<String,Object> CheckClientSign(String appKey,String clientSign,String listStr,String timestamp);
    /**
     * 生成  访问key accessToken
     * @param appkey 应用appkey
     * @param signature  客户端签名
     * @param timestamp  时间戳
     * @return
     */
	public Map<String,Object> createAccessToken(String appkey,String signature,String timestamp);

}
