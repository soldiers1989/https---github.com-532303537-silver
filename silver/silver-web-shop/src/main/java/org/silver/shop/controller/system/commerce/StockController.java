package org.silver.shop.controller.system.commerce;

import java.util.Arrays;
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
	public String addGoodsStockCount(@RequestParam("warehousCode") String warehousCode,
			@RequestParam("warehousName") String warehousName, @RequestParam("goodsInfoPack") String goodsInfoPack,HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		String[] iPs = { "http://ym.191ec.com:9528", "http://ym.191ec.com:8080", "http://ym.191ec.com:80",
				"http://ym.191ec.com:8090" };
		if (Arrays.asList(iPs).contains(originHeader)) {
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
			response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Origin", originHeader);
		}
		Map<String, Object> statusMap = new HashMap<>();
		if (warehousCode != null && warehousName != null && goodsInfoPack != null) {
			statusMap = stockTransaction.addGoodsStockCount(warehousCode, warehousName, goodsInfoPack);
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
			@RequestParam("page") int page, @RequestParam("size") int size,HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		String[] iPs = { "http://ym.191ec.com:9528", "http://ym.191ec.com:8080", "http://ym.191ec.com:80",
				"http://ym.191ec.com:8090" };
		if (Arrays.asList(iPs).contains(originHeader)) {
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
			response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Origin", originHeader);
		}
		Map<String, Object> statusMap = new HashMap<>();
		if (warehouseCode != null) {
			statusMap = stockTransaction.searchAlreadyRecordGoodsDetails(warehouseCode, page, size);
			return JSONObject.fromObject(statusMap).toString();
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
	public String addGoodSellCount(@RequestParam("goodsId") String goodsId,
			@RequestParam("sellCount") int sellCount,HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		String[] iPs = { "http://ym.191ec.com:9528", "http://ym.191ec.com:8080", "http://ym.191ec.com:80",
				"http://ym.191ec.com:8090" };
		if (Arrays.asList(iPs).contains(originHeader)) {
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
			response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Origin", originHeader);
		}
		Map<String, Object> statusMap = new HashMap<>();
		if (goodsId != null ) {
			statusMap = stockTransaction.addGoodsSellCount(goodsId,sellCount);
			return JSONObject.fromObject(statusMap).toString();
		}else{
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
	@RequestMapping(value = "/getGoodsStockInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询当前商户所有商品库存信息")
	@RequiresRoles("Merchant")
	public String getGoodsStockInfo(@RequestParam("page") int page, @RequestParam("size") int size,HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		String[] iPs = { "http://ym.191ec.com:9528", "http://ym.191ec.com:8080", "http://ym.191ec.com:80",
				"http://ym.191ec.com:8090" };
		if (Arrays.asList(iPs).contains(originHeader)) {
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
			response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Origin", originHeader);
		}
		Map<String, Object> statusMap = new HashMap<>();
			statusMap = stockTransaction.getGoodsStockInfo( page, size);
			return JSONObject.fromObject(statusMap).toString();
		
	}
	
	
	public static void main(String[] args) {
		// 模拟商品数据
		JSONArray jsonList = new JSONArray();
		Map<String, Object> data = new HashMap<>();
		data.put("entGoodsNo", "YM_20170000115075204310625696");
		data.put("goodsName", "测试商品名称");
		data.put("stockCount", 50);
		jsonList.add(data);
		System.out.println(jsonList.toString());
	}
}
