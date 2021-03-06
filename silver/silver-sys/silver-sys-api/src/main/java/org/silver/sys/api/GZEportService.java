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
	 * 商品信息验证
	 * @param opType
	 * @param ieFlag
	 * @param businessType
	 * @param records
	 * @param ebEntNo
	 * @param ebEntName
	 * @param currCode
	 * @param customsCode
	 * @param ciqOrgCode
	 * @param ebpentNo
	 * @param ebpentName
	 * @return
	 */
	public  Map<String,Object> goodsRecord(String opType,String ieFlag,String businessType,Object records,String ebEntNo,String ebEntName,String currCode,String customsCode,String ciqOrgCode,String ebpentNo,String ebpentName,String appkey,String notifyurl);
	/**
	 * 商品备案
	 * @param list    商品信息list
	 * @param path    上传FTP路径
	 * @param opType  操作方式
	 * @param customsCode  主管海关
	 * @param ciqOrgCode   检疫检验机构
	 * @param businessType 跨境业务类型  1-特殊监管区域BBC保税进口；2-保税仓库BBC保税进口；3-BC直购进口
	 * @param ebEntNo      电商企业编号
	 * @param ebEntName    电商企业名称
	 * @param currCode     币种
	 * @param ebpentNo     电商平台编号
	 * @param ebpentName   电商平台名称
	 * @param ieFlag       进出境标志       I-进，E-出
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public  Map<String,Object> createHead(JSONArray list,String path,String opType,String businessType,String ieFlag,String ebEntNo,String ebEntName,String currCode,String customsCode,String ciqOrgCode,String ebpentNo,String ebpentName,String appkey,String notifyurl) throws FileNotFoundException, IOException;
	
	/**
	 * 订单备案非空字段验证
	 * @param records
	 * @param opType
	 * @param ieFlag
	 * @param internetDomainName
	 * @param ebpentNo
	 * @param ebpentName
	 * @param ebEntNo
	 * @param ebEntName
	 * @param customsCode
	 * @param ciqOrgCode
	 * @return
	 */
	public   Map<String,Object> orderRecord(Object records,String opType,String ieFlag,String internetDomainName,String ebpentNo,String ebpentName,String ebEntNo,String ebEntName,String customsCode,String ciqOrgCode,String appkey,String notifyurl);
	
	/**
	 * 电子订单备案
	 * @param list
	 * @param path    上传FTP路径
	 * @param opType  操作方式
	 * @param ieFlag  进出口标示
	 * @param internetDomainName  电商平台域名
	 * @param ebpentNo    电商平台编号
	 * @param ebpentName  电商平台名称 
	 * @param ebEntNo     电商企业编号
	 * @param ebEntName   电商企业名称
	 * @param customsCode 主管海关
	 * @param ciqOrgCode  检验检疫机构
	 * @return
	 */
	public   Map<String,Object> createOrder(JSONArray list, String path, String opType, String ieFlag,String internetDomainName,String ebpentNo,String ebpentName,String ebEntNo,String ebEntName,String customsCode,String ciqOrgCode,String appkey,String notifyurl);
	
	
}
