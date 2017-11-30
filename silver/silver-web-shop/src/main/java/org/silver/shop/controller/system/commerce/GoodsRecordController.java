package org.silver.shop.controller.system.commerce;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * 商品备案信息Controller
 *
 */
@RequestMapping("/goodsRecord")
@Controller
public class GoodsRecordController {
	protected static final Logger logger = LogManager.getLogger();
	@Autowired
	private GoodsRecordTransaction goodsRecordTransaction;

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
	public String getMerchantGoodsRecordInfo(@RequestParam("goodsInfoPack") String goodsInfoPack,
			HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
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
	 * 
	 * @param customsPort
	 *            口岸编码
	 * @param customsCode
	 *            主管海关代码
	 * @param ciqOrgCode
	 *            检验检疫编码
	 * @param recordGoodsInfoPack
	 *            备案商品信息包
	 * @return String
	 */
	@RequestMapping(value = "/merchantSendGoodsRecord", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商戶发起商品备案")
	@RequiresRoles("Merchant")
	public String merchantSendGoodsRecord(@RequestParam("customsPort") String customsPort,
			@RequestParam("customsCode") String customsCode, @RequestParam("ciqOrgCode") String ciqOrgCode,
			@RequestParam("recordGoodsInfoPack") String recordGoodsInfoPack, HttpServletRequest req,
			HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (customsPort != null && customsCode != null && ciqOrgCode != null && recordGoodsInfoPack != null) {
			statusMap = goodsRecordTransaction.merchantSendGoodsRecord(customsPort, customsCode, ciqOrgCode,
					recordGoodsInfoPack);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/findMerchantGoodsRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商戶查询商品备案信息")
	@RequiresRoles("Merchant")
	public String findMerchantGoodsRecordInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = goodsRecordTransaction.findMerchantGoodsRecordInfo(page, size);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 备案网关异步回馈备案商品信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/reNotifyMsg", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String reNotifyMsg(HttpServletRequest req, HttpServletResponse response) {
		logger.info("-----备案网关异步回馈备案商品信息---");
		Map<String, Object> statusMap = goodsRecordTransaction.updateGoodsRecordInfo(req);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/getMerchantGoodsRecordDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商戶查询单个商品备案详情")
	@RequiresRoles("Merchant")
	public String getMerchantGoodsRecordDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entGoodsNo") String entGoodsNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = goodsRecordTransaction.getMerchantGoodsRecordDetail(entGoodsNo);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 商户修改备案商品中的商品基本信息
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/editMerchantRecordGoodsDetailInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户修改备案商品中的商品基本信息")
	@RequiresRoles("Merchant")
	public String editMerchantRecordGoodsDetailInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		int type = 0;
		try {
			type = Integer.parseInt(req.getParameter("type"));
		} catch (Exception e) {
			logger.error("-------修改指定类型出错------");
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "type类型传递错误!");
			return JSONObject.fromObject(statusMap).toString();
		}
		// type 1-全部修改,2-修改商品信息(价格除外),3-只修改商品价格(商品基本信息不修改)
		if (type == 1 || type == 2 || type == 3) {
			statusMap = goodsRecordTransaction.editMerchantRecordGoodsDetailInfo(req, type);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/addAlreadyRecordGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户添加已备案商品信息")
	@RequiresRoles("Merchant")
	public String merchantAddAlreadyRecordGoodsInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = goodsRecordTransaction.merchantAddAlreadyRecordGoodsInfo(req);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/searchGoodsRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("根据指定信息搜索商品信息")
	@RequiresRoles("Merchant")
	public String searchGoodsRecordInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (page > 0 && size > 0) {
			statusMap = goodsRecordTransaction.searchGoodsRecordInfo(req, page, size);
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/batchAddNotRecordGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("批量添加未备案商品信息")
	@RequiresRoles("Merchant")
	public String batchAddNotRecordGoodsInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = goodsRecordTransaction.batchAddRecordGoodsInfo(req);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/merchantSendSingleGoodsRecord", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户批量或单个商品备案")
	@RequiresRoles("Merchant")
	public String merchantBatchOrSingleGoodsRecord(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("goodsRecordInfo") String goodsRecordInfo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = goodsRecordTransaction.merchantBatchOrSingleGoodsRecord(goodsRecordInfo);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 管理员修改备案商品状态(包括已备案商品状态)
	 * 
	 * @param req
	 * @param response
	 * @param entGoodsNo
	 *            商品自编号
	 * @param status
	 *            备案状态：1-备案中,2-备案成功,3-备案失败,4-未备案
	 * @return String
	 */
	@RequestMapping(value = "/managerEditGoodsRecordStatus", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员修改备案商品状态")
	public String managerEditGoodsRecordStatus(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entGoodsNo") String entGoodsNo, @RequestParam("status") int status) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (entGoodsNo != null && status == 2 || status == 3) {
			statusMap = goodsRecordTransaction.editGoodsRecordStatus(entGoodsNo, status);
			return JSONObject.fromObject(statusMap).toString();
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NOTICE.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
	}

	/**
	 * 商户修改备案商品信息(局限于未备案与备案失败的商品)
	 * 
	 * @param req
	 * @param response
	 * @param entGoodsNo
	 * @param status
	 * @return
	 */
	@RequestMapping(value = "/merchantEditGoodsRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("商户修改备案商品信息")
	public String merchantEditGoodsRecordInfo(HttpServletRequest req, HttpServletResponse response, int length) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = goodsRecordTransaction.merchantEditGoodsRecordInfo(req, length);
		return JSONObject.fromObject(statusMap).toString();
	}

	public static void main(String[] args) {
		JSONArray json = new JSONArray();
		Map<String, Object> map = new HashMap<>();
		map.put("entGoodsNo", "");
		map.put("eport", 1);
		map.put("customsCode", "");
		map.put("ciqOrgCode", "");
		json.add(map);
		String s = "GR5208_2017000795720";
		System.out.println(s.length());
	}
}
