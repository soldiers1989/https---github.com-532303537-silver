package org.silver.shop.controller.system.commerce;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.commerce.GoodsContentTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商户添加商品Controller
 */
@Controller
@RequestMapping("/merchantGoods")
public class GoodsContentController {

	@Autowired
	private GoodsContentTransaction goodsContentTransaction;

	/**
	 * 商户添加商品基本信息
	 * @param req
	 * @param resp
	 * @return 
	 */
	@RequestMapping(value = "/addMerchantGoodsBaseInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	public String addMerchantGoodsBaseInfo(HttpServletRequest req, HttpServletResponse resp) {
		Map<String, Object> statusMap = new HashMap<>();
		boolean flag = goodsContentTransaction.addMerchantGoodsBaseInfo(req);
		if(flag){
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
		}else{
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
	
	
	@RequestMapping(value="/findMerchantGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	public String findMerchantGoodsInfo(@RequestParam("goodsName")String goodsName,@RequestParam("starDate")String starDate,
			@RequestParam("endDate")String endDate,@RequestParam("ymYear")String ymYear){
		Map<String,Object> statusMap = new HashMap<>();
			Map<String,Object> datasMap = goodsContentTransaction.findAllGoodsInfo(goodsName,starDate,endDate,ymYear);
		
		return null;
	}
}
