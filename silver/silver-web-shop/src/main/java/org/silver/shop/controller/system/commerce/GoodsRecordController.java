package org.silver.shop.controller.system.commerce;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.commerce.GoodsRecordTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商品备案信息Controller
 *
 */
@RequestMapping("/goodsRecord")
@Controller
public class GoodsRecordController {

	@Autowired
	private GoodsRecordTransaction goodsRecordTransaction;

	/**
	 * 查询商户下商品基本信息
	 * 
	 * @param page
	 *            页数
	 * @param size
	 *            数据条数
	 * @return String
	 * 与GoodsContentController的findMerchantGoodsInfo重复了
	 */
	@RequestMapping(value = "/findMerchantGoodsBaseInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询商户下商品基本信息")
	@RequiresRoles("Merchant")
	public String findMerchantGoodsBaseInfo(@RequestParam("page") int page, @RequestParam("size") int size) {
		Map<String,Object> datasMap = goodsRecordTransaction.findMerchantGoodsBaseInfo(page, size);
		return JSONObject.fromObject(datasMap).toString();
	}

	/**
	 * 商户选择商品基本信息后,根据商品ID与商品名查询已发起备案的商品信息
	 * 
	 * @param goodsInfoPack
	 * @return
	 */
	@RequestMapping(value = "/getMerchantGoodsRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("读取已备案的商品信息")
	@RequiresRoles("Merchant")
	public String getMerchantGoodsRecordInfo(@RequestParam("goodsInfoPack")String goodsInfoPack,HttpServletRequest req,HttpServletResponse response) {
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
		if (goodsInfoPack != null) {
			statusMap = goodsRecordTransaction.getMerchantGoodsRecordInfo(goodsInfoPack);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 商戶发起备案
	 * @param customsPort 口岸编码
	 * @param customsCode 主管海关代码
	 * @param ciqOrgCode 检验检疫编码
	 * @param recordGoodsInfoPack 备案商品信息包
	 * @return String 
	 */
	@RequestMapping(value = "/merchantSendGoodsRecord", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商戶发起商品备案")
	@RequiresRoles("Merchant")
	public String merchantSendGoodsRecord(@RequestParam("customsPort") String customsPort,
			@RequestParam("customsCode") String customsCode, @RequestParam("ciqOrgCode") String ciqOrgCode,
			@RequestParam("recordGoodsInfoPack") String recordGoodsInfoPack,HttpServletRequest req,HttpServletResponse response ) {
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
		if (customsPort !=null && customsCode != null && ciqOrgCode != null && recordGoodsInfoPack != null) {
			statusMap = goodsRecordTransaction.merchantSendGoodsRecord(customsPort, customsCode, ciqOrgCode, recordGoodsInfoPack);
			return JSONObject.fromObject(statusMap).toString();
		}
		return null;
	}
	
	@RequestMapping(value = "/findMerchantGoodsRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商戶查询商品备案信息")
	@RequiresRoles("Merchant")
	public String findMerchantGoodsRecordInfo(HttpServletRequest req,HttpServletResponse response ,String goodsId,@RequestParam("page") int page, @RequestParam("size") int size){
		String originHeader = req.getHeader("Origin");
		String[] iPs = { "http://ym.191ec.com:9528", "http://ym.191ec.com:8080", "http://ym.191ec.com:80",
				"http://ym.191ec.com:8090" };
		if (Arrays.asList(iPs).contains(originHeader)) {
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
			response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Origin", originHeader);
		}
		 Map<String,Object> statusMap = goodsRecordTransaction.findMerchantGoodsRecordInfo(goodsId,page,size);
		 return JSONObject.fromObject(statusMap).toString();
	}
	
}
