package org.silver.shop.impl.system.commerce;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.StockService;
import org.silver.shop.dao.system.commerce.StockDao;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.log.StockReviewLog;
import org.silver.shop.util.SearchUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;

@Service(interfaceClass = StockService.class)
public class StockServiceImpl implements StockService {
	private static final Logger logger = LogManager.getLogger(StockServiceImpl.class);

	@Autowired
	private StockDao stockDao;

	/**
	 * 驼峰命名：商品自编号
	 */
	private static final String ENT_GOODS_NO = "entGoodsNo";

	@Override
	public Map<String, Object> searchAlreadyRecordGoodsDetails(String merchantId, String warehouseCode, int page,
			int size, String entGoodsNo) {
		if (StringEmptyUtils.isEmpty(warehouseCode)) {
			return ReturnInfoUtils.errorInfo("仓库编号不能为空！");
		}
		int one = warehouseCode.indexOf('_');
		int two = warehouseCode.indexOf('_', one + 1);
		// 截取MerchantId_00030_|5165| 第二个下划线后4位数为仓库码
		String code = warehouseCode.substring(two + 1);
		Table reTable = stockDao.getWarehousGoodsInfo(merchantId, code, page, size, entGoodsNo);
		Table count = stockDao.getWarehousGoodsInfo(merchantId, code, 0, 0, entGoodsNo);
		if (reTable == null) {
			return ReturnInfoUtils.errorInfo("查询失败，服务器繁忙！");
		} else {
			return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reTable), count.getRows().size());
		}
	}

	@Override
	public Map<String, Object> addGoodsStockCount(String merchantId, String merchantName, String warehouseCode,
			String warehouseName, String goodsInfoPack) {
		Map<String, Object> params = null;
		Map<String, Object> errorMap = null;
		List<Map<String, Object>> listMap = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return ReturnInfoUtils.errorInfo("参数格式错误！");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			params = new HashMap<>();
			Map<String, Object> datasMap = (Map<String, Object>) jsonList.get(i);
			params.put("goodsMerchantId", merchantId);
			// 删除标识:0-未删除,1-已删除
			params.put("deleteFlag", 0);
			params.put(ENT_GOODS_NO, datasMap.get(ENT_GOODS_NO));
			// 备案状态：1-备案中，2-备案成功，3-备案失败
			params.put("status", 2);
			List<Object> orList = new ArrayList<>();
			Map<String, Object> orMap = null;
			// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案
			orMap = new HashMap<>();
			orMap.put("recordFlag", 1);
			orList.add(orMap);
			orMap = new HashMap<>();
			orMap.put("recordFlag", 2);
			orList.add(orMap);
			List<GoodsRecordDetail> goodsList = stockDao.findByPropertyOr(GoodsRecordDetail.class, params, orList, 1,
					1);
			if (goodsList == null) {
				return ReturnInfoUtils.errorInfo("查询商品失败,服务器繁忙！");
			} else if (!goodsList.isEmpty()) {
				GoodsRecordDetail goodsRecord = goodsList.get(0);
				//
				saveStockInfo(datasMap, goodsRecord, merchantId, merchantName, warehouseCode, warehouseName, listMap);
			} else {
				errorMap = new HashMap<>();
				errorMap.put(BaseCode.MSG.toString(), "商品自编号[" + datasMap.get(ENT_GOODS_NO) + "]对应商品信息不存在，或商品未通过备案审核！");
				listMap.add(errorMap);
			}
		}
		return ReturnInfoUtils.errorInfo(listMap, jsonList.size());
	}

	private void saveStockInfo(Map<String, Object> datasMap, GoodsRecordDetail goodsRecord, String merchantId,
			String merchantName, String warehouseCode, String warehouseName, List<Map<String, Object>> lm) {
		Date date = new Date();
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> errorMap = new HashMap<>();
		paramMap.put(ENT_GOODS_NO, datasMap.get(ENT_GOODS_NO));
		List<Object> stockList = stockDao.findByProperty(StockContent.class, paramMap, 1, 1);
		if (stockList == null) {
			errorMap.put(BaseCode.MSG.toString(), "查询" + goodsRecord.getGoodsName() + "失败,服务器繁忙！");
			lm.add(errorMap);
		} else if (!stockList.isEmpty()) {// 当商品在库存中存在时
			StockContent reStock = (StockContent) stockList.get(0);
			int reStockCount = Integer.parseInt(datasMap.get("stockCount") + "");
			// 商品原有库存
			int reTotalStock = reStock.getTotalStock();
			reStock.setTotalStock(reStockCount + reTotalStock);
			if (!stockDao.update(reStock)) {
				errorMap.put(BaseCode.MSG.toString(), "保存 " + reStock.getGoodsName() + " 商品错误!");
				lm.add(errorMap);
			}
		} else {// 库存中不存在商品,新添加一个商品
			StockContent stock = new StockContent();
			stock.setMerchantId(merchantId);
			stock.setMerchantName(merchantName);
			stock.setTotalStock(Integer.valueOf(datasMap.get("stockCount") + ""));
			stock.setGoodsName(goodsRecord.getGoodsName());
			// 库存中商品Id为商品基本信息Id
			stock.setGoodsId(goodsRecord.getGoodsDetailId());
			// 库存商品价格暂时设置为备案时单价
			stock.setRegPrice(goodsRecord.getRegPrice());
			stock.setFreight(0.0);
			stock.setWarehouseCode(warehouseCode);
			stock.setWarehouseName(warehouseName);
			stock.setCreateDate(date);
			stock.setCreateBy(merchantName);
			// 上下架标识：1-上架,2-下架
			stock.setSellFlag(2);
			// 商品备案Id
			stock.setEntGoodsNo(goodsRecord.getEntGoodsNo());
			stock.setCreateDate(new Date());
			stock.setCreateBy(merchantName);
			if (!stockDao.add(stock)) {
				errorMap.put(BaseCode.MSG.toString(), "保存 " + goodsRecord.getGoodsName() + " 商品错误!");
				lm.add(errorMap);
			}
		}
	}

	@Override
	// 添加商品上架数量(后续删除)
	public Map<String, Object> addGoodsSellCount(String merchantId, String merchantName, String goodsId,
			int sellCount) {
		Date date = new Date();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("goodsId", goodsId);
		List<Object> reList = stockDao.findByProperty(StockContent.class, params, 1, 1);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else {
			StockContent stock = (StockContent) reList.get(0);
			String reGoodsId = stock.getGoodsId();
			int stockCount = stock.getTotalStock();
			int oldSellCount = stock.getSellCount();
			if (reGoodsId.equals(goodsId) && sellCount <= stockCount) {
				stock.setSellCount(sellCount + oldSellCount);
				// 上下架标识：1-上架,2-下架
				stock.setSellFlag(1);
				stock.setUpdateDate(date);
				stock.setUpdateBy(merchantName);
				if (!stockDao.update(stock)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改商品上架数量失败!");
					return statusMap;
				}
			} else {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "上架数量大于库存数量!");
				return statusMap;
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getGoodsStockInfo(String merchantId, String merchantName, int page, int size,
			String warehouseCode) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("warehouseCode", warehouseCode);
		List<Object> reList = stockDao.findByProperty(StockContent.class, params, page, size);
		long totalCount = stockDao.findByPropertyCount(StockContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, totalCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> setGoodsSellAndStopSelling(String merchantId, String merchantName, String goodsInfoPack,
			int type) {
		Date date = new Date();
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("商品上下架信息包格式错误!");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> errorMap = new HashMap<>();
			Map<String, Object> stockMap = (Map<String, Object>) jsonList.get(i);
			String entGoodsNo = stockMap.get(ENT_GOODS_NO) + "";
			params.clear();
			params.put("merchantId", merchantId);
			params.put(ENT_GOODS_NO, entGoodsNo);
			List<StockContent> reStockList = stockDao.findByProperty(StockContent.class, params, 1, 1);
			params.clear();
			params.put(ENT_GOODS_NO, entGoodsNo);
			params.put("goodsMerchantId", merchantId);
			List<GoodsRecordDetail> reGoodsRecordList = stockDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
			if (reStockList == null || reGoodsRecordList == null) {
				errorMap.put(BaseCode.MSG.getBaseCode(), "商品自编号[" + entGoodsNo + "]查询失败,服务器繁忙！");
				errorList.add(errorMap);
			} else if (!reStockList.isEmpty() && !reGoodsRecordList.isEmpty()) {
				GoodsRecordDetail goodsRecordInfo = reGoodsRecordList.get(0);
				Map<String, Object> reCheckGoodsMap = checkGoodsInfo(goodsRecordInfo, errorList);
				if (!"1".equals(reCheckGoodsMap.get(BaseCode.STATUS.toString()))) {
					return reCheckGoodsMap;
				}
				StockContent stockContent = reStockList.get(0);
				if (type == 1) {// 当上架时添加商品上架时间
					stockContent.setSellFlag(1);
					// stockContent.setSellFlag(3);
					// 当库存商品商家时，添加商品审核日志,待运营人员审核商品
					// Map<String,Object> reLogMap =
					// addStockReviewLog(stockContent);
					// if(!"1".equals(reLogMap.get(BaseCode.STATUS.toString()))){
					// return reLogMap;
					// }
				} else if (type == 2) {// 当设置下架时添加商品下架时间
					stockContent.setSellFlag(2);
					stockContent.setDropOffDate(new Date());
				}
				stockContent.setCreateBy(merchantName);
				stockContent.setCreateDate(date);
				if (!stockDao.update(stockContent)) {
					errorMap.put(BaseCode.MSG.getBaseCode(), stockContent.getGoodsName() + "商品上/下架状态修改失败,服务器繁忙！");
					errorList.add(errorMap);
				}
			} else {
				errorMap.put(BaseCode.MSG.getBaseCode(), "商品自编号[" + entGoodsNo + "]未找到商品信息！");
				errorList.add(errorMap);
			}
		}
		return ReturnInfoUtils.errorInfo(errorList);
	}

	private Map<String, Object> addStockReviewLog(StockContent stockContent) {
		if (stockContent == null) {
			return ReturnInfoUtils.errorInfo("添加审核日志失败,请求参数不能为null！");
		}
		// 上下架标识：1-上架,2-下架,3-审核中
		stockContent.setSellFlag(3);
		StockReviewLog stockReviewLog = new StockReviewLog();
		stockReviewLog.setEntGoodsNo(stockContent.getEntGoodsNo());
		stockReviewLog.setMerchantId(stockContent.getMerchantId());
		stockReviewLog.setMerchantName(stockContent.getMerchantName());
		stockReviewLog.setOperationName("商品上架");
		// 审核标识：1-待审核，2-审核通过；3-审核不通过
		stockReviewLog.setReviewerFlag(1);
		stockReviewLog.setCreateDate(new Date());
		if (!stockDao.add(stockReviewLog)) {
			return ReturnInfoUtils.errorInfo("保存库存审核日志失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 检查商品信息是否符合上架要求
	 * 
	 * @param goodsRecordInfo
	 *            商品备案信息
	 * @param errorList
	 *            错误集合
	 * @return Map
	 */
	private Map<String, Object> checkGoodsInfo(GoodsRecordDetail goodsRecordInfo, List<Map<String, Object>> errorList) {
		if (goodsRecordInfo == null) {
			return ReturnInfoUtils.errorInfo("商品信息不能为null");
		}
		Map<String, Object> errorMap = null;
		String goodsFirstTypeId = goodsRecordInfo.getSpareGoodsFirstTypeId();
		String goodsSecondTypeId = goodsRecordInfo.getSpareGoodsSecondTypeId();
		String goodsThirdTypeId = goodsRecordInfo.getSpareGoodsThirdTypeId();
		if (StringEmptyUtils.isEmpty(goodsFirstTypeId) || StringEmptyUtils.isEmpty(goodsSecondTypeId)
				|| StringEmptyUtils.isEmpty(goodsThirdTypeId)) {
			errorMap = new HashMap<>();
			errorMap.put(BaseCode.MSG.getBaseCode(), goodsRecordInfo.getGoodsName() + " 上/下架状态修改失败,商品类型未设置！");
			errorList.add(errorMap);
		}
		// 备案状态：1-备案中，2-备案成功，3-备案失败,4-未备案
		int status = goodsRecordInfo.getStatus();
		if (status != 2) {
			errorMap = new HashMap<>();
			errorMap.put(BaseCode.MSG.getBaseCode(), goodsRecordInfo.getGoodsName() + " 上/下架状态修改失败,商品尚未备案成功！");
			errorList.add(errorMap);
		}
		// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案,3-审核不通过
		int recordFlag = goodsRecordInfo.getRecordFlag();
		if (recordFlag == 0 || recordFlag == 3) {
			errorMap = new HashMap<>();
			errorMap.put(BaseCode.MSG.getBaseCode(),
					goodsRecordInfo.getGoodsName() + " 上/下架状态修改失败,已备案商品并未通过审核,请联系管理员！");
			errorList.add(errorMap);
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> setGoodsStorageAndSellCount(String merchantId, String merchantName, String goodsInfoPack,
			int type) {
		Date date = new Date();
		List<Map<String, Object>> errorMsgList = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("商品信息包格式错误!");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> errorMap = new HashMap<>();
			Map<String, Object> stockMap = (Map<String, Object>) jsonList.get(i);
			String entGoodsNo = stockMap.get(ENT_GOODS_NO) + "";
			int count = Integer.parseInt(stockMap.get("count") + "");
			params.put("merchantId", merchantId);
			params.put(ENT_GOODS_NO, entGoodsNo);
			List<Object> reStockList = stockDao.findByProperty(StockContent.class, params, 0, 0);
			if (reStockList == null) {
				errorMap.put(BaseCode.MSG.getBaseCode(), "查询编号：" + entGoodsNo + "商品失败,服务器繁忙！");
				errorMsgList.add(errorMap);
			} else if (!reStockList.isEmpty()) {
				StockContent stockInfo = (StockContent) reStockList.get(0);
				// 1-库存,2-上架
				if (type == 1) {
					stockInfo.setTotalStock(count);
				} else if (type == 2) {
					int oldTotalCount = stockInfo.getTotalStock();
					if (count > oldTotalCount) {
						errorMap.put(BaseCode.MSG.toString(), stockInfo.getGoodsName() + "上架数量不能大于库存数量！");
						errorMsgList.add(errorMap);
						continue;
					}
					stockInfo.setSellCount(count);
				}
				stockInfo.setUpdateBy(merchantName);
				stockInfo.setUpdateDate(date);
				if (!stockDao.update(stockInfo)) {
					errorMap.put(BaseCode.MSG.getBaseCode(), stockInfo.getGoodsName() + "修改库存/上架失败,服务器繁忙！");
					errorMsgList.add(errorMap);
				}
			} else {
				errorMap.put(BaseCode.MSG.getBaseCode(), "没有找到编号为：" + entGoodsNo + "商品,服务器繁忙！");
				errorMsgList.add(errorMap);
			}
		}
		return ReturnInfoUtils.errorInfo(errorMsgList);
	}

	@Override
	public Map<String, Object> searchGoodsStockInfo(String merchantId, Map<String, Object> datasMap, int page,
			int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalStockSearch(datasMap);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> blurryMap = (Map<String, Object>) reDatasMap.get("blurry");
		paramMap.put("deleteFlag", 0);
		paramMap.put("merchantId", merchantId);
		List<Object> reList = stockDao.findByPropertyLike(StockContent.class, paramMap, blurryMap, page, size);
		long totalCount = stockDao.findByPropertyLikeCount(StockContent.class, paramMap, blurryMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, totalCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> merchantSetGoodsSalePriceAndMarketPrice(String merchantId, String merchantName,
			String goodsInfoPack, int type) {
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("商品信息包格式错误!");
		}
		List<Map<String, Object>> errorList = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> errorMap = null;
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> datasMap = (Map<String, Object>) jsonList.get(i);
			String entGoodsNo = datasMap.get(ENT_GOODS_NO) + "";
			params.clear();
			params.put(ENT_GOODS_NO, entGoodsNo);
			params.put("merchantId", merchantId);
			List<StockContent> reStockList = stockDao.findByProperty(StockContent.class, params, 0, 0);
			if (reStockList == null) {
				errorMap = new HashMap<>();
				errorMap.put(BaseCode.MSG.getBaseCode(), "查询商品信息失败,服务器繁忙！");
				errorList.add(errorMap);
			} else if (!reStockList.isEmpty()) {
				StockContent stockInfo = reStockList.get(0);
				Map<String, Object> reMap = updateSalePriceAndMarketPrice(stockInfo, datasMap, type, merchantName);
				if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
					errorList.add(reMap);
				}
			} else {
				errorMap = new HashMap<>();
				errorMap.put(BaseCode.MSG.getBaseCode(), "商品自编号[" + entGoodsNo + "]未找到对应商品信息！");
				errorList.add(errorMap);
			}
		}
		return ReturnInfoUtils.errorInfo(errorList, jsonList.size());
	}

	/**
	 * 更新商品市场价或销售价
	 * 
	 * @param stockInfo
	 *            库存信息
	 * @param datasMap
	 *            参数
	 * @param type
	 *            类型:1-市场价,2-销售价
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	private Map<String, Object> updateSalePriceAndMarketPrice(StockContent stockInfo, Map<String, Object> datasMap,
			int type, String merchantName) {
		if (stockInfo == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null！");
		}
		double price = 0.0;
		try {
			price = Double.parseDouble(datasMap.get("price") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("商品自编号[" + stockInfo.getEntGoodsNo() + "]价格错误！");
		}
		// 类型:1-市场价,2-销售价
		switch (type) {
		case 1:
			stockInfo.setMarketPrice(price);
			break;
		case 2:
			// 上下架标识：1-上架,2-下架,3-审核中
			if (stockInfo.getSellFlag() == 2) {
				stockInfo.setRegPrice(price);
			} else {
				return ReturnInfoUtils.errorInfo("商品自编号[" + stockInfo.getEntGoodsNo() + "]当前状态不允许修改价格！");
			}
			break;
		default:
			return ReturnInfoUtils.errorInfo("未知类型");
		}
		stockInfo.setUpdateBy(merchantName);
		stockInfo.setUpdateDate(new Date());
		if (!stockDao.update(stockInfo)) {
			return ReturnInfoUtils.errorInfo("商品自编号[" + stockInfo.getEntGoodsNo() + "]修改失败，服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}
}
