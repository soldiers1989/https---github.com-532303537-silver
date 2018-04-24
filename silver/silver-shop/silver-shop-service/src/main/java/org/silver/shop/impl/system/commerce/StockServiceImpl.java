package org.silver.shop.impl.system.commerce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.StockService;
import org.silver.shop.dao.system.commerce.StockDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.util.SearchUtils;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;

@Service(interfaceClass = StockService.class)
public class StockServiceImpl implements StockService {
	private static final Logger logger = LogManager.getLogger(StockServiceImpl.class);

	@Autowired
	private StockDao stockDao;

	@Override
	public Map<String, Object> searchAlreadyRecordGoodsDetails(String merchantId, String warehouseCode, int page,
			int size) {
		Map<String, Object> statusMap = new HashMap<>();
		int one = warehouseCode.indexOf('_');
		int two = warehouseCode.indexOf('_', one + 1);
		// 截取MerchantId_00030_|5165| 第二个下划线后4位数为仓库码
		String code = warehouseCode.substring(two + 1);
		Table reTable = stockDao.getWarehousGoodsInfo(merchantId, code, page, size);
		Table count = stockDao.getWarehousGoodsInfo(merchantId, code, 0, 0);
		if (reTable == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), Transform.tableToJson(reTable));
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.TOTALCOUNT.toString(), count.getRows().size());
			return statusMap;
		}
	}

	@Override
	// 添加库存数量
	public Map<String, Object> addGoodsStockCount(String merchantId, String merchantName, String warehouseCode,
			String warehouseName, String goodsInfoPack) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = null;
		Map<String, Object> errorMap = new HashMap<>();
		List<Map<String, Object>> listMap = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			logger.error(e.getMessage());
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
			return statusMap;
		}
		for (int i = 0; i < jsonList.size(); i++) {
			List<Object> list = new ArrayList<>();
			Map<String, List<Object>> orMap = new HashMap<>();
			params = new HashMap<>();
			Map<String, Object> datasMap = (Map<String, Object>) jsonList.get(i);
			params.put("goodsMerchantId", merchantId);
			// 删除标识:0-未删除,1-已删除
			params.put("deleteFlag", 0);
			params.put("entGoodsNo", datasMap.get("entGoodsNo"));
			// 备案状态：1-备案中，2-备案成功，3-备案失败
			params.put("status", 2);
			// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案
			list.add(1);
			list.add(2);
			orMap.put("recordFlag", list);
			List<Object> goodsList = stockDao.findByPropertyOr(GoodsRecordDetail.class, params, orMap, 1, 1);
			if (goodsList == null) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			} else if (!goodsList.isEmpty()) {
				GoodsRecordDetail goodsRecord = (GoodsRecordDetail) goodsList.get(0);
				//
				saveStockInfo(datasMap, goodsRecord, merchantId, merchantName, warehouseCode, warehouseName, listMap);
			} else {
				statusMap.put(BaseCode.MSG.toString(), "编号为：" + datasMap.get("entGoodsNo") + ",商品不存在!");
				listMap.add(errorMap);
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put(BaseCode.ERROR.toString(), listMap);
		return statusMap;
	}

	private void saveStockInfo(Map<String, Object> datasMap, GoodsRecordDetail goodsRecord, String merchantId,
			String merchantName, String warehouseCode, String warehouseName, List<Map<String, Object>> lm) {
		Date date = new Date();
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> errorMap = new HashMap<>();
		paramMap.put("entGoodsNo", datasMap.get("entGoodsNo"));
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
			String entGoodsNo = stockMap.get("entGoodsNo") + "";
			params.clear();
			params.put("merchantId", merchantId);
			params.put("entGoodsNo", entGoodsNo);
			List<StockContent> reStockList = stockDao.findByProperty(StockContent.class, params, 1, 1);
			params.clear();
			params.put("entGoodsNo", entGoodsNo);
			params.put("goodsMerchantId", merchantId);
			List<GoodsRecordDetail> reGoodsRecordList = stockDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
			if (reStockList == null || reGoodsRecordList == null) {
				errorMap.put(BaseCode.MSG.getBaseCode(), "商品自编号[" + entGoodsNo + "]查询失败,服务器繁忙！");
				errorList.add(errorMap);
			} else if (!reStockList.isEmpty() && !reGoodsRecordList.isEmpty()) {
				GoodsRecordDetail goodsRecordInfo =  reGoodsRecordList.get(0);
				String goodsFirstTypeId = goodsRecordInfo.getSpareGoodsFirstTypeId();
				String goodsSecondTypeId = goodsRecordInfo.getSpareGoodsSecondTypeId();
				String goodsThirdTypeId = goodsRecordInfo.getSpareGoodsThirdTypeId();
				if (StringEmptyUtils.isEmpty(goodsFirstTypeId) || StringEmptyUtils.isEmpty(goodsSecondTypeId)
						|| StringEmptyUtils.isEmpty(goodsThirdTypeId)) {
					errorMap.put(BaseCode.MSG.getBaseCode(), goodsRecordInfo.getGoodsName() + " 上/下架状态修改失败,商品类型未设置！");
					errorList.add(errorMap);
					continue;
				}
				StockContent stockInfo =  reStockList.get(0);
				stockInfo.setSellFlag(type);
				if(type == 1){//当上架时添加商品上架时间
					stockInfo.setSellDate(new Date());
				}else if( type == 2){//当设置下架时添加商品下架时间
					stockInfo.setDropOffDate(new Date());
				}
				stockInfo.setCreateBy(merchantName);
				stockInfo.setCreateDate(date);
				if (!stockDao.update(stockInfo)) {
					errorMap.put(BaseCode.MSG.getBaseCode(), stockInfo.getGoodsName() + "商品上/下架状态修改失败,服务器繁忙！");
					errorList.add(errorMap);
				}
			} else {
				errorMap.put(BaseCode.MSG.getBaseCode(), "商品自编号[" + entGoodsNo + "]未找到商品信息！");
				errorList.add(errorMap);
			}
		}
		return ReturnInfoUtils.errorInfo(errorList);
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
			String entGoodsNo = stockMap.get("entGoodsNo") + "";
			int count = Integer.parseInt(stockMap.get("count") + "");
			params.put("merchantId", merchantId);
			params.put("entGoodsNo", entGoodsNo);
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
	public Map<String, Object> searchGoodsStockInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalStockSearch(datasMap);
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
		Date date = new Date();
		List<Map<String, Object>> errorMsgList = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("商品信息包格式错误!");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> errorMap = new HashMap<>();
			Map<String, Object> datasMap = (Map<String, Object>) jsonList.get(i);
			Map<String, Object> params = new HashMap<>();
			String entGoodsNo = datasMap.get("entGoodsNo") + "";
			params.put("entGoodsNo", entGoodsNo);
			List<Object> reStockList = stockDao.findByProperty(StockContent.class, params, 0, 0);
			if (reStockList == null) {
				errorMap.put(BaseCode.MSG.getBaseCode(), "查询编号：" + entGoodsNo + "商品失败,服务器繁忙！");
				errorMsgList.add(errorMap);
			} else if (!reStockList.isEmpty()) {
				StockContent stockInfo = (StockContent) reStockList.get(0);
				double price = 0.0;
				try {
					price = Double.parseDouble(datasMap.get("price") + "");
				} catch (Exception e) {
					errorMap.put(BaseCode.MSG.getBaseCode(), "商品价格错误,请重试！");
					errorMsgList.add(errorMap);
					continue;
				}
				// 修改类型:1-市场价,2-销售价
				if (type == 1) {
					stockInfo.setMarketPrice(price);
				} else {
					stockInfo.setRegPrice(price);
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
}
