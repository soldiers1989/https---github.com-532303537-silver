package org.silver.shop.impl.system.commerce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ListSelectionEvent;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.OrderService;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.OrderGoodsContent;
import org.silver.shop.model.system.commerce.ShopCartContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

	@Autowired
	private OrderDao orderDao;

	@Override
	public Map<String, Object> createOrderInfo(String memberId, String memberName, String goodsInfoPack, int type
			,double totalPrice) {
		Map<String, Object> statusMap = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			e.printStackTrace();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
			return statusMap;
		}
		// 生成订单ID
		Map<String, Object> orderIdMap = createOrderId();
		if (!"1".equals(orderIdMap.get(BaseCode.STATUS.toString()))) {
			return orderIdMap;
		}
		String orderId = orderIdMap.get(BaseCode.DATAS.toString()) + "";
		// 创建订单
		Map<String, Object> reMap = createOrder(orderId, jsonList, memberName, memberId);
		if(!"1".equals(reMap.get(BaseCode.STATUS.toString()))){
			return reMap;
		}
		System.out.println(JSONObject.fromObject(reMap).toString());
		//1-余额支付,2-跳转至银盛
		if (type == 1) {
			Map<String, Object> params = new HashMap<>();
			params.put("memberId", memberId);
			params.put("memberName", memberName);
			List<Object> reList = orderDao.findByProperty(MemberWalletContent.class, params, 1, 1);
			if (reList == null) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			} else {
				MemberWalletContent wallet = (MemberWalletContent) reList.get(0);
				double balance = wallet.getBalance();
				wallet.setBalance(balance - totalPrice);
				if (!orderDao.update(wallet)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "扣款失败,请重试！");
					return statusMap;
				}
				params.put("orderId", orderId);
				List<Object> orderList = orderDao.findByProperty(OrderContent.class, params, 1, 1);
				OrderContent orderBase = (OrderContent) orderList.get(0);
				orderBase.setStatus(2);
				if (!orderDao.update(orderBase)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "更新订单状态失败,请重试！");
					return statusMap;
				}
				for(int i = 0;i<jsonList.size();i++){
					params.clear();
					Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
					String goodsId = paramsMap.get("goodsId") + "";
					params.put("goodsBaseId", goodsId);
					// 根据商品ID查询存库中商品信息
					List<Object> cartList = orderDao.findByProperty(ShopCartContent.class, params, 1, 1);
					ShopCartContent cart = (ShopCartContent) cartList.get(0);
					cart.setFlag(3);
					if (!orderDao.update(cart)) {
						statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
						statusMap.put(BaseCode.MSG.toString(), "更新购物车状态失败,请重试！");
						return statusMap;
					}
				}
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), "http://ym.191ec.com/silver-web/yspay/dopay");
			return statusMap;
		}
		
		return reMap;
	}

	// 创建订单ID
	private final Map<String, Object> createOrderId() {
		Map<String, Object> statusMap = new HashMap<>();
		Calendar cl = Calendar.getInstance();
		String property = "orderId";
		int year = cl.get(Calendar.YEAR);
		long orderIdCount = orderDao.findSerialNoCount(OrderContent.class, property, year);
		if (orderIdCount < 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else {
			String serialNo = SerialNoUtils.getSerialNo("OR_", year, orderIdCount);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), serialNo);
			return statusMap;
		}
	}

	// 生成订单
	private final Map<String, Object> createOrder(String orderId, List<Object> jsonList, String memberName,
			String memberId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		List<String> cacheList = new ArrayList<>();
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
			String goodsId = paramsMap.get("goodsId") + "";
			int count = Integer.parseInt(paramsMap.get("count") + "");
			params.put("goodsId", goodsId);
			// 根据商品ID查询存库中商品信息
			List<Object> stockList = orderDao.findByProperty(StockContent.class, params, 1, 1);
			 if (stockList!=null && stockList.size() > 0) {
				StockContent stock = (StockContent) stockList.get(0);
				if (cacheList.contains(stock.getMerchantName())) {
					cacheList.add(stock.getMerchantName());
				}
				// 商品上架数量
				int sellCount = stock.getSellCount();
				if (count > sellCount) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
					statusMap.put(BaseCode.MSG.toString(), stock.getGoodsName() + "库存不足,请重新输入购买数量!");
					return statusMap;
				}
				params.clear();
				params.put("goodsId", goodsId);
				// 根据商品ID查询商品基本信息
				List<Object> goodsList = orderDao.findByProperty(GoodsContent.class, params, 1, 1);
				if (goodsList == null) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "查询商品基本信息失败,服务器繁忙!");
					return statusMap;
				}
				if (cacheList.contains(stock.getMerchantName())) {
					Map<String, Object> reGoodsMap = createOrderGoodsInfo(memberId, memberName, orderId, count,
							goodsList, stock);
					if (!reGoodsMap.get(BaseCode.STATUS.toString()).equals("1")) {
						return reGoodsMap;
					}
				} else {// 订单商户不同时,创建新的订单基本信息
					Map<String, Object> reMap = createOrderBaseInfo(memberId, memberName, orderId, count, stock,
							cacheList);
					if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
						return reMap;
					}
					Map<String, Object> reGoodsMap = createOrderGoodsInfo(memberId, memberName, orderId, count,
							goodsList, stock);
					if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()))) {
						return reGoodsMap;
					}
				}
			} else {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "商品不存在,请核对信息！");
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	// 创建订单基本信息
	private final Map<String, Object> createOrderBaseInfo(String memberId, String memberName, String orderId, int count,
			StockContent stock, List<String> cacheList) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();

		// 当数据库根据订单ID查询不到订单时,创建一条订单数据
		OrderContent order = new OrderContent();
		order.setMerchantId(stock.getMerchantId());
		order.setMerchantName(stock.getMerchantName());
		order.setMemberId(memberId);
		order.setMemberName(memberName);
		order.setOrderId(orderId);
		order.setGoodsId(stock.getGoodsId());
		order.setGoodsName(stock.getGoodsName());
		order.setCount(count);
		order.setRegPrice(stock.getRegPrice());
		order.setTotalPrice(count * stock.getRegPrice());
		order.setFreight(0);
		order.setConsolidatedTax(0);
		order.setOrderTotalPrice(0);
		// 待付款
		order.setStatus(1);
		order.setCreateBy(memberName);
		order.setCreateDate(date);
		order.setDeleteFlag(0);
		if (!orderDao.add(order)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "订单创建失败!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		return statusMap;
	}

	// 创建订单商品信息
	private final Map<String, Object> createOrderGoodsInfo(String memberId, String memberName, String orderId,
			int count, List<Object> goodsList, StockContent stock) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		GoodsContent goods = (GoodsContent) goodsList.get(0);
		OrderGoodsContent orderGoods = new OrderGoodsContent();
		orderGoods.setMerchantId(stock.getMerchantId());
		orderGoods.setMerchantName(stock.getMerchantName());
		orderGoods.setMemberId(memberId);
		orderGoods.setMemberName(memberName);

		orderGoods.setEntOrderNo(orderId);
		orderGoods.setGoodsId(stock.getGoodsId());

		orderGoods.setGoodsName(stock.getGoodsName());
		orderGoods.setGoodsPrice(stock.getRegPrice());
		orderGoods.setGoodsCount(count);
		orderGoods.setGoodsTotalPrice(count * stock.getRegPrice());
		orderGoods.setGoodsImage(goods.getGoodsImage());
		// 待定,暂时未0
		orderGoods.setTax(0.0);
		orderGoods.setLogisticsCosts(0.0);
		orderGoods.setCreateDate(date);
		orderGoods.setCreateBy(memberName);
		orderGoods.setDeleteFlag(0);
		if (!orderDao.add(orderGoods)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), stock.getGoodsName() + "保存商品信息失败,请重试！");
			return statusMap;
		}
		int sellCount = stock.getSellCount();
		stock.setSellCount(sellCount - count);
		int paymentCount = stock.getPaymentCount();
		stock.setPaymentCount(count + paymentCount);
		if (!orderDao.update(stock)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "修改 " + stock.getGoodsName() + "上架数量失败,请重试！");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put("orderId", orderId);
		return statusMap;
	}
}
