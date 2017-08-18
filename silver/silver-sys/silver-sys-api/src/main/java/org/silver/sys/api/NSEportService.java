package org.silver.sys.api;

import java.util.Map;
/**
 * 通用网关--南沙业务处理接口
 * @author Administrator
 *
 */
public interface NSEportService {
    /**
     * 生成报文xml
     * @param list 
     * @param type 
     * @return
     */
	public Map<String,Object> createEportXML(int type, Object list);
	
	/**
	 * 存储 报文到数据库
	 * @return
	 */
	public Map<String,Object> saveEportData();
	
	/**
	 * 上送报文到电子口岸
	 * @return
	 */
	public Map<String,Object> pushEportData(String localPath,String serverPath);
}
