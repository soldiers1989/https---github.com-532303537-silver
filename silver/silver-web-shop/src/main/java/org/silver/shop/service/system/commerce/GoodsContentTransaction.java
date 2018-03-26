package org.silver.shop.service.system.commerce;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.GoodsContentService;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.ConvertUtils;
import org.silver.util.FileUpLoadService;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("goodsContentTransaction")
public class GoodsContentTransaction {

	@Reference
	private GoodsContentService goodsContentService;
	@Autowired
	private FileUpLoadService fileUpLoadService;

	// 商户添加商品基本信息
	public Map<String, Object> addMerchantGoodsBaseInfo(HttpServletRequest req) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		Date date = new Date();
		// 获取商品基本信息的自增ID
		Map<String, Object> reMap = goodsContentService.createGoodsId();
		String idStatus = reMap.get(BaseCode.STATUS.toString()) + "";
		if (idStatus.equals("1")) {// 自增ID查询成功
			Map<String, Object> params = new HashMap<>();
			Calendar cl = Calendar.getInstance();
			int goodsYear = cl.get(Calendar.YEAR);
			params.put("mainImg", req.getParameter("mainImg") + "");
			params.put("goodsId", reMap.get(BaseCode.DATAS.toString()) + "");
			params.put("goodsName", req.getParameter("goodsName"));
			params.put("goodsFirstTypeId", req.getParameter("goodsFirstTypeId"));
			params.put("goodsFirstTypeName", req.getParameter("goodsFirstTypeName"));
			params.put("goodsSecondTypeId", req.getParameter("goodsSecondTypeId"));
			params.put("goodsSecondTypeName", req.getParameter("goodsSecondTypeName"));
			params.put("goodsThirdTypeId", req.getParameter("goodsThirdTypeId"));
			params.put("goodsThirdTypeName", req.getParameter("goodsThirdTypeName"));
			params.put("goodsDetail", req.getParameter("goodsDetail"));
			params.put("goodsBrand", req.getParameter("goodsBrand"));
			params.put("goodsStyle", req.getParameter("goodsStyle"));
			params.put("goodsUnit", req.getParameter("goodsUnit"));
			params.put("goodsRegPrice", req.getParameter("goodsRegPrice"));
			params.put("goodsOriginCountry", req.getParameter("goodsOriginCountry"));
			params.put("goodsBarCode", req.getParameter("goodsBarCode"));
			return goodsContentService.addGoodsBaseInfo(merchantId, merchantName, params, goodsYear, date);
		}
		return ReturnInfoUtils.errorInfo("服务器查询ID繁忙,请重试!");
	}

	// 商户查询商品基本信息
	public Map<String, Object> findAllGoodsInfo(int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		return goodsContentService.findAllGoodsInfo(merchantName, page, size, merchantId);
	}

	// 商户修改商品信息
	public Map<String, Object> editMerchantGoodsBaseInfo(HttpServletRequest req) {
		Map<String, Object> datasMap = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return goodsContentService.editGoodsBaseInfo(datasMap,  merchantName, merchantId);
	}

	// 删除商品基本信息
	public Map<String, Object> deleteMerchantBaseInfo(String goodsId) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		boolean flag = goodsContentService.deleteBaseInfo(merchantName, goodsId);
		if (flag) {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		}
		return datasMap;
	}

	// 前台获取展示商品信息
	public Map<String, Object> getShowGoodsBaseInfo(Map<String,Object> datasMap, int page, int size) {
		return goodsContentService.getShowGoodsBaseInfo(datasMap, page, size);
	}

	// 前台根据商品ID查询商品基本信息
	public Map<String, Object> getOneGoodsBaseInfo(String entGoodsNo) {
		return goodsContentService.goodsContentService(entGoodsNo);
	}


	// 根据指定信息搜索商品信息
	public Map<String, Object> searchMerchantGoodsDetailInfo(HttpServletRequest req, int page, int size) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Enumeration<String> iskey = req.getParameterNames();
		while (iskey.hasMoreElements()) {
			String key = iskey.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return goodsContentService.searchMerchantGoodsDetailInfo(merchantId, merchantName, datasMap, page, size);
	}

	// 商户查询商品基本信息详情
	public Map<String, Object> merchantGetGoodsBaseInfo(String goodsId) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return goodsContentService.merchantGetGoodsBaseInfo(merchantId, merchantName, goodsId);
	}

}
