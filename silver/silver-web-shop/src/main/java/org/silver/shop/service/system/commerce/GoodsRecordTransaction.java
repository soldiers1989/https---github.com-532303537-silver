package org.silver.shop.service.system.commerce;

import java.beans.IntrospectionException;
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
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.model.common.base.Country;
import org.silver.shop.model.common.base.Metering;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.service.common.base.CountryTransaction;
import org.silver.shop.service.common.base.MeteringTransaction;
import org.silver.util.ConvertUtils;
import org.silver.util.ExcelUtil;
import org.silver.util.FileUpLoadService;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONObject;

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

	// 商户发起商品备案
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

	// 处理网关异步回调信息
	public Map<String, Object> updateGoodsRecordInfo(HttpServletRequest req) {
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		datasMap.put("status", req.getParameter("status") + "");
		datasMap.put("msg", req.getParameter("msg") + "");
		datasMap.put("messageID", req.getParameter("messageID") + "");
		datasMap.put("entOrderNo", req.getParameter("entOrderNo") + "");
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
	public Map<String, Object> batchAddNotRecordGoodsInfo(HttpServletRequest req) {
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
			if (!file.delete()) {
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
		int flag = 0;

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
					if (StringEmptyUtils.isNotEmpty(value)) {
						shelfGName = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "上架商品名称不能为空!");
						errl.add(errMap);
						flag = 1;
						continue;
					}
					break;

				case 1:
					if (StringEmptyUtils.isNotEmpty(value)) {
						ncadCode = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "行邮税号不能为空!");
						errl.add(errMap);
						flag = 1;
						continue;
					}
					break;
				case 2:
					if (StringEmptyUtils.isNotEmpty(value)) {
						hsCode = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "HS编码不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				case 3:
					barCode = value;
					break;
				case 4:
					if (StringEmptyUtils.isNotEmpty(value)) {
						goodsName = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "商品名称不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				case 5:
					if (StringEmptyUtils.isNotEmpty(value)) {
						goodsStyle = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "型号规格不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				case 6:
					if (StringEmptyUtils.isNotEmpty(value)) {
						brand = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "品牌不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				case 7:
					if (StringUtils.isNotEmpty(value)) {
						gUnit = findUnit(value);
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "申报计量单位不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				case 8:
					if (StringUtils.isNotEmpty(value)) {
						stdUnit = findUnit(value);
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "第一法定计量单位不能为空!");
						errl.add(errMap);
						continue;
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
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "商品单价数值有误,请核实是否填写正确数字!");
						errl.add(errMap);
					}
					break;
				case 11:
					if (StringUtils.isNotEmpty(value)) {
						giftFlag = value;
						break;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "是否赠品不能为空!");
						errl.add(errMap);
						continue;
					}
				case 12:
					if (StringUtils.isNotEmpty(value)) {
						originCountry = value;
						break;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "原产国不能为空!");
						errl.add(errMap);
						continue;
					}
				case 13:
					if (StringUtils.isNotEmpty(value)) {
						quality = value;
						break;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "商品品质及说明不能为空!");
						errl.add(errMap);
						continue;
					}
				case 14:
					qualityCertify = value;
					break;
				case 15:
					if (StringUtils.isNotEmpty(value)) {
						manufactory = value;
						break;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "生产厂家或供应商不能为空!");
						errl.add(errMap);
						continue;
					}
				case 16:
					try {
						netWt = Double.parseDouble(value);
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "商品净重数值有误,请核实是否填写正确数字!");
						errl.add(errMap);
						continue;
					}
					break;
				case 17:
					try {
						grossWt = Double.parseDouble(value);
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + "商品毛重数值有误,请核实是否填写正确数字!");
						errl.add(errMap);
						continue;
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
			Map<String, Object> param = new HashMap<>();
			// 由于行循环是从第三行开始读取,所以SEQ要减1
			param.put("seq", r - 1);
			param.put("shelfGName", shelfGName);
			param.put("ncadCode", ncadCode);
			param.put("hsCode", hsCode);
			param.put("barCode", barCode);
			param.put("goodsName", goodsName);
			param.put("goodsStyle", goodsStyle);
			param.put("brand", brand);
			param.put("gUnit", gUnit);
			param.put("stdUnit", stdUnit);
			param.put("secUnit", secUnit);
			param.put("regPrice", regPrice);
			param.put("giftFlag", giftFlag);
			param.put("originCountry", originCountry);
			param.put("quality", quality);
			param.put("qualityCertify", qualityCertify);
			param.put("manufactory", manufactory);
			param.put("netWt", netWt);
			param.put("grossWt", grossWt);
			param.put("notes", notes);
			param.put("ingredient", ingredient);
			param.put("additiveflag", additiveflag);
			param.put("poisonflag", poisonflag);
			GoodsRecordDetail goodsInfo = null;
			try {
				goodsInfo = (GoodsRecordDetail) ConvertUtils.convertMap(GoodsRecordDetail.class, param);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IntrospectionException e) {
				e.printStackTrace();
			}
			Map<String, Object> item = goodsRecordService.batchCreateNotRecordGoods(goodsInfo, merchantId,
					merchantName);
			if (!"1".equals(item.get(BaseCode.STATUS.toString()))) {
				Map<String, Object> errMap = new HashMap<>();
				errMap.put("msg", "【未备案商品表】第" + (r + 1) + "行-->" + item.get("msg"));
				errl.add(errMap);
			}
		}
		recordContentMap.clear();
		recordContentMap.put(BaseCode.STATUS.toString(), 1);
		recordContentMap.put(BaseCode.MSG.toString(), "导入完成");
		recordContentMap.put(BaseCode.ERROR.toString(), errl);
		return recordContentMap;
	}

	/**
	 *  根据计量单位的中文名称或比代码查询编码
	 * @param value
	 * @return
	 */
	public  final String findUnit(String value) {
		if (StringEmptyUtils.isNotEmpty(value)) {
			List reList = meteringTransaction.findMetering();
			if (reList != null && !reList.isEmpty()) {
				if (StringUtil.isContainChinese(value)) {
					return getChineseByUnit(reList, value);
				}
				if (StringUtil.isNumeric(value)) {
					return getNumericByUnit(reList, value);
				}
			}
		}
		return null;
	}

	/**
	 * 根据编码查询计量单位
	 * @param reList
	 * @param value 
	 * @return
	 */
	private String getNumericByUnit(List reList, String value) {
		for (int i = 0; i < reList.size(); i++) {
			Metering metering = (Metering) reList.get(i);
			String code = metering.getMeteringCode();
			if (value.equals(code)) {
				return code;
			}
		}
		return "011";
	}

	/**
	 * 根据中文名称查询计量单位
	 * @param list
	 * @param value
	 * @return
	 */
	private String getChineseByUnit(List list, String value) {
		for (int i = 0; i < list.size(); i++) {
			Metering metering = (Metering) list.get(i);
			if (value.equals(metering.getMeteringName())) {
				return metering.getMeteringCode();
			}
		}
		return "011";
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

	// 商户批量或单个商品备案
	public Map<String, Object> merchantBatchOrSingleGoodsRecord(String goodsRecordInfo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return goodsRecordService.merchantBatchOrSingleGoodsRecord(goodsRecordInfo, merchantId, merchantName);
	}

	// 修改备案商品状态
	public Map<String, Object> editGoodsRecordStatus(String goodsPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		return goodsRecordService.editGoodsRecordStatus(managerId, managerName, goodsPack);
	}

	// 商户修改备案商品信息(局限于未备案与备案失败的商品)
	public Map<String, Object> merchantEditGoodsRecordInfo(HttpServletRequest req, int length) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		String[] str = new String[length];
		for (int i = 0; i < length; i++) {
			String value = req.getParameter(Integer.toString(i));
			str[i] = value.trim();
		}
		return goodsRecordService.merchantEditGoodsRecordInfo(merchantId, merchantName, str);
	}

	// 管理员查询商品备案信息
	public Map<String, Object> managerGetGoodsRecordInfo(HttpServletRequest req, int page, int size) {
		Map<String, Object> paramMap = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			paramMap.put(key, value);
		}
		paramMap.remove("page");
		paramMap.remove("size");
		return goodsRecordService.managerGetGoodsRecordInfo(paramMap, page, size);
	}

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
			readRecordGoodsInfo(0, excel, errl, merchantId, merchantName);
			excel.closeExcel();
			if (!file.delete()) {
				System.out.println("--------excel文件没有删除-----");
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

	private Map<String, Object> readRecordGoodsInfo(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String merchantId, String merchantName) {
		String goodsSerialNo = "";
		switch (sheet) {
		case 0:
			Map<String, Object> item = readRecordGoodsHeadSheed(sheet, excel, errl, merchantId, merchantName);
			goodsSerialNo = item.get("goodsSerialNo") + "";
			if (StringEmptyUtils.isNotEmpty(goodsSerialNo)) {
				return readRecordGoodsDetailSheed(1, excel, errl, merchantId, merchantName, goodsSerialNo);
			} else {
				Map<String, Object> errMap = new HashMap<>();
				errMap.put(BaseCode.MSG.toString(), "导入备案商品(子表)出错,请重试");
				errl.add(errMap);
				return item;
			}
		default:
			return null;
		}
	}

	/**
	 * 读取已备案商品详情
	 * 
	 * @param sheet
	 *            excel工作簿序号
	 * @param excel
	 * @param errl
	 *            错误信息
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @return
	 */
	private Map<String, Object> readRecordGoodsDetailSheed(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String merchantId, String merchantName, String goodsSerialNo) {
		Map<String, Object> recordContentMap = new HashMap<>();
		int seq = 0;// 商品序号
		String entGoodsNo = "";// 企业商品自编号
		String entGoodsNoSKU = "";// 电商平台自定义的商品货号（SKU）
		String eportGoodsNo = "";// 跨境公共平台商品备案申请号
		String ciqGoodsNo = "";// 检验检疫商品备案编号
		String cusGoodsNo = "";// 海关正式备案编号
		String emsNo = "";// 账册号
		String itemNo = "";// 项号
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

		String ebEntName = "";// 电商企业名称
		String DZKNNo = "";// 电商企业海关备案号(电子口岸)
		String ebEntNo = "";// 电商企业编号(智检)
		String marCode = "";// (启邦客户)商品归属商家代码

		for (int r = 2; r <= excel.getRowCount(sheet); r++) {
			if (excel.getColumnCount(sheet, r) == 0) {
				break;
			}
			for (int c = 0; c < excel.getColumnCount(sheet, r); c++) {
				String value = excel.getCell(sheet, r, c);
				if (c == 0 && "".equals(value)) {
					recordContentMap.put("status", 1);
					recordContentMap.put("msg", "导入完成");
					recordContentMap.put("err", errl);
					return recordContentMap;
				}
				switch (c) {
				case 0:
					try {
						seq = Integer.parseInt(value);
					} catch (Exception e) {
						logger.error(e);
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【已备案商品详情表】第" + (r + 1) + "行-->" + "商品序号错误");
						errl.add(errMap);
					}
					break;
				case 1:
					// 电商企业名称
					if (StringEmptyUtils.isNotEmpty(value)) {
						ebEntName = value;
						break;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->电商企业名称不能为空!");
						errl.add(errMap);
						continue;
					}
				case 2:
					// 电商企业海关备案号(电子口岸)
					if (StringEmptyUtils.isNotEmpty(value)) {
						DZKNNo = value;
						break;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->电商企业海关(电子口岸)备案号不能为空!");
						errl.add(errMap);
						continue;
					}
				case 3:
					// 电商企业编号(智检)
					ebEntNo = value;
					break;
				case 4:// (启邦客户)商品归属商家代码
					if (StringEmptyUtils.isNotEmpty(value)) {
						marCode = value;
					}
					break;
				case 5:
					if (StringEmptyUtils.isNotEmpty(value)) {
						entGoodsNoSKU = value;
					}
					break;
				case 6:
					if (StringEmptyUtils.isNotEmpty(value)) {
						entGoodsNo = value;
						break;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->企业商品自编号不能为空!");
						errl.add(errMap);
						continue;
					}

				case 7:

					if (StringEmptyUtils.isNotEmpty(value)) {
						ciqGoodsNo = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->企业商品自编号不能为空!");
						errl.add(errMap);
					}
					break;

				case 8:
					if (StringEmptyUtils.isNotEmpty(value)) {
						cusGoodsNo = value;
					} else {
						cusGoodsNo = "*";
					}
					break;

				case 9:
					if (StringEmptyUtils.isNotEmpty(value)) {
						emsNo = value;
					}
					break;

				case 10:
					if (StringEmptyUtils.isNotEmpty(value)) {
						itemNo = value;
					}
					break;

				case 11:

					if (StringEmptyUtils.isNotEmpty(value)) {
						ncadCode = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->行邮税号不能为空!");
						errl.add(errMap);
					}
					break;
				case 12:
					if (StringEmptyUtils.isNotEmpty(value)) {
						hsCode = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->HS编码不能为空!");
						errl.add(errMap);
					}
					break;
				case 13:
					if (StringEmptyUtils.isNotEmpty(value)) {
						barCode = value;
					}
					break;
				case 14:
					if (StringEmptyUtils.isNotEmpty(value)) {
						goodsName = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->商品名称不能为空!");
						errl.add(errMap);
					}
					break;
				case 15:
					if (StringEmptyUtils.isNotEmpty(value)) {
						goodsStyle = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【商品备案详细表】第" + (r + 1) + "行,---->商品规格不能为空!");
						errl.add(errMap);
					}
					break;
				case 16:
					if (StringEmptyUtils.isNotEmpty(value)) {
						brand = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->商品品牌不能为空!");
						errl.add(errMap);
					}
					break;
				case 17:
					if (StringEmptyUtils.isNotEmpty(value)) {
						gUnit = findUnit(value);
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->申报计量单位不能为空!");
						errl.add(errMap);
					}
					break;
				case 18:
					if (StringEmptyUtils.isNotEmpty(value)) {
						stdUnit = findUnit(value);
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->第一法定计量单位不能为空!");
						errl.add(errMap);
					}
					break;
				case 19:
					if (StringEmptyUtils.isNotEmpty(value)) {
						secUnit = findUnit(value);
					}
					break;
				case 20:
					try {
						regPrice = Double.parseDouble(value);
					} catch (Exception e) {
						logger.error(e);
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->商品单价数值错误!");
						errl.add(errMap);
					}
					break;
				case 21:
					if (StringEmptyUtils.isNotEmpty(value)) {
						giftFlag = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->是否赠品不能为空!");
						errl.add(errMap);
					}
					break;
				case 22:
					if (StringEmptyUtils.isNotEmpty(value)) {
						originCountry = findCountry(value);
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->原产国不能为空!");
						errl.add(errMap);
					}
					break;
				case 23:
					if (StringEmptyUtils.isNotEmpty(value)) {
						quality = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->商品品质及说明不能为空!");
						errl.add(errMap);
					}
					break;
				case 24:
					if (StringEmptyUtils.isNotEmpty(value)) {
						qualityCertify = value;
					}
					break;
				case 25:
					if (StringEmptyUtils.isNotEmpty(value)) {
						manufactory = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,---->生产厂家或供应商不能为空!");
						errl.add(errMap);
					}
					break;
				case 26:
					try {
						netWt = Double.parseDouble(value);
					} catch (Exception e) {
						logger.error(e);
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【已备案商品详情表】第" + (r + 1) + "行-->" + "商品净重数值有误");
						errl.add(errMap);
					}
					break;
				case 27:
					try {
						grossWt = Double.parseDouble(value);
					} catch (Exception e) {
						logger.error(e);
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【已备案商品详情表】第" + (r + 1) + "行-->" + "商品毛重数值有误");
						errl.add(errMap);
					}
					break;
				case 28:
					if (StringEmptyUtils.isNotEmpty(value)) {
						notes = value;
					}
					break;
				case 29:
					if (StringEmptyUtils.isNotEmpty(value)) {
						ingredient = value;
					}
					break;
				case 30:
					if (StringEmptyUtils.isNotEmpty(value)) {
						additiveflag = value;
					}
					break;
				case 31:
					if (StringEmptyUtils.isNotEmpty(value)) {
						poisonflag = value;
					}
					break;
				default:
					break;
				}
			}
			GoodsRecordDetail goodsRecordDetail = new GoodsRecordDetail();
			if (DZKNNo.trim().length() >= 16) {
				goodsRecordDetail.setDZKNNo(DZKNNo);
			} else {
				Map<String, Object> errMap = new HashMap<>();
				errMap.put("msg", "【已备案商品详情表】第" + (r + 1) + "行-->" + "电子口岸备案号错误,请核对信息!");
				errl.add(errMap);
				continue;
			}
			// 当企邦的归属商户Id与Sku都填写了则进行针对性操作
			if (StringEmptyUtils.isNotEmpty(marCode) && StringEmptyUtils.isNotEmpty(entGoodsNoSKU)) {
				entGoodsNo = entGoodsNo + "_" + marCode;
				JSONObject json = new JSONObject();
				json.put("marCode", marCode);
				json.put("SKU", entGoodsNoSKU);
				goodsRecordDetail.setSpareParams(json.toString());
			}
			goodsRecordDetail.setEbEntNo(ebEntNo);
			goodsRecordDetail.setEbEntName(ebEntName);
			goodsRecordDetail.setSeq(seq);
			goodsRecordDetail.setEntGoodsNo(entGoodsNo);
			goodsRecordDetail.setEportGoodsNo(eportGoodsNo);
			goodsRecordDetail.setCiqGoodsNo(ciqGoodsNo);
			goodsRecordDetail.setCusGoodsNo(cusGoodsNo);
			goodsRecordDetail.setEmsNo(emsNo);
			goodsRecordDetail.setItemNo(itemNo);
			goodsRecordDetail.setShelfGName(goodsName);
			goodsRecordDetail.setNcadCode(ncadCode);
			goodsRecordDetail.setHsCode(hsCode);
			goodsRecordDetail.setBarCode(barCode);
			goodsRecordDetail.setGoodsName(goodsName);
			goodsRecordDetail.setGoodsStyle(goodsStyle);
			goodsRecordDetail.setBrand(brand);
			goodsRecordDetail.setgUnit(gUnit);
			goodsRecordDetail.setStdUnit(stdUnit);
			goodsRecordDetail.setSecUnit(secUnit);
			goodsRecordDetail.setRegPrice(regPrice);
			goodsRecordDetail.setGiftFlag(giftFlag);
			goodsRecordDetail.setOriginCountry(originCountry);
			goodsRecordDetail.setQuality(quality);
			goodsRecordDetail.setQualityCertify(qualityCertify);
			goodsRecordDetail.setManufactory(manufactory);
			goodsRecordDetail.setNetWt(netWt);
			goodsRecordDetail.setGrossWt(grossWt);
			goodsRecordDetail.setNotes(notes);
			goodsRecordDetail.setIngredient(ingredient);
			goodsRecordDetail.setAdditiveflag(additiveflag);
			goodsRecordDetail.setPoisonflag(poisonflag);
			goodsRecordDetail.setGoodsSerialNo(goodsSerialNo);
			goodsRecordDetail.setGoodsMerchantId(merchantId);
			goodsRecordDetail.setGoodsMerchantName(merchantName);
			goodsRecordDetail.setCreateBy(merchantName);
			goodsRecordDetail.setStatus(2);
			goodsRecordDetail.setRecordFlag(0);

			Map<String, Object> reMap = goodsRecordService.checkEntGoodsNoRepeat(entGoodsNo);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				Map<String, Object> errMap = new HashMap<>();
				errMap.put(BaseCode.MSG.toString(),
						"【已备案商品详情表】第" + (r + 1) + "行,---->" + reMap.get(BaseCode.MSG.toString()));
				errl.add(errMap);
				continue;
			}
			Map<String, Object> item = goodsRecordService.batchCreateRecordGoodsDetail(goodsRecordDetail);
			if (!"1".equals(item.get(BaseCode.STATUS.toString()))) {
				Map<String, Object> errMap = new HashMap<>();
				errMap.put(BaseCode.MSG.toString(),
						"【已备案商品详情表】第" + (r + 1) + "行,----> " + item.get(BaseCode.MSG.toString()));
				errl.add(errMap);
			}

		}
		recordContentMap.clear();
		recordContentMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		recordContentMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		recordContentMap.put(BaseCode.ERROR.toString(), errl);
		return recordContentMap;
	}

	/**
	 * 读取已备案商品头部流水
	 * 
	 * @param sheet
	 * @param excel
	 * @param errl
	 * @param merchantId
	 * @param merchantName
	 * @return
	 */
	private Map<String, Object> readRecordGoodsHeadSheed(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String merchantId, String merchantName) {
		Map<String, Object> headMap = new HashMap<>();
		Map<String, Object> item = new HashMap<>();
		int customsPort = 0;
		String customsPortName = "";
		String customsCode = "";
		String customsName = "";
		String ciqOrgCode = "";
		String ciqOrgName = "";
		for (int r = 2; r <= excel.getRowCount(0); r++) {
			if (excel.getColumnCount(r) == 0) {
				break;
			}
			for (int c = 0; c < excel.getColumnCount(r); c++) {
				String value = excel.getCell(sheet, r, c);
				if (c == 0 && "".equals(value)) {
					headMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
					headMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
					headMap.put(BaseCode.ERROR.toString(), errl);
					headMap.put("goodsSerialNo", item.get("goodsSerialNo"));
					return headMap;
				}
				switch (c) {
				case 0:
					try {
						int intValue = Double.valueOf(value).intValue();
						if (intValue > 0) {
							customsPort = intValue;
						} else {
							Map<String, Object> errMap = new HashMap<>();
							errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,---->口岸代码错误!");
							errl.add(errMap);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,---->口岸代码错误!");
						errl.add(errMap);
					}
					break;
				case 1:
					if (StringEmptyUtils.isNotEmpty(value)) {
						customsPortName = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【商品备案详情表】第" + (r + 1) + "行,---->口岸名称不能为空!");
						errl.add(errMap);
					}
					break;
				case 2:
					if (StringEmptyUtils.isNotEmpty(value)) {
						customsCode = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,---->海关编码不能为空!");
						errl.add(errMap);
					}
					break;
				case 3:
					if (StringEmptyUtils.isNotEmpty(value)) {
						customsName = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,---->海关名称不能为空!");
						errl.add(errMap);
					}
					break;
				case 4:
					if (StringEmptyUtils.isNotEmpty(value)) {
						ciqOrgCode = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,---->检验检疫编码不能为空!");
						errl.add(errMap);
					}
					break;
				case 5:
					if (StringEmptyUtils.isNotEmpty(value)) {
						ciqOrgName = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,---->检验检疫名称不能为空!");
						errl.add(errMap);
					}
					break;
				default:
					break;
				}
			}
			item = goodsRecordService.batchCreateRecordGoodsHead(merchantId, merchantName, customsPort, customsPortName,
					customsCode, customsName, ciqOrgCode, ciqOrgName);
			if (!"1".equals(item.get(BaseCode.STATUS.toString()))) {
				Map<String, Object> errMap = new HashMap<>();
				errMap.put(BaseCode.MSG.toString(),
						"【已备案商品信息头】第" + (r + 1) + "行,----> " + item.get(BaseCode.MSG.toString()));
				errl.add(errMap);
			}
		}
		headMap.clear();
		headMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		headMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		headMap.put(BaseCode.ERROR.toString(), errl);
		headMap.put("goodsSerialNo", item.get("goodsSerialNo"));
		return headMap;
	}

	// 管理员查看商品备案详情
	public Map<String, Object> managerGetGoodsRecordDetail(String entGoodsNo) {
		return goodsRecordService.managerGetGoodsRecordDetail(entGoodsNo);
	}

	// 商户删除商品备案信息
	public Map<String, Object> merchantDeleteGoodsRecordInfo(String entGoodsNo) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		return goodsRecordService.merchantDeleteGoodsRecordInfo(merchantId, merchantName, entGoodsNo);
	}
}
