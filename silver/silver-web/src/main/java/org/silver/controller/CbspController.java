package org.silver.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.service.CheckDatasService;
import org.silver.service.EportEntry;
import org.silver.service.OauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@RestController
@RequestMapping(value="/Eport")
public class CbspController {
       
	 
	   @Autowired
	   private OauthService oauthService;
	   @Autowired
	   private EportEntry eportEntry;
	   

	   
	   /**
	    * 通用网关接口 
	    * @param req
	    * @param resp 
	    * @param type 0 商品备案   1 订单备案   2清单（未确定）
	    * @param eport 口岸  0 广州电子口岸   1广东智检 
	    * @param ieFlag 进出口标识 I 进口  E 出口（广州口岸）
	    * @param businessType 跨境业务类型    1-特殊监管区域BBC保税进口； 2-保税仓库BBC保税进口；3-BC直购进口；（广州口岸）
	   
	    * @param appkey 
	    * @return   
	    */
	   @RequestMapping(value="/Report" ,produces = "application/json; charset=utf-8")
	   public String doNSRecord(HttpServletRequest req,HttpServletResponse resp,int type,int eport,String opType,String ieFlag,String businessType,String appkey,String clientsign,String timestamp ,String datas,String notifyurl){
		   Map statusMap = new HashMap();
		   if(opType==null){
			   opType="A";
		   }
		   if(ieFlag==null){
			   ieFlag="I";
		   }
		   
		   if(appkey!=null&&clientsign!=null&&timestamp!=null&&datas!=null&&opType!=null){
			    statusMap =oauthService.checkSign(appkey, clientsign,datas,notifyurl, timestamp); //eportService.checkDatas(req);
			    if((int)statusMap.get("status")==1){
			    	Map<String,Object> dataMap =  eportEntry.getInternetDomainName(eport, appkey);
			    	System.out.println(dataMap);
			    	if(dataMap.size()<1){
			        	statusMap.put("status", -7);
			        	statusMap.put("msg", "当前口岸未提供有电商平台备案信息");
			        	return JSONObject.fromObject(statusMap).toString();
			        }
			    	
			    	statusMap=eportEntry.uploadDatas(eport,type,opType,ieFlag,businessType,datas,notifyurl,statusMap.get("internetDomainName")+"",statusMap.get("no")+"",statusMap.get("name")+"");
			   }
			    return JSONObject.fromObject(statusMap).toString();
		   }
		   statusMap.put("status", -3);
		   statusMap.put("err", "Invalid params !");
		   return JSONObject.fromObject(statusMap).toString();
		   
	   }
}
