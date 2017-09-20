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
 * 商户商品Controller
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
	
	/**
	 * 商户查询商品基本信息
	 * @param goodsName 商品名
	 * @param starDate 开始时间
	 * @param endDate 结束时间
	 * @param ymYear 年份
	 * @param page 
	 * @param size
	 * @return
	 */
	@RequestMapping(value="/findMerchantGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户查询商品基本信息")
	public String findMerchantGoodsInfo(@RequestParam("goodsId")String goodsId,@RequestParam("goodsName")String goodsName,@RequestParam("starDate")String starDate,
			@RequestParam("endDate")String endDate,@RequestParam("ymYear")String ymYear,@RequestParam("page")int page,@RequestParam("size")int size){
		Map<String,Object> statusMap = new HashMap<>();
		Map<String,Object> datasMap = goodsContentTransaction.findAllGoodsInfo(goodsId,goodsName,starDate,endDate,ymYear,page,size);
		String status = datasMap.get(BaseCode.STATUS.getBaseCode())+"";
		if(status.equals("1")){
			return JSONObject.fromObject(datasMap).toString();
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}
	
	/**
	 * 商户修改商品信息
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value="/editMerchantGoodsInfo",method = RequestMethod.POST,produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户修改商品信息")
	@RequiresRoles("Merchant")
	public String editMerchantGoodsInfo(HttpServletRequest req,HttpServletResponse resp){
		Map<String, Object> statusMap = new HashMap<>();
		boolean flag = goodsContentTransaction.editMerchantGoodsBaseInfo(req);
		if(flag){
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
		}else{
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
}
