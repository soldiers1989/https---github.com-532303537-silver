package org.silver.pay.api;

import java.util.Map;

import net.sf.json.JSONArray;
/**
 * 广州电子口岸支付报文接口
 * @author Administrator 
 *
 */
public interface GZPayEportService {

	/**
	 * 
	 * @param records
	 * @param opType
	 * @return
	 */
	public Map<String,Object> payRecord(Object records, String opType,String customsCode,String ciqOrgCode,String tenantNo,String notifyurl);
	/**
	 * 
	 * @param list
	 * @param path
	 * @param opType
	 * @return
	 */
	public Map<String, Object> createPay(JSONArray list, String opType,String customsCode,String ciqOrgCode,String tenantNo,String notifyurl);

}
