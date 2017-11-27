package org.silver.shop.service.system.commerce;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.zookeeper.data.Stat;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.model.common.base.Country;
import org.silver.shop.model.common.base.Metering;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.service.common.base.CountryTransaction;
import org.silver.shop.service.common.base.MeteringTransaction;
import org.silver.shop.utils.ExcelUtil;
import org.silver.util.FileUpLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.StringUtils;
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
	@Autowired
	private MeteringTransaction meteringTransaction;
	@Autowired
	private CountryTransaction countryTransaction;

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
	public Map<String, Object> updateGoodsRecordInfo(HttpServletRequest req) {
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key) + "";
			datasMap.put(key, value);
		}
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
		imgMap = fileUpLoadService.universalDoUpload(req, "/opt/www/img/merchant/" + merchantId + "/goods/", ".jpg",
				false, 800, 800, null);
		status = imgMap.get(BaseCode.STATUS.getBaseCode()) + "";
		if (!"1".equals(status)) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "上传图片失败,请重试!");
		}
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			paramMap.put(key, value);
		}
		paramMap.put("imgList", imgMap.get(BaseCode.DATAS.getBaseCode()));
		return goodsRecordService.editMerchantRecordGoodsDetailInfo(merchantId, merchantName, paramMap, type);
	}

	// 商户添加已备案商品信息
	public Map<String, Object> merchantAddAlreadyRecordGoodsInfo(HttpServletRequest req) {
		Map<String, Object> paramMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			paramMap.put(key, value);
		}
		return goodsRecordService.merchantAddAlreadyRecordGoodsInfo(merchantId, merchantName, paramMap);
	}

	public Map<String, Object> searchGoodsRecordInfo(HttpServletRequest req, int page, int size) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Map<String, Object> param = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			param.put(key, value);
		}
		return goodsRecordService.searchGoodsRecordInfo(merchantId, merchantName, param, page, size);
	}

	// 批量添加未备案商品信息
	public Map<String, Object> batchAddRecordGoodsInfo(HttpServletRequest req) {
		List<Map<String, Object>> errl = new ArrayList<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/RecordGoodsAdd-excel/", ".xls", false,
				400, 400, null);
		if ((int) reqMap.get(BaseCode.STATUS.toString()) == 1) {
			List<String> list = (List<String>) reqMap.get(BaseCode.DATAS.toString());
			File file = new File("/RecordGoodsAdd-excel/" + list.get(0));
			ExcelUtil excel = new ExcelUtil();
			excel.open(file);
			batchAddNotRecordGoodsInfo(excel, errl, merchantId, merchantName);
			excel.closeExcel();
			if(!file.delete()){
				System.out.println("----------文件没有删除-----");
			}
			reqMap.clear();
			reqMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reqMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			reqMap.put(BaseCode.ERROR.toString(), errl);
			return reqMap;
		}
		reqMap.put(BaseCode.STATUS.toString(), StatusCode.UNKNOWN.toString());
		reqMap.put(BaseCode.MSG.toString(), "导入文件出错，请重试");
		return reqMap;
	}

	private Map<String, Object> batchAddNotRecordGoodsInfo(ExcelUtil excel, List<Map<String, Object>> errl,
			String merchantId, String merchantName) {
		Map<String, Object> recordContentMap = new HashMap<>();
		String shelfGName = ""; // 上架品名 在电商平台上的商品名称
		String ncadCode = ""; // 行邮税号 商品综合分类表(NCAD)
		String hsCode = ""; // HS编码
		String barCode = ""; // 商品条形码 允许包含字母和数字
		String goodsName = ""; // 商品名称 商品中文名称
		String goodsStyle = ""; // 型号规格
		String brand = ""; // 商品品牌
		String gUnit = ""; // 申报计量单位 计量单位代码表(UNIT)
		String stdUnit = ""; // 第一法定计量单位 参照公共代码表
		String secUnit = ""; // 第二法定计量单位 参照公共代码表
		Double regPrice = 0.0; // 单价 境物品：指无税的销售价格, RMB价格
		String giftFlag = ""; // 是否赠品 0-是，1-否，默认否
		String originCountry = "";// 原产国 参照国别代码表
		String quality = ""; // 商品品质及说明 用文字描述
		String qualityCertify = "";// 品质证明说明 商品品质证明性文字说明
		String manufactory = ""; // 生产厂家或供应商 此项填生成厂家或供应商名称
		Double netWt = 0.0; // 净重 单位KG
		Double grossWt = 0.0; // 毛重 单位KG
		String notes = ""; // 备注
		String ingredient = "";// 成分(商品向南沙国检备案必填)
		String additiveflag = "";// 超范围使用食品添加剂
		String poisonflag = "";// 含有毒害物质

		for (int r = 2; r <= excel.getRowCount(); r++) {
			if (excel.getColumnCount(r) == 0) {
				break;
			}
			for (int c = 0; c < excel.getColumnCount(); c++) {
				String value = excel.getCell(r, c);
				if (c == 0 && "".equals(value)) {
					recordContentMap.put("status", 1);
					recordContentMap.put("msg", "导入完成");
					recordContentMap.put("err", errl);
					return recordContentMap;
				}
				switch (c) {
				case 0:
					shelfGName = value;
					break;
				case 1:
					ncadCode = value;
					break;
				case 2:
					hsCode = value;
					break;
				case 3:
					barCode = value;
					break;
				case 4:
					goodsName = value;
					break;
				case 5:
					goodsStyle = value;
					break;
				case 6:
					brand = value;
					break;
				case 7:
					if (StringUtils.isNotEmpty(value)) {
						gUnit = findUnit(value);
					} else {
						gUnit = value;
					}
					break;
				case 8:
					if (StringUtils.isNotEmpty(value)) {
						stdUnit = findUnit(value);
					} else {
						stdUnit = value;
					}
					break;
				case 9:
					if (StringUtils.isNotEmpty(value)) {
						secUnit = findUnit(value);
					} else {
						secUnit = value;
					}
					break;
				case 10:
					try {
						regPrice = Double.parseDouble(value);
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "商品单价数值有误");
						errl.add(errMap);
					}
					break;
				case 11:
					giftFlag = value;
					break;
				case 12:
					originCountry = findCountry(value);
					break;
				case 13:
					quality = value;
					break;
				case 14:
					qualityCertify = value;
					break;
				case 15:
					manufactory = value;
					break;
				case 16:
					try {
						netWt = Double.parseDouble(value);
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "商品净重数值有误");
						errl.add(errMap);
					}
					break;
				case 17:
					try {
						grossWt = Double.parseDouble(value);
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "商品毛重数值有误");
						errl.add(errMap);
					}
					break;
				case 18:
					notes = value;
					break;
				case 19:
					ingredient = value;
					break;
				case 20:
					additiveflag = value;
					break;
				case 21:
					poisonflag = value;
					break;
				default:
					break;
				}
			}
			Map<String, Object> item = goodsRecordService.batchCreateNotRecordGoods(r, shelfGName, ncadCode, hsCode,
					barCode, goodsName, goodsStyle, brand, gUnit, stdUnit, secUnit, regPrice, giftFlag, originCountry,
					quality, qualityCertify, manufactory, netWt, grossWt, notes, merchantId, merchantName,ingredient,additiveflag,poisonflag);
			if (!"1".equals(item.get(BaseCode.STATUS.toString()))) {
				Map<String, Object> errMap = new HashMap<>();
				errMap.put("msg", "【订单工作表】第" + (r + 1) + "行-->" + item.get("msg"));
				errl.add(errMap);
			}
		}
		recordContentMap.clear();
		recordContentMap.put(BaseCode.STATUS.toString(), 1);
		recordContentMap.put(BaseCode.MSG.toString(), "导入完成");
		recordContentMap.put(BaseCode.ERROR.toString(), errl);
		return recordContentMap;
	}

	// 根据计量单位的中文名称查询编码
	private String findUnit(String value) {
		List reList = meteringTransaction.findMetering();
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Metering metering = (Metering) reList.get(i);
				if (value.equals(metering.getMeteringName())) {
					return metering.getMeteringCode();
				}
			}
		}
		return null;
	}

	// 根据国家中文名称查询国家编码
	private String findCountry(String value) {
		List reList = countryTransaction.findAllCountry();
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Country country = (Country) reList.get(i);
				if (value.equals(country.getCountryName())) {
					return country.getCountryCode();
				}
			}
		}
		return null;
	}
}
