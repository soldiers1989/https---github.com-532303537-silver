package org.silver.shop.controller.system.commerce;

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.commerce.StockTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 库存Controller
 *
 */
@Controller
@RequestMapping("/stock")
public class StockController {

	@Autowired
	private StockTransaction stockTransaction;

	/**
	 * 添加商品库存
	 * @param warehousCode 仓库编码 
	 * @param warehousName 仓库名称
	 * @param goodsInfoPack 商品信息包
	 * @return
	 */
	@RequestMapping(value = "/addGoodsStockCount", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("添加商品库存")
	@RequiresRoles("Merchant")
	public String addGoodsStockCount(@RequestParam("warehousCode")String warehousCode,@RequestParam("warehousName")String warehousName,
			@RequestParam("goodsInfoPack")String goodsInfoPack) {
		Map<String,Object> statusMap = new HashMap<>();
		if(warehousCode!=null&& warehousName!=null && goodsInfoPack!=null){
			statusMap = stockTransaction.addGoodsStockCount(warehousCode,warehousName,goodsInfoPack);
		}else{
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 搜索该仓库下已经备案成功的备案商品信息
	 * @param warehouseCode 仓库编码 
	 * @param page 页数
	 * @param size 数目
	 * @return
	 */
	@RequestMapping(value = "/searchAlreadyRecordGoodsDetails", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("搜索该仓库下已经备案成功的备案商品信息")
	@RequiresRoles("Merchant")
	public String searchAlreadyRecordGoodsDetails(@RequestParam("warehouseCode") String warehouseCode,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		Map<String, Object> statusMap = new HashMap<>();
		if (warehouseCode != null) {
			statusMap = stockTransaction.searchAlreadyRecordGoodsDetails(warehouseCode,page, size);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
	
	
	public String getGoodstotalStock(){
		return null;
	}
}
