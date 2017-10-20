package org.silver.shop.impl.system.commerce;

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

	@Override
	public Map<String, Object> searchAlreadyRecordGoodsDetails(String merchantId, String warehouseCode, int page,
			int size) {
		Map<String, Object> statusMap = new HashMap<>();
		int one = warehouseCode.indexOf('_');
		int two = warehouseCode.indexOf('_', one + 1);
		// 截取MerchantId_00030_|5165| 第二个下划线后4位数为仓库码
		String code = warehouseCode.substring(two + 1);
		Table reTable = stockDao.getWarehousGoodsInfo(merchantId, code, page, size);
		if (reTable == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), Transform.tableToJson(reTable));
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		}
		return statusMap;
	}

	@Override
	//添加库存数量
	public Map<String, Object> addGoodsStockCount(String merchantId, String merchantName, String warehousCode,
			String warehousName, String goodsInfoPack) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = null;
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
			params = new HashMap<>();
			Map<String, Object> datasMap = (Map<String, Object>) jsonList.get(i);
			params.put("goodsMerchantId", merchantId);
			// 删除标识:0-未删除,1-已删除
			params.put("deleteFlag", 0);
			params.put("entGoodsNo", datasMap.get("entGoodsNo"));
			params.put("goodsName", datasMap.get("goodsName"));
			// 备案状态：1-备案中，2-备案成功，3-备案失败
			params.put("status", 2);
			List<Object> reList = stockDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
			if(reList == null){
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(),StatusCode.WARN.getMsg());
				return statusMap;
			}else if( reList.size() > 0){
				
				StockContent stock = new StockContent();
				stock.setMerchantId(merchantId);
				stock.setMerchantName(merchantName);
				stock.setTotalStock(Integer.valueOf(datasMap.get("stockCount") + ""));
				stock.setGoodsName(datasMap.get("goodsName") + "");
				stock.setGoodsId(datasMap.get("goodsDateilId") + "");
				stock.setRegPrice(0.0);
				stock.setFreePrice(0.0);
				stock.setFreight(0.0);
				stock.setWarehousCode(warehousCode);
				stock.setWarehousName(warehousName);
				stock.setCreateDate(date);
				stock.setCreateBy(merchantName);
				// 上下架标识：1-上架,2-下架
				stock.setSellFlag(2);
				stock.setEntGoodsNo(datasMap.get("goodsDateilId") + "");
				if (!stockDao.add(stock)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "保存 " + datasMap.get("goodsName") + " 商品错误!");
					return statusMap;
				}
			}else{
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "" + datasMap.get("goodsName") + " 该商品不存在!");
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	//添加商品上架数量
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
				stock.setSellCount(sellCount+oldSellCount);
				//上下架标识：1-上架,2-下架
				stock.setSellFlag(1);
				stock.setUpdateDate(date);
				stock.setUpdateBy(merchantName);
				if (!stockDao.update(stock)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "修改商品上架数量失败!");
					return statusMap;
				}
			}else{
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "上架数量大于库存数量!");
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}
}
