package org.silver.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.pay.api.GZPayEportService;
import org.silver.pay.api.ZJPayEportService;
import org.silver.sys.api.EBPentRecordService;
import org.silver.sys.api.GZEportService;
import org.silver.sys.api.ZJEportService;
import org.silver.sys.model.EBPentRecord;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("eportEntry")
public class EportEntry {

	@Reference
	private GZEportService gZEportService;
	@Reference
	private EBPentRecordService eBPentRecordService;
	@Reference
	private ZJEportService zJEportService;
	@Reference
	private GZPayEportService gZPayEportService;
	@Reference
	private ZJPayEportService zJPayEportService;
	
	public Map<String,Object> uploadDatas(int eport, int type, String opType,String ieFlag,String businessType,Object jsonstr, String notifyurl,String internetDomainName,String ebpentNo,String ebpentName,String ebEntNo,String ebEntName,String currCode,String customsCode,String ciqOrgCode,String appkey) {
		 Map<String,Object>  reqMap = new HashMap<String,Object>();
		 if("A".equals(opType)||"M".equals(opType)||"D".equals(opType)){
			 switch(eport){
				case 1://广州
					switch(type){
					case 0://商品备案
						if(("I".equals(ieFlag)||"E".equals(ieFlag))&&("1".equals(businessType)||"2".equals(businessType)||"3".equals(businessType))){
							reqMap=gZEportService.goodsRecord(opType,ieFlag,businessType,jsonstr,ebEntNo,ebEntName,currCode,customsCode,ciqOrgCode,ebpentNo,ebpentName,appkey);
							return reqMap;
						}
						reqMap.put("status", -5);
						reqMap.put("err", "错误的业务类型或者进出口标识，请确认后再提交");
						
						return	reqMap;
					case 1://订单备案
						if(("I".equals(ieFlag)||"E".equals(ieFlag))&&internetDomainName!=null&&!"".equals(internetDomainName.trim())){
							System.out.println("JSON数据："+jsonstr.toString());
							reqMap=gZEportService.orderRecord(jsonstr,opType,ieFlag,internetDomainName,ebpentNo,ebpentName,ebEntNo,ebEntName,customsCode,ciqOrgCode,appkey);
							return reqMap;	
						}
						reqMap.put("status", -6);
						reqMap.put("err", "错误的进出口标识，请确认后再提交");
						return	reqMap;
					case 2://支付单
						if(("I".equals(ieFlag)||"E".equals(ieFlag))&&internetDomainName!=null&&!"".equals(internetDomainName.trim())){
							System.out.println("JSON数据："+jsonstr.toString());
							System.out.println("222222222222222222222"+gZPayEportService);
							
							reqMap=gZPayEportService.payRecord(jsonstr, opType, customsCode, ciqOrgCode);
							return reqMap;	
						}
						reqMap.put("status", -7);
						reqMap.put("err", "错误的进出口标识，请确认后再提交");
						return	reqMap;
					 default:	
						reqMap.put("status", -1);
						reqMap.put("err", "未知的业务类型-type");
					    return reqMap;
						 
					}
					
				case 2://南沙
					switch(type){
					case 0://商品备案
						if(("I".equals(ieFlag)||"E".equals(ieFlag))&&("1".equals(businessType)||"2".equals(businessType)||"3".equals(businessType))){
							reqMap=zJEportService.zjCreateGoodsRecord(jsonstr, "", opType, businessType, ieFlag,ebEntNo,ebEntName);
							return reqMap;
						}
						reqMap.put("status", -5);
						reqMap.put("err", "错误的业务类型或者进出口标识，请确认后再提交");
						
						return	reqMap;
					case 1://订单备案
						if(("I".equals(ieFlag)||"E".equals(ieFlag))&&internetDomainName!=null&&!"".equals(internetDomainName.trim())){
							System.out.println("JSON数据："+jsonstr.toString());
							reqMap=zJEportService.zjCreateOrderRecord(jsonstr, "", opType, ieFlag, ebEntNo, ebEntName, ebpentNo, ebpentName, internetDomainName);
							return reqMap;	
						}
						reqMap.put("status", -6);
						reqMap.put("err", "错误的进出口标识，请确认后再提交");
						return	reqMap;
					case 2://支付单
						if(("I".equals(ieFlag)||"E".equals(ieFlag))&&internetDomainName!=null&&!"".equals(internetDomainName.trim())){
							System.out.println("JSON数据："+jsonstr.toString());
							reqMap=zJPayEportService.zjCreatePayRecord(jsonstr, "", opType,customsCode, ciqOrgCode);
							return reqMap;	
						}
						reqMap.put("status", -6);
						reqMap.put("err", "错误的进出口标识，请确认后再提交");
						return	reqMap;
					default:	
						reqMap.put("status", -1);
						reqMap.put("err", "未知的业务类型-type");
					    return reqMap;
					}
						
				} 
		 }
		
		
		return null;
		
		
	}

	public Map<String,Object> getInternetDomainName(int eport,String appkey){
		Map<String,Object> params = new HashMap<>();
		params.put("eport", eport);
		params.put("app_key", appkey);
		params.put("del_flag", 0);
		List<EBPentRecord> list=(List<EBPentRecord>) eBPentRecordService.findByProperty(params, 1, 1);
		if(list!=null&&list.size()>0){
			params.put("internetDomainName", list.get(0).getInternetDomainName());//
			params.put("no", list.get(0).getEBPEntNo());//
			params.put("name", list.get(0).getEBPEntName());//
		}
		return params;
		
	}
	
}
