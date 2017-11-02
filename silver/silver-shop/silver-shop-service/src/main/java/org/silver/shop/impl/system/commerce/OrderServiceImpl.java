package org.silver.shop.impl.system.commerce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.OrderService;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.OrderGoodsContent;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.commerce.ShopCartContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.RecipientContent;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;

import net.sf.json.JSONArray;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

	protected static final Logger logger = LogManager.getLogger();
	@Autowired
	private OrderDao orderDao;

	@Override
	public Map<String, Object> createOrderInfo(String memberId, String memberName, String goodsInfoPack, int type,
			String recipientId) {
		Map<String, Object> statusMap = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			logger.error("用户提交订单传递参数格式错误!", e);
			e.printStackTrace();
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
			return statusMap;
		}
		// 校验前台传递的收货人ID查询信息
		Map<String, Object> reRecMap = checkRecipient(recipientId);
		if (!"1".equals(reRecMap.get(BaseCode.STATUS.toString()) + "")) {
			return reRecMap;
		}
		RecipientContent recInfo = (RecipientContent) reRecMap.get(BaseCode.DATAS.toString());
		int gacOrderTypeId = 2;
		// 生成对应海关订单ID
		Map<String, Object> entOrderNoMap = createOrderId(gacOrderTypeId);
		if (!"1".equals(entOrderNoMap.get(BaseCode.STATUS.toString()))) {
			return entOrderNoMap;
		}
		String entOrderNo = entOrderNoMap.get(BaseCode.DATAS.toString()) + "";
		// 创建订单
		Map<String, Object> reMap = createOrder(jsonList, memberName, memberId, recInfo, entOrderNo);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		Map<String, Object> reOrderMap = (Map<String, Object>) reMap.get("orderIdMap");
		double totalPrice = Double.parseDouble(reMap.get("goodsTotalPrice") + "");
		// 更新订单状态与删除购物车商品信息
		Map<String, Object> reStatusMap = updateOrderInfo(memberId, type, jsonList, totalPrice, reOrderMap);
		if (!"1".equals(reStatusMap.get(BaseCode.STATUS.toString()))) {
			return reStatusMap;
		}
		reStatusMap.put("entOrderNo", reMap.get("entOrderNo"));
		return reStatusMap;
	}

	// 创建订单ID
	private final Map<String, Object> createOrderId(int type) {
		Map<String, Object> statusMap = new HashMap<>();
		Calendar cl = Calendar.getInstance();
		int year = cl.get(Calendar.YEAR);
		String property = "";
		String topStr = "";
		if (type == 1) {
			topStr = "OR_";
			property = "orderId";
		} else {
			// 用于发往支付与海关的总订单头
			topStr = "GACNO_";
			property = "entOrderNo";
		}
		long orderIdCount = orderDao.findSerialNoCount(OrderContent.class, property, year);
		if (orderIdCount < 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else {
			String serialNo = SerialNoUtils.getSerialNo(topStr, year, orderIdCount);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), serialNo);
			return statusMap;
		}
	}

	// 生成订单
	private final Map<String, Object> createOrder(List<Object> jsonList, String memberName, String memberId,
			RecipientContent recInfo, String entOrderNo) {
		Double goodsTotalPrice = 0.0;
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> warehouseMap = new HashMap<>();
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
			String goodsId = paramsMap.get("goodsId") + "";
			int count = Integer.parseInt(paramsMap.get("count") + "");
			params.put("goodsId", goodsId);
			// 根据商品ID查询存库中商品信息
			List<Object> stockList = orderDao.findByProperty(StockContent.class, params, 1, 1);
			if (stockList != null && stockList.size() > 0) {
				StockContent stock = (StockContent) stockList.get(0);
				if (warehouseMap != null && warehouseMap.get(stock.getWarehousCode()) == null) {
					// 将仓库编号放入缓存中
					warehouseMap.put(stock.getWarehousCode(), "");
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
				if (goodsList == null || goodsList.size() <= 0) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "查询商品基本信息失败,服务器繁忙!");
					return statusMap;
				}
				GoodsContent goodsInfo = (GoodsContent) goodsList.get(0);
				// 根据前台传递的商品数量获取单价,得出商品总价
				goodsTotalPrice += count * goodsInfo.getGoodsRegPrice();
				if (warehouseMap.get(stock.getWarehousCode()) != null && !"".equals(warehouseMap.get(stock.getWarehousCode()))) {
					String orderId = warehouseMap.get(stock.getWarehousCode()) + "";
					Map<String, Object> reGoodsMap = createOrderGoodsInfo(memberId, memberName, orderId, count,
							goodsInfo, stock, entOrderNo);
					if (!reGoodsMap.get(BaseCode.STATUS.toString()).equals("1")) {
						return reGoodsMap;
					}
				} else {// 订单商户(仓库)不同时,创建新的订单基本信息
					int orderId = 1;
					// 生成订单ID
					Map<String, Object> newOrderIdMap = createOrderId(orderId);
					if (!"1".equals(newOrderIdMap.get(BaseCode.STATUS.toString()))) {
						return newOrderIdMap;
					}
					String newOrderId = newOrderIdMap.get(BaseCode.DATAS.toString()) + "";
					warehouseMap.put(stock.getWarehousCode(), newOrderId);
					Map<String, Object> reMap = createOrderBaseInfo(memberId, memberName, newOrderId, count, stock,
							entOrderNo, recInfo);
					if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
						return reMap;
					}
					Map<String, Object> reGoodsMap = createOrderGoodsInfo(memberId, memberName, newOrderId, count,
							goodsInfo, stock, entOrderNo);
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
		statusMap.put("entOrderNo", entOrderNo);
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put("orderIdMap", warehouseMap);
		statusMap.put("goodsTotalPrice", goodsTotalPrice);
		return statusMap;
	}

	// 创建订单基本信息
	private final Map<String, Object> createOrderBaseInfo(String memberId, String memberName, String orderId, int count,
			StockContent stock, String entOrderNo, RecipientContent recInfo) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		// 当数据库根据订单ID查询不到订单时,创建一条订单数据
		OrderContent order = new OrderContent();
		order.setMerchantId(stock.getMerchantId());
		order.setMerchantName(stock.getMerchantName());
		order.setMemberId(memberId);
		order.setMemberName(memberName);
		order.setOrderId(orderId);
		order.setFreight(0);
		order.setReceiptId(recInfo.getRecipientId());
		order.setRecipientName(recInfo.getRecipientName());
		order.setRecipientTel(recInfo.getRecipientTel());
		order.setRecipientCardId(recInfo.getRecipientCardId());
		order.setRecipientAddr(recInfo.getRecipientAddr());
		order.setRecipientCountryName(recInfo.getRecipientCountryName());
		order.setRecipientCountryCode(recInfo.getRecipientCountryCode());
		order.setRecProvincesName(recInfo.getRecProvincesName());
		order.setRecProvincesCode(recInfo.getRecProvincesCode());
		order.setRecCityName(recInfo.getRecCityName());
		order.setRecCityCode(recInfo.getRecCityCode());
		order.setRecAreaName(recInfo.getRecAreaName());
		order.setRecAreaCode(recInfo.getRecAreaCode());
		order.setOrderTotalPrice(stock.getRegPrice() * count);
		// 待付款
		order.setStatus(1);
		order.setCreateBy(memberName);
		order.setCreateDate(date);
		order.setDeleteFlag(0);
		order.setEntOrderNo(entOrderNo);
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
			int count, GoodsContent goodsInfo, StockContent stock, String entOrderNo) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		OrderGoodsContent orderGoods = new OrderGoodsContent();
		orderGoods.setMerchantId(stock.getMerchantId());
		orderGoods.setMerchantName(stock.getMerchantName());
		orderGoods.setMemberId(memberId);
		orderGoods.setMemberName(memberName);
		orderGoods.setOrderId(orderId);
		paramsMap.put("orderId", orderId);
		List<Object> reList = orderDao.findByProperty(OrderContent.class, paramsMap, 1, 1);
		// 查询订单头,更新订单商品总价格
		if (reList != null && reList.size() > 0) {
			OrderContent orderInfo = (OrderContent) reList.get(0);
			double reTotalPirce = orderInfo.getOrderTotalPrice();
			orderInfo.setOrderTotalPrice(reTotalPirce + (count * stock.getRegPrice()));
			if (!orderDao.update(orderInfo)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), stock.getGoodsName() + "更新订单头商品总价格失败,请重试！");
				return statusMap;
			}
		}
		orderGoods.setGoodsId(stock.getGoodsId());
		orderGoods.setEntGoodsNo(stock.getEntGoodsNo());
		orderGoods.setGoodsName(stock.getGoodsName());
		orderGoods.setGoodsPrice(stock.getRegPrice());
		orderGoods.setGoodsCount(count);
		orderGoods.setGoodsTotalPrice(count * stock.getRegPrice());
		orderGoods.setGoodsImage(goodsInfo.getGoodsImage());
		// 待定,暂时未0
		orderGoods.setTax(0.0);
		orderGoods.setLogisticsCosts(0.0);
		orderGoods.setCreateDate(date);
		orderGoods.setCreateBy(memberName);
		orderGoods.setDeleteFlag(0);
		orderGoods.setEntOrderNo(entOrderNo);
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
		return statusMap;
	}

	// 支付完成后根据类型进行业务处理
	private Map<String, Object> updateOrderInfo(String memberId, int type, List jsonList, double totalPrice,
			Map<String, Object> reOrderMap) {
		Map<String, Object> statusMap = new HashMap<>();
		if (type == 1) {// 1-余额支付,2-跳转至银盛
			Map<String, Object> params = new HashMap<>();
			params.put("memberId", memberId);
			List<Object> reList = orderDao.findByProperty(MemberWalletContent.class, params, 1, 1);
			if (reList != null && reList.size() > 0) {
				MemberWalletContent wallet = (MemberWalletContent) reList.get(0);
				double balance = wallet.getBalance();
				wallet.setBalance(balance - totalPrice);
				if (!orderDao.update(wallet)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "扣款失败,请重试！");
					return statusMap;
				}
			}
			return updateOrderInfo2(memberId, jsonList, reOrderMap);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), "http://ym.191ec.com/silver-web/yspay/dopay");
			return statusMap;
		}

	}

	@Override
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("reOrderSerialNo", datasMap.get("messageID") + "");
		paramMap.put("entOrderNo", datasMap.get("entOrderNo") + "");
		String reMsg = datasMap.get("errMsg") + "";
		List<Object> reList = orderDao.findByProperty(OrderRecordContent.class, paramMap, 1, 1);
		if (reList != null && reList.size() > 0) {
			OrderRecordContent order = (OrderRecordContent) reList.get(0);
			String status = datasMap.get("status") + "";
			String note = order.getReNote();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if ("1".equals(status)) {
				// 支付单备案状态修改为成功
				order.setOrderRecordStatus(2);
			} else {
				order.setOrderRecordStatus(3);
			}
			order.setReNote(note + defaultDate + ":" + reMsg + ";");
			order.setUpdateDate(date);
			if (!orderDao.update(order)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "异步更新订单备案信息错误!");
				return paramMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> getMerchantOrderInfo(String merchantId, String merchantName, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Map<String, Object>> orderMap = new HashMap<>();
		Map<String, Object> orderHeadMap = null;
		List<Object> reDataList = new ArrayList<>();
		Table reTable = orderDao.getMerchantOrderInfo(merchantId, page, size);
		paramMap.put("merchantId", merchantId);
		long totalCount = orderDao.findByPropertyCount(OrderContent.class, paramMap);
		if (reTable.getRows() != null && reTable.getRows().size() > 0) {
			List<Row> lr = reTable.getRows();
			for (int i = 0; i < lr.size(); i++) {
				String orderId = lr.get(i).getValue("orderId") + "";
				String goodsId = lr.get(i).getValue("goodsId") + "";
				String goodsName = lr.get(i).getValue("goodsName") + "";
				String goodsImage = lr.get(i).getValue("goodsImage") + "";
				String goodsPrice = lr.get(i).getValue("goodsPrice") + "";
				String goodsCount = lr.get(i).getValue("goodsCount") + "";
				String goodsTotalPrice = lr.get(i).getValue("goodsTotalPrice") + "";
				String remerchantId = lr.get(i).getValue("merchantId") + "";
				String remerchantName = lr.get(i).getValue("merchantName") + "";
				String reMemberId = lr.get(i).getValue("memberId") + "";
				String reMemberName = lr.get(i).getValue("memberName") + "";
				String freight = lr.get(i).getValue("freight") + "";
				String consolidatedTax = lr.get(i).getValue("consolidatedTax") + "";
				String logisticsCosts = lr.get(i).getValue("logisticsCosts") + "";
				String recipientName = lr.get(i).getValue("recipientName") + "";
				String recipientCardId = lr.get(i).getValue("recipientCardId") + "";
				String recipientTel = lr.get(i).getValue("recipientTel") + "";
				String recProvincesCode = lr.get(i).getValue("recProvincesCode") + "";
				String recCityCode = lr.get(i).getValue("recCityCode") + "";
				String recAreaCode = lr.get(i).getValue("recAreaCode") + "";
				String recipientAddr = lr.get(i).getValue("recipientAddr") + "";
				if (orderMap != null && orderMap.get(orderId + "_head") != null) {
					Map<String, Object> goodsMap = new HashMap<>();
					goodsMap.put("orderId", orderId);
					goodsMap.put("goodsId", goodsId);
					goodsMap.put("goodsName", goodsName);
					goodsMap.put("goodsImage", goodsImage);
					goodsMap.put("goodsPrice", goodsPrice);
					goodsMap.put("goodsCount", goodsCount);
					goodsMap.put("goodsTotalPrice", goodsTotalPrice);
					goodsMap.put("logisticsCosts", logisticsCosts);
					orderMap.get(orderId + "_head").put(goodsId + "_content", goodsMap);
				} else {
					orderHeadMap = new HashMap<>();
					orderHeadMap.put("orderId", orderId);
					orderHeadMap.put("remerchantId", remerchantId);
					orderHeadMap.put("remerchantName", remerchantName);
					orderHeadMap.put("remerchantName", remerchantName);
					orderHeadMap.put("reMemberId", reMemberId);
					orderHeadMap.put("reMemberName", reMemberName);
					orderHeadMap.put("freight", freight);
					orderHeadMap.put("consolidatedTax", consolidatedTax);
					orderHeadMap.put("recipientName", recipientName);
					orderHeadMap.put("recipientCardId", recipientCardId);
					orderHeadMap.put("recipientTel", recipientTel);
					orderHeadMap.put("recProvincesCode", recProvincesCode);
					orderHeadMap.put("recProvincesCode", recProvincesCode);
					orderHeadMap.put("recCityCode", recCityCode);
					orderHeadMap.put("recAreaCode", recAreaCode);
					orderHeadMap.put("recipientAddr", recipientAddr);
					orderMap.put(orderId + "_head", orderHeadMap);
					Map<String, Object> goodsMap = new HashMap<>();
					goodsMap.put("orderId", orderId);
					goodsMap.put("goodsId", goodsId);
					goodsMap.put("goodsName", goodsName);
					goodsMap.put("goodsImage", goodsImage);
					goodsMap.put("goodsPrice", goodsPrice);
					goodsMap.put("goodsCount", goodsCount);
					goodsMap.put("goodsTotalPrice", goodsTotalPrice);
					goodsMap.put("logisticsCosts", logisticsCosts);
					orderHeadMap.put(goodsId + "_content", goodsMap);
					reDataList.add(orderHeadMap);
				}
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.DATAS.toString(), reDataList);
		statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount);
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	// 检查收货人信息
	private Map<String, Object> checkRecipient(String recipientId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> param = new HashMap<>();
		param.put("recipientId", recipientId);
		List<Object> recList = orderDao.findByProperty(RecipientContent.class, param, 1, 1);
		if (recList != null && recList.size() > 0) {
			RecipientContent recInfo = (RecipientContent) recList.get(0);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), recInfo);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "用户收货地址信息错误,请重试");
		}
		return statusMap;
	}

	// 更新订单状态及删除用户购物车数据
	private Map<String, Object> updateOrderInfo2(String memberId, List<Object> jsonList,
			Map<String, Object> reOrderMap) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		// 更新订单状态
		for (String key : reOrderMap.keySet()) {
			params.clear();
			params.put("memberId", memberId);
			params.put("orderId", reOrderMap.get(key));
			List<Object> orderList = orderDao.findByProperty(OrderContent.class, params, 1, 1);
			OrderContent orderBase = (OrderContent) orderList.get(0);
			orderBase.setStatus(2);
			if (!orderDao.update(orderBase)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "更新订单状态失败,请重试！");
				return statusMap;
			}
		}
		// 遍历购物车中信息,删除已支付的商品
		for (int i = 0; i < jsonList.size(); i++) {
			params.clear();
			Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
			String goodsId = paramsMap.get("goodsId") + "";
			params.put("goodsBaseId", goodsId);
			params.put("memberId", memberId);
			// 根据商品ID查询购物车中商品
			List<Object> cartList = orderDao.findByProperty(ShopCartContent.class, params, 1, 1);
			ShopCartContent cart = (ShopCartContent) cartList.get(0);
			if (!orderDao.delete(cart)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "更新购物车状态失败,请重试！");
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}
}
