package org.silver.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.sys.api.EBPentRecordService;
import org.silver.sys.api.GZEportService;
import org.silver.sys.model.EBPentRecord;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("eportEntry")
public class EportEntry {

	@Reference
	private GZEportService gZEportService;
	@Reference
	private EBPentRecordService eBPentRecordService;
	
	
	public Map<String,Object> uploadDatas(int eport, int type, String opType,String ieFlag,String businessType,Object jsonstr, String notifyurl,String internetDomainName,String ebpentNo,String ebpentName) {
		 Map<String,Object>  reqMap = new HashMap<String,Object>();
		 if("A".equals(opType)||"M".equals(opType)||"D".equals(opType)){
			 switch(eport){
				case 0://广州
					switch(type){
					case 0://商品备案
						if(("I".equals(ieFlag)||"E".equals(ieFlag))&&("1".equals(businessType)||"2".equals(businessType)||"3".equals(businessType))){
							reqMap=gZEportService.goodsRecord(opType,ieFlag,businessType,jsonstr);
							return reqMap;
						}
						reqMap.put("status", -5);
						reqMap.put("err", "错误的业务类型或者进出口标识，请确认后再提交");
						
						return	reqMap;
					case 1://订单备案
						if(("I".equals(ieFlag)||"E".equals(ieFlag))&&internetDomainName!=null&&!"".equals(internetDomainName.trim())){
							reqMap=gZEportService.orderRecord(jsonstr, opType, ieFlag,internetDomainName,ebpentNo,ebpentName);
							return reqMap;	
						}
						reqMap.put("status", -6);
						reqMap.put("err", "错误的进出口标识，请确认后再提交");
						return	reqMap;
					case 2://电子清单
						//reqMap=gZEportService.payRecord(jsonstr, opType);
						
						return	reqMap;
					 default:	
						reqMap.put("status", -1);
						reqMap.put("err", "未知的业务类型-type");
					    return reqMap;
						 
					}
					
				case 1://南沙
				default:
						
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
