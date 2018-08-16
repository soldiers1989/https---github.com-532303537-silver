package org.silver.shop.controller.system.commerce;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.commerce.GoodsRecordTransaction;
import org.silver.util.ReturnInfoUtils;
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
	@ApiOperation("商户发起商品备案")
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
		return JSONObject.fromObject(goodsRecordTransaction.updateGoodsRecordInfo(req)).toString();
	}

	@RequestMapping(value = "/getMerchantGoodsRecordDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户查询单个商品备案详情")
	@RequiresRoles("Merchant")
	public String getMerchantGoodsRecordDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entGoodsNo") String entGoodsNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(goodsRecordTransaction.getGoodsRecordDetail(entGoodsNo)).toString();
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
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("type类型传递错误!")).toString();
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
	@ApiOperation("商户搜索备案商品信息")
	@RequiresRoles("Merchant")
	public String searchGoodsRecordInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(goodsRecordTransaction.searchGoodsRecordInfo(req, page, size)).toString();
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
		Map<String, Object> statusMap = goodsRecordTransaction.batchAddNotRecordGoodsInfo(req);
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
	@RequiresPermissions("goodsRecord:managerEditGoodsRecordStatus")
	@ApiOperation("管理员修改(审核)备案商品状态")
	public String managerEditGoodsRecordStatus(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("goodsPack") String goodsPack) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isNotEmpty(goodsPack)) {
			return JSONObject.fromObject(goodsRecordTransaction.editGoodsRecordStatus(goodsPack)).toString();
		} else {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("商品信息包不能为空,请核对信息!")).toString();
		}
	}

	/**
	 * 商户修改备案商品信息(仅限于未备案与备案失败、或已备案待审核状态)
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
		return JSONObject.fromObject(goodsRecordTransaction.merchantEditGoodsRecordInfo(req)).toString();
	}

	@RequestMapping(value = "/managerGetGoodsRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询商品备案信息")
	@RequiresPermissions("goodsRecord:managerGetGoodsRecordInfo")
	public String managerGetGoodsRecordInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = goodsRecordTransaction.managerGetGoodsRecordInfo(req, page, size);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/batchAddRecordGoodsInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("批量添加已备案商品信息")
	@RequiresRoles("Merchant")
	public String batchAddRecordGoodsInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(goodsRecordTransaction.batchAddRecordGoodsInfo(req)).toString();
	}

	@RequestMapping(value = "/managerGetGoodsRecordDetail", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员查询备案商品详情")
	@RequiresRoles("Manager")
	public String managerGetGoodsRecordDetail(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entGoodsNo") String entGoodsNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(goodsRecordTransaction.getGoodsRecordDetail(entGoodsNo)).toString();
	}

	@RequestMapping(value = "/merchantDeleteGoodsRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商戶删除商品备案信息")
	@RequiresRoles("Merchant")
	public String merchantDeleteGoodsRecordInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entGoodsNo") String entGoodsNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = goodsRecordTransaction.merchantDeleteGoodsRecordInfo(entGoodsNo);
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/managerUpdateGoodsRecordInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员修改商品备案信息")
	// @RequiresPermissions("goodsRecord:managerUpdateGoodsRecordInfo")
	public String managerUpdateGoodsRecordInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKes = req.getParameterNames();
		while (isKes.hasMoreElements()) {
			String key = isKes.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		if (datasMap.isEmpty()) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("参数不能为空!")).toString();
		}
		return JSONObject.fromObject(goodsRecordTransaction.managerUpdateGoodsRecordInfo(datasMap)).toString();
	}

	@RequestMapping(value = "/managerReviewerInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员审核商品信息")
	// @RequiresPermissions("goodsRecord:managerReviewerInfo")
	public String managerReviewerInfo(HttpServletRequest req, HttpServletResponse response, String entGoodsNo,
			String note, int reviewerFlag) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isNotEmpty(entGoodsNo)) {
			return JSONObject.fromObject(goodsRecordTransaction.managerReviewerInfo(entGoodsNo, note, reviewerFlag))
					.toString();
		} else {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("商品自编号不能为空！")).toString();
		}
	}

	@RequestMapping(value = "/merchantUpdateInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	// @RequiresPermissions("goodsRecord:managerReviewerInfo")
	@RequiresRoles("Merchant")
	@ApiOperation("商户修改商品备案信息")
	public String merchantUpdateInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKes = req.getParameterNames();
		while (isKes.hasMoreElements()) {
			String key = isKes.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return JSONObject.fromObject(goodsRecordTransaction.merchantUpdateInfo(datasMap)).toString();
	}
	
	@RequestMapping(value = "/merchantInitiateReview", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("商户发起备案商品审核")
	// @RequiresPermissions("goodsRecord:managerReviewerInfo")
	public String merchantInitiateReview(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		List<String> goodsIdList = new ArrayList<>();
		Enumeration<String> isKes = req.getParameterNames();
		while (isKes.hasMoreElements()) {
			String key = isKes.nextElement();
			String value = req.getParameter(key);
			goodsIdList.add(value);
		}
		return JSONObject.fromObject(goodsRecordTransaction.merchantInitiateReview(goodsIdList)).toString();
	}
	
}
