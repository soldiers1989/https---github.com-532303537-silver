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

	public boolean addMerchantGoodsBaseInfo(HttpServletRequest req) {
		boolean flag = false;
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		Map<String, Object> imgMap = fileUpLoadService.universalDoUpload(req, "/opt/www/img/merchant/goods/", ".jpg",
				false, 800, 800, null);
		String status = imgMap.get(BaseCode.STATUS.getBaseCode()) + "";
		if (status.equals("1")) {// 商品展示图片上传成功后
			Date date = new Date();
			String goodsId = "";
			String topStr = "YM_";
			Calendar cal = Calendar.getInstance();
			// 获取当前年份
			int year = cal.get(Calendar.YEAR);
			// 获取商品基本信息的自增ID
			Map<String, Object> reMap = goodsContentService.findGoodsId();
			// 商品自增ID
			String id = reMap.get(BaseCode.DATAS.getBaseCode()) + "";
			// 获取到当前时间戳
			Long current = System.currentTimeMillis();
			// 随机4位数
			int ramCount = (int) ((Math.random() * 9 + 1) * 1000);
			// 商品自编号为 YM_+(当前)年+五位数(数据库表自增ID)+时间戳(13位)+4位随机数
			goodsId = topStr + year + id + current + ramCount;
			List<Object> imgList = (List) imgMap.get(BaseCode.DATAS.getBaseCode());
			String merchantName = merchantInfo.getMerchantName();
			String goodsName = req.getParameter("goodsName");
			String goodsFirstType = req.getParameter("goodsFirstType");
			String goodsSecondType = req.getParameter("goodsSecondType");
			String goodsThirdType = req.getParameter("goodsThirdType");
			String goodsDetail = req.getParameter("goodsDetail");
			String goodsBrand = req.getParameter("goodsBrand");
			String goodsStyle = req.getParameter("goodsStyle");
			String goodsUnit = req.getParameter("goodsUnit");
			String goodsRegPrice = req.getParameter("goodsRegPrice");
			String goodsOriginCountry = req.getParameter("goodsOriginCountry");
			String goodsBarCode = req.getParameter("goodsBarCode");
			flag = goodsContentService.addGoodsBaseInfo(goodsId, merchantName, goodsName, imgList, goodsFirstType,
					goodsSecondType, goodsThirdType, goodsDetail, goodsBrand, goodsStyle, goodsUnit, goodsRegPrice,
					goodsOriginCountry, goodsBarCode, year, date);
		}
		return flag;
	}

	public Map<String, Object> findAllGoodsInfo(String goodsId, String goodsName, String startTime, String endTime,
			String ymYear, int page, int size) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		if (merchantName != null && !"".equals(merchantName.trim())) {
			List<Object> datasList = goodsContentService.blurryFindGoodsInfo(goodsId, merchantName, goodsName,
					startTime, endTime, ymYear, page, size);
			if (datasList != null && !datasList.isEmpty()) {
				datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
				datasMap.put(BaseCode.DATAS.getBaseCode(), datasList);
				datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			}
		} else {
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.PERMISSION_DENIED.getStatus());
			datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.PERMISSION_DENIED.getMsg());
		}
		return datasMap;
	}

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
		return false;
	}

	public Map<String,Object> deleteMerchantBaseInfo(String goodsId) {
		Map<String, Object> datasMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		boolean flag = goodsContentService.deleteBaseInfo(merchantName, goodsId);
		if (flag) {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		}else{
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		}
		return datasMap;
	}
	
	
}
