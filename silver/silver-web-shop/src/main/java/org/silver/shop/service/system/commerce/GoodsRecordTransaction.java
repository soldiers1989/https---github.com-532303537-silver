package org.silver.shop.service.system.commerce;

import java.beans.IntrospectionException;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
import org.silver.shop.service.system.manual.ManualService;
import org.silver.shop.utils.ExcelBufferUtils;
import org.silver.shop.utils.InvokeTaskUtils;
import org.silver.shop.utils.RedisInfoUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.ConvertUtils;
import org.silver.util.DateUtil;
import org.silver.util.ExcelUtil;
import org.silver.util.FileUpLoadService;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;
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
	@Autowired
	private ExcelBufferUtils excelBufferUtils;
	@Autowired
	private InvokeTaskUtils invokeTaskUtils;
	/**
	 * 错误标识
	 */
	private static final String ERROR = "error";

	// 商户选择商品基本信息后,根据商品ID与商品名查询已发起备案的商品信息
	public Map<String, Object> getMerchantGoodsRecordInfo(String goodsInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		return goodsRecordService.getGoodsRecordInfo(merchantName, goodsInfoPack);
	}

	// 商户发起商品备案
	public Map<String, Object> merchantSendGoodsRecord(String customsPort, String customsCode, String ciqOrgCode,
			String recordGoodsInfoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		return goodsRecordService.merchantSendGoodsRecord(merchantName, merchantId, customsPort, customsCode,
				ciqOrgCode, recordGoodsInfoPack);
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

	//
	public Map<String, Object> getGoodsRecordDetail(String entGoodsNo) {
		return goodsRecordService.getGoodsRecordDetail(entGoodsNo);
	}

	// 商户修改备案商品中的商品基本信息
	public Map<String, Object> editMerchantRecordGoodsDetailInfo(HttpServletRequest req, int type) {
		Map<String, Object> paramMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();

		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			paramMap.put(key, value);
		}
		return goodsRecordService.editMerchantRecordGoodsDetailInfo(merchantId, merchantName, paramMap, type);
	}

	// 商户添加已备案商品信息
	public Map<String, Object> merchantAddAlreadyRecordGoodsInfo(HttpServletRequest req) {
		Map<String, Object> paramMap = new HashMap<>();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
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
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Map<String, Object> param = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			param.put(key, value);
		}
		param.remove("page");
		param.remove("size");
		return goodsRecordService.searchGoodsRecordInfo(merchantId, merchantName, param, page, size);
	}

	// 批量添加未备案商品信息
	public Map<String, Object> batchAddNotRecordGoodsInfo(HttpServletRequest req) {
		Vector errl = new Vector();
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/RecordGoodsAdd-excel/", ".xls", false,
				400, 400, null);
		if ((int) reqMap.get(BaseCode.STATUS.toString()) == 1) {
			List<String> list = (List<String>) reqMap.get(BaseCode.DATAS.toString());
			File file = new File("/RecordGoodsAdd-excel/" + list.get(0));
			ExcelUtil excel = new ExcelUtil();
			excel.open(file);
			// 有数据的总行数
			int realRowCount = ManualService.excelRealRowCount(excel.getRowCount(0), excel);
			// 用于前台区查询缓存
			String serialNo = "notRecordGoods_" + SerialNoUtils.getSerialNo("notRecordGoods");
			// readQBSheet(0, excel, errl, merchantId, serialNo);
			// 多了一行说明
			realRowCount += 1;
			invokeTaskUtils.startTask(3, realRowCount, file, merchantId, serialNo, merchantName);
			// batchAddNotRecordGoodsInfo(excel, errl, merchantId,
			// merchantName);
			excel.closeExcel();
			if (!file.delete()) {
				System.out.println("----------文件没有删除-----");
			}
			reqMap.clear();
			reqMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reqMap.put(BaseCode.MSG.toString(), "执行成功,正在读取数据.......");
			reqMap.put(BaseCode.ERROR.toString(), errl);
			reqMap.put("serialNo", serialNo);
			return reqMap;
		}
		reqMap.put(BaseCode.STATUS.toString(), StatusCode.UNKNOWN.toString());
		reqMap.put(BaseCode.MSG.toString(), "导入文件出错，请重试");
		return reqMap;
	}

	public void batchAddNotRecordGoodsInfo(ExcelUtil excel, List<Map<String, Object>> errl,
			Map<String, Object> params) {
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
		String regPrice = ""; // 单价 境物品：指无税的销售价格, RMB价格
		String giftFlag = ""; // 是否赠品 0-是，1-否，默认否
		String originCountry = "";// 原产国 参照国别代码表
		String quality = ""; // 商品品质及说明 用文字描述
		String qualityCertify = "";// 品质证明说明 商品品质证明性文字说明
		String manufactory = ""; // 生产厂家或供应商 此项填生成厂家或供应商名称
		String netWt = ""; // 净重 单位KG
		String grossWt = ""; // 毛重 单位KG
		String notes = ""; // 备注
		String ingredient = "";// 成分(商品向南沙国检备案必填)
		String additiveflag = "";// 超范围使用食品添加剂
		String poisonflag = "";// 含有毒害物质
		int totalColumnCount = excel.getColumnCount(0);
		String merchantId = params.get("merchantId") + "";
		int startCount = Integer.parseInt(params.get("startCount") + "");
		int endCount = Integer.parseInt(params.get("endCount") + "");
		String merchantName = params.get("merchantName") + "";
		// 未备案商品导入key
		params.put("name", "notRGImport");
		for (int r = startCount; r <= endCount; r++) {
			if (excel.getColumnCount(r) == 0) {
				break;
			}
			for (int c = 0; c < totalColumnCount; c++) {
				String value = excel.getCell(0, r, c);
				if (c == 0 && "".equals(value)) {
					break;
				}
				switch (c) {
				case 1:
					if (StringEmptyUtils.isNotEmpty(value)) {
						shelfGName = value;
					}
					break;
				case 2:
					if (StringEmptyUtils.isNotEmpty(value)) {
						ncadCode = value;
					}
					break;
				case 3:
					if (StringEmptyUtils.isNotEmpty(value)) {
						hsCode = value;
					}
					break;
				case 4:
					barCode = value;
					break;
				case 5:
					if (StringEmptyUtils.isNotEmpty(value)) {
						goodsName = value;
					}
					break;
				case 6:
					if (StringEmptyUtils.isNotEmpty(value)) {
						goodsStyle = value;
					}
					break;
				case 7:
					if (StringEmptyUtils.isNotEmpty(value)) {
						brand = value;
					}
					break;
				case 8:
					if (StringUtils.isNotEmpty(value)) {
						gUnit = findUnit(value);
					}
					break;
				case 9:
					if (StringUtils.isNotEmpty(value)) {
						stdUnit = findUnit(value);
					}
					break;
				case 10:
					if (StringUtils.isNotEmpty(value)) {
						secUnit = findUnit(value);
					} else {
						secUnit = value;
					}
					break;
				case 11:
					regPrice = value;
					break;
				case 12:
					if (StringUtils.isNotEmpty(value)) {
						giftFlag = value;
					}
					break;
				case 13:
					if (StringUtils.isNotEmpty(value)) {
						originCountry = findCountry(value);
					}
					break;
				case 14:
					if (StringUtils.isNotEmpty(value)) {
						quality = value;
					}
					break;
				case 15:
					qualityCertify = value;
					break;
				case 16:
					if (StringUtils.isNotEmpty(value)) {
						manufactory = value;
					}
					break;
				case 17:
					netWt = value;
					break;
				case 18:
					grossWt = value;
					break;
				case 19:
					notes = value;
					break;
				case 20:
					ingredient = value;
					break;
				case 21:
					additiveflag = value;
					break;
				case 22:
					poisonflag = value;
					break;
				default:
					break;
				}
			}
			// 查询缓存中商品自编号自增Id
			int count = SerialNoUtils.getSerialNo("goods");
			String entGoodsNo = SerialNoUtils.getSerialNo("GR", count);
			Map<String, Object> param = new HashMap<>();
			// 由于行循环是从第三行开始读取,所以SEQ要减1
			param.put("seq", r - 1);
			param.put("entGoodsNo", entGoodsNo);
			param.put("shelfGName", shelfGName);
			param.put("ncadCode", ncadCode);
			param.put("hsCode", hsCode);
			if (StringEmptyUtils.isEmpty(barCode)) {
				param.put("barCode", entGoodsNo);
			} else {
				param.put("barCode", barCode);
			}
			param.put("goodsName", goodsName);
			param.put("goodsStyle", goodsStyle);
			param.put("brand", brand);
			param.put("gUnit", gUnit);
			param.put("stdUnit", stdUnit);
			param.put("secUnit", secUnit);
			try {
				param.put("regPrice", Double.parseDouble(regPrice));
			} catch (Exception e) {
				e.printStackTrace();
				String msg = "【表格】第" + (r + 1) + "行-->" + "商品单价数值有误,请核实是否填写正确数字!";
				RedisInfoUtils.commonErrorInfo(msg, errl, ERROR, params);
				continue;
			}
			param.put("giftFlag", giftFlag);
			param.put("originCountry", originCountry);
			param.put("quality", quality);
			param.put("qualityCertify", qualityCertify);
			param.put("manufactory", manufactory);
			try {
				param.put("netWt", Double.parseDouble(netWt));
			} catch (Exception e) {
				e.printStackTrace();
				String msg = "【表格】第" + (r + 1) + "行-->" + "商品净重数值有误,请核实是否填写正确数字!";
				RedisInfoUtils.commonErrorInfo(msg, errl, ERROR, params);
				continue;
			}
			try {
				param.put("grossWt", Double.parseDouble(grossWt));
			} catch (Exception e) {
				e.printStackTrace();
				String msg = "【表格】第" + (r + 1) + "行-->" + "商品毛重数值有误,请核实是否填写正确数字!";
				RedisInfoUtils.commonErrorInfo(msg, errl, ERROR, params);
				continue;
			}
			param.put("notes", notes);
			param.put("ingredient", ingredient);
			param.put("additiveflag", additiveflag);
			param.put("poisonflag", poisonflag);
			Map<String, Object> reGoodsMap = checkGoodsInfo(JSONObject.fromObject(param));
			if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
				String msg = "【表格】第" + (r + 1) + "行-->" + reGoodsMap.get(BaseCode.MSG.toString()) + "";
				RedisInfoUtils.commonErrorInfo(msg, errl, ERROR, params);
				continue;
			}
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
				String msg = "【表格】第" + (r + 1) + "行-->" + item.get("msg");
				RedisInfoUtils.commonErrorInfo(msg, errl, ERROR, params);
				continue;
			}
			excelBufferUtils.writeRedis(errl, params);
		}
		excel.closeExcel();
		excelBufferUtils.writeCompletedRedis(errl, params);
	}

	/**
	 * 校验商品信息是否都已按照要求填写
	 * 
	 * @param jsonData
	 * @return
	 */
	private Map<String, Object> checkGoodsInfo(JSONObject jsonData) {
		JSONArray datas = new JSONArray();
		datas.add(jsonData);
		List<String> noNullKeys = new ArrayList<>();
		// noNullKeys.add("shelfGName");
		noNullKeys.add("ncadCode");
		noNullKeys.add("hsCode");
		// noNullKeys.add("BarCode");
		noNullKeys.add("barCode");
		noNullKeys.add("goodsName");
		noNullKeys.add("goodsStyle");
		noNullKeys.add("gUnit");
		noNullKeys.add("stdUnit");
		noNullKeys.add("regPrice");
		noNullKeys.add("giftFlag");
		noNullKeys.add("originCountry");
		noNullKeys.add("quality");
		noNullKeys.add("manufactory");
		noNullKeys.add("netWt");
		noNullKeys.add("grossWt");
		return CheckDatasUtil.changeMsg(datas, noNullKeys);
	}

	/**
	 * 根据计量单位的中文名称或比代码查询编码
	 * 
	 * @param value
	 * @return
	 */
	public final String findUnit(String value) {
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
	 * 
	 * @param reList
	 * @param value
	 *            计量单位编码
	 * @return String
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
	 * 
	 * @param list
	 * @param name
	 *            计量单位中文名称
	 * @return String
	 */
	private String getChineseByUnit(List list, String name) {
		for (int i = 0; i < list.size(); i++) {
			Metering metering = (Metering) list.get(i);
			if (name.equals(metering.getMeteringName())) {
				return metering.getMeteringCode();
			}
		}
		return "011";
	}

	/**
	 * 根据国家中文名称查询国家编码
	 * 
	 * @param value
	 *            国家中文名称
	 * @return String 国家对应编码
	 */
	private String findCountry(String value) {
		Map<String, Object> reMap = countryTransaction.findAllCountry();
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return null;
		}
		List<Country> reList = (List<Country>) reMap.get(BaseCode.DATAS.toString());
		if (reList != null && !reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Country country = reList.get(i);
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
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return goodsRecordService.merchantBatchOrSingleGoodsRecord(goodsRecordInfo, merchantId, merchantName);
	}

	// 修改备案商品状态
	public Map<String, Object> editGoodsRecordStatus(String goodsPack) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		return goodsRecordService.editGoodsRecordStatus(managerInfo, goodsPack);
	}

	// 商户修改备案商品信息(局限于未备案与备案失败的商品)
	public Map<String, Object> merchantEditGoodsRecordInfo(HttpServletRequest req) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		return goodsRecordService.merchantEditGoodsRecordInfo(merchantId, merchantName, params);
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
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/RecordGoodsAdd-excel/", ".xls", false,
				400, 400, null);
		if ((int) reqMap.get(BaseCode.STATUS.toString()) == 1) {
			List<String> list = (List<String>) reqMap.get(BaseCode.DATAS.toString());
			File file = new File("/RecordGoodsAdd-excel/" + list.get(0));
			ExcelUtil excel = new ExcelUtil();
			excel.open(file);
			String serialNo = "RecordGoods_" + SerialNoUtils.getSerialNo("RecordGoods");
			return readRecordGoodsInfo(0, excel, errl, merchantId, merchantName);
			/*
			 * excel.closeExcel(); if (!file.delete()) {
			 * System.out.println("--------excel文件没有删除-----"); } reqMap.clear();
			 * reqMap.put(BaseCode.STATUS.toString(),
			 * StatusCode.SUCCESS.getStatus());
			 * reqMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			 * reqMap.put(BaseCode.ERROR.toString(), errl); return reqMap;
			 */
		}
		return ReturnInfoUtils.errorInfo("导入文件出错，请重试!");
	}

	private Map<String, Object> readRecordGoodsInfo(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String merchantId, String merchantName) {
		try {
			Map<String, Object> item = readRecordGoodsHeadSheed(sheet, excel, errl, merchantId, merchantName);
			if (!"1".equals(item.get(BaseCode.STATUS.toString()))) {
				return item;
			} else {
				String goodsSerialNo = item.get("goodsSerialNo") + "";
				// 有数据的总行数
				// int realRowCount =
				// ManualService.excelRealRowCount(excel.getRowCount(0), excel);
				// invokeTaskUtils.startTask(1, realRowCount, file, merchantId,
				// serialNo, merchantName);
				return readRecordGoodsDetailSheed(1, excel, errl, merchantId, merchantName, goodsSerialNo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("导入失败，请重试!");
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
					return ReturnInfoUtils.errorInfo(errl);
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
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,-->电商企业名称不能为空!");
						errl.add(errMap);
						continue;
					}
				case 2:
					// 电商企业海关备案号(电子口岸)
					if (StringEmptyUtils.isNotEmpty(value) && value.trim().length() == 16) {
						DZKNNo = value;
						break;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,-->电商企业海关(电子口岸)备案号错误!");
						errl.add(errMap);
						continue;
					}
				case 3:
					// 电商企业编号(智检)
					ebEntNo = value;
					break;
				case 4:// (启邦客户)商品归属商家代码
					marCode = value;
					break;
				case 5:
					entGoodsNoSKU = value;
					break;
				case 6:
					entGoodsNo = value;
					break;
				case 7:
					ciqGoodsNo = value;
					break;
				case 8:
					if (StringEmptyUtils.isNotEmpty(value)) {
						cusGoodsNo = value;
					} else {
						cusGoodsNo = "*";
					}
					break;
				case 9:
					emsNo = value;
					break;
				case 10:
					itemNo = value;
					break;
				case 11:
					ncadCode = value;
					break;
				case 12:
					hsCode = value;
					break;
				case 13:
					barCode = value;
					break;
				case 14:
					goodsName = value;
					break;
				case 15:
					goodsStyle = value;
					break;
				case 16:
					brand = value;
					break;
				case 17:
					gUnit = findUnit(value);
					break;
				case 18:
					stdUnit = findUnit(value);
					break;
				case 19:
					secUnit = findUnit(value);
					break;
				case 20:
					try {
						regPrice = Double.parseDouble(value);
					} catch (Exception e) {
						logger.error(e);
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,-->商品单价错误!");
						errl.add(errMap);
						continue;
					}
					break;
				case 21:
					giftFlag = value;
					break;
				case 22:
					value = findCountry(value);
					if (StringEmptyUtils.isEmpty(value)) {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品详情表】第" + (r + 1) + "行,-->商品原产国错误!");
						errl.add(errMap);
						continue;
					} else {
						originCountry = value;
					}
					break;
				case 23:
					quality = value;
					break;
				case 24:
					qualityCertify = value;
					break;
				case 25:
					manufactory = value;
					break;
				case 26:
					try {
						netWt = Double.parseDouble(value);
					} catch (Exception e) {
						logger.error(e);
						Map<String, Object> errMap = new HashMap<>();
						errMap.put("msg", "【已备案商品详情表】第" + (r + 1) + "行-->" + "商品净重数值有误");
						errl.add(errMap);
						continue;
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
						continue;
					}
					break;
				case 28:
					notes = value;
					break;
				case 29:
					ingredient = value;
					break;
				case 30:
					additiveflag = value;
					break;
				case 31:
					poisonflag = value;
					break;
				default:
					break;
				}
			}
			GoodsRecordDetail goodsRecordDetail = new GoodsRecordDetail();
			goodsRecordDetail.setDZKNNo(DZKNNo);
			// 当企邦的归属商户Id与Sku都填写了则进行针对性操作
			if (StringEmptyUtils.isNotEmpty(marCode)) {
				entGoodsNo = entGoodsNo + "_" + marCode;
				JSONObject json = new JSONObject();
				json.put("marCode", marCode);
				if (StringEmptyUtils.isNotEmpty(entGoodsNoSKU)) {
					json.put("SKU", entGoodsNoSKU);
				}
				goodsRecordDetail.setSpareParams(json.toString());
			}
			goodsRecordDetail.setEntGoodsNo(entGoodsNo);
			goodsRecordDetail.setEbEntNo(ebEntNo);
			goodsRecordDetail.setEbEntName(ebEntName);
			goodsRecordDetail.setSeq(seq);
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
						"【已备案商品详情表】第" + (r + 1) + "行,-->" + reMap.get(BaseCode.MSG.toString()));
				errl.add(errMap);
				continue;
			}
			Map<String, Object> param = new HashMap<>();
			// 由于行循环是从第三行开始读取,所以SEQ要减1
			param.put("seq", r - 1);
			param.put("entGoodsNo", entGoodsNo);
			param.put("shelfGName", shelfGName);
			param.put("ncadCode", ncadCode);
			param.put("hsCode", hsCode);
			param.put("goodsName", goodsName);
			param.put("goodsStyle", goodsStyle);
			param.put("barCode", barCode);
			param.put("brand", brand);
			param.put("gUnit", gUnit);
			param.put("stdUnit", stdUnit);
			param.put("regPrice", regPrice);
			param.put("giftFlag", giftFlag);
			param.put("originCountry", originCountry);
			param.put("quality", quality);
			param.put("manufactory", manufactory);
			param.put("netWt", netWt);
			param.put("grossWt", grossWt);
			Map<String, Object> reGoodsMap = checkRecordGoodsInfo(JSONObject.fromObject(param));
			if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
				Map<String, Object> errMap = new HashMap<>();
				errMap.put(BaseCode.MSG.toString(),
						"【已备案商品详情表】第" + (r + 1) + "行,--> " + reGoodsMap.get(BaseCode.MSG.toString()));
				errl.add(errMap);
			} else {
				Map<String, Object> item = goodsRecordService.batchCreateRecordGoodsDetail(goodsRecordDetail);
				if (!"1".equals(item.get(BaseCode.STATUS.toString()))) {
					Map<String, Object> errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(),
							"【已备案商品详情表】第" + (r + 1) + "行,--> " + item.get(BaseCode.MSG.toString()));
					errl.add(errMap);
				}
			}
		}
		logger.error("--导入已备案商品信息结束-->" + errl.toString());
		return ReturnInfoUtils.errorInfo(errl);
	}

	/**
	 * 已备案商品信息导入校验
	 * 
	 * @param jsonData
	 * @return
	 */
	private Map<String, Object> checkRecordGoodsInfo(JSONObject jsonData) {
		JSONArray datas = new JSONArray();
		datas.add(jsonData);
		List<String> noNullKeys = new ArrayList<>();
		// noNullKeys.add("shelfGName");
		noNullKeys.add("ncadCode");
		noNullKeys.add("hsCode");
		// noNullKeys.add("BarCode");
		noNullKeys.add("barCode");
		noNullKeys.add("goodsName");
		noNullKeys.add("goodsStyle");
		noNullKeys.add("gUnit");
		noNullKeys.add("stdUnit");
		noNullKeys.add("regPrice");
		noNullKeys.add("giftFlag");
		noNullKeys.add("originCountry");
		noNullKeys.add("quality");
		noNullKeys.add("manufactory");
		noNullKeys.add("netWt");
		noNullKeys.add("grossWt");
		return CheckDatasUtil.changeMsg(datas, noNullKeys);
	}

	/**
	 * 读取已备案商品信息头信息
	 * 
	 * @param sheet
	 *            excel工作表的索引
	 * @param excel
	 * @param errl
	 *            错误信息集合
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @return Map
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
						int intValue = Integer.parseInt(value);
						if (intValue == 1 || intValue == 2) {
							customsPort = intValue;
						} else {
							Map<String, Object> errMap = new HashMap<>();
							errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,-->口岸代码错误!");
							errl.add(errMap);
							continue;
						}
					} catch (Exception e) {
						e.printStackTrace();
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,-->口岸代码错误!");
						errl.add(errMap);
						continue;
					}
					break;
				case 1:
					if (StringEmptyUtils.isNotEmpty(value)) {
						customsPortName = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【商品备案详情表】第" + (r + 1) + "行,-->口岸名称不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				case 2:
					if (StringEmptyUtils.isNotEmpty(value)) {
						customsCode = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,-->海关编码不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				case 3:
					if (StringEmptyUtils.isNotEmpty(value)) {
						customsName = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,-->海关名称不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				case 4:
					if (StringEmptyUtils.isNotEmpty(value)) {
						ciqOrgCode = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,-->检验检疫编码不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				case 5:
					if (StringEmptyUtils.isNotEmpty(value)) {
						ciqOrgName = value;
					} else {
						Map<String, Object> errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "【已备案商品信息头】第" + (r + 1) + "行,-->检验检疫名称不能为空!");
						errl.add(errMap);
						continue;
					}
					break;
				default:
					break;
				}
			}
			item = goodsRecordService.batchCreateRecordGoodsHead(merchantId, merchantName, customsPort, customsPortName,
					customsCode, customsName, ciqOrgCode, ciqOrgName);
			if (!"1".equals(item.get(BaseCode.STATUS.toString()))) {
				return ReturnInfoUtils
						.errorInfo("【已备案商品信息头】第" + (r + 1) + "行,--> " + item.get(BaseCode.MSG.toString()));
			}
		}
		headMap.clear();
		headMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		headMap.put(BaseCode.ERROR.toString(), errl);
		headMap.put("goodsSerialNo", item.get("goodsSerialNo"));
		return headMap;
	}

	// 商户删除商品备案信息
	public Map<String, Object> merchantDeleteGoodsRecordInfo(String entGoodsNo) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		String merchantName = merchantInfo.getMerchantName();
		String merchantId = merchantInfo.getMerchantId();
		return goodsRecordService.merchantDeleteGoodsRecordInfo(merchantId, merchantName, entGoodsNo);
	}

	// 管理员修改商品备案信息
	public Map<String, Object> managerUpdateGoodsRecordInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		// String managerId = managerInfo.getManagerId();
		String managerName = managerInfo.getManagerName();
		datasMap.put("updateBy", managerName);
		return goodsRecordService.managerUpdateGoodsRecordInfo(datasMap);
	}

	// 管理员审核商品信息
	public Map<String, Object> managerReviewerInfo(String entGoodsNo, String note, int reviewerFlag) {
		Subject currentUser = SecurityUtils.getSubject();
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGER_INFO.toString());
		return goodsRecordService.managerReviewerInfo(managerInfo, entGoodsNo, note, reviewerFlag);
	}

	//
	public Map<String, Object> merchantUpdateInfo(Map<String, Object> datasMap) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return goodsRecordService.merchantUpdateInfo(merchantInfo, datasMap);
	}

	public Map<String,Object> merchantInitiateReview(List<String> goodsIdList) {
		Subject currentUser = SecurityUtils.getSubject();
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANT_INFO.toString());
		return goodsRecordService.merchantInitiateReview(merchantInfo, goodsIdList);
	}
}
