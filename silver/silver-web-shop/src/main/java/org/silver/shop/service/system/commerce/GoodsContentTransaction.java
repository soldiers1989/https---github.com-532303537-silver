package org.silver.shop.service.system.commerce;

import java.util.Calendar;
import java.util.Date;
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
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.FileUpLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("goodsContentTransaction")
public class GoodsContentTransaction {

	@Reference
	private GoodsContentService goodsContentService;
	@Autowired
	private FileUpLoadService fileUpLoadService;

	//商户添加商品基本信息
	public Map<String,Object> addMerchantGoodsBaseInfo(HttpServletRequest req) {
		Map<String,Object> statusMap=new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, "/opt/www/img/merchant/"+merchantId+"/goods/", ".jpg",
				false, 800, 800, null);
		String status = imgMap.get(BaseCode.STATUS.getBaseCode()) + "";
		if (status.equals("1")) {// 商品展示图片上传成功后
			Date date = new Date();
			// 获取商品基本信息的自增ID
			Map<String, Object> reMap = goodsContentService.createGoodsId();
			String idStatus = reMap.get(BaseCode.STATUS.toString()) + "";
			if (idStatus.equals("1")) {//自增ID查询成功
				Map<String,Object> params = new HashMap<>();
				Calendar cl = Calendar.getInstance();
				int goodsYear = cl.get(Calendar.YEAR) ;
				List<Object> imgList = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
				params.put("goodsId",reMap.get(BaseCode.DATAS.toString()) + "");
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
				return goodsContentService.addGoodsBaseInfo(merchantId, merchantName, params,imgList, goodsYear, date);
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		return statusMap;
	}


	//商户查询商品基本信息
	public Map<String, Object> findAllGoodsInfo(String goodsId, String goodsName, String startTime, String endTime,
			String ymYear, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId= merchantInfo.getMerchantId();
		return goodsContentService.blurryFindGoodsInfo(goodsId, merchantName, goodsName,startTime, endTime, ymYear, page, size,merchantId);
	}

	
	//商户修改商品信息
	public boolean editMerchantGoodsBaseInfo(HttpServletRequest req) {
		boolean flag = false;
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, "/opt/www/img/merchant/goods/", ".jpg",
				false, 800, 800, null);
		String status = imgMap.get(BaseCode.STATUS.getBaseCode()) + "";
		if (status.equals("1")) {
			String goodsId = req.getParameter("goodsId");
			String goodsName = req.getParameter("goodsName");
			String goodsFirstType = req.getParameter("goodsFirstType");
			String goodsSecondType = req.getParameter("goodsSecondType");
			String goodsThirdType = req.getParameter("goodsThirdType");
			List<Object> imgList = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
			String goodsDetail = req.getParameter("goodsDetail");
			String goodsBrand = req.getParameter("goodsBrand");
			String goodsStyle = req.getParameter("goodsStyle");
			String goodsUnit = req.getParameter("goodsUnit");
			String goodsRegPrice = req.getParameter("goodsRegPrice");
			String goodsOriginCountry = req.getParameter("goodsOriginCountry");
			String goodsBarCode = req.getParameter("goodsBarCode");
			String merchantName = merchantInfo.getMerchantName();
			flag = goodsContentService.editGoodsBaseInfo(goodsId, goodsName, goodsFirstType, goodsSecondType,
					goodsThirdType, imgList, goodsDetail, goodsBrand, goodsStyle, goodsUnit, goodsRegPrice,
					goodsOriginCountry, goodsBarCode, merchantName);
		}
		return flag;
	}

	//删除商品基本信息
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

	//前台获取展示商品信息
	public Map<String,Object> getShowGoodsBaseInfo(int firstType, int secndType,int thirdType,int page,int size) {
		return goodsContentService.getShowGoodsBaseInfo(firstType,secndType,thirdType,page,size);
	}

	//前台根据商品ID查询商品基本信息
	public Map<String,Object> getOneGoodsBaseInfo(String entGoodsNo) {
		return goodsContentService.goodsContentService(entGoodsNo);
	}


	//商城根据商品类型搜索商品
	public Map<String, Object> getCategoryGoods(Integer firstType, Integer secndType, Integer thirdType, Integer page, int size) {
		return goodsContentService.getCategoryGoods(firstType,secndType,thirdType,page,size);
	}

	//
	public Map<String, Object> searchGoodsInfo(String goodsName, int page, int size) {
		return goodsContentService.searchGoodsInfo(goodsName,page,size);
	}

}
