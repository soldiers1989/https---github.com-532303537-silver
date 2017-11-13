package org.silver.shop.service.system.commerce;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.FileUpLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

/**
 * 商品备案Transaction
 */
@Service("goodsRecordTransaction")
public class GoodsRecordTransaction {

	protected static final Logger logger = LogManager.getLogger();

	@Reference
	private GoodsRecordService goodsRecordService;
	@Autowired
	private FileUpLoadService fileUpLoadService;

	// 商户选择商品基本信息后,根据商品ID与商品名查询已发起备案的商品信息
	public Map<String, Object> getMerchantGoodsRecordInfo(String goodsInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		return goodsRecordService.getGoodsRecordInfo(merchantName, goodsInfoPack);
	}

	public Map<String, Object> merchantSendGoodsRecord(String customsPort, String customsCode, String ciqOrgCode,
			String recordGoodsInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		return goodsRecordService.merchantSendGoodsRecord(merchantName, merchantId, customsPort, customsCode,
				ciqOrgCode, recordGoodsInfoPack);
	}

	public Map<String, Object> findMerchantGoodsRecordInfo(String goodsId, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		return goodsRecordService.findAllGoodsRecordInfo(merchantId, goodsId, page, size);
	}

	// 处理网关异步回调信息
	public Map<String, Object> updateGoodsRecordInfo(Map<String, Object> datasMap) {
		return goodsRecordService.updateGoodsRecordInfo(datasMap);
	}

	// 商戶查询单个商品备案详情
	public Map<String, Object> getMerchantGoodsRecordDetail(String entGoodsNo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		return goodsRecordService.getMerchantGoodsRecordDetail(merchantId, merchantName, entGoodsNo);
	}

	// 商户修改备案商品中的商品基本信息
	public Map<String, Object> editMerchantRecordGoodsDetailInfo(HttpServletRequest req, int type) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> imgMap = null;
		String status = "";
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		imgMap = fileUpLoadService.universalDoUpload(req, "/opt/www/img/merchant/goods/", ".jpg", false, 800, 800,
				null);
		status = imgMap.get(BaseCode.STATUS.getBaseCode()) + "";
		if (!"1".equals(status)) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "上传图片失败,请重试!");
		}
		paramMap.put("entGoodsNo", req.getParameter("entGoodsNo"));
		paramMap.put("goodsName", req.getParameter("spareGoodsName"));
		paramMap.put("goodsFirstTypeId", req.getParameter("spareGoodsFirstTypeId"));
		paramMap.put("goodsFirstTypeName", req.getParameter("spareGoodsFirstTypeName"));
		paramMap.put("goodsSecondTypeId", req.getParameter("spareGoodsSecondTypeId"));
		paramMap.put("goodsSecondTypeName", req.getParameter("spareGoodsSecondTypeName"));
		paramMap.put("goodsThirdTypeId", req.getParameter("spareGoodsThirdTypeId"));
		paramMap.put("goodsThirdTypeName", req.getParameter("spareGoodsThirdTypeName"));
		paramMap.put("imgList", imgMap.get(BaseCode.DATAS.getBaseCode()));
		paramMap.put("goodsDetail", req.getParameter("spareGoodsDetail"));
		paramMap.put("goodsBrand", req.getParameter("spareGoodsBrand"));
		paramMap.put("goodsStyle", req.getParameter("spareGoodsStyle"));
		paramMap.put("goodsUnit", req.getParameter("spareGoodsUnit"));
		paramMap.put("goodsOriginCountry", req.getParameter("spareGoodsOriginCountry"));
		paramMap.put("goodsBarCode", req.getParameter("spareGoodsBarCode"));
		paramMap.put("regPrice", req.getParameter("regPrice"));
		paramMap.put("marketPrice", req.getParameter("marketPrice"));
		paramMap.put("freightPrice", req.getParameter("freightPrice"));
		return goodsRecordService.editMerchantRecordGoodsDetailInfo(merchantId, merchantName, paramMap, type);
	}
}
