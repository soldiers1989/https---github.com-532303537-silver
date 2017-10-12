package org.silver.shop.controller.system.commerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.commerce.GoodsRecordTransaction;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import javassist.compiler.ast.Symbol;
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
	public String getMerchantGoodsRecordInfo(@RequestParam("goodsIdPack") String goodsInfoPack) {
		Map<String, Object> statusMap = new HashMap<>();
		if (goodsInfoPack != null) {
			List<Object> datasList = goodsRecordTransaction.getMerchantGoodsRecordInfo(goodsInfoPack);
			if (datasList != null && datasList.isEmpty()) {
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

	/**
	 * 商户商品批量备案
	 * 
	 * @param eport
	 *            口岸
	 * @param customsCode
	 *            检验检疫机构代码
	 * @param ciqOrgCode
	 *            主管海关代码
	 * @param recordGoodsInfoPack
	 *            备案商品信息
	 * @return
	 */
	@RequestMapping(value = "/merchantSendGoodsRecord", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商戶发起商品备案")
	@RequiresRoles("Merchant")
	public String merchantSendGoodsRecord(@RequestParam("eport") String eport,
			@RequestParam("customsCode") String customsCode, @RequestParam("ciqOrgCode") String ciqOrgCode,
			@RequestParam("recordGoodsInfoPack") String recordGoodsInfoPack) {
		Map<String, Object> statusMap = new HashMap<>();
		if (eport != null && customsCode != null && ciqOrgCode != null && recordGoodsInfoPack != null) {
			statusMap = goodsRecordTransaction.merchantSendGoodsRecord(eport, customsCode, ciqOrgCode,
					recordGoodsInfoPack);
			return JSONObject.fromObject(statusMap).toString();
		}
		return null;
	}

	public static void main(String[] args) {
		// 流水号头
		String topStr = "YM_";
		// 生成商品基本信息ID
		String goodsId = SerialNoUtils.getSerialNo(topStr, null);
		System.out.println(goodsId.length());
	}
}
