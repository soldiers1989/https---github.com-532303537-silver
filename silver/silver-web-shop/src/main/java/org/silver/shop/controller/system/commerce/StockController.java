package org.silver.shop.controller.system.commerce;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import net.sf.json.JSONArray;
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
	 * 
	 * @param warehousCode
	 *            仓库编码
	 * @param warehousName
	 *            仓库名称
	 * @param goodsInfoPack
	 *            商品信息包
	 * @return
	 */
	@RequestMapping(value = "/addGoodsStockCount", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("添加商品库存")
	@RequiresRoles("Merchant")
	public String addGoodsStockCount(@RequestParam("warehouseCode") String warehouseCode,
			@RequestParam("warehouseName") String warehouseName, @RequestParam("goodsInfoPack") String goodsInfoPack,
			HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (warehouseCode != null && warehouseName != null && goodsInfoPack != null) {
			statusMap = stockTransaction.addGoodsStockCount(warehouseCode, warehouseName, goodsInfoPack);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 搜索该仓库下已经备案成功的备案商品信息
	 * 
	 * @param warehouseCode
	 *            仓库编码
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return
	 */
	@RequestMapping(value = "/searchAlreadyRecordGoodsDetails", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("搜索该仓库下已经备案成功的备案商品信息")
	@RequiresRoles("Merchant")
	public String searchAlreadyRecordGoodsDetails(@RequestParam("warehouseCode") String warehouseCode,
			@RequestParam("page") int page, @RequestParam("size") int size, HttpServletRequest req,
			HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (warehouseCode != null) {
			statusMap = stockTransaction.searchAlreadyRecordGoodsDetails(warehouseCode, page, size);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/addGoodSellCount", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商品上架及上架数量")
	@RequiresRoles("Merchant")
	// 单个商品,后续删除
	public String addGoodSellCount(@RequestParam("goodsId") String goodsId, @RequestParam("sellCount") int sellCount,
			HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (goodsId != null) {
			statusMap = stockTransaction.addGoodsSellCount(goodsId, sellCount);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 商户查询当前仓库下商品库存信息
	 * 
	 * @param warehouseCode
	 *            仓库编码
	 * @param page
	 *            页数
	 * @param size
	 *            数目
	 * @return
	 */
	@RequestMapping(value = "/getMerchantGoodsStockInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户查询当前仓库下商品库存信息")
	@RequiresRoles("Merchant")
	public String getMerchantGoodsStockInfo(@RequestParam("page") int page, @RequestParam("size") int size,
			@RequestParam("warehouseCode")String warehouseCode,HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = stockTransaction.getGoodsStockInfo(page, size,warehouseCode);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/merchantSetGoodsSellAndStopSelling", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户批量与单个商品上/下架状态修改")
	@RequiresRoles("Merchant")
	public String merchantSetGoodsSellAndStopSelling(@RequestParam("goodsInfoPack") String goodsInfoPack, @RequestParam("type") int type,
			HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (goodsInfoPack != null && goodsInfoPack.length() > 0 && type == 1 || type == 2) {
			statusMap = stockTransaction.merchantSetGoodsSellAndStopSelling(goodsInfoPack, type);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/merchantSetGoodsStorageAndSellCount", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户批量与单个商品入库与上架")
	@RequiresRoles("Merchant")
	public String merchantSetGoodsStorageAndSellCount(@RequestParam("goodsInfoPack") String goodsInfoPack,
			@RequestParam("type") int type, HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (goodsInfoPack != null && goodsInfoPack.length() > 0 && type == 1 || type == 2) {
			statusMap = stockTransaction.merchantSetGoodsStorageAndSellCount(goodsInfoPack, type);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	public static void main(String[] args) {
		// 模拟商品数据
		JSONArray jsonList = new JSONArray();
		Map<String, Object> data = new HashMap<>();
		/*
		 * data.put("entGoodsNo", "YM_20170000115075204310625696");
		 * data.put("goodsName", "测试商品名称"); data.put("stockCount", 50);
		 */
		data.put("goodsId", "YM_20170000215082908696099795");
		jsonList.add(data);
		System.out.println(jsonList.toString());
	}
}
