package org.silver.sys.api;

import java.util.Map;

/**
 * 异步回调，将海关和国检的审核结果，通知第三方商城 
 * @author zhangxin 2017/10/16
 *
 */
public interface CallbackService {

	/**
	 * 
	 * @param messageID 报文编号
	 * @param type      报文类型
	 * @param resultMap 审核结果
	 * @return
	 */
	public  Map<String,Object> AsynchronousCallback(String messageID,int type,Map<String,Object> resultMap);
}
