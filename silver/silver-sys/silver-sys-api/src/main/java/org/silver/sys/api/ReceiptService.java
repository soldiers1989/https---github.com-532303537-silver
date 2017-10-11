package org.silver.sys.api;

import java.util.Map;

/**
 * 异步回调，通知第三方报文审核结果
 * @author Administrator
 *
 */
public interface ReceiptService {

	public Map<String,Object> createAccessToken(String messageID,String orgMessageType);
}
