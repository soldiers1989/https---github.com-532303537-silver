package org.silver.shop.controller.system.commerce;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.zookeeper.data.Stat;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.commerce.ShopCarTransaction;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
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
		return JSONObject.fromObject(shopCarTransaction.getGoodsToShopCartInfo()).toString();
	}

	@RequestMapping(value = "/deleteShopCartGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "用户删除购物车信息")
	@RequiresRoles("Member")
	public String deleteShopCartGoodsInfo(HttpServletRequest req, HttpServletResponse response,
			String entGoodsNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if(StringEmptyUtils.isNotEmpty(entGoodsNo)){
			statusMap = shopCarTransaction.deleteShopCartGoodsInfo(entGoodsNo);
		}else{
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.LOSS_SESSION.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "商品Id参数不正确!");
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 用户修改购物车选中标识与商品数量
	 * 
	 * @param req
	 * @param response
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
	
	@RequestMapping(value = "/temporaryUpdate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation(value = "临时接口--修改旧用户购物车店铺信息")
	//@RequiresRoles("Member")
	public String temporaryUpdate(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(shopCarTransaction.temporaryUpdate()).toString();
	}
}
