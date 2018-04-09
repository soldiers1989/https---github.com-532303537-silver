package org.silver.shop.impl.system.manual;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.manual.ManualOrderService;
import org.silver.shop.dao.system.manual.ManualOrderDao;
import org.silver.shop.dao.system.manual.MorderSubDao;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.util.DateUtil;
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

	@Override
	public void onMessage(Message message) {
		TextMessage textmessage = (TextMessage) message;
		System.out.println("----------------000000000000000000---------");
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(textmessage.getText());
		} catch (Exception e) {
			logger.error("--队列获取参数错误->", e);
		}

		String type = json.get("type") + "";
		switch (type) {
		// 国宗excel表单订单导入
		case "guozongExcelOrderImpl":
			guoCreateNew(json);
			break;
		default:
			break;
		}
	}

	public void guoCreateNew(JSONObject datas) {
		String waybill = datas.get("waybill") + "";
		// 缓存参数
		Map<String, Object> params = (Map<String, Object>) datas.get("other");
		// 错误信息
		List<Map<String, Object>> errorList = (List<Map<String, Object>>) datas.get("errorList");
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
			RedisInfoUtils.commonErrorInfo("运单号[" + waybill + "]查询订单信息失败,服务器繁忙!", errorList, 1, paramsMap);
		} else if (!ml.isEmpty()) {
			Morder morder = ml.get(0);
			// 删除标识0-未删除,1-已删除
			if (morder.getDel_flag() == 1) {
				RedisInfoUtils.commonErrorInfo(morder.getOrder_id() + "<--订单已被刪除,无法再次导入,请联系管理员!", errorList, 1,
						paramsMap);
			}
			Map<String, Object> reCheckMap = judgmentOrderInfo(morder, goodsInfo, orderTotalAmount, tax, 1);
			if ("10".equals(reCheckMap.get("status") + "")) {// 当遇到超额时
				RedisInfoUtils.commonErrorInfo(reCheckMap.get("msg") + "", errorList, 2, paramsMap);
			} else if (!"1".equals(reCheckMap.get("status") + "")) {
				RedisInfoUtils.commonErrorInfo(reCheckMap.get("msg") + "", errorList, 1, paramsMap);
			}
			// 成功后、调用创建商品方法

			// return judgmentOrderInfo(morder, goodsInfo, FCY, Tax, 1);
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
			morder.setRecipientName(datas.get("recipientName")+"");
			morder.setRecipientID(datas.get("recipientID")+"");
			morder.setRecipientTel(datas.get("recipientTel")+"");
			// 暂时默认为下单人姓名
			morder.setRecipientAddr(datas.get("recipientAddr")+"");
			morder.setOrderDocAcount(datas.get("orderDocAcount")+"");
			morder.setOrderDocName(datas.get("orderDocName")+"");
			morder.setOrderDocType("01");
			// 身份证
			morder.setOrderDocId(datas.get("orderDocId")+"");
			morder.setOrderDocTel(datas.get("orderDocTel")+"");
			morder.setMerchant_no(goodsInfo.get("merchantId")+"");
			morder.setDateSign(DateUtil.formatDate(new Date(), "yyyyMMdd"));
			morder.setSerial(Integer.parseInt(params.get("serialNo")+""));
			morder.setWaybill(waybill);
			//刪除标识
			morder.setDel_flag(0); 
			// 订单备案状态 
			morder.setOrder_record_status(1);
			morder.setCreate_date(new Date());
			morder.setCreate_by(goodsInfo.get("merchantName") + "");
			morder.setFcode("142");
			morder.setSenderName(datas.get("senderName")+"");
			morder.setSenderCountry(datas.get("senderCountry")+"");
			morder.setSenderAreaCode(datas.get("senderAreaCode")+"");
			morder.setSenderAddress(datas.get("senderAddress")+"");
			morder.setSenderTel(datas.get("senderTel")+"");

			morder.setPostal(datas.get("postal")+"");
			morder.setRecipientProvincesCode(datas.get("provinceCode")+"");
			morder.setRecipientProvincesName(datas.get("provinceName")+"");
			morder.setRecipientCityCode(datas.get("cityCode")+"");
			morder.setRecipientCityName(datas.get("cityName")+"");
			morder.setRecipientAreaCode(datas.get("areaCode")+"");
			morder.setRecipientAreaName(datas.get("areaName")+"");
			String randomDate = DateUtil.randomCreateDate();
			morder.setOrderDate(randomDate);
			morder.setCustomsCode(goodsInfo.get("customsCode") + "");
			if (manualOrderDao.add(morder)) {
				//保存成功之后,进行商品实例化
				
				//statusMap.put("status", 1);
				//statusMap.put("order_id", newOrderId);
				//return statusMap;
			}
			//保存失败之后
			RedisInfoUtils.commonErrorInfo(morder.getOrder_id() + "<--订单存储失败，请重试!", errorList, 1, paramsMap);
		}

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
		// paramsMap.put("merchant_no", merchantId);
		paramsMap.put("order_id", morder.getOrder_id());
		List<MorderSub> ms = manualOrderDao.findByProperty(MorderSub.class, paramsMap, 0, 0);
		if (ms == null) {
			return ReturnInfoUtils.errorInfo("运单号[" + waybill + "]  <--查询订单商品信息失败!");
		} else if (!ms.isEmpty()) {
			if (flag == 1) {
				return ReturnInfoUtils.errorInfo("运单号[" + waybill + "]  <--与商品信息已存在,无需重复导入!");
			}
			return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]  <--该订单与商品信息已存在,无需重复导入!");
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
			statusMap.put("order_id", morder.getOrder_id());
			if (newFcy >= 2000) {// 当订单金额超过2000时
				if (flag == 1) {
					statusMap.clear();
					statusMap.put(BaseCode.STATUS.toString(), "10");
					statusMap.put("order_id", morder.getOrder_id());
					statusMap.put(BaseCode.MSG.toString(),
							"运单号[" + waybill + "],订单号[" + orderId + "]<--关联商品总计金额超过2000,请核对金额!");
				} else if (flag == 2) {
					statusMap.clear();
					statusMap.put(BaseCode.STATUS.toString(), "10");
					statusMap.put("order_id", morder.getOrder_id());
					statusMap.put(BaseCode.MSG.toString(), "订单号[" + orderId + "]<--关联商品总计金额超过2000,请核对金额!");
				}
			}
			return statusMap;
		}
	}
	
	public Map<String, Object> createNewSub(JSONObject goodsInfo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		String orderId = goodsInfo.get("order_id") + "";
		params.put("order_id", orderId);
		Long count = manualOrderDao.findByPropertyCount(MorderSub.class, params);
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
		mosb.setMerchant_no(goodsInfo.get("merchantId") + "");
		mosb.setCreateBy(goodsInfo.get("merchantName") + "");
		// 删除标识:0-未删除,1-已删除
		mosb.setDeleteFlag(0);
		if (manualOrderDao.add(mosb)) {
			statusMap.put("status", 1);
			statusMap.put("msg", "订单商品【" + goodsInfo.get("GoodsName") + "】存储成功");
			return statusMap;
		}
		statusMap.put("status", -1);
		statusMap.put("msg", "订单商品【" + goodsInfo.get("GoodsName") + "】存储失败，请重试!");
		return statusMap;
	}
}
