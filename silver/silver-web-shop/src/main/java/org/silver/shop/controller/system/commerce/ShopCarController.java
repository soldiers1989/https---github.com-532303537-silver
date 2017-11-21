package org.silver.shop.controller.system.commerce;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.commerce.ShopCarTransaction;
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
 * 购物车
 *
 */
@Controller
@RequestMapping("/shopCar")
public class ShopCarController {

	@Autowired
	private ShopCarTransaction shopCarTransaction;

	@RequestMapping(value = "/addGoodsToShopCar", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户添加商品至购物车")
	@RequiresRoles("Member")
	public String addGoodsToShopCar(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entGoodsNo") String entGoodsNo, @RequestParam("count") int count) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = shopCarTransaction.addGoodsToShopCar(entGoodsNo, count);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getGoodsShopCartInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户查询购物车信息")
	@RequiresRoles("Member")
	public String getGoodsShopCartInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = shopCarTransaction.getGoodsToShopCartInfo();
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/deleteShopCartGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户删除购物车信息")
	@RequiresRoles("Member")
	public String deleteShopCartGoodsInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("goodsId") String goodsId) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = shopCarTransaction.deleteShopCartGoodsInfo(goodsId);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 用户修改购物车选中标识与商品数量
	 * 
	 * @param req
	 * @param response
	 * @param entGoodsNo
	 *            商品备案Id
	 * @param count
	 *            商品数量
	 * @return
	 */
	@RequestMapping(value = "/editShopCarGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户修改购物车选中标识与商品数量")
	@RequiresRoles("Member")
	public String editShopCarGoodsInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("goodsInfo") String goodsInfo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = shopCarTransaction.editShopCarGoodsInfo(goodsInfo);
		return JSONObject.fromObject(statusMap).toString();
	}
	public static void main(String[] args) {
		List<Map<String,Object>> list = new ArrayList<>();
		Map<String,Object> map = new HashMap();
		map.put("entGoodsNo", "GR_20170000215088379460286258");
		map.put("count", "2");
		list.add(map);
		System.out.println("--------->>>>>>>"+list.toString());
		System.out.println(JSONArray.fromObject(list).toString());
	}
}
