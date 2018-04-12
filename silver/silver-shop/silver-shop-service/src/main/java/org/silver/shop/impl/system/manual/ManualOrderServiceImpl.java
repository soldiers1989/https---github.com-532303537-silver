package org.silver.shop.impl.system.manual;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.manual.ManualOrderService;
import org.silver.shop.api.system.manual.MorderService;
import org.silver.shop.dao.system.manual.ManualOrderDao;
import org.silver.shop.impl.system.organization.MemberServiceImpl;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.util.BufferUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONObject;

@Service(interfaceClass = ManualOrderService.class)
@Component("manualOrderService")
public class ManualOrderServiceImpl implements ManualOrderService, MessageListener {

	private static Logger logger = Logger.getLogger(Object.class);

	@Autowired
	private ManualOrderDao manualOrderDao;
	@Autowired
	private BufferUtils bufferUtils;
	@Autowired
	private MorderService morderService;
	@Autowired
	private MemberServiceImpl memberServiceImpl;

	// 海关币制默认为人名币
	private static final String FCODE = "142";
	/**
	 * 错误标识
	 */
	private static final String ERROR = "error";
	/**
	 * 身份证标识-idCard
	 */
	private static final String IDCARD = "idCard";
	/**
	 * 商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 商户名称
	 */
	private static final String MERCHANT_NAME = "merchantName";
	/**
	 * 下划线版订单Id
	 */
	private static final String ORDER_ID = "order_id";

	@Override
	public void onMessage(Message message) {
		TextMessage textmessage = (TextMessage) message;
		JSONObject jsonDatas = null;
		try {
			jsonDatas = JSONObject.fromObject(textmessage.getText());
		} catch (Exception e) {
			logger.error("--队列获取参数错误->", e);
		}
		//
		chooseCreate(jsonDatas);
	}

	private void chooseCreate(JSONObject jsonDatas) {
		try {
			String type = jsonDatas.get("type") + "";
			// 缓存参数
			Map<String, Object> redisMap = (Map<String, Object>) jsonDatas.get("other");
			switch (type) {
			// 国宗excel表单订单导入
			case "guoZongExcelOrderImpl":
				//
				Map<String, Object> reCheckMap = checkGuoZongOrderInfo(jsonDatas);
				if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
					break;
				}
				guoZongCreateOrder(jsonDatas);
				break;
			case "qiBangExcelOrderImpl":
				//
				Map<String, Object> reCheckMap2 = checkQiBangOrderInfo(jsonDatas);
				if (!"1".equals(reCheckMap2.get(BaseCode.STATUS.toString()))) {
					break;
				}
				qiBangCreateOrder(jsonDatas);
				break;
			default:
				break;
			}
			bufferUtils.writeCompletedRedisMq(redisMap);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Thread.currentThread().getName() + "--订单创建-->", e);
		}
	}

	/**
	 * 根据国宗excel表单信息,创建手工订单信息
	 * 
	 * @param datas
	 * 
	 */
	public void guoZongCreateOrder(JSONObject datas) {
		String waybill = datas.get("waybill") + "";
		// 缓存参数
		Map<String, Object> params = (Map<String, Object>) datas.get("other");
		// 商品信息
		JSONObject goodsInfo = JSONObject.fromObject(datas.get("goodsInfo") + "");
		// 订单商品总金额
		Double orderTotalAmount = Double.parseDouble(datas.get("orderTotalAmount") + "");
		// 税费
		Double tax = Double.parseDouble(datas.get("tax") + "");
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("waybill", waybill);
		List<Morder> ml = manualOrderDao.findByProperty(Morder.class, paramsMap, 0, 0);
		if (ml == null) {
			RedisInfoUtils.errorInfoMq("运单号[" + waybill + "]查询订单信息失败,服务器繁忙!", ERROR, params);
		} else if (!ml.isEmpty()) {
			Morder morder = ml.get(0);
			// 删除标识0-未删除,1-已删除
			if (morder.getDel_flag() == 1) {
				RedisInfoUtils.errorInfoMq(morder.getOrder_id() + "<--订单已被刪除,无法再次导入,请联系管理员!", ERROR, params);
			}
			Map<String, Object> reCheckMap = judgmentOrderInfo(morder, goodsInfo, orderTotalAmount, tax, 1);
			if ("10".equals(reCheckMap.get(BaseCode.STATUS.toString()) + "")) {// 当遇到超额时
				RedisInfoUtils.errorInfoMq(reCheckMap.get("msg") + "", "orderExcess", params);
			} else if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()) + "")) {
				RedisInfoUtils.errorInfoMq(reCheckMap.get("msg") + "", ERROR, params);
			} else {
				goodsInfo.put(ORDER_ID, reCheckMap.get(ORDER_ID)); // 根据订单Id创建订单商品
				// 成功后、调用创建商品方法
				Map<String, Object> reGoodsMap = createNewSub(goodsInfo);
				if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
					RedisInfoUtils.errorInfoMq(reGoodsMap.get(BaseCode.MSG.toString()) + "", ERROR, params);
				}
			}
			// 注册会员账号信息
			memberServiceImpl.registerMember(morder);
		} else {
			Morder morder = new Morder();
			// 查询缓存中订单自增Id
			int count = SerialNoUtils.getRedisIdCount("order");
			String newOrderId = SerialNoUtils.getSerialNo("YM", count);
			morder.setOrder_id(newOrderId);
			// 原导入表中的订单编号
			morder.setOldOrderId(datas.get("orderId") + "");
			morder.setFCY(orderTotalAmount);
			morder.setTax(tax);
			morder.setActualAmountPaid(orderTotalAmount + tax);
			morder.setRecipientName(datas.get("recipientName") + "");
			morder.setRecipientID(datas.get("recipientID") + "");
			morder.setRecipientTel(datas.get("recipientTel") + "");
			// 暂时默认为下单人姓名
			morder.setRecipientAddr(datas.get("recipientAddr") + "");
			morder.setOrderDocAcount(datas.get("orderDocAcount") + "");
			morder.setOrderDocName(datas.get("orderDocName") + "");
			morder.setOrderDocType("01");
			// 身份证
			morder.setOrderDocId(datas.get("orderDocId") + "");
			morder.setOrderDocTel(datas.get("orderDocTel") + "");
			morder.setMerchant_no(goodsInfo.get(MERCHANT_ID) + "");
			morder.setDateSign(datas.get("dateSign") + "");
			morder.setSerial(Integer.parseInt(datas.get("serial") + ""));
			morder.setWaybill(waybill);
			// 刪除标识
			morder.setDel_flag(0);
			// 订单备案状态
			morder.setOrder_record_status(1);
			morder.setCreate_date(new Date());
			morder.setCreate_by(goodsInfo.get(MERCHANT_NAME) + "");
			morder.setFcode("142");
			morder.setSenderName(datas.get("senderName") + "");
			morder.setSenderCountry(datas.get("senderCountry") + "");
			morder.setSenderAreaCode(datas.get("senderAreaCode") + "");
			morder.setSenderAddress(datas.get("senderAddress") + "");
			morder.setSenderTel(datas.get("senderTel") + "");

			morder.setPostal(datas.get("postal") + "");
			morder.setRecipientProvincesCode(datas.get("provinceCode") + "");
			morder.setRecipientProvincesName(datas.get("provinceName") + "");
			morder.setRecipientCityCode(datas.get("cityCode") + "");
			morder.setRecipientCityName(datas.get("cityName") + "");
			morder.setRecipientAreaCode(datas.get("areaCode") + "");
			morder.setRecipientAreaName(datas.get("areaName") + "");
			String randomDate = DateUtil.randomCreateDate();
			morder.setOrderDate(randomDate);
			morder.setCustomsCode(goodsInfo.get("customsCode") + "");
			if (manualOrderDao.add(morder)) {
				goodsInfo.put(ORDER_ID, newOrderId);
				// 保存成功之后,进行商品实例化
				Map<String, Object> reGoodsMap = createNewSub(goodsInfo);
				if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
					RedisInfoUtils.errorInfoMq(reGoodsMap.get(BaseCode.MSG.toString()) + "", ERROR, params);
				}
				// 注册会员账号信息
				memberServiceImpl.registerMember(morder);
			} else {
				RedisInfoUtils.errorInfoMq(morder.getOrder_id() + "<--订单存储失败，请重试!", ERROR, params);
			}
		}
		// 用于缓存辨别计算完成
		params.put("type", "success");
		bufferUtils.writeRedisMq(null, params);
	}

	/**
	 * 校验国宗订单信息
	 * 
	 * @param datas
	 * @return Map
	 */
	private Map<String, Object> checkGuoZongOrderInfo(JSONObject datas) {
		// 缓存参数
		Map<String, Object> params = (Map<String, Object>) datas.get("other");
		// 商品信息
		JSONObject goodsInfo = JSONObject.fromObject(datas.get("goodsInfo") + "");
		//
		String waybill = datas.get("waybill") + "";
		//
		String recipientID = datas.get("recipientID") + "";
		Map<String, Object> reCheckIdCardMap = morderService.checkIdCardCount(recipientID);
		if (!"1".equals(reCheckIdCardMap.get(BaseCode.STATUS.toString()))) {
			String msg = "运单号[" + waybill + "]" + reCheckIdCardMap.get(BaseCode.MSG.toString());
			RedisInfoUtils.errorInfoMq(msg, IDCARD, params);
		}
		String recipientTel = datas.get("recipientTel") + "";
		Map<String, Object> reCheckPhoneMap = morderService.checkRecipientTel(recipientTel);
		if (!"1".equals(reCheckPhoneMap.get(BaseCode.STATUS.toString()))) {
			String msg = "运单号[" + waybill + "]" + reCheckPhoneMap.get(BaseCode.MSG.toString());
			RedisInfoUtils.errorInfoMq(msg, "phone", params);
		}
		String entGoodsNo = goodsInfo.get("entGoodsNo") + "";
		if (!checkEntGoodsNoLength(entGoodsNo)) {
			String msg = "运单号[" + waybill + "]商品货号长度超过20,请核对商品货号是否正确!";
			RedisInfoUtils.errorInfoMq(msg, ERROR, params);
			return ReturnInfoUtils.errorInfo(msg);
		}
		if (!IdcardValidator.validate18Idcard(recipientID)) {
			String msg = "运单号[" + waybill + "]实名认证不通过,请核实身份证与姓名信息!";
			RedisInfoUtils.errorInfoMq(msg, IDCARD, params);
		}
		//
		double netWt = Double.parseDouble(goodsInfo.get("netWt") + "");
		double grossWt = Double.parseDouble(goodsInfo.get("grossWt") + "");
		if (netWt > grossWt) {
			String msg = "运单号[" + waybill + "]商品净重大于毛重,请核实信息!";
			RedisInfoUtils.errorInfoMq(msg, "overweight", params);
		}
		//
		return ReturnInfoUtils.successInfo();
	}

	private Map<String, Object> checkQiBangOrderInfo(JSONObject jsonDatas) {
		// 缓存参数
		Map<String, Object> params = (Map<String, Object>) jsonDatas.get("other");
		//
		String orderId = jsonDatas.get("orderId") + "";
		String orderDocId = jsonDatas.get("orderDocId") + "";
		Map<String, Object> reCheckIdCardMap = morderService.checkIdCardCount(orderDocId);
		if (!"1".equals(reCheckIdCardMap.get(BaseCode.STATUS.toString()))) {
			String msg = "订单号[" + orderId + "]" + reCheckIdCardMap.get(BaseCode.MSG.toString());
			RedisInfoUtils.errorInfoMq(msg, IDCARD, params);
		}
		String recipientTel = jsonDatas.get("recipientTel") + "";
		Map<String, Object> reCheckPhoneMap = morderService.checkRecipientTel(recipientTel);
		if (!"1".equals(reCheckPhoneMap.get(BaseCode.STATUS.toString()))) {
			String msg = "订单号[" + orderId + "]" + reCheckPhoneMap.get(BaseCode.MSG.toString());
			RedisInfoUtils.errorInfoMq(msg, "phone", params);
		}
		if (!IdcardValidator.validate18Idcard(orderDocId)) {
			String msg = "订单号[" + orderId + "]实名认证不通过,请核实身份证与姓名信息!";
			RedisInfoUtils.errorInfoMq(msg, IDCARD, params);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 判断订单与订单商品信息是否存在,如果只是商品不存在则更新订单金额,如果订单号+商品自编号+序号+商户Id查询商品是否已存在,已存在则判定为重复导入
	 * 
	 * @param morder
	 *            订单(实体)信息
	 * @param goodsInfo
	 *            商品信息
	 * @param FCY
	 *            商品总额
	 * @param tax
	 *            税费
	 * @param flag
	 *            暂定标识1-国宗,2-企邦
	 * @return Map
	 */
	private Map<String, Object> judgmentOrderInfo(Morder morder, JSONObject goodsInfo, Double FCY, Double tax,
			int flag) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		String waybill = morder.getWaybill().trim();
		String orderId = morder.getOrder_id().trim();
		paramsMap.put("seqNo", goodsInfo.get("seqNo"));
		paramsMap.put("EntGoodsNo", goodsInfo.get("entGoodsNo"));
		paramsMap.put(ORDER_ID, morder.getOrder_id());
		List<MorderSub> ms = manualOrderDao.findByProperty(MorderSub.class, paramsMap, 0, 0);
		if (ms == null) {
			return ReturnInfoUtils.errorInfo("运单[" + waybill + "]<--查询订单商品信息失败!");
		} else if (!ms.isEmpty()) {
			if (flag == 1) {
				return ReturnInfoUtils.errorInfo("运单[" + waybill + "]<--与商品信息已存在,请勿需重复导入!");
			}
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]<--该订单与商品信息已存在,请勿需重复导入!");
		} else {
			double reFCY = morder.getFCY();
			double newFcy = reFCY + FCY;
			morder.setFCY(newFcy);
			double newActualAmountPaid = newFcy + tax;
			morder.setActualAmountPaid(newActualAmountPaid);
			if (!manualOrderDao.update(morder)) {
				return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]<--订单更新总价失败,服务器繁忙!");
			}
			statusMap.put("status", 1);
			statusMap.put(ORDER_ID, morder.getOrder_id());
			if (newFcy >= 2000) {// 当订单金额超过2000时
				if (flag == 1) {
					statusMap.clear();
					statusMap.put(BaseCode.STATUS.toString(), "10");
					statusMap.put(ORDER_ID, morder.getOrder_id());
					statusMap.put(BaseCode.MSG.toString(),
							"运单号[" + waybill + "],订单号[" + orderId + "]<--关联商品总计金额超过2000,请核对金额!");
				} else if (flag == 2) {
					statusMap.clear();
					statusMap.put(BaseCode.STATUS.toString(), "10");
					statusMap.put(ORDER_ID, morder.getOrder_id());
					statusMap.put(BaseCode.MSG.toString(), "订单号[" + orderId + "]<--关联商品总计金额超过2000,请核对金额!");
				}
			}
			return statusMap;
		}
	}

	/**
	 * 创建订单商品信息
	 * 
	 * @param goodsInfo
	 * @return
	 */
	public Map<String, Object> createNewSub(JSONObject goodsInfo) {
		Map<String, Object> params = new HashMap<>();
		String orderId = goodsInfo.get(ORDER_ID) + "";
		params.put(ORDER_ID, orderId);
		Long count = manualOrderDao.findByPropertyCount(MorderSub.class, params);
		if (count < 0) {
			return ReturnInfoUtils.errorInfo("查询订单商品序列号错误,服务器繁忙!");
		}
		MorderSub mosb = new MorderSub();
		mosb.setSeq((count.intValue() + 1));
		mosb.setOrder_id(orderId);
		mosb.setEntGoodsNo(goodsInfo.get("entGoodsNo") + "");
		mosb.setHSCode(goodsInfo.get("HSCode") + "");
		mosb.setBrand(goodsInfo.get("Brand") + "");
		mosb.setBarCode(goodsInfo.get("BarCode") + "");
		mosb.setCusGoodsNo(goodsInfo.get("CusGoodsNo") + "");
		mosb.setCIQGoodsNo(goodsInfo.get("CIQGoodsNo") + "");
		mosb.setGoodsName(goodsInfo.get("GoodsName") + "");
		mosb.setGoodsStyle(goodsInfo.get("GoodsStyle") + "");
		mosb.setOriginCountry(goodsInfo.get("OriginCountry") + "");
		mosb.setUnit(goodsInfo.get("Unit") + "");
		Double p = goodsInfo.getDouble("Price");
		int q = Integer.parseInt(goodsInfo.get("Qty") + "");
		mosb.setPrice(p);
		mosb.setQty(q);
		mosb.setTotal(p * q);
		mosb.setCreate_date(new Date());
		mosb.setNetWt(goodsInfo.getDouble("netWt"));
		mosb.setGrossWt(goodsInfo.getDouble("grossWt"));
		if (StringEmptyUtils.isEmpty(goodsInfo.get("stdUnit") + "")) {
			mosb.setStdUnit("");
		} else {
			mosb.setStdUnit(goodsInfo.get("stdUnit") + "");
		}
		mosb.setSecUnit(goodsInfo.get("secUnit") + "");
		mosb.setNumOfPackages(q);
		mosb.setSeqNo(goodsInfo.getInt("seqNo"));
		String firstLegalCount = goodsInfo.get("firstLegalCount") + "";
		String secondLegalCount = goodsInfo.get("secondLegalCount") + "";
		String transportModel = goodsInfo.get("transportModel") + "";
		String numOfPackages = goodsInfo.get("numOfPackages") + "";

		if (StringEmptyUtils.isNotEmpty(firstLegalCount) && StringEmptyUtils.isNotEmpty(secondLegalCount)) {
			mosb.setFirstLegalCount(goodsInfo.getDouble("firstLegalCount"));
			mosb.setSecondLegalCount(goodsInfo.getDouble("secondLegalCount"));
		}
		if (StringEmptyUtils.isNotEmpty(numOfPackages)) {
			mosb.setNumOfPackages(Integer.parseInt(numOfPackages));
		}
		String packageType = goodsInfo.get("packageType") + "";
		if (StringEmptyUtils.isNotEmpty(packageType)) {
			mosb.setPackageType(Integer.parseInt(packageType));
		}
		if (StringEmptyUtils.isNotEmpty(transportModel)) {
			mosb.setTransportModel(transportModel);
		}

		// (启邦客户)商品归属商家代码
		String marCode = goodsInfo.get("marCode") + "";
		// (启邦客户)商品归属SKU
		String sku = goodsInfo.get("SKU") + "";

		//
		String ebEntNo = goodsInfo.get("ebEntNo") + "";
		String ebEntName = goodsInfo.get("ebEntName") + "";
		String DZKNNo = goodsInfo.get("DZKNNo") + "";

		if (StringEmptyUtils.isNotEmpty(sku) && StringEmptyUtils.isNotEmpty(marCode)
				&& StringEmptyUtils.isNotEmpty(ebEntNo) && StringEmptyUtils.isNotEmpty(ebEntName)
				&& StringEmptyUtils.isNotEmpty(DZKNNo)) {
			JSONObject spareParams = new JSONObject();
			spareParams.put("marCode", marCode);
			spareParams.put("SKU", sku);
			spareParams.put("ebEntNo", ebEntNo);
			spareParams.put("ebEntName", ebEntName);
			spareParams.put("DZKNNo", DZKNNo);
			mosb.setSpareParams(spareParams.toString());
		}
		mosb.setMerchant_no(goodsInfo.get(MERCHANT_ID) + "");
		mosb.setCreateBy(goodsInfo.get(MERCHANT_NAME) + "");
		// 删除标识:0-未删除,1-已删除
		mosb.setDeleteFlag(0);
		if (manualOrderDao.add(mosb)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("订单商品【" + goodsInfo.get("GoodsName") + "】存储失败，请重试!");
	}

	/**
	 * 校验商品自编号是否超过海关要求长度
	 * 
	 * @param entGoodsNo
	 *            商品自编号
	 * @return boolean
	 */
	private boolean checkEntGoodsNoLength(String entGoodsNo) {
		return StringEmptyUtils.isNotEmpty(entGoodsNo) && entGoodsNo.length() <= 20;
	}

	/**
	 * 启邦创建订单信息
	 * 
	 * @param datas
	 * @return
	 */
	public void qiBangCreateOrder(JSONObject datas) {
		// 缓存参数
		Map<String, Object> params = (Map<String, Object>) datas.get("other");
		//
		String merchantId = datas.get(MERCHANT_ID) + "";
		String merchantName = datas.get(MERCHANT_NAME) + "";
		double orderTotalAmount = Double.parseDouble(datas.get("orderTotalPrice") + "");
		double tax = Double.parseDouble(datas.get("tax") + "");
		double actualAmountPaid = Double.parseDouble(datas.get("actualAmountPaid") + "");
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String orderId = datas.get("orderId") + "";
		if (StringEmptyUtils.isEmpty(orderId)) {// 当表单中未填写订单Id时,则系统生成
			// 查询缓存中订单自增Id
			int count = SerialNoUtils.getRedisIdCount("order");
			orderId = SerialNoUtils.getSerialNo("YM", count);
		}
		Map<String, Object> paramsMap = new HashMap<>();
		// 校验企邦是否已经录入已备案商品信息
		String entGoodsNo = datas.get("entGoodsNo") + "";
		String marCode = datas.get("marCode") + "";
		if (StringEmptyUtils.isNotEmpty(marCode)) {
			paramsMap.put("entGoodsNo", entGoodsNo.trim() + "_" + marCode.trim());
		} else {
			paramsMap.put("entGoodsNo", entGoodsNo.trim());
		}
		paramsMap.put("goodsMerchantId", merchantId);
		List<GoodsRecordDetail> goodsList = manualOrderDao.findByProperty(GoodsRecordDetail.class, paramsMap, 1, 1);
		if (goodsList == null || goodsList.isEmpty()) {
			String msg = "商品编号[" + entGoodsNo.trim() + "]与商家平台号[" + marCode.trim() + "]-->对应商品不存在,请核实信息!";
			RedisInfoUtils.errorInfoMq(msg, ERROR, params);
		} else {
			paramsMap.clear();
			paramsMap.put(ORDER_ID, orderId);
			List<Morder> ml = manualOrderDao.findByProperty(Morder.class, paramsMap, 1, 1);
			if (ml != null && !ml.isEmpty()) {
				Morder morder = ml.get(0);
				// 企邦的税费暂写死为0
				Map<String, Object> reCheckMap = judgmentOrderInfo(morder, datas, orderTotalAmount, 0.0, 2);
				if ("10".equals(reCheckMap.get(BaseCode.STATUS.toString()) + "")) {// 当遇到超额时
					RedisInfoUtils.errorInfoMq(reCheckMap.get(BaseCode.MSG.toString()) + "", "orderExcess", params);
				} else if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()) + "")) {
					RedisInfoUtils.errorInfoMq(reCheckMap.get(BaseCode.MSG.toString()) + "", ERROR, params);
				} else {
					// 成功后、调用创建商品方法
					Map<String, Object> reGoodsMap = createQBOrderSub(merchantId, datas, merchantName);
					if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
						RedisInfoUtils.errorInfoMq(reGoodsMap.get(BaseCode.MSG.toString()) + "", ERROR, params);
					}
				}
				// 注册会员账号信息
				memberServiceImpl.registerMember(morder);
			} else {
				Morder morder = new Morder();
				morder.setOrder_id(orderId);
				morder.setMerchant_no(merchantId);
				morder.setFCY(orderTotalAmount);
				morder.setTax(tax);
				morder.setActualAmountPaid(actualAmountPaid);
				morder.setRecipientName(datas.get("recipientName") + "");
				morder.setRecipientID(datas.get("orderDocId") + "");
				morder.setRecipientTel(datas.get("recipientTel") + "");
				morder.setRecipientProvincesCode(datas.get("provinceCode") + "");
				morder.setRecipientProvincesName(datas.get("provinceName") + "");
				morder.setRecipientCityCode(datas.get("cityCode") + "");
				morder.setRecipientCityName(datas.get("cityName") + "");
				morder.setRecipientAreaCode(datas.get("areaCode") + "");
				morder.setRecipientAreaName(datas.get("areaName") + "");
				morder.setRecipientAddr(datas.get("recipientAddr") + "");
				morder.setOrderDocAcount(datas.get("orderDocAcount") + "");
				morder.setOrderDocName(datas.get("orderDocName") + "");
				morder.setOrderDocType("01");// 身份证
				morder.setOrderDocId(datas.get("orderDocId") + "");
				morder.setOrderDocTel(datas.get("orderDocTel") + "");
				morder.setDateSign(dateSign);
				morder.setSerial(Integer.parseInt(datas.get("serial") + ""));
				morder.setWaybill(datas.get("waybillNo") + "");
				morder.setDel_flag(0);
				morder.setOrder_record_status(1);
				morder.setCreate_date(new Date());
				morder.setCreate_by(merchantName);
				morder.setFcode(FCODE);
				String randomDate = DateUtil.randomCreateDate();
				morder.setOrderDate(randomDate);
				String ehsEntName = datas.get("ehsEntName") + "";
				if (StringEmptyUtils.isNotEmpty(ehsEntName)) {
					JSONObject json = new JSONObject();
					json.put("ehsEntName", ehsEntName);
					morder.setSpareParams(json.toString());
				}
				if (manualOrderDao.add(morder)) {
					// 保存成功调用保存商品
					Map<String, Object> reGoodsMap = createQBOrderSub(merchantId, datas, merchantName);
					if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
						RedisInfoUtils.errorInfoMq(reGoodsMap.get(BaseCode.MSG.toString()) + "", ERROR, params);
					}
					// 注册会员账号信息
					memberServiceImpl.registerMember(morder);
				} else {
					RedisInfoUtils.errorInfoMq("订单[" + orderId + "]保存失败,请核对订单信息!", ERROR, params);
				}
			}
		}
		// 用于缓存辨别计算完成
		params.put("type", "success");
		bufferUtils.writeRedisMq(null, params);
	}

	public Map<String, Object> createQBOrderSub(String merchantId, JSONObject item, String merchantName) {
		Map<String, Object> params = new HashMap<>();
		String entGoodsNo = item.get("entGoodsNo") + "";
		String marCode = item.get("marCode") + "";
		String goodsName = item.get("goodsName") + "";
		if (StringEmptyUtils.isNotEmpty(marCode)) {
			params.put("entGoodsNo", entGoodsNo.trim() + "_" + marCode.trim());
		} else {
			params.put("entGoodsNo", entGoodsNo.trim());
		}
		params.put("goodsMerchantId", merchantId);
		List<GoodsRecordDetail> goodsList = manualOrderDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
		if (goodsList != null && !goodsList.isEmpty()) {
			GoodsRecordDetail goods = goodsList.get(0);
			JSONObject goodsInfo = new JSONObject();
			goodsInfo.put(ORDER_ID, item.get("orderId") + "");
			String reEntGoodsNo = goods.getEntGoodsNo();
			if (StringEmptyUtils.isNotEmpty(marCode)) {
				String[] str = reEntGoodsNo.split("_");
				reEntGoodsNo = str[0];
			}
			goodsInfo.put("entGoodsNo", reEntGoodsNo);
			goodsInfo.put("HSCode", goods.getHsCode());
			if (StringEmptyUtils.isEmpty(goods.getBrand())) {
				goodsInfo.put("Brand", reEntGoodsNo);
			} else {
				goodsInfo.put("Brand", goods.getBrand());
			}
			goodsInfo.put("BarCode", goods.getBarCode());
			goodsInfo.put("CusGoodsNo", goods.getCusGoodsNo());
			goodsInfo.put("CIQGoodsNo", goods.getCiqGoodsNo());
			goodsInfo.put("GoodsName", goods.getGoodsName());
			goodsInfo.put("GoodsStyle", goods.getGoodsStyle());
			goodsInfo.put("OriginCountry", goods.getOriginCountry());
			goodsInfo.put("Unit", goods.getgUnit());
			goodsInfo.put("Price", item.get("price") + "");
			goodsInfo.put("Qty", item.get("count") + "");
			goodsInfo.put("netWt", goods.getNetWt());
			goodsInfo.put("grossWt", goods.getGrossWt());
			goodsInfo.put("stdUnit", goods.getStdUnit());
			goodsInfo.put("secUnit", goods.getSecUnit());
			goodsInfo.put("ebEntNo", goods.getEbEntNo());
			goodsInfo.put("ebEntName", goods.getEbEntName());
			goodsInfo.put("DZKNNo", goods.getDZKNNo());
			goodsInfo.put("seqNo", item.get("seqNo") + "");
			goodsInfo.put(MERCHANT_ID, merchantId);
			goodsInfo.put(MERCHANT_NAME, merchantName);
			String spareParam = goods.getSpareParams();
			if (StringEmptyUtils.isNotEmpty(spareParam)) {
				JSONObject json = JSONObject.fromObject(spareParam);
				String sku = json.get("SKU") + "";
				goodsInfo.put("SKU", sku);
				goodsInfo.put("marCode", marCode.trim());
			}
			return createNewSub(goodsInfo);
		} else {
			return ReturnInfoUtils.errorInfo(goodsName + "-->该商品不存在,请核实信息!");
		}
	}
}
