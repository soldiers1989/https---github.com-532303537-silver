package org.silver.shop.controller.system.commerce;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.commerce.GoodsContentTransaction;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商户商品Controller,商品基本信息的操作
 */
@Controller
@RequestMapping("/goods")
public class GoodsContentController {

	@Autowired
	private GoodsContentTransaction goodsContentTransaction;

	/**
	 * 商户添加商品基本信息
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/addMerchantGoodsBaseInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	//@RequiresRoles("Merchant")
	public String addMerchantGoodsBaseInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		/*
		 * String[] iPs = { "http://ym.191ec.com:9528",
		 * "http://ym.191ec.com:8080", "http://ym.191ec.com:80",
		 * "http://ym.191ec.com:8090" }; if
		 * (Arrays.asList(iPs).contains(originHeader)) {
		 */
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		/* } */
		Map<String, Object> statusMap = goodsContentTransaction.addMerchantGoodsBaseInfo(req);
		if (statusMap != null) {
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 商户查询商品基本信息
	 * 
	 * @param goodsName
	 *            商品名
	 * @param starDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @param ymYear
	 *            年份
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping(value = "/findMerchantGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户查询商品基本信息")
	public String findMerchantGoodsInfo(String goodsId, String goodsName, String starDate, String endDate,
			String ymYear, @RequestParam("page") int page, @RequestParam("size") int size, HttpServletRequest req,
			HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		System.out.println(WebUtil.getSession().getId());
		Map<String, Object> datasMap = goodsContentTransaction.findAllGoodsInfo(goodsId, goodsName, starDate, endDate,
				ymYear, page, size);	
		return JSONObject.fromObject(datasMap).toString();
	}

	/**
	 * 商户修改商品信息
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/editMerchantGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户修改商品信息")
	@RequiresRoles("Merchant")
	public String editMerchantGoodsInfo(HttpServletRequest req, HttpServletResponse resp) {
		Map<String, Object> statusMap = new HashMap<>();
		boolean flag = goodsContentTransaction.editMerchantGoodsBaseInfo(req);
		if (flag) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 删除商品基本信息
	 * 
	 * @param goodsId
	 *            商品ID
	 * @return
	 */
	@RequestMapping(value = "/deleteMerchantBaseInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("删除商品基本信息")
	@RequiresRoles("Merchant")
	public String deleteMerchantBaseInfo(@RequestParam("goodsId") String goodsId) {
		Map<String, Object> statusMap = new HashMap<>();
		if (goodsId != null) {
			Map<String, Object> datasMap = goodsContentTransaction.deleteMerchantBaseInfo(goodsId);
			String stauts = datasMap.get(BaseCode.STATUS.toString()) + "";
			if (stauts.equals("1")) {
				return JSONObject.fromObject(datasMap).toString();
			}
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getShowGoodsBaseInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("前台获取展示商品信息")
	public String getShowGoodsBaseInfo(String firstType, String secndType, String thirdType,
			@RequestParam("page") int page, @RequestParam("size") int size, HttpServletRequest req,
			HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Origin", "*");
		if (firstType == null || firstType.equals("")) {
			firstType = "0";
		}
		if (secndType == null || secndType.equals("")) {
			secndType = "0";
		}
		if (thirdType == null || thirdType.equals("")) {
			thirdType = "0";
		}
		Map<String, Object> statusMap = goodsContentTransaction.getShowGoodsBaseInfo(Integer.valueOf(firstType),
				Integer.valueOf(secndType), Integer.valueOf(thirdType), page, size);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getOneGoodsBaseInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("前台根据商品ID查询商品基本信息")
	public String getOneGoodsBaseInfo(@RequestParam("goodsId") String goodsId, HttpServletRequest req,
			HttpServletResponse response) {
		Map<String, Object> statusMap = new HashMap<>();
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Origin", "*");
		if (goodsId != null) {
			statusMap = goodsContentTransaction.getOneGoodsBaseInfo(goodsId);
			return JSONObject.fromObject(statusMap).toString();
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		return null;
	}

}
