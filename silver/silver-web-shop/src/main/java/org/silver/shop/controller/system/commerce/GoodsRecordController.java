package org.silver.shop.controller.system.commerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 商品备案信息Controller,商品备案的操作
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
	 */
	@RequestMapping(value = "/findMerchantGoodsBaseInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询商户下商品基本信息")
	@RequiresRoles("Merchant")
	public String findMerchantGoodsBaseInfo(@RequestParam("page") int page, @RequestParam("size") int size) {
		Map<String, Object> statusMap = new HashMap<>();
		List datasList = goodsRecordTransaction.findMerchantGoodsBaseInfo(page, size);
		if (datasList != null && datasList.size() > 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), datasList);
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getMerchantGoodsRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("读取已备案的商品信息")
	public String getMerchantGoodsRecordInfo(@RequestParam("goodsIdPack") String goodsIdPack) {
		Map<String, Object> statusMap = new HashMap<>();
		if (goodsIdPack != null) {
			List datasList = goodsRecordTransaction.getMerchantGoodsRecordInfo(goodsIdPack);
			if (datasList != null && datasList.size() > 0) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.DATAS.toString(), datasList);
				statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			}
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
	
	
	public static void main(String[] args) {
		JSONArray json = new JSONArray();
		String a = "YM_20170000115058114089963091";
		List li = new ArrayList<>();
		li.add(a);
		System.out.println(JSONArray.fromObject(li).toString());
	}
}
