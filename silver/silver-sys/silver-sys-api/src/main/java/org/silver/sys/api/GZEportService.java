package org.silver.sys.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

/**
 * 海关网关--电子口岸业务处理接口
 * @author Administrator
 *
 */
public interface GZEportService {

	/**
	 * 信息请求
	 * @param records  商品信息
	 * @param eport    走哪个口岸  0 电子口岸  1 智检
	 * @param type     报文类型      0商品备案   1订单备案     2支付备案
	 */
	public  void requestAnalysis(Object records,String type,String eport);
	/**
	 * 商品备案申请验证
	 * @param records  商品信息
	 * @return
	 */
	public  Map<String,Object> goodsRecord(String opType,String ieFlag,String businessType,Object records);
	
	/**
	 * 
	 * @param list    商品信息list
	 * @param path    上传FTP路径
	 * @param opType  操作方式
	 * @param customsCode  主管海关
	 * @param ciqOrgCode   检疫检验机构
	 * @param businessType 跨境业务类型  1-特殊监管区域BBC保税进口；
     *                             2-保税仓库BBC保税进口；
     *                             3-BC直购进口
	 * @param ieFlag       进出境标志       I-进，E-出
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public  Map<String,Object> createHead(JSONArray list,String path,String opType,String businessType,String ieFlag) throws FileNotFoundException, IOException;
	
	/**
	 * 订单备案申请数据验证
	 * @param records  订单信息
	 * @param  opType  操作方式
	 * @param  ieFlag      进出境标志       I-进，E-出
	 * @param internetDomainName 电商平台互联网域名
	 * @return
	 */
	
	public   Map<String,Object> orderRecord(Object records,String opType,String ieFlag,String internetDomainName);
	
	/**
	 * 生成订单备案xml 
	 * @param list
	 * @param path
	 * @param opType
	 * @param ieFlag
	 * @return
	 */
	public   Map<String,Object> createOrder(JSONArray list, String path, String opType, String ieFlag,String internetDomainName);
	
	/**
	 * 
	 * @param records
	 * @param opType
	 * @return
	 */
	public Map<String,Object> payRecord(Object records, String opType);
	
	
	/**
	 * 
	 * @param list
	 * @param path
	 * @param opType
	 * @return
	 */
	public Map<String, Object> createPay(JSONArray list, String path, String opType);
}
