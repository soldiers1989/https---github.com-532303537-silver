package org.silver.shop.impl.system.commerce;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.RedisKey;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CountryService;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.api.system.commerce.OrderService;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.api.system.cross.YsPayReceiveService;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.api.system.tenant.RecipientService;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.common.base.Area;
import org.silver.shop.model.common.base.City;
import org.silver.shop.model.common.base.Country;
import org.silver.shop.model.common.base.Metering;
import org.silver.shop.model.common.base.Province;
import org.silver.shop.model.common.category.GoodsThirdType;
import org.silver.shop.model.common.category.HsCode;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.OrderGoodsContent;
import org.silver.shop.model.system.commerce.OrderRecordContent;
import org.silver.shop.model.system.commerce.ShopCarContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.manual.OldManualOrder;
import org.silver.shop.model.system.manual.OldManualOrderSub;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantBusinessContent;
import org.silver.shop.model.system.tenant.RecipientContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.JedisUtil;
import org.silver.util.PhoneUtils;
import org.silver.util.RandomPasswordUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SendMsg;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

	protected static final Logger logger = LogManager.getLogger();
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private YsPayReceiveService ysPayReceiveService;
	@Autowired
	private CountryService countryService;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private CustomsPortService customsPortService;
	@Autowired
	private RecipientService recipientService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private WalletUtils walletUtils;
	@Autowired
	private PaymentService paymentService;

	/**
	 * 小写开头订单编号
	 */
	private static final String ENT_ORDER_NO = "entOrderNo";
	/**
	 * 订单商品总金额
	 */
	private static final String GOODS_TOTAL_PRICE = "goodsTotalPrice";
	/**
	 * 小写开头-商品自编号
	 */
	private static final String ENT_GOODS_NO = "entGoodsNo";

	/**
	 * 第三方平台业务Id
	 */
	private static final String THIR_DPARTY_ID = "thirdPartyId";

	@Override
	public Map<String, Object> createOrderInfo(String memberId, String memberName, String goodsInfoPack, int type,
			String recipientId) {
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			logger.error("用户提交订单传递参数格式错误!", e);
			return ReturnInfoUtils.errorInfo("用户提交订单传递参数格式错误!");
		}
		// 校验前台传递的收货人ID查询信息
		Map<String, Object> reRecMap = checkRecipient(recipientId);
		if (!"1".equals(reRecMap.get(BaseCode.STATUS.toString()) + "")) {
			return reRecMap;
		}
		RecipientContent recInfo = (RecipientContent) reRecMap.get(BaseCode.DATAS.toString());
		int gacOrderTypeId = 2;
		// 生成对应海关订单ID
		String entOrderNo = createOrderId(gacOrderTypeId);
		// 校验订单商品信息及创建订单
		Map<String, Object> reMap = checkOrderGoodsInfo(jsonList, memberName, memberId, recInfo, entOrderNo);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		double totalPrice = Double.parseDouble(reMap.get(GOODS_TOTAL_PRICE) + "");
		// 订单结算
		Map<String, Object> reStatusMap = liquidation(memberId, type, jsonList, totalPrice, entOrderNo, memberName);
		if (!"1".equals(reStatusMap.get(BaseCode.STATUS.toString()))) {
			return reStatusMap;
		}
		// 返回海关订单编号
		reStatusMap.put(ENT_ORDER_NO, entOrderNo);
		return reStatusMap;
	}

	/**
	 * 创建订单ID
	 * 
	 * @param type
	 *            1-商城自用订单、2-用于发往支付与海关的总订单头
	 * @return Map
	 */
	private final String createOrderId(int type) {
		String topStr = "";
		String name = "";
		if (type == 1) {
			// 商城自用订单抬头
			topStr = "YMDS";
			// 查询缓存中商城自用订单自增数关键字
			name = "shop_Order";
		} else if (type == 2) {
			// 用于发往支付与海关的总订单头
			topStr = "YM";
			name = "total_Shop_Order";
		}
		// 查询缓存中商城下单自增Id
		int count = SerialNoUtils.getSerialNo(name);
		return SerialNoUtils.getSerialNo(topStr, count);
	}

	// 校验前台传递订单商品信息
	private final Map<String, Object> checkOrderGoodsInfo(List<Object> jsonList, String memberName, String memberId,
			RecipientContent recInfo, String entOrderNo) {
		double goodsTotalPrice = 0.0;
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		// 商城自用订单
		int orderIdType = 1;
		String newOrderId = createOrderId(orderIdType);
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
			int count = Integer.parseInt(paramsMap.get("count") + "");
			String entGoodsNo = paramsMap.get(ENT_GOODS_NO) + "";
			params.clear();
			params.put(ENT_GOODS_NO, entGoodsNo);
			// 根据商品ID查询存库中商品信息
			List<Object> stockList = orderDao.findByProperty(StockContent.class, params, 1, 1);
			if (stockList != null && !stockList.isEmpty()) {
				StockContent stock = (StockContent) stockList.get(0);
				/*
				 * if (warehouseMap != null &&
				 * warehouseMap.get(stock.getWarehousCode()) == null) { //
				 * 将仓库编号放入缓存中 warehouseMap.put(stock.getWarehousCode(), ""); }
				 */
				// 商品上架数量
				int sellCount = stock.getSellCount();
				if (count > sellCount) {
					return ReturnInfoUtils.errorInfo(stock.getGoodsName() + " 库存不足！");
				}
				// 根据商品ID查询商品基本信息
				List<Object> goodsRecordList = orderDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
				if (goodsRecordList == null || goodsRecordList.isEmpty()) {
					return ReturnInfoUtils.errorInfo("查询商品基本信息失败,服务器繁忙!");
				}
				GoodsRecordDetail goodsRecordInfo = (GoodsRecordDetail) goodsRecordList.get(0);
				// 获取库存中商品上架的单价
				double regPrice = stock.getRegPrice();
				// 计算税费
				Map<String, Object> reRepiceMap = calculationTaxesFees(count, entGoodsNo, regPrice);
				if (!"1".equals(reRepiceMap.get(BaseCode.STATUS.toString()) + "")) {
					return reRepiceMap;
				}
				Map<String, Object> feeMap = new HashMap<>();
				goodsTotalPrice = Double.parseDouble(reRepiceMap.get(BaseCode.DATAS.toString()) + "");
				// 商品总金额的税费
				double tax = Double.parseDouble(reRepiceMap.get("tax") + "");
				feeMap.put(GOODS_TOTAL_PRICE, goodsTotalPrice);
				feeMap.put("tax", tax);
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
						stock, entOrderNo, feeMap, recInfo);
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
		statusMap.put(ENT_ORDER_NO, entOrderNo);
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put(GOODS_TOTAL_PRICE, goodsTotalPrice);
		return statusMap;
	}

	/**
	 * 创建订单头信息
	 * 
	 * @param memberId
	 *            用户Id
	 * @param memberName
	 *            用户名称
	 * @param orderId
	 *            订单Id
	 * @param stock
	 *            商品库存实体类
	 * @param entOrderNo
	 *            订单Id
	 * @param recInfo
	 *            收货人信息实体类
	 * @param feeMap
	 *            费率信息Map
	 * @return Map
	 */
	private final Map<String, Object> createOrderHeadInfo(String memberId, String memberName, String orderId,
			StockContent stock, String entOrderNo, RecipientContent recInfo, Map<String, Object> feeMap) {
		if (stock == null || recInfo == null) {
			return ReturnInfoUtils.errorInfo("生成订单头信息失败,请求参数错误！");
		}
		Date date = new Date();
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
		double goodsTotalPrice = Double.parseDouble(feeMap.get(GOODS_TOTAL_PRICE) + "");
		double tax = Double.parseDouble(feeMap.get("tax") + "");
		// 使用计算税费后的商品总金额
		order.setOrderTotalPrice(goodsTotalPrice);
		order.setTax(tax);
		// 待付款
		order.setStatus(1);
		order.setCreateBy(memberName);
		order.setCreateDate(date);
		order.setDeleteFlag(0);
		order.setEntOrderNo(entOrderNo);

		if (!orderDao.add(order)) {
			return ReturnInfoUtils.errorInfo("订单生成失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	// 创建订单商品信息
	private final Map<String, Object> createOrderGoodsInfo(String memberId, String memberName, String orderId,
			int count, GoodsRecordDetail goodsRecordInfo, StockContent stock, String entOrderNo,
			Map<String, Object> feeMap) {
		double goodsTotalPrice = Double.parseDouble(feeMap.get(GOODS_TOTAL_PRICE) + "");
		double tax = Double.parseDouble(feeMap.get("tax") + "");
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
		List<OrderContent> reList = orderDao.findByProperty(OrderContent.class, paramsMap, 1, 1);
		// 查询订单头,更新订单商品总价格
		if (reList != null && !reList.isEmpty()) {
			OrderContent orderInfo = reList.get(0);
			Double oldOrderTotalPrice = orderInfo.getOrderTotalPrice();
			double oldTax = orderInfo.getTax();
			if (oldOrderTotalPrice != goodsTotalPrice) {
				orderInfo.setOrderTotalPrice(oldOrderTotalPrice + goodsTotalPrice);
				orderInfo.setTax(oldTax + tax);
				if (!orderDao.update(orderInfo)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), stock.getGoodsName() + "更新订单头商品总价格失败,请重试！");
					return statusMap;
				}
			}
		}
		orderGoods.setGoodsId(stock.getGoodsId());
		orderGoods.setEntGoodsNo(stock.getEntGoodsNo());
		orderGoods.setGoodsName(goodsRecordInfo.getSpareGoodsName());
		orderGoods.setGoodsPrice(stock.getRegPrice());
		orderGoods.setGoodsCount(count);
		orderGoods.setGoodsTotalPrice(count * stock.getRegPrice());
		String image = goodsRecordInfo.getSpareGoodsImage();
		if (StringEmptyUtils.isNotEmpty(image)) {
			String[] strArr = image.split(";");
			orderGoods.setGoodsImage(strArr[0]);
		}
		// 待定,暂时未0
		orderGoods.setTax(0.0);
		orderGoods.setLogisticsCosts(0.0);
		orderGoods.setCreateDate(date);
		orderGoods.setCreateBy(memberName);
		orderGoods.setDeleteFlag(0);
		orderGoods.setEntOrderNo(entOrderNo);
		orderGoods.setTax(tax);
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
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 支付完成后根据类型进行业务处理
	 * 
	 * @param memberId
	 *            用户Id
	 * @param type
	 *            类型:1-余额支付,2-跳转至银盛
	 * @param jsonList
	 * @param totalPrice
	 *            订单商品总金额
	 * @param reEntOrderNo
	 *            海关订单编号
	 * @param memberName
	 *            用户名称
	 * @return Map
	 */
	private Map<String, Object> liquidation(String memberId, int type, List jsonList, double totalPrice,
			String reEntOrderNo, String memberName) {
		if (type == 1) {// 1-余额支付,2-跳转至银盛
			Map<String, Object> reWalletMap = walletUtils.checkWallet(2, memberId, memberName);
			if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
				return reWalletMap;
			}
			MemberWalletContent wallet = (MemberWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
			double balance = wallet.getBalance();
			if ((balance - totalPrice) < 0) {
				return ReturnInfoUtils.errorInfo("余额不足！");
			}
			wallet.setBalance(balance - totalPrice);
			if (!orderDao.update(wallet)) {
				return ReturnInfoUtils.errorInfo("扣款失败,请重试！");
			}
			Map<String, Object> datasMap = new HashMap<>();
			datasMap.put("out_trade_no", reEntOrderNo);
			datasMap.put("total_amount", totalPrice);
			Map<String, Object> rePayMap = ysPayReceiveService.balancePayReceive(datasMap);
			if (!"1".equals(rePayMap.get(BaseCode.STATUS.toString()))) {
				return rePayMap;
			}
			return updateOrderStatusAndShopCar(memberId, jsonList, reEntOrderNo);
		} else {
			//
			// return
			// ReturnInfoUtils.successDataInfo("https://ym.191ec.com/silver-web-shop/yspay/dopay");
			return ReturnInfoUtils.successDataInfo("https://ym.191ec.com/silver-web-shop/yspay/shoppingPayment");
		}
	}

	@Override
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("回调参数不能为空!");
		}
		System.out.println("-----订单异步回调参数->" + datasMap.toString());
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		String reMsg = datasMap.get("errMsg") + "";
		String entOrderNo = datasMap.get(ENT_ORDER_NO) + "";
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("reOrderSerialNo", datasMap.get("messageID") + "");
		paramMap.put(ENT_ORDER_NO, entOrderNo);
		List<Object> reList = orderDao.findByPropertyOr2(OrderRecordContent.class, paramMap, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			OrderRecordContent order = (OrderRecordContent) reList.get(0);
			String status = datasMap.get("status") + "";
			String note = order.getReNote();
			if (StringEmptyUtils.isEmpty(note)) {
				note = "";
			}
			if ("1".equals(status)) {
				// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
				order.setOrderRecordStatus(3);
			} else {
				order.setOrderRecordStatus(4);
			}
			order.setReNote(note + defaultDate + " " + reMsg + "#");
			order.setUpdateDate(date);
			// 当商城订单三单对碰通过后
			if (reMsg.contains("逻辑校验通过")) {
				Map<String, Object> reMap = updateOrderLogistics(order);
				if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
					logger.debug("--三单对碰后，更新快递单号错误-->>" + reMap.get(BaseCode.MSG.toString()));
				}
			}
			if (!orderDao.update(order)) {
				return ReturnInfoUtils.errorInfo("异步更新订单备案信息错误!");
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("订单号[" + entOrderNo + "]未找到对应订单信息");
		}
	}

	/**
	 * 当海关清关成功后请求物流信息
	 * 
	 * @param order
	 *            订单实体
	 * @return
	 */
	private Map<String, Object> updateOrderLogistics(OrderRecordContent orderRecord) {
		if (orderRecord == null) {
			return ReturnInfoUtils.errorInfo("更新物流状态请求参数不能为null");
		}
		Map<String, Object> item = new HashMap<>();
		String entOrderNo = orderRecord.getEntOrderNo();
		item.put("order_code", entOrderNo);
		String reString = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/waybill/queryOrderStatus", item);
		if (StringEmptyUtils.isNotEmpty(reString)) {
			JSONObject json = null;
			try {
				json = JSONObject.fromObject(reString);
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("返回参数格式错误！");
			}
			// 快递单号
			String waybillNumber = json.get("waybill_number") + "";
			orderRecord.setWaybillNo(waybillNumber);
			// 订单交易状态：1-待付款、2-已付款,待商家处理、3-待揽件、4-快件运输中、5-快件已签收、200-交易成功、400-交易关闭
			orderRecord.setOrderTradingStatus(4);
			Map<String, Object> params = new HashMap<>();
			params.put(ENT_ORDER_NO, entOrderNo);
			List<OrderContent> reList = orderDao.findByProperty(OrderContent.class, params, 1, 1);
			if (reList != null && !reList.isEmpty()) {
				OrderContent order = reList.get(0);
				// 订单交易状态：1-待付款、2-已付款,待商家处理、3-待揽件、4-快件运输中、5-快件已签收、200-交易成功、400-交易关闭
				order.setStatus(4);
				order.setWaybillNo(waybillNumber);
				if (!orderDao.update(order)) {
					return ReturnInfoUtils.errorInfo("更新用户订单快递单号失败，服务器繁忙！");
				}
			} else {
				return ReturnInfoUtils.errorInfo("查询用户订单信息失败，服务器繁忙！");
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("请求物流信息失败，服务器繁忙！");
		}
	}

	@Override
	public Map<String, Object> getMerchantOrderRecordInfo(String merchantId, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantId", merchantId);
		List<OrderRecordContent> reOrderList = orderDao.findByProperty(OrderRecordContent.class, paramMap, page, size);
		long totalCount = orderDao.findByPropertyCount(OrderRecordContent.class, paramMap);
		if (reOrderList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reOrderList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reOrderList, totalCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据！");
		}
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

	/**
	 * 更新订单状态及删除用户购物车数据
	 * 
	 * @param memberId
	 *            用户Id
	 * @param jsonList
	 *            商品数据
	 * @param reEntOrderNo
	 *            海关订单Id
	 * @return Map
	 */
	private Map<String, Object> updateOrderStatusAndShopCar(String memberId, List<Object> jsonList,
			String reEntOrderNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		// 更新订单状态
		if (StringEmptyUtils.isNotEmpty(reEntOrderNo)) {
			params.clear();
			params.put("memberId", memberId);
			params.put(ENT_ORDER_NO, reEntOrderNo);
			List<Object> orderList = orderDao.findByProperty(OrderContent.class, params, 1, 1);
			OrderContent orderBase = (OrderContent) orderList.get(0);
			// 订单状态：1-待付款,2-已付款;3-商户待处理;4-订单超时
			orderBase.setStatus(2);
			if (!orderDao.update(orderBase)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "更新订单状态失败,请重试！");
				return statusMap;
			}
		} else {
			return ReturnInfoUtils.errorInfo("更新订单状态失败,订单编号错误,请重试！");
		}

		// 遍历购物车中信息,删除已支付的商品
		for (int i = 0; i < jsonList.size(); i++) {
			params.clear();
			Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
			String entGoodsNo = paramsMap.get(ENT_GOODS_NO) + "";
			params.put(ENT_GOODS_NO, entGoodsNo);
			params.put("memberId", memberId);
			// 根据商品ID查询购物车中商品
			List<Object> cartList = orderDao.findByProperty(ShopCarContent.class, params, 1, 1);
			ShopCarContent cart = (ShopCarContent) cartList.get(0);
			if (!orderDao.delete(cart)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "删除已支付的商品失败！");
				return statusMap;
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	// 创建订单及订单关联的商品信息
	private Map<String, Object> createOrder(String newOrderId, String memberId, String memberName, int count,
			GoodsRecordDetail goodsRecordInfo, StockContent stock, String entOrderNo, Map<String, Object> feeMap,
			RecipientContent recInfo) {
		Map<String, Object> params = new HashMap<>();
		params.clear();
		params.put("orderId", newOrderId);
		List<Object> reOrderList = orderDao.findByProperty(OrderContent.class, params, 1, 1);
		if (reOrderList != null && !reOrderList.isEmpty()) {
			return createOrderGoodsInfo(memberId, memberName, newOrderId, count, goodsRecordInfo, stock, entOrderNo,
					feeMap);
		} else {
			Map<String, Object> reMap = createOrderHeadInfo(memberId, memberName, newOrderId, stock, entOrderNo,
					recInfo, feeMap);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			return createOrderGoodsInfo(memberId, memberName, newOrderId, count, goodsRecordInfo, stock, entOrderNo,
					feeMap);
		}
	}

	@Override
	public Map<String, Object> checkOrderGoodsCustoms(String orderGoodsInfoPack, String recipientId) {
		List<Object> cacheList = new ArrayList<>();
		Map<String, Object> param = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(orderGoodsInfoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单商品信息格式错误!");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> paramsMap = (Map<String, Object>) jsonList.get(i);
			String entGoodsNo = paramsMap.get(ENT_GOODS_NO) + "";
			param.clear();
			param.put(ENT_GOODS_NO, entGoodsNo);
			List<Object> reGoodsRecordDetailList = orderDao.findByProperty(GoodsRecordDetail.class, param, 1, 1);
			if (reGoodsRecordDetailList != null && !reGoodsRecordDetailList.isEmpty()) {
				GoodsRecordDetail goodsInfo = (GoodsRecordDetail) reGoodsRecordDetailList.get(0);
				param.clear();
				param.put("goodsSerialNo", goodsInfo.getGoodsSerialNo());
				List<Object> reGoodsRecordInfo = orderDao.findByProperty(GoodsRecord.class, param, 1, 1);
				GoodsRecord goodsRecord = (GoodsRecord) reGoodsRecordInfo.get(0);
				if (cacheList.isEmpty()) {
					cacheList.add(goodsRecord.getCustomsCode() + "_" + goodsRecord.getCiqOrgCode());
				} else if (!cacheList.contains(goodsRecord.getCustomsCode() + "_" + goodsRecord.getCiqOrgCode())) {
					return ReturnInfoUtils.errorInfo("不同海关不能一并下单,请分开下单！");
				}
			} else {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			}
		}
		return checkRecipientInfo(recipientId);
	}

	/**
	 * 检查收货人姓名与电话是否与用户信息一致
	 * 
	 * @param recipientId
	 *            收货地址Id
	 * @return Map
	 */
	private Map<String, Object> checkRecipientInfo(String recipientId) {
		Map<String, Object> reRecipientMap = recipientService.getRecipientInfo(recipientId);
		if (!"1".equals(reRecipientMap.get(BaseCode.STATUS.toString()))) {
			return reRecipientMap;
		}
		RecipientContent recipient = (RecipientContent) reRecipientMap.get(BaseCode.DATAS.toString());

		Map<String, Object> reMemberMap = memberService.getMemberInfo(recipient.getMemberId());
		if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
			return reMemberMap;
		}
		Member member = (Member) reMemberMap.get(BaseCode.DATAS.toString());
		if (member.getRealNameFlag() == 1) {
			return ReturnInfoUtils.errorInfo("用户尚未实名,暂不能下单,请先实名认证!");
		}
		if (!(recipient.getRecipientName().trim()).equals(member.getMemberIdCardName())) {
			return ReturnInfoUtils.errorInfo("收货人姓名与用户真实姓名不匹配,请核对信息!");
		}
		if (!(recipient.getRecipientCardId()).trim().equals(member.getMemberIdCard())) {
			return ReturnInfoUtils.errorInfo("收货人身份证号码与用户身份证号码不匹配,请核对信息!");
		}
		return ReturnInfoUtils.successInfo();
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
		paramsMap.put(ENT_GOODS_NO, entGoodsNo);
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
			return ReturnInfoUtils.errorInfo("查询商品税率失败,服务器繁忙!");
		} else if (!reGoodsThirdList.isEmpty()) {
			GoodsThirdType thirdInfo = (GoodsThirdType) reGoodsThirdList.get(0);
			// 综合税率
			double consolidatedTax = thirdInfo.getConsolidatedTax();

			double goodsTotalPrice = regPrice * count;

			// 税费 = 购买单价 × 件数 × 跨境电商综合税率
			double tax = goodsTotalPrice * (consolidatedTax / 1000d);
			total = goodsTotalPrice + tax;
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), total);
			statusMap.put("tax", tax);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("查询商品税率无数据,服务器繁忙!");
		}
	}

	@Override
	public Map<String, Object> getMemberOrderInfo(Map<String, Object> datasMap, int page, int size) {
		List<Map<String, Object>> listMap = new ArrayList<>();
		Map<String, Object> reDatasMap = SearchUtils.universalMemberOrderSearch(datasMap);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		Map<String, Object> params = (Map<String, Object>) reDatasMap.get("param");
		params.put("memberId", datasMap.get("memberId"));
		params.put("deleteFlag", 0);
		String descParams = "createDate";
		List<OrderContent> reOrderList = orderDao.findByPropertyDesc(OrderContent.class, params, descParams, page,
				size);
		long orderTotalCount = orderDao.findByPropertyCount(OrderContent.class, params);
		if (reOrderList != null && !reOrderList.isEmpty()) {
			for (OrderContent orderInfo : reOrderList) {
				params.clear();
				params.put("orderId", orderInfo.getOrderId());
				params.put("memberId", datasMap.get("memberId"));
				String evaluationFlag = datasMap.get("evaluationFlag") + "";
				if (StringEmptyUtils.isNotEmpty(evaluationFlag)) {
					int flag = Integer.parseInt(evaluationFlag);
					params.put("evaluationFlag", flag);
				}
				List<OrderContent> reOrderGoodsList = orderDao.findByProperty(OrderGoodsContent.class, params, 0, 0);
				Map<String, Object> item = new HashMap<>();
				item.put("order", orderInfo);
				item.put("orderGoods", reOrderGoodsList);
				listMap.add(item);
			}
			return ReturnInfoUtils.successDataInfo(listMap, orderTotalCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> getMerchantOrderDetail(String merchantId, String merchantName, String entOrderNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> lm = new ArrayList<>();
		paramMap.put("merchantId", merchantId);
		paramMap.put(ENT_ORDER_NO, entOrderNo);
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
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> getMemberOrderDetail(String memberId, String memberName, String entOrderNo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		List<Map<String, Object>> lm = new ArrayList<>();
		paramMap.put("memberId", memberId);
		paramMap.put(ENT_ORDER_NO, entOrderNo);
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
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> searchMerchantOrderInfo(String merchantId, String merchantName,
			Map<String, Object> datasMap, int page, int size) {
		datasMap.put("merchantId", merchantId);
		Map<String, Object> reDatasMap = SearchUtils.universalMerchantOrderSearch(datasMap);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		String type = datasMap.get("type") + "";
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> viceParams = (Map<String, Object>) reDatasMap.get("viceParams");
		switch (type) {
		case "online":
			return getOnlineOrderInfo(paramMap, page, size);
		case "offline":
			return getOfflineOrderInfo(viceParams, page, size);
		default:
			List<OrderRecordContent> reList = orderDao.unionOrderInfo(OrderRecordContent.class, paramMap, viceParams,
					page, size);
			long reTotalCount = orderDao.unionOrderCount(OrderRecordContent.class, paramMap, viceParams);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
			} else if (!reList.isEmpty()) {
				return ReturnInfoUtils.successDataInfo(reList, reTotalCount);
			} else {
				return ReturnInfoUtils.errorInfo("暂无数据！");
			}
		}
	}

	private Map<String, Object> getOfflineOrderInfo(Map<String, Object> params, int page, int size) {
		// 将日期字段转换
		if (StringEmptyUtils.isNotEmpty(params.get("startDate"))) {
			params.put("startTime", params.get("startDate"));
			params.remove("startDate");
		}
		if (StringEmptyUtils.isNotEmpty(params.get("endDate"))) {
			params.put("endTime", params.get("endDate"));
			params.remove("endDate");
		}
		List<Morder> reList = orderDao.findByPropertyLike(Morder.class, params, null, page, size);
		long reTotalCount = orderDao.findByPropertyLikeCount(Morder.class, params, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, reTotalCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据！");
		}
	}

	private Map<String, Object> getOnlineOrderInfo(Map<String, Object> params, int page, int size) {

		List<OrderRecordContent> reList = orderDao.findByPropertyLike(OrderRecordContent.class, params, null, page,
				size);
		long reTotalCount = orderDao.findByPropertyLikeCount(OrderRecordContent.class, params, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, reTotalCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据！");
		}
	}

	@Override
	public Map<String, Object> getMerchantOrderDailyReport(String merchantId, String merchantName, String startDate,
			String endDate) {
		Map<String, Object> paramsMap = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(merchantId)) {
			paramsMap.put("merchantId", merchantId);
		}
		paramsMap.put("merchantName", merchantName);
		paramsMap.put("startDate", startDate);
		paramsMap.put("endDate", endDate);
		Table reList = orderDao.getOrderDailyReport(paramsMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reList).getJSONArray("rows"));
		} else {
			return ReturnInfoUtils.errorInfo("暂无报表数据!");
		}
	}

	/**
	 * 根据商户第三方自编号,查询商户信息
	 * 
	 * @param merchant_cus_no
	 *            商户第三方自编号
	 * @return Map
	 */
	private Map<String, Object> getMerchantInfo(String merchant_cus_no) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchant_cus_no", merchant_cus_no);
		List<Appkey> reList = orderDao.findByProperty(Appkey.class, paramMap, 1, 1);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "商户编号[" + merchant_cus_no + "]查询appkey失败,服务器繁忙!");
			return statusMap;
		} else if (!reList.isEmpty()) {
			Appkey appkey = reList.get(0);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), appkey);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "商户编号[" + merchant_cus_no + "]查询appkey失败,请核实信息!");
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> getManualOrderInfo(Map<String, Object> dataMap, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalMOrderSearch(dataMap);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List orList = (List) reDatasMap.get("orList");
		List<Morder> orderList = orderDao.findByPropertyOr(Morder.class, paramMap, orList, page, size);
		long count = orderDao.findByPropertyOrCount(Morder.class, paramMap, orList);
		if (orderList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!orderList.isEmpty()) {
			List<Map<String, Object>> list = new ArrayList<>();
			for (Morder order : orderList) {
				Map<String, Object> item = new HashMap<>();
				String orderId = order.getOrder_id();
				paramMap.clear();
				paramMap.put("order_id", orderId);
				List<MorderSub> goodsList = orderDao.findByProperty(MorderSub.class, paramMap, 0, 0);
				item.put("head", order);
				item.put("content", goodsList);
				list.add(item);
			}
			return ReturnInfoUtils.successDataInfo(list, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> memberDeleteOrderInfo(String entOrderNo, String memberName) {
		if (StringEmptyUtils.isNotEmpty(entOrderNo)) {
			Map<String, Object> params = new HashMap<>();
			params.put(ENT_ORDER_NO, entOrderNo);
			List<OrderContent> orderList = orderDao.findByProperty(OrderContent.class, params, 0, 0);
			if (orderList != null && !orderList.isEmpty()) {
				for (OrderContent order : orderList) {
					// 订单状态：1-待付款
					if (order.getStatus() == 1) {
						order.setDeleteFlag(1);
						order.setDeleteBy(memberName);
						order.setDeleteDate(new Date());
						if (!orderDao.update(order)) {
							return ReturnInfoUtils.errorInfo("订单删除失败,请重试!");
						}
						return ReturnInfoUtils.successInfo();
					} else {
						return ReturnInfoUtils.errorInfo("订单当前状态不允许删除!");
					}
				}
			}
			return ReturnInfoUtils.errorInfo("订单信息不存在!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	@Override
	public Map<String, Object> getAgentOrderReport(Map<String, Object> datasMap) {
		if (datasMap != null && !datasMap.isEmpty()) {
			Table reTable = orderDao.getAgentOrderReport(datasMap);
			if (reTable == null) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			} else if (!reTable.getRows().isEmpty()) {
				return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reTable).getJSONArray("rows"), 0);
			} else {
				return ReturnInfoUtils.errorInfo("暂无数据");
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数错误！");
	}

	@Override
	public Map<String, Object> thirdPartyBusiness(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		JSONObject orderJson = null;
		// 获取订单信息
		try {
			orderJson = JSONObject.fromObject(datasMap.get("datas"));
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单参数格式不正确,请核对信息!");
		}
		try {
			Map<String, Object> reCheckMerchantMap = merchantUtils.getMerchantInfo(datasMap.get("merchantId") + "");
			if (!"1".equals(reCheckMerchantMap.get(BaseCode.STATUS.toString()))) {
				return reCheckMerchantMap;
			}
			Merchant merchant = (Merchant) reCheckMerchantMap.get(BaseCode.DATAS.toString());
			Map<String, Object> reCheckOrderMap = checkOrderInfo(orderJson);
			if (!"1".equals(reCheckOrderMap.get(BaseCode.STATUS.toString()) + "")) {
				return reCheckOrderMap;
			}
			String entOrderNo = orderJson.get("EntOrderNo") + "";
			// 获取订单信息中商品信息
			List<JSONObject> orderGoodsList = (List<JSONObject>) orderJson.get("orderGoodsList");
			Map<String, Object> reCheckGoodsMap = checkGoods(entOrderNo, orderGoodsList);
			if (!"1".equals(reCheckGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
				return reCheckGoodsMap;
			}
			Map<String, Object> reCheckAmountMap = checkAmount(orderJson, orderGoodsList);
			if (!"1".equals(reCheckAmountMap.get(BaseCode.STATUS.toString()))) {
				return reCheckAmountMap;
			}
			orderJson.put(THIR_DPARTY_ID, datasMap.get(THIR_DPARTY_ID));
			Map<String, Object> reSaveOrderMap = saveOrderInfo(merchant, orderJson);
			if (!"1".equals(reSaveOrderMap.get(BaseCode.STATUS.toString()))) {
				return reSaveOrderMap;
			}
			return saveOrderGoodsInfo(merchant, orderGoodsList, entOrderNo);
		} catch (Exception e) {
			logger.error("--第三方订单信息错误-->", e);
			return ReturnInfoUtils.errorInfo("系统内部错误，请联系管理员！");
		}

	}

	/**
	 * 保存订单商品信息
	 * 
	 * @param merchant
	 *            商户实体信息
	 * @param orderGoodsList
	 *            订单商品信息集合
	 * @param entOrderNo
	 *            订单编号
	 * @return Map
	 */
	private Map<String, Object> saveOrderGoodsInfo(Merchant merchant, List<JSONObject> orderGoodsList,
			String entOrderNo) {
		if (merchant == null || orderGoodsList == null || StringEmptyUtils.isEmpty(entOrderNo)) {
			return ReturnInfoUtils.errorInfo("保存订单商品信息错误，请求参数不能为空！");
		}
		for (int i = 0; i < orderGoodsList.size(); i++) {
			JSONObject goodsJson = orderGoodsList.get(i);
			MorderSub goods = new MorderSub();
			String entGoodsNo = goodsJson.get("EntGoodsNo") + "";
			goods.setSeq(Integer.parseInt(goodsJson.get("Seq") + ""));
			goods.setEntGoodsNo(entGoodsNo);
			goods.setCIQGoodsNo(goodsJson.get("CIQGoodsNo") + "");
			goods.setCusGoodsNo(goodsJson.get("CusGoodsNo") + "");
			goods.setHSCode(goodsJson.get("HSCode") + "");
			goods.setGoodsName(goodsJson.get("GoodsName") + "");
			goods.setGoodsStyle(goodsJson.get("GoodsStyle") + "");
			goods.setOriginCountry(goodsJson.get("OriginCountry") + "");
			// goodsJson.get("GoodsDescribe");
			goods.setBrand(goodsJson.get("Brand") + "");
			goods.setQty(Integer.parseInt(goodsJson.get("Qty") + ""));
			// goodsJson.get("BarCode");
			goods.setUnit(goodsJson.get("Unit") + "");
			goods.setPrice(Double.parseDouble(goodsJson.get("Price") + ""));
			goods.setTotal(Double.parseDouble(goodsJson.get("Total") + ""));
			// goodsJson.get("CurrCode");
			// goodsJson.get("Notes");
			goods.setCreate_date(new Date());
			goods.setMerchant_no(merchant.getMerchantId());
			goods.setCreateBy(merchant.getMerchantName());
			goods.setDeleteFlag(0);
			goods.setOrder_id(entOrderNo);
			String spareParams = String.valueOf(goodsJson.get("spareParams"));
			Map<String, Object> reMap = setGoodsSpareParams(goods, spareParams);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			if (!orderDao.add(goods)) {
				return ReturnInfoUtils.errorInfo("商品自编号[" + entGoodsNo + "]保存失败,服务器繁忙！");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 保存商品的备用参数字段信息
	 * 
	 * @param goods
	 *            手工订单商品信息实体类
	 * @param spareParams
	 *            备用参数
	 * @return Map
	 */
	private Map<String, Object> setGoodsSpareParams(MorderSub goods, String spareParams) {
		if (StringEmptyUtils.isNotEmpty(spareParams)) {
			JSONObject json = null;
			try {
				json = JSONObject.fromObject(spareParams);
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("订单商品备用字段参数格式错误！");
			}
			// 商品归属商家代码
			String marCode = json.get("marCode") + "";
			String ebEntNo = json.get("ebEntNo") + "";
			String ebEntName = json.get("ebEntName") + "";
			// 电子口岸16位编码
			String dzknNo = json.get("DZKNNo") + "";
			JSONObject params = new JSONObject();
			if (StringEmptyUtils.isNotEmpty(marCode)) {
				params.put("marCode", marCode);
			}
			if (StringEmptyUtils.isNotEmpty(ebEntNo)) {
				params.put("ebEntNo", ebEntNo);
			}
			if (StringEmptyUtils.isNotEmpty(ebEntName)) {
				params.put("ebEntName", ebEntName);
			}
			if (StringEmptyUtils.isNotEmpty(dzknNo) && dzknNo.length() == 16) {
				params.put("DZKNNo", dzknNo);
			} else {
				return ReturnInfoUtils.errorInfo("电子口岸16位企业备案编号错误，请核实信息！");
			}
			if (!params.isEmpty()) {
				goods.setSpareParams(params.toString());
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 保存订单信息
	 * 
	 * @param merchant
	 *            商户实体类
	 * @param orderJson
	 *            订单信息
	 * @return Map
	 */
	private Map<String, Object> saveOrderInfo(Merchant merchant, JSONObject orderJson) {
		if (merchant == null || orderJson == null) {
			return ReturnInfoUtils.errorInfo("保存订单信息错误，请求参数不能为null");
		}
		String entOrderNo = orderJson.get("EntOrderNo") + "";
		Map<String, Object> params = new HashMap<>();
		params.put("order_id", entOrderNo);
		List<Morder> reList = orderDao.findByProperty(Morder.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询订单信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("订单编号[" + entOrderNo + "]已存在，请勿重复推送！");
		} else {
			Morder order = new Morder();
			order.setMerchant_no(merchant.getMerchantId());
			order.setOrder_id(orderJson.get("EntOrderNo") + "");
			// 币制
			order.setFcode(orderJson.get("OrderGoodTotalCurr") + "");
			try {
				order.setFCY(Double.parseDouble(orderJson.get("OrderGoodTotal") + ""));
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("订单商品总金额，参数格式错误！");
			}
			// 运费
			if (StringEmptyUtils.isNotEmpty(orderJson.get("freight"))) {
				try {
					order.setFreight(Double.parseDouble(orderJson.get("freight") + ""));
				} catch (Exception e) {
					return ReturnInfoUtils.errorInfo("订单运费，参数格式错误！");
				}
			}
			// order.setOtherPayment(otherPayment);
			// order.setOtherPayNotes(otherPayNotes);
			try {
				order.setTax(Double.parseDouble(orderJson.get("Tax") + ""));
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("订单税费，参数格式错误！");
			}
			try {
				order.setActualAmountPaid(Double.parseDouble(orderJson.get("ActualAmountPaid") + ""));
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("订单实际支付金额，参数格式错误！");
			}
			order.setRecipientName(orderJson.get("RecipientName") + "");
			order.setRecipientAddr(orderJson.get("RecipientAddr") + "");
			order.setRecipientID(orderJson.get("RecipientID") + "");
			order.setRecipientTel(orderJson.get("RecipientTel") + "");
			order.setRecipientProvincesCode(orderJson.get("RecipientProvincesCode") + "");
			// order.setRecipientCityCode(orderJson.get("RecipientCityCode") +
			// "");
			// order.setRecipientAreaCode(orderJson.get("RecipientAreaCode") +
			// "");
			order.setOrderDocAcount(orderJson.get("OrderDocAcount") + "");
			order.setOrderDocName(orderJson.get("OrderDocName") + "");
			order.setOrderDocType(orderJson.get("OrderDocType") + "");
			order.setOrderDocId(orderJson.get("OrderDocId") + "");
			order.setOrderDocTel(orderJson.get("OrderDocTel") + "");
			order.setOrderDate(orderJson.get("OrderDate") + "");
			order.setWaybill(orderJson.get("waybill") + "");
			// 删除标识:0-未删除,1-已删除
			order.setDel_flag(0);
			order.setCreate_date(new Date());
			order.setCreate_by(merchant.getMerchantName());
			// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
			order.setOrder_record_status(1);
			order.setDateSign(DateUtil.formatDate(new Date(), "yyyyMMdd"));
			order.setRemarks(orderJson.get("Notes") + "");
			order.setThirdPartyId(orderJson.get(THIR_DPARTY_ID) + "");
			// 订单口岸标识
			String eport = orderJson.get("eport") + "";
			order.setEport(eport);
			// 国检检疫机构代码
			String ciqOrgCode = orderJson.get("ciqOrgCode") + "";
			order.setCiqOrgCode(ciqOrgCode);
			// 海关关区代码
			String customsCode = orderJson.get("customsCode") + "";
			order.setCustomsCode(customsCode);
			Map<String, Object> reCheckMap = checkOrderBusinessType(order, merchant);
			if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
				return reCheckMap;
			}
			String tradeNo = reCheckMap.get(BaseCode.DATAS.toString()) + "";
			if (StringEmptyUtils.isNotEmpty(tradeNo)) {
				order.setTrade_no(tradeNo);
			}
			if (!orderDao.add(order)) {
				return ReturnInfoUtils.errorInfo("保存订单信息失败,服务器繁忙！");
			}
			return ReturnInfoUtils.successInfo();
		}
	}

	/**
	 * 校验订单业务类型，根据不同的类型进行线上或线下的订单针对处理
	 * 
	 * @param order
	 *            订单信息实体类
	 * @param merchant
	 *            商户信息实体类
	 * @return Map
	 */
	private Map<String, Object> checkOrderBusinessType(Morder order, Merchant merchant) {
		int thirdPartyFlag = merchant.getThirdPartyFlag();
		// 第三方标识：1-银盟(银盟商城平台),2-第三方商城平台
		if (thirdPartyFlag == 2 && !"MerchantId_00047".equals(merchant.getMerchantId())) {
			Map<String, Object> reBusinessMap = merchantUtils.getMerchantBusinessInfo(merchant.getMerchantId());
			if (!"1".equals(reBusinessMap.get(BaseCode.STATUS.toString()))) {
				return reBusinessMap;
			}
			MerchantBusinessContent business = (MerchantBusinessContent) reBusinessMap.get(BaseCode.DATAS.toString());
			// 推送类型：all-全部推送、orderRecord-订单推送、paymentRecord-支付单推送、goodsRecord-商品备案推送
			String pustType = business.getPushType();
			if ("all".equals(pustType) || "orderRecord".equals(pustType)) {
				order.setOrder_record_status(10);
				order.setStatus(1);
			}
			String businessType = business.getBusinessType();
			// 业务类型:all-全部,online-线上支付、offline-线下支付
			if ("all".equals(businessType) || "online".equals(businessType)) {
				return getPaymentInfo(order);
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 获取线上的支付单信息
	 * 
	 * @param order
	 *            订单信息实体类
	 * @return Map
	 */
	private Map<String, Object> getPaymentInfo(Morder order) {
		if (order == null) {
			return ReturnInfoUtils.errorInfo("获取支付信息错误，订单信息不能为null");
		}
		JSONObject json = null;
		Map<String, Object> item = new HashMap<>();
		// 1533028184696
		// item.put("out_trade_no", "1533028184696");
		item.put("out_trade_no", order.getOrder_id());
		String result = YmHttpUtil.HttpPost("https://ezpay.191ec.com/silver-web-ezpay/ympay/getTradeNo", item);
		if (StringEmptyUtils.isEmpty(result)) {
			return ReturnInfoUtils.errorInfo("查询交易信息失败,服务器繁忙！");
		} else {
			json = JSONObject.fromObject(result);
			String status = json.get("status") + "";
			if ("-1".equals(status)) {
				return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]未找到对应的支付信息！");
			} else if (!"1".equals(status)) {
				return json;
			}
			List<Object> listJson = (List<Object>) json.get("datas");
			Map<String, Object> paymentMap = (Map<String, Object>) listJson.get(0);
			// amount
			double amount = Double.parseDouble(paymentMap.get("amount") + "");
			String tradeNo = paymentMap.get("trade_no") + "";
			if (order.getActualAmountPaid() != amount) {
				return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]中实际支付金额为["
						+ order.getActualAmountPaid() + "],与支付信息中支付金额[" + amount + "]不一致,请核对订单信息！");
			}
			JSONObject json2 = JSONObject.fromObject(paymentMap.get("update_date"));
			String payTime = json2.get("time") + "";
			if (StringEmptyUtils.isEmpty(payTime)) {
				return ReturnInfoUtils.errorInfo("支付单时间异常，请联系管理员！");
			}
			paymentMap.clear();
			paymentMap.put("merchantId", order.getMerchant_no());
			paymentMap.put("tradeNo", tradeNo);
			paymentMap.put("orderId", order.getOrder_id());
			paymentMap.put("amount", order.getActualAmountPaid());
			paymentMap.put("orderDocName", order.getOrderDocName());
			paymentMap.put("orderDocId", order.getOrderDocId());
			paymentMap.put("orderDocTel", order.getOrderDocTel());
			paymentMap.put("orderDate", order.getOrderDate());
			paymentMap.put("createBy", order.getCreate_by());
			paymentMap.put("eport", order.getEport());
			paymentMap.put("ciqOrgCode", order.getCiqOrgCode());
			paymentMap.put("customsCode", order.getCustomsCode());
			paymentMap.put(THIR_DPARTY_ID, order.getThirdPartyId());
			paymentMap.put("payTime", DateUtil.formatDate(new Date(Long.parseLong(payTime)), "yyyy-MM-dd HH:mm:ss"));
			// 申报状态：1-待申报、2-申报中、3-申报成功、4-申报失败、10-申报中(待系统处理)
			paymentMap.put("pay_record_status", 10);
			if (!paymentService.addEntity(paymentMap)) {
				return ReturnInfoUtils.errorInfo("保存支付信息异常，请联系管理员！");
			}
			return ReturnInfoUtils.successDataInfo(tradeNo);
		}
	}

	/**
	 * 校验订单商品金额
	 * 
	 * @param orderJson
	 *            订单信息
	 * @param orderGoodsList
	 *            订单商品信息集合
	 * @return Map
	 */
	private Map<String, Object> checkAmount(JSONObject orderJson, List<JSONObject> orderGoodsList) {
		DecimalFormat df = new DecimalFormat("#.00");
		String entOrderNo = orderJson.get("EntOrderNo") + "";
		// 商品总金额
		double goodsTotal = 0.0;
		// 订单信息中商品总金额
		double orderGoodTotal = 0.0;
		// 税费
		double tax;
		// 实际支付金额
		double actualAmountPaid;
		try {
			orderGoodTotal = Double.parseDouble(orderJson.get("OrderGoodTotal") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单商品总金额,参数格式错误！");
		}
		try {
			tax = Double.parseDouble(orderJson.get("Tax") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单税费,参数格式错误！");
		}
		try {
			actualAmountPaid = Double.parseDouble(orderJson.get("ActualAmountPaid") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单实际支付金额,参数格式错误！");
		}
		//
		for (int i = 0; i < orderGoodsList.size(); i++) {
			int count = 0;
			double price;
			double total;
			JSONObject goodsJson = orderGoodsList.get(i);
			String entGoodsNo = goodsJson.get("EntGoodsNo") + "";
			try {
				count = Integer.parseInt(goodsJson.get("Qty") + "");
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo(
						"订单号[" + entOrderNo + "]中关联商品自编号[" + entGoodsNo + "]，数量[" + goodsJson.get("Qty") + "]格式错误!");
			}
			try {
				price = Double.parseDouble(goodsJson.get("Price") + "");
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo(
						"订单号[" + entOrderNo + "]中关联商品自编号[" + entGoodsNo + "]，单价[" + goodsJson.get("Price") + "]格式错误!");
			}
			try {
				total = Double.parseDouble(goodsJson.get("Total") + "");
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("订单号[" + entOrderNo + "]中关联商品自编号[" + entGoodsNo + "]，商品总金额["
						+ goodsJson.get("Total") + "]格式错误!");
			}
			// 由于出现浮点数,故而得出的商品总金额只保留后两位，其余全部舍弃
			double temToal = Double.parseDouble(df.format(count * price));
			if (temToal != total) {
				return ReturnInfoUtils.errorInfo("订单号[" + entOrderNo + "]中关联商品自编号[" + entGoodsNo + "]商品总金额为：" + total
						+ ",与" + count + "(数量)*" + price + "(单价)=" + temToal + "(商品总金额)不对等！");
			}
			//
			goodsTotal += temToal;
		}
		// 由于出现浮点数,故而得出的商品总金额只保留后两位
		goodsTotal = Double.parseDouble(df.format(goodsTotal));
		// 判断订单商品总金额是否与计算出来的订单信息中商品总金额是否一致
		if (orderGoodTotal != goodsTotal) {
			return ReturnInfoUtils.errorInfo(
					"订单[" + entOrderNo + "]中填写的商品总金额[" + orderGoodTotal + "]与实际商品总金额[" + goodsTotal + "]不对等！");
		}
		// 判断商品总金额+税费是否等于实际金额
		if ((goodsTotal + tax) != actualAmountPaid) {
			return ReturnInfoUtils.errorInfo("订单[" + entOrderNo + "]中订单中填写的" + actualAmountPaid + "(实际支付金额)与"
					+ goodsTotal + "(订单商品金额)+" + tax + "(税费)=" + (goodsTotal + tax) + "(实际支付金额)不对等！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验订单信息
	 * 
	 * @param orderJson
	 *            订单信息json串
	 * @return Map
	 */
	private Map<String, Object> checkOrderInfo(JSONObject orderJson) {
		JSONArray datas = new JSONArray();
		datas.add(orderJson);
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("EntOrderNo");
		noNullKeys.add("OrderStatus");
		noNullKeys.add("PayStatus");
		noNullKeys.add("OrderGoodTotal");
		noNullKeys.add("OrderGoodTotalCurr");
		noNullKeys.add("Freight");
		noNullKeys.add("Tax");
		noNullKeys.add("OtherPayment");
		noNullKeys.add("ActualAmountPaid");
		noNullKeys.add("RecipientName");
		noNullKeys.add("RecipientAddr");
		noNullKeys.add("RecipientTel");
		noNullKeys.add("RecipientCountry");
		noNullKeys.add("RecipientProvincesCode");
		noNullKeys.add("OrderDocAcount");
		noNullKeys.add("OrderDocName");
		noNullKeys.add("OrderDocType");
		noNullKeys.add("OrderDocId");
		noNullKeys.add("OrderDocTel");
		noNullKeys.add("OrderDate");

		noNullKeys.add("eport");
		noNullKeys.add("ciqOrgCode");
		noNullKeys.add("customsCode");
		Map<String, Object> reCheckMap = CheckDatasUtil.changeOrderMsg(datas, noNullKeys);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()) + "")) {
			return reCheckMap;
		}
		//订单自编号
		String entOrderNo = orderJson.get("EntOrderNo")+"";
		String countryCode = orderJson.get("RecipientCountry") + "";
		Map<String, Object> reCheckCountryMap = checkCountry(countryCode);
		if (!"1".equals(reCheckCountryMap.get(BaseCode.STATUS.toString()) + "")) {
			return reCheckCountryMap;
		}
		String provinceCode = orderJson.get("RecipientProvincesCode") + "";
		Map<String, Object> reCheckProvinceMap = checkProvinceCode(provinceCode);
		if (!"1".equals(reCheckProvinceMap.get(BaseCode.STATUS.toString()))) {
			return reCheckProvinceMap;
		}
		try {
			double orderGoodTotal = Double.parseDouble(orderJson.get("OrderGoodTotal") + "");
			if (orderGoodTotal > 2000) {
				return ReturnInfoUtils.errorInfo("订单号["+entOrderNo+"]商品总金额超过2000！");
			}
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单号["+entOrderNo+"]商品总金额参数格式错误！");
		}
		// 下单人身份证号码
		String orderDocId = orderJson.get("OrderDocId") + "";
		if (!IdcardValidator.validate18Idcard(orderDocId)) {
			return ReturnInfoUtils.errorInfo("订单号["+entOrderNo+"]下单人身份证号码错误！");
		}
		if (orderDocId.contains("x")) {
			return ReturnInfoUtils.errorInfo("订单号["+entOrderNo+"]下单人身份证号码必须是大写的[X]");
		}
		String recipientTel = orderJson.get("RecipientTel") + "";
		if (!PhoneUtils.isPhone(recipientTel)) {
			return ReturnInfoUtils.errorInfo("订单号["+entOrderNo+"]收货人手机号码错误！");
		}
		String orderDocTel = orderJson.get("OrderDocTel") + "";
		if (!PhoneUtils.isPhone(orderDocTel)) {
			return ReturnInfoUtils.errorInfo("订单号["+entOrderNo+"]下单人手机号码错误！");
		}
		String orderDocName = orderJson.get("OrderDocName") + "";
		if (!StringUtil.isChinese(orderDocName) || orderDocName.contains("先生") || orderDocName.contains("女士")
				|| orderDocName.contains("小姐")) {
			return ReturnInfoUtils.errorInfo("订单号["+entOrderNo+"]下单人姓名错误！");
		}
		if(DateUtil.parseDate(orderJson.get("OrderDate")+"", "yyyyMMddHHmmss") == null){
			return ReturnInfoUtils.errorInfo("订单号["+entOrderNo+"]下单日期错误！");
		}
		if (StringEmptyUtils.isNotEmpty(orderJson.get("otherPayment"))) {
			// 抵付金额
			double otherPayment = Double.parseDouble(orderJson.get("otherPayment") + "");
			if (otherPayment > 0) {
				String otherPayNotes = orderJson.get("otherPayNotes") + "";
				if (StringEmptyUtils.isEmpty(otherPayNotes)) {
					return ReturnInfoUtils.errorInfo("订单号["+entOrderNo+"]抵付金额为[" + otherPayment + "]，抵付说明不能为空！");
				}
			}
		}
		return checkOrderEport(orderJson);
	}

	/**
	 * 校验订单对应口岸信息是否符合要求
	 * 
	 * @param orderJson
	 *            订单信息
	 * @return Map
	 */
	private Map<String, Object> checkOrderEport(JSONObject orderJson) {
		// 订单口岸标识
		String eport = orderJson.get("eport") + "";
		// 国检检疫机构代码
		String ciqOrgCode = orderJson.get("ciqOrgCode") + "";
		// 海关关区代码
		String customsCode = orderJson.get("customsCode") + "";
		switch (eport) {
		case "1":
			if (customsPortService.checkCCIQ(ciqOrgCode) && customsPortService.checkGAC(customsCode)) {
				return ReturnInfoUtils.successInfo();
			} else {
				return ReturnInfoUtils.errorInfo("电子口岸对应的海关代码与国检检疫机构代码错误,请核对信息!");
			}
		case "2":
			// 暂定只有南沙旅检
			if ("000069".equals(ciqOrgCode) && "5165".equals(customsCode)) {
				return ReturnInfoUtils.successInfo();
			} else {
				return ReturnInfoUtils.errorInfo("智检对应的海关代码与国检检疫机构代码错误,请核对信息!");
			}
		default:
			return ReturnInfoUtils.errorInfo("口岸标识暂未支持[" + eport + "],请核对信息!");
		}
	}

	/**
	 * 根据省份Code校验省份信息是否准确
	 * 
	 * @param provinceCode
	 *            省份Code
	 * @return Map
	 */
	private Map<String, Object> checkProvinceCode(String provinceCode) {
		List<Province> reList = orderDao.findByProperty(Province.class, null, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询省份信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			for (int i = 0; i < reList.size(); i++) {
				Province province = reList.get(i);
				if (provinceCode.equals(province.getProvinceCode())) {
					return ReturnInfoUtils.successInfo();
				}
			}
			return ReturnInfoUtils.errorInfo("收货人行政区代码[" + provinceCode + "]海关暂未支持，请参照跨境公共平台代码表！");
		}
		return ReturnInfoUtils.errorInfo("暂无省份数据!");
	}

	/**
	 * 根据国家代码校验国家信息是否准确
	 * 
	 * @param countryCode
	 *            国家代码
	 * @return Map
	 */
	private Map<String, Object> checkCountry(String countryCode) {
		Map<String, Object> countryMap = getCountry();
		if (!"1".equals(countryMap.get(BaseCode.STATUS.toString()))) {
			return countryMap;
		}
		List<Country> countryList = (List<Country>) countryMap.get(BaseCode.DATAS.toString());
		if (countryList != null && !countryList.isEmpty()) {
			for (int i = 0; i < countryList.size(); i++) {
				Country country = countryList.get(i);
				if (countryCode.equals(country.getCountryCode())) {
					return ReturnInfoUtils.successInfo();
				}
			}
			return ReturnInfoUtils.errorInfo("收货人所在国[" + countryCode + "]海关暂未支持，请参照跨境公共平台代码表！");
		}
		return ReturnInfoUtils.errorInfo("国家信息查询失败!");
	}

	/**
	 * 获取所有国家信息
	 * 
	 * @return Map
	 */
	private Map<String, Object> getCountry() {
		List<Country> countryList = null;
		byte[] redisByte = JedisUtil.get(RedisKey.SHOP_KEY_COUNTRY_LIST.getBytes(), 3600);
		if (redisByte != null) {
			countryList = (List<Country>) SerializeUtil.toObject(redisByte);
			return ReturnInfoUtils.successDataInfo(countryList, 0);
		} else {
			Map<String, Object> countryMap = countryService.findAllCountry();
			if (!"1".equals(countryMap.get(BaseCode.STATUS.toString()))) {
				return countryMap;
			}
			JedisUtil.set(RedisKey.SHOP_KEY_COUNTRY_LIST.getBytes(),
					SerializeUtil.toBytes(countryMap.get(BaseCode.DATAS.toString())), 3600);
			return countryMap;
		}
	}

	/**
	 * 准备开始检查商品信息
	 * 
	 * @param entOrderNo
	 *            订单编号
	 * @param orderGoodsList
	 *            订单商品信息集合
	 * @return Map
	 */
	private Map<String, Object> checkGoods(String entOrderNo, List<JSONObject> orderGoodsList) {
		if (orderGoodsList != null && !orderGoodsList.isEmpty()) {
			for (int i = 0; i < orderGoodsList.size(); i++) {
				JSONObject goodsJson = orderGoodsList.get(i);
				Map<String, Object> reCheckGoodsMap = checkGoodsInfo(goodsJson);
				if (!"1".equals(reCheckGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
					return ReturnInfoUtils.errorInfo("订单号[" + entOrderNo + "]关联商品自编号为[" + goodsJson.get("EntGoodsNo")
							+ "]商品信息中" + reCheckGoodsMap.get(BaseCode.MSG.toString()));
				}
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("订单号[" + entOrderNo + "]关联订单商品信息不能为空,请核对信息!");

	}

	/**
	 * 校验商品信息
	 * 
	 * @param goodsInfo
	 *            商品信息
	 * @return Map
	 */
	private Map<String, Object> checkGoodsInfo(JSONObject goodsInfo) {
		JSONArray datas = new JSONArray();
		datas.add(goodsInfo);
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("Seq");
		noNullKeys.add("EntGoodsNo");
		noNullKeys.add("CIQGoodsNo");
		noNullKeys.add("CusGoodsNo");
		noNullKeys.add("GoodsName");
		noNullKeys.add("GoodsStyle");
		noNullKeys.add("OriginCountry");
		noNullKeys.add("Qty");
		noNullKeys.add("HSCode");
		noNullKeys.add("Brand");
		noNullKeys.add("Unit");
		noNullKeys.add("Price");
		noNullKeys.add("Total");
		noNullKeys.add("CurrCode");
		Map<String, Object> reCheckMap = CheckDatasUtil.changeMsg(datas, noNullKeys);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()) + "")) {
			return reCheckMap;
		}
		String countryCode = goodsInfo.get("OriginCountry") + "";
		Map<String, Object> reCheckCountryMap = checkCountry(countryCode);
		if (!"1".equals(reCheckCountryMap.get(BaseCode.STATUS.toString()) + "")) {
			return ReturnInfoUtils.errorInfo("原产国[" + countryCode + "]未找到对应的国家信息,请核对信息!");
		}
		String unitCode = goodsInfo.get("Unit") + "";
		Map<String, Object> reCheckUnitMap = checkUnit(unitCode);
		if (!"1".equals(reCheckUnitMap.get(BaseCode.STATUS.toString()) + "")) {
			return reCheckUnitMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据计量单位Code校验计量单位信息是否准确
	 * 
	 * @param unitCode
	 *            计量单位代码
	 * @return Map
	 */
	private Map<String, Object> checkUnit(String unitCode) {
		List<Metering> meteringList = getMetering();
		if (meteringList != null && !meteringList.isEmpty()) {
			for (int i = 0; i < meteringList.size(); i++) {
				Metering metering = meteringList.get(i);
				if (unitCode.equals(metering.getMeteringCode())) {
					return ReturnInfoUtils.successInfo();
				}
			}
			return ReturnInfoUtils.errorInfo("计量单位[" + unitCode + "]未找到对应的计量单位信息,请核对信息!");
		}
		return ReturnInfoUtils.errorInfo("计量单位查询失败!");
	}

	/**
	 * 获取所有计量单位信息
	 * 
	 * @return List
	 */
	private List<Metering> getMetering() {
		List<Metering> meteringList = null;
		byte[] redisByte = JedisUtil.get(RedisKey.SHOP_KEY_METERING_LIST.getBytes(), 3600);
		if (redisByte != null) {
			meteringList = (List<Metering>) SerializeUtil.toObject(redisByte);
			return meteringList;
		} else {
			List<Metering> reList = orderDao.findByProperty(Metering.class, null, 0, 0);
			if (reList != null && !reList.isEmpty()) {
				// 将查询出来的数据放入到缓存中
				JedisUtil.set(RedisKey.SHOP_KEY_METERING_LIST.getBytes(), SerializeUtil.toBytes(reList), 3600);
				return reList;
			}
		}
		return null;
	}

	@Override
	public Map<String, Object> getAlreadyDelOrderInfo(Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalMOrderSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List<OldManualOrder> mlist = orderDao.findByPropertyLike(OldManualOrder.class, paramMap, null, page, size);
		long count = orderDao.findByPropertyLikeCount(OldManualOrder.class, paramMap, null);
		if (mlist == null) {
			return ReturnInfoUtils.errorInfo("查询订单信息失败,服务器繁忙!");
		} else if (!mlist.isEmpty()) {
			List<Object> list = new ArrayList<>();
			for (OldManualOrder manualOrder : mlist) {
				Map<String, Object> item = new HashMap<>();
				paramMap.clear();
				paramMap.put("order_id", manualOrder.getOrder_id());
				List<OldManualOrderSub> goodsList = orderDao.findByPropertyLike(OldManualOrderSub.class, paramMap, null,
						page, size);
				item.put("head", manualOrder);
				item.put("content", goodsList);
				list.add(item);
			}
			return ReturnInfoUtils.successDataInfo(list, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Object getThirdPartyInfo(Map<String, Object> datasMap) {
		if (datasMap != null && !datasMap.isEmpty()) {
			Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(datasMap.get("merchantId") + "");
			if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
				return reMerchantMap;
			}
			Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
			String thirdPartyId = datasMap.get(THIR_DPARTY_ID) + "";
			return thirdPartyOrderInfo(merchant.getMerchantId(), thirdPartyId);
		}
		return ReturnInfoUtils.errorInfo("请求参数不能为空!");
	}

	/**
	 * 第三方平获取订单信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param thirdPartyId
	 * @return Map
	 */
	private Map<String, Object> thirdPartyOrderInfo(String merchantId, String thirdPartyId) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchant_no", merchantId);
		params.put(THIR_DPARTY_ID, thirdPartyId);
		List<Morder> reOrderList = orderDao.findByProperty(Morder.class, params, 1, 1);
		if (reOrderList == null) {
			return ReturnInfoUtils.errorInfo("查询订单信息失败,服务器繁忙!");
		} else if (!reOrderList.isEmpty()) {
			List<Object> list = new ArrayList<>();
			for (Morder order : reOrderList) {
				Map<String, Object> item = new HashMap<>();
				String orderId = order.getOrder_id();
				params.clear();
				params.put("order_id", orderId);
				List<MorderSub> goodsList = orderDao.findByProperty(MorderSub.class, params, 0, 0);
				item.put("head", order);
				item.put("content", goodsList);
				list.add(item);
			}
			return ReturnInfoUtils.successDataInfo(list);
		} else {
			return ReturnInfoUtils.errorInfo("没有找到订单信息!");
		}
	}

	@Override
	public Map<String, Object> checkOrderPort(List<String> orderIDs, String merchantId) {
		if (orderIDs == null || orderIDs.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		List<Morder> cacheList = new ArrayList<>();
		for (int i = 0; i < orderIDs.size(); i++) {
			String orderId = orderIDs.get(i);
			Map<String, Object> params = new HashMap<>();
			params.put("order_id", orderId);
			params.put("merchant_no", merchantId);
			List<Morder> reList = orderDao.findByProperty(Morder.class, params, 0, 0);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询订单信息失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {
				Morder order = reList.get(0);
				String eport = order.getEport();
				String ciqOrgCode = order.getCiqOrgCode();
				String customsCode = order.getCustomsCode();
				if (StringEmptyUtils.isNotEmpty(eport) && StringEmptyUtils.isNotEmpty(ciqOrgCode)
						&& StringEmptyUtils.isNotEmpty(customsCode)) {
					cacheList.add(order);
				}
				for (int c = 0; c < cacheList.size(); c++) {
					Morder cacheOrder = cacheList.get(c);
					String cacheEport = cacheOrder.getEport();
					String cacheCiqOrgCode = cacheOrder.getCiqOrgCode();
					String cacheCustomsCode = cacheOrder.getCustomsCode();
					if (!cacheEport.equals(eport) && !cacheCiqOrgCode.equals(ciqOrgCode)
							&& !cacheCustomsCode.equals(customsCode)) {
						return ReturnInfoUtils.errorInfo("所选订单为多个不同的口岸/海关关区/国检检疫机构信息,请重新选择!");
					}
				}
			} else {
				return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]未找到订单信息,请重新选择!");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> managerGetOrderReportInfo(String startDate, String endDate, String merchantId) {
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("startDate", startDate);
		paramsMap.put("endDate", endDate);
		paramsMap.put("merchantId", merchantId);
		Table reList = orderDao.getOrderDailyReportInfo(paramsMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reList).getJSONArray("rows"));
		} else {
			return ReturnInfoUtils.errorInfo("暂无报表数据!");
		}
	}

	@Override
	public Map<String, Object> managerGetOrderReportDetails(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return ReturnInfoUtils.errorInfo("参数不能为空!");
		}
		Table reList = orderDao.getOrderDailyReportetDetails(params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			return ReturnInfoUtils.successDataInfo(Transform.tableToJson(reList).getJSONArray("rows"));
		} else {
			return ReturnInfoUtils.errorInfo("暂无报表数据!");
		}
	}

	@Override
	public Map<String, Object> thirdPromoteBusiness(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("下单参数不能为空!");
		}

		Map<String, Object> rePhoneMap = checkPhoneVerificationCode(datasMap);
		if (!"1".equals(rePhoneMap.get(BaseCode.STATUS.toString()))) {
			return rePhoneMap;
		}

		// 校验参数
		Map<String, Object> reCheckMap = checkDatas(datasMap);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		// 校验商品信息
		Map<String, Object> reGoodsMap = checkGoodsDatas(datasMap);
		if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()))) {
			return reGoodsMap;
		}
		// 库存信息
		StockContent stock = (StockContent) reGoodsMap.get(BaseCode.DATAS.toString());
		// 校验身份证号码
		Map<String, Object> reCheckIdcardMap = checkIdCardInfo(datasMap);
		if (!"1".equals(reCheckIdcardMap.get(BaseCode.STATUS.toString()))) {
			return reCheckIdcardMap;
		}
		String memberId = reCheckIdcardMap.get(BaseCode.DATAS.toString()) + "";
		// 添加收货人地址信息
		Map<String, Object> reRecipientMap = addRecipientInfo(datasMap, memberId);
		if (!"1".equals(reRecipientMap.get(BaseCode.STATUS.toString()))) {
			return reRecipientMap;
		}
		RecipientContent recipient = (RecipientContent) reRecipientMap.get(BaseCode.DATAS.toString());
		Map<String, Object> reOrderHeadMap = saveOrderHead(stock, recipient);
		if (!"1".equals(reOrderHeadMap.get(BaseCode.STATUS.toString()))) {
			return reOrderHeadMap;
		}
		OrderContent order = (OrderContent) reOrderHeadMap.get(BaseCode.DATAS.toString());
		int count = Integer.parseInt(datasMap.get("count") + "");
		Map<String, Object> reOrderGoodsMap = saveOrderContent(order, stock, datasMap.get(ENT_GOODS_NO) + "", count);
		if (!"1".equals(reOrderGoodsMap.get(BaseCode.STATUS.toString()))) {
			return reOrderGoodsMap;
		}
		return ReturnInfoUtils.successDataInfo("https://ym.191ec.com/silver-web-shop/yspay/shoppingPayment?entOrderNo=" + order.getEntOrderNo());
		//return ReturnInfoUtils.successDataInfo(
			//	"https://ym.191ec.com/silver-web-shop/yspay/dopay?entOrderNo=" + order.getEntOrderNo());
	}

	/**
	 * 保存订单商品信息
	 * 
	 * @param order
	 *            订单头信息
	 * @param stock
	 *            库存信息
	 * @param entGoodsNo
	 *            商品自编号
	 * @param count
	 *            下单商品数量
	 * @return Map
	 */
	private Map<String, Object> saveOrderContent(OrderContent order, StockContent stock, String entGoodsNo, int count) {
		OrderGoodsContent orderGoods = new OrderGoodsContent();
		orderGoods.setMerchantId(order.getMerchantId());
		orderGoods.setMerchantName(order.getMerchantName());
		orderGoods.setMemberId(order.getMemberId());
		orderGoods.setMemberName(order.getMemberName());
		orderGoods.setOrderId(order.getOrderId());
		Map<String, Object> params = new HashMap<>();
		params.put(ENT_GOODS_NO, entGoodsNo);
		// 删除标识:0-未删除,1-已删除
		params.put("deleteFlag", 0);
		List<GoodsRecordDetail> reList = orderDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			GoodsRecordDetail goods = reList.get(0);
			orderGoods.setGoodsId(goods.getGoodsDetailId());
			orderGoods.setGoodsName(goods.getSpareGoodsName());
			orderGoods.setGoodsPrice(stock.getRegPrice());
			orderGoods.setGoodsCount(count);
			orderGoods.setGoodsTotalPrice(count * stock.getRegPrice());
			orderGoods.setGoodsImage(goods.getSpareGoodsImage());
		} else {
			return ReturnInfoUtils.errorInfo("查询商品备案信息失败,服务器繁忙!");
		}
		orderGoods.setTax(0.0);
		orderGoods.setLogisticsCosts(0.0);
		orderGoods.setCreateBy(order.getMemberName());
		orderGoods.setCreateDate(new Date());
		orderGoods.setEntGoodsNo(entGoodsNo);
		orderGoods.setEntOrderNo(order.getEntOrderNo());
		orderGoods.setEvaluationFlag(0);
		if (!orderDao.add(orderGoods)) {
			return ReturnInfoUtils.errorInfo("添加订单商品信息失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 保存商城订单头信息
	 * 
	 * @param stock
	 *            库存信息
	 * @param recipient
	 *            收获地址信息
	 * @return Map
	 */
	private Map<String, Object> saveOrderHead(StockContent stock, RecipientContent recipient) {
		if (stock == null || recipient == null) {
			return ReturnInfoUtils.errorInfo("保存订单头时,请求参数不能为空!");
		}
		OrderContent order = new OrderContent();
		order.setMerchantId(stock.getMerchantId());
		order.setMerchantName(stock.getMerchantName());
		order.setMemberId(recipient.getMemberId());
		order.setMemberName(recipient.getMemberName());
		order.setOrderId(createOrderId(1));
		order.setFreight(0);
		int count = 0;
		double totalPrice = 0;
		// 获取前面存储进备用字段的用户下单商品数量
		count = Integer.parseInt(stock.getReMark());
		double price = stock.getRegPrice();
		totalPrice += count * price;
		order.setOrderTotalPrice(totalPrice);
		// 订单状态：1-待付款,2-已付款;3-商户待处理;4-交易关闭,5-交易成功
		order.setStatus(1);
		order.setCreateBy(recipient.getMemberName());
		order.setCreateDate(new Date());
		order.setReceiptId(recipient.getRecipientId());
		order.setRecipientName(recipient.getRecipientName());
		order.setRecipientCardId(recipient.getRecipientCardId());
		order.setRecipientTel(recipient.getRecipientTel());
		order.setRecipientCountryName(recipient.getRecipientCountryName());
		order.setRecipientCountryCode(recipient.getRecipientCountryCode());
		order.setRecProvincesCode(recipient.getRecProvincesCode());
		order.setRecProvincesName(recipient.getRecProvincesName());
		order.setRecCityCode(recipient.getRecCityCode());
		order.setRecCityName(recipient.getRecCityName());
		order.setRecAreaName(recipient.getRecAreaName());
		order.setRecAreaCode(recipient.getRecAreaCode());
		order.setRecipientAddr(recipient.getRecipientAddr());
		// 物流状态：0-待发货,1-快件运输中,2-快件已签收
		order.setEhsStatus(0);
		// order.setWbEhsentName(wbEhsentName);
		// 来源标识：1-银盟商城、2-第三方推广
		order.setSourceFlag(2);
		order.setEntOrderNo(createOrderId(2));

		if (!orderDao.add(order)) {
			return ReturnInfoUtils.errorInfo("保存订单头失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successDataInfo(order);
	}

	/**
	 * 校验手机验证码是否正确
	 * 
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> checkPhoneVerificationCode(Map<String, Object> datasMap) {
		// 手机号码
		String phone = datasMap.get("phone") + "";
		String verificationCode = datasMap.get("verificationCode") + "";
		if (StringEmptyUtils.isEmpty(verificationCode)) {
			return ReturnInfoUtils.errorInfo("手机验证码不能为空!");
		}
		// 获取缓存中用户注册手机验证码
		String redis = JedisUtil.get(RedisKey.SHOP_KEY_THIRD_PROMOTE_BUSINESS_CAPTCHA_CODE + phone);
		if (StringEmptyUtils.isNotEmpty(redis)) {
			JSONObject json = JSONObject.fromObject(redis);
			String code = json.get("code") + "";
			if (verificationCode.equals(code)) {
				return ReturnInfoUtils.successInfo();
			}
		}
		return ReturnInfoUtils.errorInfo("手机验证码错误,请重新输入！");

	}

	/**
	 * 添加收货人地址信息
	 * 
	 * @param datasMap
	 * @param memberId
	 *            用户Id
	 * @return
	 */
	private Map<String, Object> addRecipientInfo(Map<String, Object> datasMap, String memberId) {
		// 下单人姓名=身份证号码
		String idcard = datasMap.get("idcard") + "";
		// 下单人姓名
		String idName = datasMap.get("idName") + "";
		// 手机号码
		String phone = datasMap.get("phone") + "";
		Map<String, Object> params = new HashMap<>();
		params.put("recipientCardId", idcard);
		params.put("recipientName", idName);
		params.put("recipientTel", phone);
		params.put("recipientAddr", datasMap.get("recipientAddr") + "".trim());
		List<RecipientContent> reList = orderDao.findByProperty(RecipientContent.class, params, 0, 0);
		// 当根据身份证、姓名、手机号码、详细地址查询收货人地址已存在时则无需重复添加收货人信息
		if (reList != null && !reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList.get(0));
		} else {
			RecipientContent recipient = new RecipientContent();
			recipient.setMemberId(memberId);
			recipient.setMemberName(idcard);
			if (!StringUtil.isContainChinese(idName.replaceAll("·", ""))) {
				return ReturnInfoUtils.errorInfo("收货人姓名错误,请重新输入");
			}
			recipient.setRecipientName(idName);
			if (!IdcardValidator.validate18Idcard(idcard)) {
				return ReturnInfoUtils.errorInfo("收货人身份证号码错误,请重新输入!");
			}
			recipient.setRecipientCardId(idcard);
			if (!PhoneUtils.isPhone(phone)) {
				return ReturnInfoUtils.errorInfo("收货人手机号码不正确,请重新输入!");
			}
			recipient.setRecipientTel(phone);
			recipient.setRecipientCountryName("中国");
			recipient.setRecipientCountryCode("142");
			String recProvincesName = datasMap.get("recProvincesName") + "";
			recipient.setRecProvincesName(recProvincesName);
			Map<String, Object> reProvincesMap = setRecProvincesCode(recipient);
			if (!"1".equals(reProvincesMap.get(BaseCode.STATUS.toString()))) {
				return reProvincesMap;
			}
			recipient.setRecCityName(datasMap.get("recCityName") + "");
			Map<String, Object> reCityMap = setRecCityCode(recipient);
			if (!"1".equals(reCityMap.get(BaseCode.STATUS.toString()))) {
				return reCityMap;
			}
			recipient.setRecAreaName(datasMap.get("recAreaName") + "");
			Map<String, Object> reAreaMap = setRecAreaCode(recipient);
			if (!"1".equals(reAreaMap.get(BaseCode.STATUS.toString()))) {
				return reAreaMap;
			}
			String recipientAddr = datasMap.get("address") + "";
			if (StringEmptyUtils.isEmpty(recipientAddr)) {
				return ReturnInfoUtils.errorInfo("详细地址不能为空!");
			}
			recipient.setRecipientAddr(recipientAddr.trim());
			recipient.setCreateBy(idName);
			recipient.setCreateDate(new Date());
			// 删除标识
			recipient.setDeleteFlag(0);
			List<RecipientContent> cacheList = new ArrayList<>();
			cacheList.add(recipient);
			return recipientService.saveRecipientContent(cacheList);
		}
	}

	/**
	 * 根据区域中文名称,查询对应的城市编码
	 * 
	 * @param recipient
	 *            收货地址信息实体类
	 * @return Map
	 */
	private Map<String, Object> setRecAreaCode(RecipientContent recipient) {
		String recAreaName = recipient.getRecAreaName();
		if (StringEmptyUtils.isEmpty(recAreaName)) {
			return ReturnInfoUtils.errorInfo("区域名称不能为空!");
		}
		String str = "";
		Map<String, Object> params = new HashMap<>();
		params.put("areaName", recAreaName.trim());
		List<Area> reList = orderDao.findByProperty(Area.class, params, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (Area area : reList) {
				if (area.getAreaName().equals(recAreaName.trim())) {
					str = area.getAreaCode();
				}
			}
		}
		if (StringEmptyUtils.isEmpty(str)) {
			return ReturnInfoUtils.errorInfo("查询地区编码错误!");
		} else {
			recipient.setRecAreaCode(str);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据城市中文名称,查询对应的城市编码
	 * 
	 * @param recipient
	 *            收货地址信息实体类
	 * @return Map
	 */
	private Map<String, Object> setRecCityCode(RecipientContent recipient) {
		String recCityName = recipient.getRecCityName();
		if (StringEmptyUtils.isEmpty(recCityName)) {
			return ReturnInfoUtils.errorInfo("城市名称不能为空!");
		}
		String str = "";
		Map<String, Object> params = new HashMap<>();
		params.put("cityName", recCityName.trim());
		List<City> reList = orderDao.findByProperty(City.class, params, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (City city : reList) {
				if (city.getCityName().equals(recCityName.trim())) {
					str = city.getCityCode();
				}
			}
		}
		if (StringEmptyUtils.isEmpty(str)) {
			return ReturnInfoUtils.errorInfo("查询城市编码错误!");
		} else {
			recipient.setRecCityCode(str);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据省份中文名称,查询对应的省份编码
	 * 
	 * @param recipient
	 *            收货地址信息实体类
	 * @return Map
	 */
	private Map<String, Object> setRecProvincesCode(RecipientContent recipient) {
		String recProvincesName = recipient.getRecProvincesName();
		if (StringEmptyUtils.isEmpty(recProvincesName)) {
			return ReturnInfoUtils.errorInfo("省份名称不能为空!");
		}
		String str = "";
		Map<String, Object> params = new HashMap<>();
		params.put("provinceName", recProvincesName.trim());
		List<Province> reList = orderDao.findByProperty(Province.class, params, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (Province province : reList) {
				if (province.getProvinceName().equals(recProvincesName)) {
					str = province.getProvinceCode();
				}
			}
		}
		if (StringEmptyUtils.isEmpty(str)) {
			return ReturnInfoUtils.errorInfo("查询省份编码错误!");
		} else {
			recipient.setRecProvincesCode(str);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验身份证号码是否已在商城注册,如果已经注册则需要提供用户登陆密码,进行登陆验证,如果没有注册过,则进行会员注册
	 * 
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> checkIdCardInfo(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("核对身份证是否注册时,请求参数不能为空!");
		}
		String idcard = datasMap.get("idcard") + "";
		Map<String, Object> reMemberMap = getMemberInfo(idcard);
		String msg = reMemberMap.get(BaseCode.MSG.toString()) + "";
		if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
			if (msg.contains("暂无数据")) {
				// 当身份号码在系统中不存在时进行会员注册
				return memberRegister(datasMap);
			} else {
				return reMemberMap;
			}
		} else {
			return reMemberMap;
		}
	}

	/**
	 * 根据身份证号码,查询用户信息
	 * 
	 * @return
	 */
	private Map<String, Object> getMemberInfo(String idcard) {
		if (StringEmptyUtils.isEmpty(idcard)) {
			return ReturnInfoUtils.errorInfo("身份证号码不能为空");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("memberIdCard", idcard);
		List<Member> memberList = orderDao.findByProperty(Member.class, params, 0, 0);
		if (memberList == null) {
			return ReturnInfoUtils.errorInfo("查询会员信息失败！");
		} else if (!memberList.isEmpty()) {
			Member member = memberList.get(0);
			return ReturnInfoUtils.successDataInfo(member.getMemberId());
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据");
		}
	}

	/**
	 * 会员注册
	 * 
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> memberRegister(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("注册会员账号时,请求参数不能为空!");
		}
		String idcard = datasMap.get("idcard") + "";
		String idName = datasMap.get("idName") + "";
		String phone = datasMap.get("phone") + "";
		Map<String, Object> reIdMap = memberService.createMemberId();

		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		String memberId = reIdMap.get(BaseCode.DATAS.toString()) + "";
		String loginPassword = RandomPasswordUtils.createPassWord(8);
		Map<String, Object> reRegisterMap = memberService.memberRegister(idcard, loginPassword, idName, idcard,
				memberId, phone);
		if (!"1".equals(reRegisterMap.get(BaseCode.STATUS.toString()))) {
			return reRegisterMap;
		}
		try {
			SendMsg.sendMsg(phone,
					"【广州银盟】感谢你使用银盟商城进行购物,现已为您自动注册账号,密码为: " + loginPassword + " ,请妥善保管您的登陆密码!链接：https://www.191ec.com");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ReturnInfoUtils.successDataInfo(memberId);
	}

	/**
	 * 校验商品是否上架与库存是否足够
	 * 
	 * @param datasMap
	 * @return Map
	 */
	private Map<String, Object> checkGoodsDatas(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("核对商品信息时,请求参数不能为空!");
		}
		int count = 0;
		try {
			count = Integer.parseInt(datasMap.get("count") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("下单商品数量格式错误!");
		}
		String entGoodsNo = datasMap.get(ENT_GOODS_NO) + "";
		Map<String, Object> params = new HashMap<>();
		params.put(ENT_GOODS_NO, entGoodsNo);
		// 上下架标识：1-上架,2-下架
		params.put("sellFlag", 1);
		// 删除标识:0-未删除,1-已删除
		params.put("deleteFlag", 0);
		List<StockContent> reList = orderDao.findByProperty(StockContent.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商品库存信息失败!");
		} else if (!reList.isEmpty()) {
			StockContent stock = reList.get(0);
			// 上架数量
			int sellCount = stock.getSellCount();
			if ((sellCount - count) < 0) {
				return ReturnInfoUtils.errorInfo("下单失败,商品库存不足！");
			}
			// 使用备用字段存储用户提交过来的下单商品数量
			stock.setReMark(String.valueOf(count));
			return ReturnInfoUtils.successDataInfo(stock);
		} else {
			return ReturnInfoUtils.errorInfo("商品自编号[" + entGoodsNo + "]未找到商品信息,或该商品已被下架!");
		}

	}

	/**
	 * 校验第三方推广下单参数是否齐全
	 * 
	 * @param params
	 *            参数信息
	 * @return Map
	 */
	private Map<String, Object> checkDatas(Map<String, Object> params) {
		if (params == null) {
			return ReturnInfoUtils.errorInfo("校验参数不能为空");
		}
		// 商品信息包、姓名、身份证、手机号码、收货人地址
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue() + "";
			switch (key) {
			case ENT_GOODS_NO:
				if (StringEmptyUtils.isEmpty(value)) {
					return ReturnInfoUtils.errorInfo("[entGoodsNo]商品自编号不能为空！");
				}
				break;
			case "count":
				if (StringEmptyUtils.isEmpty(value)) {
					return ReturnInfoUtils.errorInfo("[count]商品数量不能为空！");
				}
				break;
			case "idName":
				if (StringEmptyUtils.isEmpty(value)) {
					return ReturnInfoUtils.errorInfo("[idName]姓名不能为空！");
				} else if (!StringUtil.isContainChinese(value.replaceAll("·", ""))) {
					return ReturnInfoUtils.errorInfo("姓名必须为中文!");
				}
				break;
			case "idcard":
				if (!IdcardValidator.validate18Idcard(value)) {
					return ReturnInfoUtils.errorInfo("身份证号码错误!");
				}
				break;
			case "phone":
				if (!PhoneUtils.isPhone(value)) {
					return ReturnInfoUtils.errorInfo("手机号码错误!");
				}
				break;
			case "address":
				if (StringEmptyUtils.isEmpty(value)) {
					return ReturnInfoUtils.errorInfo("[address]详细地址不能为空!");
				}
				break;
			default:
				break;
			}
		}
		return ReturnInfoUtils.successInfo();
	}
}
