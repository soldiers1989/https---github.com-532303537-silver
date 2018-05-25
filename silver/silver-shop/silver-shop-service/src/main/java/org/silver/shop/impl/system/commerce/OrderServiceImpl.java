package org.silver.shop.impl.system.commerce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CountryService;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.commerce.OrderService;
import org.silver.shop.api.system.cross.YsPayReceiveService;
import org.silver.shop.api.system.manual.AppkeyService;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.impl.common.base.CustomsPortServiceImpl;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.impl.system.tenant.RecipientServiceImpl;
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
import org.silver.shop.model.system.tenant.RecipientContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.JedisUtil;
import org.silver.util.PhoneUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
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
	private MerchantWalletServiceImpl merchantWalletServiceImpl;
	@Autowired
	private YsPayReceiveService ysPayReceiveService;
	@Autowired
	private AppkeyService appkeyService;
	@Autowired
	private CountryService countryService;
	@Autowired
	private AccessTokenService accessTokenService;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private CustomsPortServiceImpl customsPortServiceImpl;
	@Autowired
	private RecipientServiceImpl recipientServiceImpl;
	@Autowired
	private MemberService memberService;
	@Autowired
	private WalletUtils walletUtils;
	
	
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
			name = "shopOrder";
		} else if (type == 2) {
			// 用于发往支付与海关的总订单头
			topStr = "YMGAC";
			name = "totalShopOrder";
		}
		// 查询缓存中商城下单自增Id
		int count = SerialNoUtils.getRedisIdCount(name);
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
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> datasMap = new HashMap<>();
		if (type == 1) {// 1-余额支付,2-跳转至银盛
			Map<String, Object> reWalletMap = walletUtils.checkWallet(2, memberId, memberName);
			if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
				return reWalletMap;
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
			datasMap.put("out_trade_no", reEntOrderNo);
			datasMap.put("total_amount", totalPrice);
			Map<String, Object> rePayMap = ysPayReceiveService.balancePayReceive(datasMap);
			if (!"1".equals(rePayMap.get(BaseCode.STATUS.toString()))) {
				return rePayMap;
			}
			return updateOrderStatusAndShopCar(memberId, jsonList, reEntOrderNo);
		} else {
			return ReturnInfoUtils.successDataInfo("https://ym.191ec.com/silver-web-shop/yspay/dopay");
		}
	}

	@Override
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("reOrderSerialNo", datasMap.get("messageID") + "");
		String entOrderNo = datasMap.get(ENT_ORDER_NO) + "";
		paramMap.put(ENT_ORDER_NO, entOrderNo);
		String reMsg = datasMap.get("errMsg") + "";
		List<Object> reList = orderDao.findByProperty(OrderRecordContent.class, paramMap, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			OrderRecordContent order = (OrderRecordContent) reList.get(0);
			String status = datasMap.get("status") + "";
			String note = order.getReNote();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if ("1".equals(status)) {
				// 订单备案状态修改为成功
				order.setOrderRecordStatus(2);
			} else {
				order.setOrderRecordStatus(3);
			}
			order.setReNote(note + defaultDate + ":" + reMsg + ";");
			order.setUpdateDate(date);
			if (!orderDao.update(order)) {
				return ReturnInfoUtils.errorInfo("异步更新订单备案信息错误!");
			}
			return ReturnInfoUtils.successInfo();
		} else {
			return ReturnInfoUtils.errorInfo("订单号[" + entOrderNo + "]未找到对应订单信息");
		}
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
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
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
			Map<String, Object> reGoodsMap = createOrderGoodsInfo(memberId, memberName, newOrderId, count,
					goodsRecordInfo, stock, entOrderNo, feeMap);
			if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()))) {
				return reGoodsMap;
			}
			return reGoodsMap;
		} else {
			Map<String, Object> reMap = createOrderHeadInfo(memberId, memberName, newOrderId, stock, entOrderNo,
					recInfo, feeMap);
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			Map<String, Object> reGoodsMap = createOrderGoodsInfo(memberId, memberName, newOrderId, count,
					goodsRecordInfo, stock, entOrderNo, feeMap);
			if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()))) {
				return reGoodsMap;
			}
			return reMap;
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
		Map<String, Object> reRecipientMap = recipientServiceImpl.getRecipientInfo(recipientId);
		if (!"1".equals(reRecipientMap.get(BaseCode.STATUS.toString()))) {
			return reRecipientMap;
		}
		RecipientContent recipient = (RecipientContent) reRecipientMap.get(BaseCode.DATAS.toString());

		Map<String, Object> reMemberMap = memberService.getMemberInfo(recipient.getMemberId());
		if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
			return reMemberMap;
		}
		Member member = (Member) reMemberMap.get(BaseCode.DATAS.toString());
		if (member.getMemberRealName() == 1) {
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
		Map<String, Object> statusMap = new HashMap<>();
		datasMap.put("merchantId", merchantId);
		Map<String, Object> reDatasMap = SearchUtils.universalMerchantOrderSearch(datasMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> viceParams = (Map<String, Object>) reDatasMap.get("viceParams");
		List<OrderRecordContent> reList = orderDao.merchantuUnionOrderInfo(OrderRecordContent.class, paramMap,
				viceParams, page, size);
		long reTotalCount = orderDao.merchantuUnionOrderCount(OrderRecordContent.class, paramMap, viceParams);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), reTotalCount);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
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
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		List<Morder> orderList = orderDao.findByPropertyLike(Morder.class, paramMap, null, page, size);
		Long count = orderDao.findByPropertyLikeCount(Morder.class, paramMap, null);
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
		if (datasMap != null && !datasMap.isEmpty()) {
			try {
				JSONObject orderJson = null;
				Map<String, Object> reCheckMerchantMap = merchantUtils.getMerchantInfo(datasMap.get("merchantId") + "");
				if (!"1".equals(reCheckMerchantMap.get(BaseCode.STATUS.toString()))) {
					return reCheckMerchantMap;
				}
				Merchant merchant = (Merchant) reCheckMerchantMap.get(BaseCode.DATAS.toString());
				// 获取订单信息
				try {
					orderJson = JSONObject.fromObject(datasMap.get("datas"));
				} catch (Exception e) {
					return ReturnInfoUtils.errorInfo("订单参数格式不正确,请核对信息!");
				}
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
				Map<String, Object> reSaveOrderGoodsMap = saveOrderGoodsInfo(merchant, orderGoodsList, entOrderNo);
				if (!"1".equals(reSaveOrderGoodsMap.get(BaseCode.STATUS.toString()))) {
					return reSaveOrderGoodsMap;
				}
				return ReturnInfoUtils.successInfo();
			} catch (Exception e) {
				logger.error("--------第三方订单信息错误-------", e);
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数不能为空！");
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
			if (StringEmptyUtils.isNotEmpty(spareParams)) {
				JSONObject json = null;
				try {
					json = JSONObject.fromObject(spareParams);
				} catch (Exception e) {
					return ReturnInfoUtils.errorInfo("订单商品备用字段参数格式错误,请核对信息!");
				}
				// 商品归属商家代码
				String marCode = json.get("marCode") + "";
				//
				String ebEntNo = json.get("ebEntNo") + "";
				String ebEntName = json.get("ebEntName") + "";
				// 电子口岸16位编码
				String DZKNNo = json.get("DZKNNo") + "";
				if (StringEmptyUtils.isNotEmpty(marCode) && StringEmptyUtils.isNotEmpty(ebEntNo)
						&& StringEmptyUtils.isNotEmpty(ebEntName) && StringEmptyUtils.isNotEmpty(DZKNNo)) {
					JSONObject params = new JSONObject();
					params.put("marCode", marCode);
					params.put("ebEntNo", ebEntNo);
					params.put("ebEntName", ebEntName);
					params.put("DZKNNo", DZKNNo);
					goods.setSpareParams(params.toString());
				}
			}
			if (!orderDao.add(goods)) {
				return ReturnInfoUtils.errorInfo("商品自编号[" + entGoodsNo + "]保存失败,服务器繁忙！");
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
		String entOrderNo = orderJson.get("EntOrderNo") + "";
		Map<String, Object> params = new HashMap<>();
		params.put("order_id", entOrderNo);
		List<Morder> reList = orderDao.findByProperty(Morder.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询订单信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("订单编号[" + entOrderNo + "]对应的订单信息已存在,请勿重复导入!");
		} else {
			Morder order = new Morder();
			order.setMerchant_no(merchant.getMerchantId());
			order.setOrder_id(orderJson.get("EntOrderNo") + "");
			// 币制
			order.setFcode(orderJson.get("OrderGoodTotalCurr") + "");
			order.setFCY(Double.parseDouble(orderJson.get("OrderGoodTotal") + ""));
			order.setTax(Double.parseDouble(orderJson.get("Tax") + ""));
			order.setActualAmountPaid(Double.parseDouble(orderJson.get("ActualAmountPaid") + ""));
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
			// 国检检疫机构代码
			String ciqOrgCode = orderJson.get("ciqOrgCode") + "";
			// 海关关区代码
			String customsCode = orderJson.get("customsCode") + "";
			order.setEport(eport);
			order.setCiqOrgCode(ciqOrgCode);
			order.setCustomsCode(customsCode);
			if (!orderDao.add(order)) {
				return ReturnInfoUtils.errorInfo("保存订单信息失败,服务器繁忙！");
			}
			return ReturnInfoUtils.successInfo();
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
			return ReturnInfoUtils.errorInfo("订单商品总金额参数格式错误,请核对信息！");
		}
		try {
			tax = Double.parseDouble(orderJson.get("Tax") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单税费参数格式错误,请核对信息！");
		}
		try {
			actualAmountPaid = Double.parseDouble(orderJson.get("ActualAmountPaid") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单实际支付金额参数格式错误,请核对信息！");
		}
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
						"订单号[" + entOrderNo + "]中关联商品自编号[" + entGoodsNo + "]数量[" + goodsJson.get("Qty") + "]格式错误!");
			}
			try {
				price = Double.parseDouble(goodsJson.get("Price") + "");
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo(
						"订单号[" + entOrderNo + "]中关联商品自编号[" + entGoodsNo + "]单价[" + goodsJson.get("Price") + "]格式错误!");
			}
			try {
				total = Double.parseDouble(goodsJson.get("Total") + "");
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("订单号[" + entOrderNo + "]中关联商品自编号[" + entGoodsNo + "]商品总金额["
						+ goodsJson.get("Total") + "]格式错误!");
			}
			if ((count * price) != total) {
				return ReturnInfoUtils.errorInfo("订单号[" + entOrderNo + "]中关联商品自编号[" + entGoodsNo + "]商品总金额为：" + total
						+ ",与" + count + "(数量)*" + price + "(单价)=" + (count * price) + "(商品总金额)不对等,请核对信息!");
			}
			goodsTotal += total;
		}
		// 判断订单商品总金额是否与计算出来的订单信息中商品总金额是否一致
		if (orderGoodTotal != goodsTotal) {
			return ReturnInfoUtils.errorInfo(
					"订单[" + entOrderNo + "]中填写的商品总金额:" + orderGoodTotal + "与实际商品总金额:" + goodsTotal + "不对等,请核对信息!");
		}
		// 判断商品总金额+税费是否等于实际金额
		if ((goodsTotal + tax) != actualAmountPaid) {
			return ReturnInfoUtils.errorInfo("订单[" + entOrderNo + "]中订单中填写的" + actualAmountPaid + "(实际支付金额)与"
					+ goodsTotal + "(订单商品金额)+" + tax + "(税费)=" + (goodsTotal + tax) + "(实际支付金额)不对等!请核对信息!");
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
		double orderGoodTotal = Double.parseDouble(orderJson.get("OrderGoodTotal") + "");
		if (orderGoodTotal > 2000) {
			return ReturnInfoUtils.errorInfo("接收订单失败,订单商品总金额超过2000,请核对订单信息!");
		}
		// 下单人身份证号码
		String orderDocId = orderJson.get("OrderDocId") + "";
		if (!IdcardValidator.validate18Idcard(orderDocId)) {
			return ReturnInfoUtils.errorInfo("下单人身份证号码实名认证失败,请核对订单信息!");
		}
		String recipientTel = orderJson.get("RecipientTel") + "";
		if (!PhoneUtils.isPhone(recipientTel)) {
			return ReturnInfoUtils.errorInfo("收货人手机号码格式,请核对订单信息!");
		}
		String orderDocTel = orderJson.get("OrderDocTel") + "";
		if (!PhoneUtils.isPhone(orderDocTel)) {
			return ReturnInfoUtils.errorInfo("下单人手机号码格式不正确,请核对订单信息!");
		}
		String orderDocName = orderJson.get("OrderDocName") + "";
		if (!StringUtil.isChinese(orderDocName) || orderDocName.contains("先生") || orderDocName.contains("女士")
				|| orderDocName.contains("小姐")) {
			return ReturnInfoUtils.errorInfo("接收订单失败,订单下单人姓名错误,请核对订单信息!");
		}
		return checkOrderEport(orderJson);
	}

	private Map<String, Object> checkOrderEport(JSONObject orderJson) {
		// 订单口岸标识
		String eport = orderJson.get("eport") + "";
		// 国检检疫机构代码
		String ciqOrgCode = orderJson.get("ciqOrgCode") + "";
		// 海关关区代码
		String customsCode = orderJson.get("customsCode") + "";
		switch (eport) {
		case "1":
			if (customsPortServiceImpl.checkCCIQ(ciqOrgCode) && customsPortServiceImpl.checkGAC(customsCode)) {
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
			return ReturnInfoUtils.errorInfo("收货人行政区代码[" + provinceCode + "],未找到对应的省份信息,请核实信息！");
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
			return ReturnInfoUtils.errorInfo("收货人所在国[" + countryCode + "],未找到对应的国家信息,请核实信息");
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
		byte[] redisByte = JedisUtil.get("Shop_Key_Country_List".getBytes(), 3600);
		if (redisByte != null) {
			countryList = (List<Country>) SerializeUtil.toObject(redisByte);
			return ReturnInfoUtils.successDataInfo(countryList, 0);
		} else {
			Map<String, Object> countryMap = countryService.findAllCountry();
			if (!"1".equals(countryMap.get(BaseCode.STATUS.toString()))) {
				return countryMap;
			}
			JedisUtil.set("Shop_Key_Country_List".getBytes(),
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
		byte[] redisByte = JedisUtil.get("Shop_Key_Metering_List".getBytes(), 3600);
		if (redisByte != null) {
			meteringList = (List<Metering>) SerializeUtil.toObject(redisByte);
			return meteringList;
		} else {
			List<Metering> reList = orderDao.findByProperty(Metering.class, null, 0, 0);
			if (reList != null && !reList.isEmpty()) {
				// 将查询出来的数据放入到缓存中
				JedisUtil.set("Shop_Key_Metering_List".getBytes(), SerializeUtil.toBytes(reList), 3600);
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
}
