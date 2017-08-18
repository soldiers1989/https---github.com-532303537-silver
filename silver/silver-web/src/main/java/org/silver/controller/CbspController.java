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
	    * @param type 0 商品备案   1 订单备案   2支付单备案 
	    * @param eport 口岸  0 广州电子口岸   1广东智检 
	    * @param ieFlag 进出口标识 I 进口  E 出口（广州口岸）
	    * @param businessType 跨境业务类型    1-特殊监管区域BBC保税进口； 2-保税仓库BBC保税进口；3-BC直购进口；（广州口岸）
	    * @param opType 操作类型  A新增     M修改     D取消
	    * @param internetDomainName 电商平台互联网域名 （订单备案时必须字段）
	    * @return   
	    */
	   @RequestMapping(value="/Report" ,produces = "application/json; charset=utf-8")
	   public String doNSRecord(HttpServletRequest req,HttpServletResponse resp,int type,int eport,String opType,String ieFlag,String businessType,String appkey,String clientsign,String timestamp ,String datas,String notifyurl,String internetDomainName){
		   Map statusMap = new HashMap();
		   if(appkey!=null&&clientsign!=null&&timestamp!=null&&datas!=null&&opType!=null){
			    statusMap =oauthService.checkSign(appkey, clientsign,datas,notifyurl, timestamp); //eportService.checkDatas(req);
			    if((int)statusMap.get("status")==1){
			    	statusMap=eportEntry.uploadDatas(eport,type,opType,ieFlag,businessType,datas,notifyurl,internetDomainName);
			   }
			    return JSONObject.fromObject(statusMap).toString();
		   }
		   statusMap.put("status", -3);
		   statusMap.put("err", "Invalid params !");
		   return JSONObject.fromObject(statusMap).toString();
		   
	   }
}
