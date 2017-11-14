package org.silver.shop.impl.system.commerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.ShopCarService;
import org.silver.shop.dao.system.commerce.ShopCarDao;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.ShopCarContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = ShopCarService.class)
public class ShopCarServiceImpl implements ShopCarService {

	@Autowired
	private ShopCarDao shopCarDao;

	@Override
	public Map<String, Object> addGoodsToShopCar(String memberId, String memberName, String entGoodsNo, int count) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("entGoodsNo", entGoodsNo);
		// 根据前台传递的商品ID查询商品是否存在
		List<Object> goodsRecordList = shopCarDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
		// 在查询库存中上架(售卖数量是够足够)
		List<Object> stockList = shopCarDao.findByProperty(StockContent.class, params, 1, 1);
		params.clear();
		params.put("memberId", memberId);
		params.put("entGoodsNo", entGoodsNo);
		List<Object> reShopCart = shopCarDao.findByProperty(ShopCarContent.class, params, 1, 1);
		if (goodsRecordList == null || stockList == null || reShopCart == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		if (!goodsRecordList.isEmpty() && !stockList.isEmpty()) {
			// 获取库存信息
			StockContent stock = (StockContent) stockList.get(0);
			int reSellCount = stock.getSellCount();
			// 判断是否有足够的库存
			if (count > reSellCount) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "库存不足,请重新输入");
				return statusMap;
			}
			GoodsRecordDetail goodsRecord = (GoodsRecordDetail) goodsRecordList.get(0);
			ShopCarContent shopCart = new ShopCarContent();
			// 查询当前用户购物车中是否有该商品
			if (!reShopCart.isEmpty()) {
				ShopCarContent oldShopCart = (ShopCarContent) reShopCart.get(0);
				int oldCount = oldShopCart.getCount();
				int newCount = oldCount + count;
				oldShopCart.setCount(newCount);
				// 获取到原购物车中商品单价
				Double price = oldShopCart.getRegPrice();
				oldShopCart.setTotalPrice(newCount * price);
				if (!shopCarDao.update(oldShopCart)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
				return statusMap;
			}
			shopCart.setMemberId(memberId);
			shopCart.setMemberName(memberName);
			shopCart.setMerchantId(goodsRecord.getGoodsMerchantId());
			shopCart.setMerchantName(goodsRecord.getGoodsMerchantName());
			shopCart.setGoodsBaseId(goodsRecord.getGoodsDetailId());
			shopCart.setGoodsName(goodsRecord.getSpareGoodsName());
			shopCart.setGoodsImage(goodsRecord.getSpareGoodsImage());
			shopCart.setGoodsStyle(goodsRecord.getSpareGoodsStyle());
			shopCart.setCount(count);
			// 购物车商品选中标识：1-选中,2-未选中
			shopCart.setFlag(1);
			shopCart.setRegPrice(stock.getRegPrice());
			Double totalPrice = stock.getRegPrice() * count;
			shopCart.setTotalPrice(totalPrice);
			shopCart.setEntGoodsNo(stock.getEntGoodsNo());
			if (!shopCarDao.add(shopCart)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			}
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "商品不存在！");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> getGoodsToShopCartInfo(String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		List<Object> carList = shopCarDao.findByProperty(ShopCarContent.class, params, 0, 0);
		if (carList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!carList.isEmpty()) {
			for (int i = 0; i < carList.size(); i++) {
				ShopCarContent cart = (ShopCarContent) carList.get(i);
				String entGoodsNo = cart.getEntGoodsNo();
				params.clear();
				params.put("entGoodsNo", entGoodsNo);
				// 根据备案Id实时查询库存中商品销售(上架)数量
				List<Object> stockList = shopCarDao.findByProperty(StockContent.class, params, 1, 1);
				if (stockList != null && stockList.size() > 0) {
					StockContent stock = (StockContent) stockList.get(0);
					cart.setSellCount(stock.getSellCount());
				} else {
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
					return statusMap;
				}
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.getBaseCode(), carList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> deleteShopCartGoodsInfo(String goodsId, String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("goodsBaseId", goodsId);
		params.put("memberId", memberId);
		List<Object> reList = shopCarDao.findByProperty(ShopCarContent.class, params, 1, 1);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			ShopCarContent cart = (ShopCarContent) reList.get(0);
			if (!shopCarDao.delete(cart)) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> editShopCarGoodsInfo(String memberId, String memberName, String goodsInfo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String,Object>> litMap = new ArrayList<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfo);
		} catch (Exception e) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.FORMAT_ERR.getMsg());
			return statusMap;
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> dataMap = (Map<String, Object>) jsonList.get(i);
			Map<String,Object> errorMap = new HashMap<>();
			String entGoodsNo = dataMap.get("entGoodsNo") + "";
			int count = Integer.parseInt((dataMap.get("count") + ""));
			int type = Integer.parseInt((dataMap.get("type") + ""));
			paramMap.put("entGoodsNo", entGoodsNo);
			List<Object> reShopCarList = shopCarDao.findByProperty(ShopCarContent.class, paramMap, 1, 1);
			List<Object> reStockList = shopCarDao.findByProperty(StockContent.class, paramMap, 1, 1);
			if (reShopCarList == null || reStockList == null) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			} else if (!reShopCarList.isEmpty() && !reStockList.isEmpty()) {
				ShopCarContent shopCarInfo = (ShopCarContent) reShopCarList.get(0);
				StockContent stockInfo = (StockContent) reStockList.get(0);
				int sellCount = stockInfo.getSellCount();
				if(count > sellCount){
					errorMap.put(BaseCode.ERROR.toString(), shopCarInfo.getEntGoodsNo()+"购买数量大于库存数量!");
					litMap.add(errorMap);
					
					continue;
				}
				//type修改类型:1-全部修改,2-只修改商品数量
				if(type == 1){
					int reShopFlag = shopCarInfo.getFlag();
					// 购物车商品选中标识：1-选中,2-未选中
					int shopFlag = reShopFlag != 1 ? 1 : 2;
					shopCarInfo.setFlag(shopFlag);
					shopCarInfo.setCount(count);
				}else if(type ==2){
					shopCarInfo.setCount(count);
				}
				if(!shopCarDao.update(shopCarInfo)){
					errorMap.put(BaseCode.ERROR.toString(), shopCarInfo.getEntGoodsNo()+"更新失败!");
					litMap.add(errorMap);
				}
			} else {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
		statusMap.put(BaseCode.ERROR.toString(), litMap);
		return statusMap;
	}
}
