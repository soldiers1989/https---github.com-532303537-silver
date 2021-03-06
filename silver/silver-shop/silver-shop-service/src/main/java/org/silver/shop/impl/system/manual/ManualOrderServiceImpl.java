package org.silver.shop.impl.system.manual;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.manual.ManualOrderService;
import org.silver.shop.api.system.manual.MorderService;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.dao.system.manual.ManualOrderDao;
import org.silver.shop.model.common.base.City;
import org.silver.shop.model.common.base.Province;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.util.BufferUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.PhoneUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = ManualOrderService.class)
@Component("manualOrderService")
public class ManualOrderServiceImpl implements ManualOrderService, MessageListener {

	private static Logger logger = LogManager.getLogger(ManualOrderServiceImpl.class);

	@Autowired
	private ManualOrderDao manualOrderDao;
	@Autowired
	private BufferUtils bufferUtils;
	@Autowired
	private MorderService morderService;
	@Autowired
	private MemberService memberService;

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
	 * 驼峰命名：商户Id
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

	/**
	 * 运单号
	 */
	private static final String WAYBILL = "waybill";

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
				Map<String, Object> reCheckQiBangMap = checkQiBangOrderInfo(jsonDatas);
				if (!"1".equals(reCheckQiBangMap.get(BaseCode.STATUS.toString()))) {
					break;
				}
				qiBangCreateOrder(jsonDatas);
				break;
			default:
				break;
			}
			bufferUtils.writeCompletedRedisMq(redisMap);
		} catch (Exception e) {
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
		String waybill = datas.get(WAYBILL) + "";
		// 缓存参数
		Map<String, Object> params = (Map<String, Object>) datas.get("other");
		// 商品信息
		JSONObject goodsInfo = JSONObject.fromObject(datas.get("goodsInfo") + "");
		// 订单商品总金额
		Double orderTotalAmount = Double.parseDouble(datas.get("orderTotalAmount") + "");
		// 税费
		Double tax = Double.parseDouble(datas.get("tax") + "");
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put(WAYBILL, waybill);
		List<Morder> ml = manualOrderDao.findByProperty(Morder.class, paramsMap, 0, 0);
		if (ml == null) {
			RedisInfoUtils.errorInfoMq("运单号[" + waybill + "]查询订单信息失败,服务器繁忙!", ERROR, params);
		} else if (!ml.isEmpty()) {
			Morder morder = ml.get(0);
			checkExistedGuoZongOrder(morder, datas, params, orderTotalAmount, tax);
		} else {
			Morder morder = new Morder();
			// 查询缓存中订单自增Id
			int count = SerialNoUtils.getSerialNo("order");
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
				memberService.registerMember(morder);
			} else {
				RedisInfoUtils.errorInfoMq(morder.getOrder_id() + "<--订单存储失败，请重试!", ERROR, params);
			}
		}
		// 用于缓存辨别计算完成
		params.put("type", "success");
		bufferUtils.writeRedisMq(null, params);
	}

	/**
	 * 校验国宗已存在的订单信息
	 * 
	 * @param morder
	 *            订单信息实体类
	 * @param goodsInfo
	 *            商品信息
	 * @param params
	 *            缓存参数
	 * @param orderTotalAmount
	 *            订单商品总金额
	 * @param tax
	 *            税费
	 */
	private void checkExistedGuoZongOrder(Morder morder, JSONObject goodsInfo, Map<String, Object> params,
			Double orderTotalAmount, Double tax) {
		// 删除标识0-未删除,1-已删除
		if (morder.getDel_flag() == 1) {
			RedisInfoUtils.errorInfoMq(morder.getOrder_id() + "<--订单已被刪除,无法再次导入,请联系管理员!", ERROR, params);
		} else {
			Map<String, Object> reCheckMap = judgmentOrderInfo(morder, goodsInfo, orderTotalAmount, tax, 1);
			if ("10".equals(reCheckMap.get(BaseCode.STATUS.toString()) + "")) {// 当遇到超额时
				RedisInfoUtils.errorInfoMq(reCheckMap.get("msg") + "", "orderExcess", params);
				goodsInfo.put(ORDER_ID, reCheckMap.get(ORDER_ID)); // 根据订单Id创建订单商品
				// 成功后、调用创建商品方法
				Map<String, Object> reGoodsMap = createNewSub(goodsInfo);
				if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
					RedisInfoUtils.errorInfoMq(reGoodsMap.get(BaseCode.MSG.toString()) + "", ERROR, params);
				}
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
			memberService.registerMember(morder);
		}
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
		String waybill = datas.get(WAYBILL) + "";
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

	/**
	 * 校验启邦订单信息
	 * 
	 * @param jsonDatas
	 *            订单信息
	 * @return Map
	 */
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
			String msg = "订单号[" + orderId + "]实名认证不通过,请核实姓名与身份证号码！";
			RedisInfoUtils.errorInfoMq(msg, IDCARD, params);
		}
		// 收货人
		String recipientName = jsonDatas.get("recipientName") + "";
		// 订单人
		String orderDocName = jsonDatas.get("orderDocName") + "";
		// 娜地拉·艾孜拉提
		if (!StringUtil.isChinese(recipientName) || recipientName.contains("先生") || recipientName.contains("女士")) {
			String msg = "订单号[" + orderId + "]收货人姓名错误！";
			RedisInfoUtils.errorInfoMq(msg, "name", params);
		}
		if (!StringUtil.isChinese(orderDocName) || orderDocName.contains("先生") || orderDocName.contains("女士")) {
			String msg = "订单号[" + orderId + "]订单人姓名错误！";
			RedisInfoUtils.errorInfoMq(msg, "name", params);
		}
		if (!PhoneUtils.isPhone(recipientTel)) {
			String msg = "订单号[" + orderId + "]收件人电话错误！";
			RedisInfoUtils.errorInfoMq(msg, "phone", params);
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
		String orderId = morder.getOrder_id().trim();
		paramsMap.put("seqNo", goodsInfo.get("seqNo"));
		paramsMap.put("EntGoodsNo", goodsInfo.get("entGoodsNo"));
		paramsMap.put(ORDER_ID, morder.getOrder_id());
		List<MorderSub> orderGoodsList = manualOrderDao.findByProperty(MorderSub.class, paramsMap, 0, 0);
		if (orderGoodsList == null) {
			return ReturnInfoUtils.errorInfo("订单号[" + morder.getOrder_id() + "]<--查询订单商品信息失败!");
		} else if (!orderGoodsList.isEmpty()) {
			if (flag == 1) {
				return ReturnInfoUtils.errorInfo("运单号[" + morder.getWaybill() + "]<--与商品信息已存在,请勿需重复导入!");
			}
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]<--该订单与商品信息已存在,请勿需重复导入!");
		} else {
			double reFCY = morder.getFCY();
			double newFcy = reFCY + FCY;
			morder.setFCY(newFcy);
			double newActualAmountPaid = newFcy + tax;
			morder.setActualAmountPaid(newActualAmountPaid);
			if (!manualOrderDao.update(morder)) {
				return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]<--更新总价失败,服务器繁忙!");
			}
			statusMap.put("status", 1);
			statusMap.put(ORDER_ID, morder.getOrder_id());
			if (newFcy >= 2000) {// 当订单金额超过2000时
				if (flag == 1) {
					statusMap.clear();
					statusMap.put(BaseCode.STATUS.toString(), "10");
					statusMap.put(ORDER_ID, morder.getOrder_id());
					statusMap.put(BaseCode.MSG.toString(),
							"运单号[" + morder.getWaybill() + "],订单号[" + orderId + "]<--关联商品总计金额超过2000,请核对金额!");
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
		JSONObject spareParams = new JSONObject();
		if (StringEmptyUtils.isNotEmpty(marCode)) {
			spareParams.put("marCode", marCode);
		}
		if (StringEmptyUtils.isNotEmpty(sku)) {
			spareParams.put("SKU", sku);
		}
		if (StringEmptyUtils.isNotEmpty(ebEntNo)) {
			spareParams.put("ebEntNo", ebEntNo);
		}
		if (StringEmptyUtils.isNotEmpty(ebEntName)) {
			spareParams.put("ebEntName", ebEntName);
		}
		if (StringEmptyUtils.isNotEmpty(DZKNNo)) {
			spareParams.put("DZKNNo", DZKNNo);
		}
		if (!spareParams.isEmpty()) {
			mosb.setSpareParams(spareParams.toString());
		}
		mosb.setMerchant_no(goodsInfo.get(MERCHANT_ID) + "");
		mosb.setCreateBy(goodsInfo.get(MERCHANT_NAME) + "");
		// 删除标识:0-未删除,1-已删除
		mosb.setDeleteFlag(0);
		if (manualOrderDao.add(mosb)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]商品[" + goodsInfo.get("GoodsName") + "]存储失败，请重试!");
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
			int count = SerialNoUtils.getSerialNo("order");
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
			paramsMap.put("merchant_no", merchantId);
			List<Morder> ml = manualOrderDao.findByProperty(Morder.class, paramsMap, 1, 1);
			if (ml != null && !ml.isEmpty()) {
				Morder morder = ml.get(0);
				checkExistedQiBangOrder(morder, datas, params, orderTotalAmount, merchantId, merchantName);
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
				String waybillNo = datas.get("waybillNo") + "";
				if (StringEmptyUtils.isNotEmpty(waybillNo)) {
					morder.setWaybill(waybillNo);
				}
				morder.setDel_flag(0);
				morder.setOrder_record_status(1);
				morder.setCreate_date(new Date());
				morder.setCreate_by(merchantName);
				morder.setFcode(FCODE);
				String randomDate = DateUtil.randomCreateDate();
				morder.setOrderDate(randomDate);
				// 订单录入系统类型：online-商城真实下单(线上)、offline-线下导入
				morder.setOrderSourceType("offline");
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
					memberService.registerMember(morder);
				} else {
					RedisInfoUtils.errorInfoMq("订单[" + orderId + "]保存失败,订单信息错误或订单已存在,请核对信息!", ERROR, params);
				}
			}
		}
		// 用于缓存辨别计算完成
		params.put("type", "success");
		bufferUtils.writeRedisMq(null, params);
	}

	/**
	 * 启邦创建订单时,当订单已存在则进行业务处理
	 * 
	 * @param morder
	 *            订单实体类
	 * @param datas
	 *            订单信息
	 * @param params
	 *            缓存参数信息
	 * @param orderTotalAmount
	 *            订单商品总金额
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 */
	private void checkExistedQiBangOrder(Morder morder, JSONObject datas, Map<String, Object> params,
			double orderTotalAmount, String merchantId, String merchantName) {
		// 删除标识0-未删除,1-已删除
		if (morder.getDel_flag() == 1) {
			RedisInfoUtils.errorInfoMq(morder.getOrder_id() + "<--订单已被刪除,无法再次导入,请联系管理员!", ERROR, params);
		} else if (morder.getOrder_record_status() == 3) {// 备案状态：1-未备案,2-备案中,3-备案成功,4-备案失败
			RedisInfoUtils.errorInfoMq(morder.getOrder_id() + "<--订单在[" + morder.getCreate_date() + "]已经申报成功,请勿重复导入！",
					ERROR, params);
		} else {
			// 企邦的税费暂写死为0
			Map<String, Object> reCheckMap = judgmentOrderInfo(morder, datas, orderTotalAmount, 0.0, 2);
			if ("10".equals(reCheckMap.get(BaseCode.STATUS.toString()) + "")) {// 当遇到超额时
				RedisInfoUtils.errorInfoMq(reCheckMap.get(BaseCode.MSG.toString()) + "", "orderExcess", params);
				// 成功后、调用创建商品方法
				Map<String, Object> reGoodsMap = createQBOrderSub(merchantId, datas, merchantName);
				if (!"1".equals(reGoodsMap.get(BaseCode.STATUS.toString()) + "")) {
					RedisInfoUtils.errorInfoMq(reGoodsMap.get(BaseCode.MSG.toString()) + "", ERROR, params);
				}
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
			memberService.registerMember(morder);
		}
	}

	/**
	 * 准备开始创建启邦订单商品信息,根据商品自编号+平台号查询商品备案信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param item
	 *            商品信息
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
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

	@Override
	public Map<String, Object> updateManualOrderInfo(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("修改参数不能为空！");
		}
		String merchantId = datasMap.get(MERCHANT_ID) + "";
		String orderId = datasMap.get("orderId") + "";
		Map<String, Object> params = new HashMap<>();
		params.put("merchant_no", merchantId);
		params.put(ORDER_ID, orderId);
		List<Morder> orderList = manualOrderDao.findByProperty(Morder.class, params, 0, 0);
		if (orderList == null) {
			return ReturnInfoUtils.errorInfo("查询订单信息失败,服务器繁忙！");
		} else if (!orderList.isEmpty()) {
			Morder order = orderList.get(0);
			// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
			if (order.getOrder_record_status() == 1 || order.getOrder_record_status() == 4) {
				return updateManualOrder(order, datasMap);
			} else {
				return ReturnInfoUtils.errorInfo("当前订单申报状态不允许修改订单信息！");
			}
		} else {
			return ReturnInfoUtils.errorInfo("未找到订单信息！");
		}
	}

	/**
	 * 更新手工订单信息
	 * 
	 * @param order
	 *            订单实体信息类
	 * @param datasMap
	 *            订单参数
	 * @return Map
	 */
	private Map<String, Object> updateManualOrder(Morder order, Map<String, Object> datasMap) {
		if (order == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("更新订单信息时,请求参数不能为null");
		}
		String recipientName = datasMap.get("recipientName") + "";
		// 娜地拉·艾孜拉提
		if (!StringUtil.isContainChinese(recipientName.replace("·", ""))) {
			return ReturnInfoUtils.errorInfo("收货人姓名错误！");
		}
		order.setRecipientName(recipientName);
		String recipientID = datasMap.get("recipientID") + "";
		if (!IdcardValidator.validate18Idcard(recipientID)) {
			return ReturnInfoUtils.errorInfo("收货人身份证号码错误！");
		}
		order.setRecipientID(recipientID.replace("x", "X"));
		String recipientTel = datasMap.get("recipientTel") + "";
		if (!PhoneUtils.isPhone(recipientTel)) {
			return ReturnInfoUtils.errorInfo("收货人电话号码错误,暂只支持移动电话号码！");
		}
		order.setRecipientTel(recipientTel);
		order.setRecipientProvincesCode(datasMap.get("recipientProvincesCode") + "");
		order.setRecipientCityCode(datasMap.get("recipientCityCode") + "");
		order.setRecipientAreaCode(datasMap.get("recipientAreaCode") + "");
		order.setRecipientAreaName(datasMap.get("recipientAreaName") + "");
		Map<String, Object> reCheckMap = checkProvincesCityArea(order);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		String oldOrderDocName = order.getOrderDocName();
		String oldOrderDocId = order.getOrderDocId();
		String orderDocTel = datasMap.get("orderDocTel") + "";
		if (!PhoneUtils.isPhone(orderDocTel)) {
			return ReturnInfoUtils.errorInfo("下单人电话错误,暂只支持移动电话号码！");
		}
		order.setOrderDocTel(orderDocTel);
		order.setRecipientAddr(datasMap.get("recipientAddr") + "");
		String newOrderDocName = datasMap.get("orderDocName") + "";
		if (!StringUtil.isContainChinese(newOrderDocName.replace("·", ""))) {
			return ReturnInfoUtils.errorInfo("下单人姓名错误！");
		}
		order.setOrderDocName(newOrderDocName);
		String newOrderDocId = datasMap.get("orderDocId") + "";
		if (!IdcardValidator.validate18Idcard(newOrderDocId)) {
			return ReturnInfoUtils.errorInfo("下单人身份证号码错误！");
		}
		order.setOrderDocId(newOrderDocId.replace("x", "X"));
		if (!oldOrderDocName.equals(newOrderDocName) || !oldOrderDocId.equals(newOrderDocId)) {
			// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
			order.setIdcardCertifiedFlag(0);
		}
		order.setWaybill(datasMap.get(WAYBILL) + "");
		if (!manualOrderDao.update(order)) {
			return ReturnInfoUtils.errorInfo("修改失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 检查省市区代码与名称是否正确
	 * 
	 * @param order
	 *            订单信息实体类
	 * @return Map
	 */
	private Map<String, Object> checkProvincesCityArea(Morder order) {
		if (order == null) {
			return ReturnInfoUtils.errorInfo("检查省市区失败,订单不能为null");
		}
		String recipientProvincesCode = order.getRecipientProvincesCode();
		Map<String, Object> params = new HashMap<>();
		params.put("provinceCode", recipientProvincesCode);
		List<Province> rePronvinceList = manualOrderDao.findByProperty(Province.class, params, 1, 1);
		if (rePronvinceList == null || rePronvinceList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("收货人省份名称错误！");
		}
		Province province = rePronvinceList.get(0);
		order.setRecipientProvincesName(province.getProvinceName());
		params.clear();
		params.put("cityCode", order.getRecipientCityCode());
		List<City> reCityList = manualOrderDao.findByProperty(City.class, params, 1, 1);
		if (reCityList == null || reCityList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("收货人城市名称错误！");
		}
		City city = reCityList.get(0);
		order.setRecipientCityName(city.getCityName());
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> updateManualOrderGoodsInfo(Merchant merchantInfo, Map<String, Object> datasMap) {
		if (datasMap == null || merchantInfo == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		String oldEntGoodsNo = datasMap.get("oldEntGoodsNo") + "";
		String orderId = datasMap.get("orderId") + "";
		int seqNo = Integer.parseInt(datasMap.get("seqNo") + "");
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(ORDER_ID, orderId);
		paramMap.put("EntGoodsNo", oldEntGoodsNo);
		paramMap.put("merchant_no", merchantInfo.getMerchantId());
		paramMap.put("seqNo", seqNo);
		List<MorderSub> reOrderSubList = manualOrderDao.findByProperty(MorderSub.class, paramMap, 1, 1);
		if (reOrderSubList == null) {
			return ReturnInfoUtils.errorInfo("查询失败，服务器繁忙！");
		} else if (!reOrderSubList.isEmpty()) {
			MorderSub goodsInfo = reOrderSubList.get(0);
			JSONArray jsonList = new JSONArray();
			jsonList.add(datasMap);
			Map<String, Object> reCheckMap = checkManualOrderGoodsInfo(jsonList);
			if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
				return reCheckMap;
			}
			return updateManualOrderGoodsInfo(merchantInfo, datasMap, goodsInfo);
		} else {
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]中未找到商品自编号[" + oldEntGoodsNo + "]的商品信息！");
		}
	}

	private Map<String, Object> updateManualOrderGoodsInfo(Merchant merchantInfo, Map<String, Object> datasMap,
			MorderSub goodsInfo) {
		if (datasMap == null || merchantInfo == null || goodsInfo == null) {
			return ReturnInfoUtils.errorInfo("修改参数不能为null");
		}
		// goodsInfo.setSeq(Integer.parseInt(strArr[1]));
		goodsInfo.setEntGoodsNo(datasMap.get("newEntGoodsNo") + "");
		goodsInfo.setHSCode(datasMap.get("hsCode") + "");
		goodsInfo.setGoodsName(datasMap.get("goodsName") + "");
		goodsInfo.setCusGoodsNo(datasMap.get("cusGoodsNo") + "");
		goodsInfo.setCIQGoodsNo(datasMap.get("ciqGoodsNo") + "");
		goodsInfo.setOriginCountry(datasMap.get("originCountry") + "");
		goodsInfo.setGoodsStyle(datasMap.get("goodsStyle") + "");
		goodsInfo.setBarCode(datasMap.get("barCode") + "");
		goodsInfo.setBrand(datasMap.get("brand") + "");
		goodsInfo.setUnit(datasMap.get("unit") + "");
		double netWt = 0;
		double grossWt = 0;
		try {
			netWt = Double.parseDouble(datasMap.get("netWt") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("净重错误！");
		}
		try {
			grossWt = Double.parseDouble(datasMap.get("grossWt") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("毛重错误！");
		}
		if (netWt > grossWt) {
			return ReturnInfoUtils.errorInfo("净重不能大于毛重！");
		}
		goodsInfo.setNetWt(netWt);
		goodsInfo.setGrossWt(grossWt);
		if (StringEmptyUtils.isEmpty(datasMap.get("firstLegalCount") + "")) {
			goodsInfo.setFirstLegalCount(0.0);
		} else {
			goodsInfo.setFirstLegalCount(Double.parseDouble(datasMap.get("firstLegalCount") + ""));
		}
		if (StringEmptyUtils.isEmpty(datasMap.get("secondLegalCount") + "")) {
			goodsInfo.setSecondLegalCount(0.0);
		} else {
			goodsInfo.setSecondLegalCount(Double.parseDouble(datasMap.get("secondLegalCount") + ""));
		}
		goodsInfo.setStdUnit(datasMap.get("stdUnit") + "");
		goodsInfo.setSecUnit(datasMap.get("secUnit") + "");
		if (StringEmptyUtils.isEmpty(datasMap.get("numOfPackages"))) {
			goodsInfo.setNumOfPackages(0);
		} else {
			goodsInfo.setNumOfPackages(Integer.parseInt(datasMap.get("numOfPackages") + ""));
		}
		if (StringEmptyUtils.isEmpty(datasMap.get("packageType"))) {
			goodsInfo.setPackageType(0);
		} else {
			goodsInfo.setPackageType(Integer.parseInt(datasMap.get("packageType") + ""));
		}
		goodsInfo.setTransportModel(datasMap.get("transportModel") + "");
		goodsInfo.setUpdateBy(merchantInfo.getMerchantName());
		goodsInfo.setUpdateDate(new Date());
		if (!manualOrderDao.update(goodsInfo)) {
			return ReturnInfoUtils.errorInfo("更新订单备案商品错误,请重试!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验修改手工订单商品信息
	 * 
	 * @param jsonList
	 * @return Map
	 */
	private Map<String, Object> checkManualOrderGoodsInfo(JSONArray jsonList) {
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("newEntGoodsNo");
		noNullKeys.add("ciqGoodsNo");
		noNullKeys.add("cusGoodsNo");
		noNullKeys.add("hsCode");
		noNullKeys.add("goodsName");
		noNullKeys.add("goodsStyle");
		noNullKeys.add("originCountry");
		return CheckDatasUtil.changeMsg(jsonList, noNullKeys);
	}

	@Override
	public Map<String, Object> sendMsgToLogistics(Merchant merchantInfo, List<String> orderList) {
		if (merchantInfo == null || orderList == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		List<Map<String, Object>> errList = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> errorMap = null;
		for (int i = 0; i < orderList.size(); i++) {
			String orderId = orderList.get(i);
			params.clear();
			params.put(ORDER_ID, orderId);
			List<Morder> reOrderList = manualOrderDao.findByProperty(Morder.class, params, 1, 1);
			if (reOrderList == null) {
				return ReturnInfoUtils.errorInfo("查询失败，服务器繁忙！");
			} else if (!reOrderList.isEmpty()) {
				Morder order = reOrderList.get(0);
				Map<String, Object> reSendMap = sendLogistics(order, merchantInfo);
				if (!"1".equals(reSendMap.get(BaseCode.STATUS.toString()))) {
					errorMap = new HashMap<>();
					errorMap.put(BaseCode.MSG.toString(), reSendMap.get(BaseCode.MSG.toString()));
					errList.add(errorMap);
				}
			} else {
				errorMap = new HashMap<>();
				errorMap.put(BaseCode.MSG.toString(), "订单编号[" + orderId + "]未找到订单信息！");
				errList.add(errorMap);
			}
		}
		return ReturnInfoUtils.errorInfo(errList);
	}

	/**
	 *	订单推送至物流
	 * @param order 订单信息
	 * @param merchantInfo 商户信息
	 * @return Map
	 */
	private Map<String, Object> sendLogistics(Morder order, Merchant merchantInfo) {
		if (order == null || merchantInfo == null) {
			return ReturnInfoUtils.errorInfo("推送参数不能为null");
		}
		Map<String, Object> item = new HashMap<>();
		item.put("messageId", order.getOrder_serial_no());
		item.put("logisticsSign", merchantInfo.getLogisticsCompanyCode());
		JSONObject json = JSONObject.fromObject(order.getSpareParams());
		item.put("logisticsSignSub", json.get("ehsEntName"));
		System.out.println("--请求参数->>>" + item.toString());
		String result = YmHttpUtil.HttpPost("http://cbsp.191ec.com/silver-web/Eport/sendMsgToLogistics", item);
		if (StringEmptyUtils.isEmpty(result)) {
			return ReturnInfoUtils.errorInfo("订单编号[" + order.getOrder_id() + "]推送物流失败！");
		} else {
			JSONObject reJSON = JSONObject.fromObject(result);
			System.out.println("--返回参数->>" + reJSON.toString());
			if (!"1".equals(reJSON.get(BaseCode.STATUS.toString()) + "")) {
				return ReturnInfoUtils.errorInfo("订单编号[" + order.getOrder_id() + "]" + reJSON.get("msg"));
			}
			return ReturnInfoUtils.successInfo();
		}
	}

	@Override
	public Map<String, Object> getWaybillNumber(Merchant merchantInfo, String orderId) {
		if(merchantInfo == null){
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Map<String,Object> params = new HashMap<>();
		params.put(ORDER_ID, orderId);
		List<Morder> reOrderList = manualOrderDao.findByProperty(Morder.class, params, 1, 1);
		if (reOrderList == null) {
			return ReturnInfoUtils.errorInfo("查询失败，服务器繁忙！");
		}else if(reOrderList.isEmpty()){
			Morder order = reOrderList.get(0);
			Map<String,Object> item = new HashMap<>();
			item.put("order_code", order.getOrder_id());
			String result = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web/waybill/queryOrderStatus", item);
			if (StringEmptyUtils.isEmpty(result)) {
				return ReturnInfoUtils.errorInfo("订单编号[" + order.getOrder_id() + "]获取运单号失败，请联系管理员！");
			} else {
				JSONObject reJSON = JSONObject.fromObject(result);
				System.out.println("--返回参数->>" + reJSON.toString());
				if (!"true".equals(reJSON.get("success") + "")) {
					return ReturnInfoUtils.errorInfo(reJSON.get("errorMsg")+" 请联系管理员！");
				}
				order.setWaybill(reJSON.get("waybill_number")+"");
				if(!manualOrderDao.update(order)){
					return ReturnInfoUtils.errorInfo("更新失败，服务器繁忙！");
				}
				return ReturnInfoUtils.successInfo();
			}
		}else{
			return ReturnInfoUtils.errorInfo("订单编号[" + orderId + "]未找到订单信息！");
		}
	}

}
