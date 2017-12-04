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
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.common.category.GoodsThirdType;
import org.silver.shop.model.common.category.HsCode;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.OrderGoodsContent;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.commerce.ShopCarContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.RecipientContent;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import net.sf.json.JSONArray;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

	protected static final Logger logger = LogManager.getLogger();
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private StockServiceImpl stockServiceImpl;
	@Autowired
	private MerchantWalletServiceImpl merchantWalletServiceImpl;
	
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
		// 校验订单商品信息及创建订单
		Map<String, Object> reMap = checkOrderGoodsInfo(jsonList, memberName, memberId, recInfo, entOrderNo);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		String reEntOrderNo = reMap.get("entOrderNo") + "";
		double totalPrice = Double.parseDouble(reMap.get("goodsTotalPrice") + "");
		// 更新订单状态与删除购物车商品信息
		Map<String, Object> reStatusMap = updateOrderInfo(memberId, type, jsonList, totalPrice, reEntOrderNo,memberName);
		if (!"1".equals(reStatusMap.get(BaseCode.STATUS.toString()))) {
			return reStatusMap;
		}
		// 返回海关订单编号
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
			topStr = "GAC_";
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

	// 校验前台传递订单商品信息
	private final Map<String, Object> checkOrderGoodsInfo(List<Object> jsonList, String memberName, String memberId,
			RecipientContent recInfo, String entOrderNo) {
		double goodsTotalPrice = 0.0;
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> warehouseMap = new HashMap<>();
		int orderId = 1;
		// 生成订单ID
		Map<String, Object> newOrderIdMap = createOrderId(orderId);
		if (!"1".equals(newOrderIdMap.get(BaseCode.STATUS.toString()))) {
			return newOrderIdMap;
		}
		String newOrderId = newOrderIdMap.get(BaseCode.DATAS.toString()) + "";
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
			int count = Integer.parseInt(paramsMap.get("count") + "");
			String entGoodsNo = paramsMap.get("entGoodsNo") + "";
			params.clear();
			params.put("entGoodsNo", entGoodsNo);
			// 根据商品ID查询存库中商品信息
			List<Object> stockList = orderDao.findByProperty(StockContent.class, params, 1, 1);
			if (stockList != null && stockList.size() > 0) {
				StockContent stock = (StockContent) stockList.get(0);
				/*
				 * if (warehouseMap != null &&
				 * warehouseMap.get(stock.getWarehousCode()) == null) { //
				 * 将仓库编号放入缓存中 warehouseMap.put(stock.getWarehousCode(), ""); }
				 */
				// 商品上架数量
				int sellCount = stock.getSellCount();
				if (count > sellCount) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
					statusMap.put(BaseCode.MSG.toString(), stock.getGoodsName() + "库存不足,请重新输入购买数量!");
					return statusMap;
				}
				// 根据商品ID查询商品基本信息
				List<Object> goodsRecordList = orderDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
				if (goodsRecordList == null || goodsRecordList.size() <= 0) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "查询商品基本信息失败,服务器繁忙!");
					return statusMap;
				}
				GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) goodsRecordList.get(0);
				// 获取库存中商品上架的单价
				double regPrice = stock.getRegPrice();
				// 计算税费
				Map<String, Object> reRepiceMap = calculationTaxesFees(count, entGoodsNo, regPrice);
				if (!"1".equals(reRepiceMap.get(BaseCode.STATUS.toString()) + "")) {
					return reRepiceMap;
				}
				goodsTotalPrice = Double.parseDouble(reRepiceMap.get(BaseCode.DATAS.toString()) + "");
				/*
				 * if (warehouseMap.get(stock.getWarehousCode()) != null &&
				 * !"".equals(warehouseMap.get(stock.getWarehousCode()))) {
				 * String orderId = warehouseMap.get(stock.getWarehousCode()) +
				 * ""; Map<String, Object> reGoodsMap =
				 * createOrderGoodsInfo(memberId, memberName, orderId, count,
				 * goodsInfo, stock, entOrderNo); if
				 * (!reGoodsMap.get(BaseCode.STATUS.toString()).equals("1")) {
				 * return reGoodsMap; } // 订单商户(仓库)不同时,创建新的订单基本信息 } else {
				 * warehouseMap.put(stock.getWarehousCode(), newOrderId);
				 */
				// 开始创建订单
				Map<String, Object> reOrderMap = createOrder(newOrderId, memberId, memberName, count, goodsRecordInfo,
						stock, entOrderNo, goodsTotalPrice, recInfo);
				if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()) + "")) {
					return reOrderMap;
				}
			}
			/*
			 * } else { statusMap.put(BaseCode.STATUS.toString(),
			 * StatusCode.NO_DATAS.getStatus());
			 * statusMap.put(BaseCode.MSG.toString(), "商品不存在,请核对信息！"); return
			 * statusMap; }
			 */
		}
		statusMap.put("entOrderNo", entOrderNo);
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put("goodsTotalPrice", goodsTotalPrice);
		return statusMap;
	}

	// 创建订单头信息
	private final Map<String, Object> createOrderHeadInfo(String memberId, String memberName, String orderId, int count,
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
			int count, GoodsRecordDetail goodsRecordInfo, StockContent stock, String entOrderNo,
			double goodsTotalPrice) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		OrderGoodsContent orderGoods = new OrderGoodsContent();
		orderGoods.setMerchantId(goodsRecordInfo.getGoodsMerchantId());
		orderGoods.setMerchantName(goodsRecordInfo.getGoodsMerchantName());
		orderGoods.setMemberId(memberId);
		orderGoods.setMemberName(memberName);
		orderGoods.setOrderId(orderId);
		paramsMap.put("orderId", orderId);
		List<Object> reList = orderDao.findByProperty(OrderContent.class, paramsMap, 1, 1);
		// 查询订单头,更新订单商品总价格
		if (reList != null && reList.size() > 0) {
			OrderContent orderInfo = (OrderContent) reList.get(0);
			orderInfo.setOrderTotalPrice(goodsTotalPrice);
			if (!orderDao.update(orderInfo)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), stock.getGoodsName() + "更新订单头商品总价格失败,请重试！");
				return statusMap;
			}
		}
		orderGoods.setGoodsId(stock.getGoodsId());
		orderGoods.setEntGoodsNo(stock.getEntGoodsNo());
		orderGoods.setGoodsName(goodsRecordInfo.getSpareGoodsName());
		orderGoods.setGoodsPrice(stock.getRegPrice());
		orderGoods.setGoodsCount(count);
		orderGoods.setGoodsTotalPrice(count * stock.getRegPrice());
		orderGoods.setGoodsImage(goodsRecordInfo.getSpareGoodsImage());
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
		// 修改上架商品数量
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
			String reEntOrderNo,String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		if (type == 1) {// 1-余额支付,2-跳转至银盛
			Map<String, Object> reWalletMap = merchantWalletServiceImpl.checkWallet(2, memberId, memberName);
			if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getMsg());
				statusMap.put(BaseCode.MSG.toString(), "检查钱包失败!");
				return statusMap;
			}
			MemberWalletContent wallet = (MemberWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
			double balance = wallet.getBalance();
			if (balance - totalPrice < 0) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "余额不足!");
				return statusMap;
			}
			wallet.setBalance(balance - totalPrice);
			if (!orderDao.update(wallet)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "扣款失败,请重试！");
				return statusMap;
			}

			return updateOrderInfo2(memberId, jsonList, reEntOrderNo);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), "http://ym.191ec.com/silver-web-shop/yspay/dopay");
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
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return statusMap;
	}

	@Override
	public Map<String, Object> getMerchantOrderRecordInfo(String merchantId, String merchantName, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantId", merchantId);
		List<OrderRecordContent> reOrderList = orderDao.findByProperty(OrderRecordContent.class, paramMap, page, size);
		long totalCount = orderDao.findByPropertyCount(OrderRecordContent.class, paramMap);
		if (reOrderList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		} else if (!reOrderList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), reOrderList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount);
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
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
	private Map<String, Object> updateOrderInfo2(String memberId, List<Object> jsonList, String reEntOrderNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		// 更新订单状态
		if (reEntOrderNo != null && !"null".equals(reEntOrderNo)) {
			params.clear();
			params.put("memberId", memberId);
			params.put("entOrderNo", reEntOrderNo);
			List<Object> orderList = orderDao.findByProperty(OrderContent.class, params, 1, 1);
			OrderContent orderBase = (OrderContent) orderList.get(0);
			// 订单状态：1-待付款,2-已付款;3-商户待处理;4-订单超时;
			orderBase.setStatus(2);
			if (!orderDao.update(orderBase)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "更新订单状态失败,请重试！");
				return statusMap;
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "更新订单状态失败,订单编号错误,请重试！");
			return statusMap;
		}

		// 遍历购物车中信息,删除已支付的商品
		for (int i = 0; i < jsonList.size(); i++) {
			params.clear();
			Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
			String entGoodsNo = paramsMap.get("entGoodsNo") + "";
			params.put("entGoodsNo", entGoodsNo);
			params.put("memberId", memberId);
			// 根据商品ID查询购物车中商品
			List<Object> cartList = orderDao.findByProperty(ShopCarContent.class, params, 1, 1);
			ShopCarContent cart = (ShopCarContent) cartList.get(0);
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

	// 创建订单及订单关联的商品信息
	private Map<String, Object> createOrder(String newOrderId, String memberId, String memberName, int count,
			GoodsRecordDetail goodsRecordInfo, StockContent stock, String entOrderNo, double goodsTotalPrice,
			RecipientContent recInfo) {
		Map<String, Object> params = new HashMap<>();
		params.clear();
		params.put("orderId", newOrderId);
		List<Object> reOrderList = orderDao.findByProperty(OrderContent.class, params, 1, 1);
		if (reOrderList != null && !reOrderList.isEmpty()) {
			Map<String, Object> reGoodsMap = createOrderGoodsInfo(memberId, memberName, newOrderId, count,
					goodsRecordInfo, stock, entOrderNo, goodsTotalPrice);
			if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()))) {
				return reGoodsMap;
			}
			return reGoodsMap;
		} else {
			Map<String, Object> reMap = createOrderHeadInfo(memberId, memberName, newOrderId, count, stock, entOrderNo,
					recInfo);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			Map<String, Object> reGoodsMap = createOrderGoodsInfo(memberId, memberName, newOrderId, count,
					goodsRecordInfo, stock, entOrderNo, goodsTotalPrice);
			if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()))) {
				return reGoodsMap;
			}
			return reMap;
		}
	}

	@Override
	public Map<String, Object> checkOrderGoodsCustoms(String orderGoodsInfoPack) {
		Map<String, Object> statusMap = new HashMap<>();
		List<Object> cacheList = new ArrayList<>();
		Map<String, Object> param = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(orderGoodsInfoPack);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
			String entGoodsNo = paramsMap.get("entGoodsNo") + "";
			param.clear();
			param.put("entGoodsNo", entGoodsNo);
			List<Object> reGoodsRecordDetailList = orderDao.findByProperty(GoodsRecordDetail.class, param, 1, 1);
			if (reGoodsRecordDetailList != null && reGoodsRecordDetailList.size() > 0) {
				GoodsRecordDetail goodsInfo = (GoodsRecordDetail) reGoodsRecordDetailList.get(0);
				param.clear();
				param.put("goodsSerialNo", goodsInfo.getGoodsSerialNo());
				List<Object> reGoodsRecordInfo = orderDao.findByProperty(GoodsRecord.class, param, 1, 1);
				GoodsRecord goodsRecord = (GoodsRecord) reGoodsRecordInfo.get(0);
				if (cacheList != null && cacheList.size() == 0) {
					cacheList.add(goodsRecord.getCustomsCode() + "_" + goodsRecord.getCiqOrgCode());
				} else if (!cacheList.contains(goodsRecord.getCustomsCode() + "_" + goodsRecord.getCiqOrgCode())) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "不同海关不能一并下单,请分开下单！");
					return statusMap;
				}
			} else {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	/**
	 * 根据商品基本信息Id,备案商品Id,查询HS编码或大小类别计算税费
	 * 
	 * @param count
	 *            商品数量
	 * @param entGoodsNo
	 *            商品备案Id
	 * @param regPrice
	 *            单价
	 * @return Map
	 */
	public Map<String, Object> calculationTaxesFees(int count, String entGoodsNo, double regPrice) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("entGoodsNo", entGoodsNo);
		List<Object> reGoodsRecordDetailList = orderDao.findByProperty(GoodsRecordDetail.class, paramsMap, 1, 1);
		paramsMap.clear();
		if (reGoodsRecordDetailList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reGoodsRecordDetailList.isEmpty()) {
			// 查询备案商品信息
			GoodsRecordDetail goodsRecord = (GoodsRecordDetail) reGoodsRecordDetailList.get(0);
			String reHsCode = goodsRecord.getHsCode();
			paramsMap.put("hsCode", reHsCode);
			List<Object> reHsCodeList = orderDao.findByProperty(HsCode.class, paramsMap, 1, 1);
			paramsMap.clear();
			if (reHsCodeList != null && reHsCodeList.size() > 0) {
				HsCode hsCodeInfo = (HsCode) reHsCodeList.get(0);
				// 先判断Hs编码是否有税率
				if (hsCodeInfo.getVat() > 0 && hsCodeInfo.getConsolidatedTax() > 0) {
					return findTaxesFees(goodsRecord, count, regPrice);
				} else {// 其次以商品类型中的税率来计算
					return findTaxesFees(goodsRecord, count, regPrice);
				}
			} else {
				return findTaxesFees(goodsRecord, count, regPrice);
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询商品备案详情失败,服务器繁忙！");
			return statusMap;
		}
	}

	private Map<String, Object> findTaxesFees(GoodsRecordDetail goodsRecord, int count, double regPrice) {
		double total = 0;
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		long thirdId = Long.parseLong(goodsRecord.getSpareGoodsThirdTypeId());
		paramsMap.put("id", thirdId);
		List<Object> reGoodsThirdList = orderDao.findByProperty(GoodsThirdType.class, paramsMap, 1, 1);
		if (reGoodsThirdList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询商品税率失败,服务器繁忙！");
		} else if (!reGoodsThirdList.isEmpty()) {
			GoodsThirdType thirdInfo = (GoodsThirdType) reGoodsThirdList.get(0);
			// 税费 = 购买单价 × 件数 × 跨境电商综合税率
			double consolidatedTax = thirdInfo.getConsolidatedTax();
			// Double d= 0.03*5*(119/100d)
			total += regPrice * count * (consolidatedTax / 100d);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), total);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "查询商品税率无数据,服务器繁忙！");
		}
		return statusMap;
	}

	@Override
	public Map<String, Object> getMemberOrderInfo(String memberId, String memberName, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> lMap = new ArrayList<>();
		String descParams = "createDate";
		params.put("memberId", memberId);
		List<OrderContent> reOrderList = orderDao.findByPropertyDesc(OrderContent.class, params, descParams, page,
				size);
		long orderTotalCount = orderDao.findByPropertyCount(OrderContent.class, params);
		if (reOrderList != null && reOrderList.size() > 0) {
			for (OrderContent orderInfo : reOrderList) {
				String orderId = orderInfo.getOrderId();
				params.put("orderId", orderId);
				params.put("memberId", memberId);
				List<OrderContent> reOrderGoodsList = orderDao.findByProperty(OrderGoodsContent.class, params, 0, 0);
				Map<String, Object> item = new HashMap<>();
				item.put("order", orderInfo);
				item.put("orderGoods", reOrderGoodsList);
				lMap.add(item);
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), lMap);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), orderTotalCount);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> getMerchantOrderDetail(String merchantId, String merchantName, String entOrderNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> lm = new ArrayList<>();
		paramMap.put("merchantId", merchantId);
		paramMap.put("entOrderNo", entOrderNo);
		List<Object> reOrderList = orderDao.findByProperty(OrderRecordContent.class, paramMap, 1, 1);
		if (reOrderList != null && reOrderList.size() > 0) {
			Map<String, Object> item = new HashMap<>();
			List<Object> reOrderGoodsList = orderDao.findByProperty(OrderGoodsContent.class, paramMap, 0, 0);
			item.put("order", reOrderList);
			item.put("orderGoods", reOrderGoodsList);
			lm.add(item);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), lm);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> getMemberOrderDetail(String memberId, String memberName, String entOrderNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> lm = new ArrayList<>();
		paramMap.put("memberId", memberId);
		paramMap.put("entOrderNo", entOrderNo);
		List<Object> reOrderList = orderDao.findByProperty(OrderContent.class, paramMap, 1, 1);
		if (reOrderList != null && reOrderList.size() > 0) {
			Map<String, Object> item = new HashMap<>();
			List<Object> reOrderGoodsList = orderDao.findByProperty(OrderGoodsContent.class, paramMap, 0, 0);
			item.put("order", reOrderList);
			item.put("orderGoods", reOrderGoodsList);
			lm.add(item);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), lm);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> searchMerchantOrderInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reDatasMap = stockServiceImpl.universalSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> blurryMap = (Map<String, Object>) reDatasMap.get("blurry");
		List<Map<String, Object>> errorList = (List<Map<String, Object>>) reDatasMap.get("error");
		paramMap.put("merchantId", merchantId);
		paramMap.put("deleteFlag", 0);
		List<Object> reList = orderDao.findByPropertyLike(OrderRecordContent.class, paramMap, blurryMap, page, size);
		long totalCount = orderDao.findByPropertyLikeCount(OrderRecordContent.class, paramMap, blurryMap);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			statusMap.put(BaseCode.ERROR.toString(), errorList);
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount);
			statusMap.put(BaseCode.ERROR.toString(), errorList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			statusMap.put(BaseCode.ERROR.toString(), errorList);
			return statusMap;
		}
	}
}
