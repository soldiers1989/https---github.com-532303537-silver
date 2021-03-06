package org.silver.shop.impl.system.manual;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.GoodsRecordService;
import org.silver.shop.api.system.manual.MorderService;
import org.silver.shop.dao.system.manual.MorderDao;
import org.silver.shop.dao.system.manual.MorderSubDao;
import org.silver.shop.dao.system.manual.MuserDao;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.system.commerce.GoodsRecord;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.manual.Muser;
import org.silver.shop.model.system.manual.OldManualOrder;
import org.silver.shop.model.system.manual.OldManualOrderSub;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.task.UpdateMOrderGoodsTask;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.util.CopyUtils;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = MorderService.class)
public class MorderServiceImpl implements MorderService {

	@Resource
	private MorderDao morderDao;
	@Resource
	private MorderSubDao morderSubDao;
	@Resource
	private MuserDao muserDao;
	@Autowired
	private GoodsRecordService goodsRecordService;
	@Autowired
	private MerchantUtils merchantUtils;

	// 海关币制默认为人名币
	private static final String FCODE = "142";
	/**
	 * 线程锁
	 */
	private static final Object LOCK = "lock";

	/**
	 * 下划线命名版：订单Id
	 */
	private static final String ORDER_ID = "order_id";

	@Override
	public boolean saveRecord(String merchant_no, String[] head, int body_length, String[][] body) {
		Date d = new Date();
		if (saveHead(merchant_no, head, d)) {
			if (saveBody(head[1], body_length, body, d)) {
				return true;
			}
		}
		return false;
	}

	private boolean saveHead(String merchant_no, String[] head, Date d) {
		Morder morder = new Morder();
		morder.setOrderDate(head[0]);
		morder.setOrder_id(head[1]);
		morder.setFCY(Double.parseDouble(head[2]));
		morder.setTax(Double.parseDouble(head[3]));
		morder.setActualAmountPaid(Double.parseDouble(head[4]));
		morder.setRecipientName(head[5]);
		morder.setRecipientID(head[6]);
		morder.setRecipientTel(head[7]);
		morder.setRecipientProvincesCode(head[8]);
		morder.setRecipientAddr(head[9]);
		morder.setOrderDocAcount(head[10]);
		morder.setOrderDocName(head[11]);
		morder.setOrderDocType(head[12]);
		morder.setOrderDocId(head[13]);
		morder.setOrderDocTel(head[14]);

		morder.setMerchant_no(merchant_no);
		morder.setDel_flag(0);
		morder.setStatus(0);
		morder.setCreate_date(d);
		morder.setCreate_by(merchant_no);
		morder.setFcode(FCODE);
		return morderDao.add(morder);

	}

	private boolean saveBody(String order_id, int body_length, String[][] body, Date d) {
		for (int r = 0; r < body_length; r++) {
			MorderSub mob = new MorderSub();
			mob.setSeq(Integer.parseInt(body[r][0]));
			mob.setEntGoodsNo(body[r][1]);
			mob.setHSCode(body[r][2]);
			mob.setBrand(body[r][3]);
			mob.setBarCode(body[r][4]);
			mob.setGoodsName(body[r][5]);
			mob.setOriginCountry(body[r][6]);
			mob.setCusGoodsNo(body[r][7]);
			mob.setCIQGoodsNo(body[r][8]);
			mob.setGoodsStyle(body[r][9]);
			mob.setUnit(body[r][10]);
			mob.setPrice(Double.parseDouble(body[r][11]));
			mob.setQty(Integer.parseInt(body[r][12]));
			mob.setTotal(Double.parseDouble(body[r][13]));
			mob.setCreate_date(d);
			mob.setOrder_id(order_id);
			if (!morderSubDao.add(mob)) {
				return false;
			}
		}
		return true;

	}

	@Override
	public Map<String, Object> pageFindRecords(Map<String, Object> dataMap, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalMOrderSearch(dataMap);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		paramMap.put("del_flag", 0);
		List orList = (List) reDatasMap.get("orList");
		List<Morder> mlist = morderDao.findByPropertyOr(Morder.class, paramMap, orList, page, size);
		long count = morderDao.findByPropertyOrCount(Morder.class, paramMap, orList);
		if (mlist == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!mlist.isEmpty()) {
			List<Map<String, Object>> lMap = new ArrayList<>();
			paramMap.clear();
			for (Morder m : mlist) {
				paramMap.put(ORDER_ID, m.getOrder_id());
				paramMap.put("deleteFlag", 0);
				List<MorderSub> mslist = morderSubDao.findByProperty(paramMap, 0, 0);
				Map<String, Object> item = new HashMap<>();
				item.put("head", m);
				item.put("content", mslist);
				lMap.add(item);
			}
			Map<String, Object> statusMap = new HashMap<>();
			statusMap.put("status", 1);
			statusMap.put("datas", lMap);
			statusMap.put("count", count);
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String, Object> createNew(String merchant_no, String OrderDate, String order_id, Double FCY, Double Tax,
			Double ActualAmountPaid, String RecipientName, String RecipientID, String RecipientTel,
			String RecipientProvincesCode, String RecipientAddr, String OrderDocAcount, String OrderDocName,
			String OrderDocId, String OrderDocTel, String autoMSHR, String autoMXDR) {
		Map<String, Object> statusMap = new HashMap<>();

		if (needTofindMu(autoMSHR, autoMXDR)) {
			List<Muser> mul = muserDao.randomGet(merchant_no);
			if (mul != null && mul.size() > 0) {
				if (autoMSHR != null && autoMSHR.trim().equals("1")) {
					RecipientName = mul.get(0).getMuser_name();
					RecipientID = mul.get(0).getMuser_ID();
					RecipientTel = mul.get(0).getMuser_tel();
					RecipientProvincesCode = mul.get(0).getAdm_area_code();
					RecipientAddr = mul.get(0).getMuser_addr();
				}

				if (autoMXDR != null && autoMXDR.trim().equals("1")) {
					OrderDocAcount = mul.get(0).getMuser_sys_no();
					OrderDocName = mul.get(0).getMuser_name();
					OrderDocId = mul.get(0).getMuser_ID();
					OrderDocTel = mul.get(0).getMuser_tel();
				}
			} else {
				statusMap.put("status", -7);
				statusMap.put("msg", "未能找到自动匹配的人员信息，请录入后再试");
				return statusMap;

			}
		}

		if (merchant_no != null && !"".equals(merchant_no.trim()) && OrderDate != null && !"".equals(OrderDate.trim())
				&& order_id != null && !"".equals(order_id.trim()) && FCY != null && Tax != null
				&& ActualAmountPaid != null && RecipientName != null && RecipientID != null && RecipientTel != null
				&& RecipientProvincesCode != null && RecipientAddr != null && OrderDocAcount != null
				&& OrderDocName != null && OrderDocId != null && OrderDocTel != null) {
			Map<String, Object> params = new HashMap<>();
			params.put(ORDER_ID, order_id);
			List<Morder> ml = morderDao.findByProperty(Morder.class, params, 0, 0);
			if (ml != null && ml.size() > 0) {
				statusMap.put("status", -4);
				statusMap.put("msg", "订单号:【" + order_id + "】已经存在,不需要重复添加");
				return statusMap;
			}
			Morder morder = new Morder();
			morder.setOrderDate(OrderDate);
			morder.setOrder_id(order_id);
			morder.setFCY(FCY);
			morder.setTax(Tax);
			morder.setActualAmountPaid(ActualAmountPaid);
			morder.setRecipientName(RecipientName);
			morder.setRecipientID(RecipientID);
			morder.setRecipientTel(RecipientTel);
			morder.setRecipientProvincesCode(RecipientProvincesCode);
			morder.setRecipientAddr(RecipientAddr);
			morder.setOrderDocAcount(OrderDocAcount);
			morder.setOrderDocName(OrderDocName);
			morder.setOrderDocType("01");// 身份证
			morder.setOrderDocId(OrderDocId);
			morder.setOrderDocTel(OrderDocTel);
			morder.setDateSign(OrderDate.substring(0, 7));
			morder.setMerchant_no(merchant_no);
			morder.setDel_flag(0);
			morder.setStatus(0);
			morder.setCreate_date(new Date());
			morder.setCreate_by(merchant_no);
			morder.setFcode(FCODE);
			if (morderDao.add(morder)) {
				statusMap.put("status", 1);
				statusMap.put("msg", "存储完毕");
				return statusMap;
			}
			statusMap.put("status", -1);
			statusMap.put("msg", "存储失败，请稍后重试");
			return statusMap;
		}
		statusMap.put("status", -3);
		statusMap.put("msg", "参数有误，请检查金额或表格数据是否填写正确完整");
		return statusMap;
	}

	private boolean needTofindMu(String autoMSHR, String autoMXDR) {
		if ((autoMSHR != null && autoMSHR.trim().equals("1")) || autoMXDR != null && autoMXDR.trim().equals("1")) {
			return true;
		}
		return false;
	}

	@Override
	public Map<String, Object> createNewSub(JSONObject goodsInfo) {
		Map<String, Object> statusMap = new HashMap<>();
		/*
		 * JSONArray datas = new JSONArray(); datas.add(goodsInfo); List<String>
		 * noNullKeys = new ArrayList<>(); noNullKeys.add("entGoodsNo");
		 * noNullKeys.add("HSCode"); noNullKeys.add("Brand"); //
		 * noNullKeys.add("BarCode"); noNullKeys.add("GoodsName");
		 * noNullKeys.add("OriginCountry"); noNullKeys.add("CusGoodsNo");
		 * noNullKeys.add("CIQGoodsNo"); noNullKeys.add("GoodsStyle");
		 * noNullKeys.add("Unit"); noNullKeys.add("Price");
		 * noNullKeys.add("Qty");
		 * 
		 * Map<String, Object> checkMap = CheckDatasUtil.changeMsg(datas,
		 * noNullKeys); if ((int) checkMap.get("status") != 1) { return
		 * checkMap; }
		 */
		Map<String, Object> map = new HashMap<>();
		String orderId = goodsInfo.get(ORDER_ID) + "";
		map.put(ORDER_ID, orderId);
		long count = morderSubDao.findByPropertyCount(map);
		/*
		 * map.put("EntGoodsNo", goodsInfo.get("EntGoodsNo")); List<MorderSub>
		 * mls = morderSubDao.findByProperty(map, 0, 0); if (mls != null &&
		 * mls.size() > 0) { statusMap.put("status", -4); statusMap.put("msg",
		 * "订单【" + goodsInfo.get(ORDER_ID) + "】" + "关联的商品【" +
		 * goodsInfo.get("GoodsName") + "】已经存在，不需要重复添加"); return statusMap; }
		 */
		MorderSub mosb = new MorderSub();
		mosb.setSeq(Integer.parseInt((count + 1) + ""));
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
		if (morderSubDao.add(mosb)) {
			statusMap.put("status", 1);
			statusMap.put("msg", "订单商品【" + goodsInfo.get("GoodsName") + "】存储成功");
			return statusMap;
		}
		statusMap.put("status", -1);
		statusMap.put("msg", "订单商品【" + goodsInfo.get("GoodsName") + "】存储失败，请重试!");
		return statusMap;
	}

	@Override
	public Map<String, Object> deleteByOrderId(String merchantId, String merchantName, String orderIdPack) {
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(orderIdPack);
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("订单Id参数格式错误!");
		}
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		Map<String, Object> errMap = null;
		for (int i = 0; i < jsonList.size(); i++) {
			params.clear();
			params.put("merchant_no", merchantId);
			String orderId = jsonList.get(i) + "";
			params.put(ORDER_ID, orderId);
			List<Morder> orderlist = morderDao.findByProperty(Morder.class, params, 1, 1);
			if (orderlist != null && !orderlist.isEmpty()) {
				Morder order = orderlist.get(0);
				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				if (order.getOrder_record_status() == 1 || order.getOrder_record_status() == 4) {
					// 删除标识:0-未删除,1-已删除
					order.setDel_flag(1);
					order.setUpdate_by(merchantName);
					order.setUpdate_date(new Date());
					if (!morderDao.update(order)) {
						errMap = new HashMap<>();
						errMap.put(BaseCode.MSG.toString(), "订单号[" + orderId + "]删除失败,服务器繁忙!");
						errMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
						errorList.add(errMap);
					}
				} else {
					errMap = new HashMap<>();
					errMap.put(BaseCode.MSG.toString(), "订单号[" + orderId + "]订单当前状态不允许删除,请联系管理员!");
					errMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					errorList.add(errMap);
				}
			}
		}
		return ReturnInfoUtils.errorInfo(errorList, jsonList.size());
	}

	@Override
	public Map<String, Object> guoCreateNew(String merchant_no, String waybill, int serial, String dateSign,
			String OrderDate, Double FCY, Double Tax, Double ActualAmountPaid, String RecipientName, String RecipientID,
			String RecipientTel, String RecipientProvincesCode, String RecipientAddr, String OrderDocAcount,
			String OrderDocName, String OrderDocId, String OrderDocTel, String senderName, String senderCountry,
			String senderAreaCode, String senderAddress, String senderTel, String areaCode, String cityCode,
			String provinceCode, String postal, String provinceName, String cityName, String areaName, String orderId,
			JSONObject goodsInfo) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		// 加锁保证订单信息、金额准确
		synchronized (LOCK) {
			params.put("waybill", waybill);
			// params.put("merchant_no", merchant_no);
			List<Morder> ml = morderDao.findByProperty(Morder.class, params, 1, 1);
			if (ml == null) {
				return ReturnInfoUtils.errorInfo("运单号[" + waybill + "]查询订单信息失败,服务器繁忙!");
			} else if (!ml.isEmpty()) {
				Morder morder = ml.get(0);
				if (morder.getDel_flag() == 1) {
					return ReturnInfoUtils.errorInfo(morder.getOrder_id() + "<--订单已被刪除,无法再次导入,请联系管理员!");
				}

				return judgmentOrderInfo(morder, goodsInfo, FCY, Tax, 1);
			}
			Morder morder = new Morder();
			// 查询缓存中订单自增Id
			int count = SerialNoUtils.getSerialNo("order");
			String newOrderId = SerialNoUtils.getSerialNo("YM", count);
			morder.setOrder_id(newOrderId);
			// 原导入表中的订单编号
			morder.setOldOrderId(orderId);
			morder.setFCY(FCY);
			morder.setTax(Tax);
			morder.setActualAmountPaid(ActualAmountPaid);
			morder.setRecipientName(RecipientName);
			morder.setRecipientID(RecipientID);
			morder.setRecipientTel(RecipientTel);
			morder.setRecipientAddr(RecipientAddr);
			// 暂时默认为下单人姓名
			morder.setOrderDocAcount(OrderDocName);
			morder.setOrderDocName(OrderDocName);
			morder.setOrderDocType("01");// 身份证
			morder.setOrderDocId(OrderDocId);
			morder.setOrderDocTel(OrderDocTel);
			morder.setMerchant_no(merchant_no);
			morder.setDateSign(dateSign);
			morder.setSerial(serial);
			morder.setWaybill(waybill);
			morder.setDel_flag(0);
			// 订单备案状态
			morder.setOrder_record_status(1);
			morder.setCreate_date(new Date());
			morder.setCreate_by(goodsInfo.get("merchantName") + "");
			morder.setFcode(FCODE);
			morder.setSenderName(senderName);
			morder.setSenderCountry(senderCountry);
			morder.setSenderAreaCode(senderAreaCode);
			morder.setSenderAddress(senderAddress);
			morder.setSenderTel(senderTel);

			morder.setPostal(postal);
			morder.setRecipientProvincesCode(provinceCode);
			morder.setRecipientCityCode(cityCode);
			morder.setRecipientAreaCode(areaCode);
			morder.setRecipientProvincesName(provinceName);
			morder.setRecipientCityName(cityName);
			morder.setRecipientAreaName(areaName);
			String randomDate = DateUtil.randomCreateDate();
			morder.setOrderDate(randomDate);
			morder.setCustomsCode(goodsInfo.get("customsCode") + "");
			if (morderDao.add(morder)) {
				statusMap.put("status", 1);
				statusMap.put(ORDER_ID, newOrderId);
				return statusMap;
			}
			return ReturnInfoUtils.errorInfo(morder.getOrder_id() + "<--订单存储失败，请稍后重试!");
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
		paramsMap.put(ORDER_ID, morder.getOrder_id());
		List<MorderSub> ms = morderDao.findByProperty(MorderSub.class, paramsMap, 0, 0);
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
			if (!morderDao.update(morder)) {
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

	@Override
	public Map<String, Object> checkEntGoodsNo(String entGoodsNo, String goodsName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("entGoodsNo", entGoodsNo.trim());
		params.put("goodsName", goodsName.trim());
		List<GoodsRecordDetail> goodsList = morderDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
		if (goodsList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		} else if (!goodsList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.toString(), goodsList);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "商品不存在,请先录入已备案商品！");
		}
		return statusMap;
	}

	@Override
	public Map<String, Object> createQBOrder(String merchantId, Map<String, Object> item, String merchantName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		synchronized (LOCK) {
			double orderTotalPrice = Double.parseDouble(item.get("orderTotalPrice") + "");
			double tax = Double.parseDouble(item.get("tax") + "");
			double actualAmountPaid = Double.parseDouble(item.get("actualAmountPaid") + "");
			String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
			String orderId = item.get("orderId") + "";
			if (StringEmptyUtils.isEmpty(orderId)) {// 当表单中未填写订单Id时,则系统生成
				// 查询缓存中订单自增Id
				int count = SerialNoUtils.getSerialNo("order");
				orderId = SerialNoUtils.getSerialNo("YM", count);
			}
			// 校验企邦是否已经录入已备案商品信息
			String entGoodsNo = item.get("entGoodsNo") + "";
			String marCode = item.get("marCode") + "";
			if (StringEmptyUtils.isNotEmpty(marCode)) {
				params.put("entGoodsNo", entGoodsNo.trim() + "_" + marCode.trim());
			} else {
				params.put("entGoodsNo", entGoodsNo.trim());
			}
			params.put("goodsMerchantId", merchantId);
			List<GoodsRecordDetail> goodsList = morderDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
			if (goodsList == null || goodsList.isEmpty()) {
				statusMap.put("status", -1);
				statusMap.put("msg", "商品编号[" + entGoodsNo.trim() + "]与商家平台号[" + marCode.trim() + "]-->对应商品不存在,请核实!");
				return statusMap;
			}
			params.clear();
			params.put(ORDER_ID, orderId);
			List<Morder> ml = morderDao.findByProperty(Morder.class, params, 1, 1);
			if (ml != null && !ml.isEmpty()) {
				Morder morder = ml.get(0);
				// 企邦的税费暂写死为0
				return judgmentOrderInfo(morder, JSONObject.fromObject(item), orderTotalPrice, 0.0, 2);
			}

			Morder morder = new Morder();
			morder.setOrder_id(orderId);
			morder.setMerchant_no(merchantId);
			morder.setFCY(orderTotalPrice);
			morder.setTax(tax);
			morder.setActualAmountPaid(actualAmountPaid);
			morder.setRecipientName(item.get("recipientName") + "");
			morder.setRecipientID(item.get("orderDocId") + "");
			morder.setRecipientTel(item.get("recipientTel") + "");
			morder.setRecipientProvincesCode(item.get("provinceCode") + "");
			morder.setRecipientProvincesName(item.get("provinceName") + "");
			morder.setRecipientCityCode(item.get("cityCode") + "");
			morder.setRecipientCityName(item.get("cityName") + "");
			morder.setRecipientAreaCode(item.get("areaCode") + "");
			morder.setRecipientAreaName(item.get("areaName") + "");
			morder.setRecipientAddr(item.get("recipientAddr") + "");
			morder.setOrderDocAcount(item.get("orderDocAcount") + "");
			morder.setOrderDocName(item.get("orderDocName") + "");
			morder.setOrderDocType("01");// 身份证
			morder.setOrderDocId(item.get("orderDocId") + "");
			morder.setOrderDocTel(item.get("orderDocTel") + "");
			morder.setDateSign(dateSign);
			morder.setSerial(Integer.parseInt(item.get("serial") + ""));
			morder.setWaybill(item.get("waybillNo") + "");
			morder.setDel_flag(0);
			morder.setOrder_record_status(1);
			morder.setCreate_date(new Date());
			morder.setCreate_by(merchantName);
			morder.setFcode(FCODE);
			String randomDate = DateUtil.randomCreateDate();
			morder.setOrderDate(randomDate);
			String ehsEntName = item.get("ehsEntName") + "";
			if (StringEmptyUtils.isNotEmpty(ehsEntName)) {
				JSONObject json = new JSONObject();
				json.put("ehsEntName", ehsEntName);
				morder.setSpareParams(json.toString());
			}
			if (!morderDao.add(morder)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
				statusMap.put("msg", "保存订单错误，请核对订单信息!");
				return statusMap;
			}

			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(ORDER_ID, morder.getOrder_id());
			statusMap.put("date", randomDate);
			statusMap.put("msg", "存储完毕");
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> createQBOrderSub(String merchantId, Map<String, Object> item, String merchantName) {
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
		List<GoodsRecordDetail> goodsList = morderDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
		if (goodsList != null && !goodsList.isEmpty()) {
			GoodsRecordDetail goods = goodsList.get(0);
			JSONObject param = new JSONObject();
			param.put(ORDER_ID, item.get("orderId") + "");
			String reEntGoodsNo = goods.getEntGoodsNo();
			if (StringEmptyUtils.isNotEmpty(marCode)) {
				String[] str = reEntGoodsNo.split("_");
				reEntGoodsNo = str[0];
			}
			param.put("entGoodsNo", reEntGoodsNo);
			param.put("HSCode", goods.getHsCode());
			if (StringEmptyUtils.isEmpty(goods.getBrand())) {
				param.put("Brand", reEntGoodsNo);
			} else {
				param.put("Brand", goods.getBrand());
			}
			param.put("BarCode", goods.getBarCode());
			param.put("CusGoodsNo", goods.getCusGoodsNo());
			param.put("CIQGoodsNo", goods.getCiqGoodsNo());
			param.put("GoodsName", goods.getGoodsName());
			param.put("GoodsStyle", goods.getGoodsStyle());
			param.put("OriginCountry", goods.getOriginCountry());
			param.put("Unit", goods.getgUnit());
			param.put("Price", item.get("price") + "");
			param.put("Qty", item.get("count") + "");
			param.put("netWt", goods.getNetWt());
			param.put("grossWt", goods.getGrossWt());
			param.put("stdUnit", goods.getStdUnit());
			param.put("secUnit", goods.getSecUnit());

			param.put("ebEntNo", goods.getEbEntNo());
			param.put("ebEntName", goods.getEbEntName());
			param.put("DZKNNo", goods.getDZKNNo());

			param.put("seqNo", item.get("seqNo") + "");
			param.put("merchantId", merchantId);
			param.put("merchantName", merchantName);
			String spareParam = goods.getSpareParams();
			if (StringEmptyUtils.isNotEmpty(spareParam)) {
				JSONObject json = JSONObject.fromObject(spareParam);
				String sku = json.get("SKU") + "";
				param.put("SKU", sku);
				param.put("marCode", marCode.trim());
			}
			return createNewSub(param);
		} else {
			return ReturnInfoUtils.errorInfo(goodsName + "------>该商品不存在,请核实!");
		}
	}

	@Override
	public Map<String, Object> deleteOrderGoodsInfo(String id, String name, String idPack) {
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(idPack);
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("参数错误!");
		}
		Map<String, Object> params = new HashMap<>();
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> datas = (Map<String, Object>) jsonList.get(i);
			String orderId = datas.get("orderId") + "";
			String goodsId = datas.get("goodsId") + "";
			params.clear();
			params.put(ORDER_ID, orderId);
			List<Morder> orderlist = morderDao.findByProperty(Morder.class, params, 1, 1);
			params.put("EntGoodsNo", goodsId);
			List<MorderSub> orderSublist = morderDao.findByProperty(MorderSub.class, params, 1, 1);
			if (orderSublist != null && !orderSublist.isEmpty()) {
				Morder order = orderlist.get(0);
				// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
				if (order.getOrder_record_status() == 2 || order.getOrder_record_status() == 3
						|| order.getOrder_record_status() == 10) {
					return ReturnInfoUtils.errorInfo("订单当前状态不能删除商品信息！");
				}
				MorderSub goodsInfo = orderSublist.get(0);
				//删除标识
				goodsInfo.setDeleteFlag(1);
				if (!morderDao.update(goodsInfo)) {
					return ReturnInfoUtils.errorInfo(goodsInfo.getGoodsName() + "<--删除失败,服务器繁忙!");
				} else {
					// 单价
					Double price = goodsInfo.getPrice();
					// 数量
					int count = goodsInfo.getQty();
					// 总价
					Double total = price * count;
					return updateOrderAmount(orderlist, total);
				}
			} else {
				return ReturnInfoUtils.errorInfo("订单号["+orderId+"]未找到商品信息！");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 商戶刪除订单商品信息后,扣减订单商品总金额
	 * 
	 * @param orderlist
	 *            订单信息
	 * @param total
	 *            商品总金额
	 * @return Map
	 */
	private Map<String, Object> updateOrderAmount(List<Morder> orderlist, Double total) {
		if (orderlist != null && !orderlist.isEmpty() && total >= 0) {
			Morder order = orderlist.get(0);
			// 订单商品总金额
			Double fcy = order.getFCY();
			// 实际支付金额
			Double actualAmountPaid = order.getActualAmountPaid();
			order.setFCY(fcy - total);
			order.setActualAmountPaid(actualAmountPaid - total);
			if (!morderDao.update(order)) {
				return ReturnInfoUtils.errorInfo("更新订单金额失败,服务器繁忙!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("未找到订单信息,请核实参数是否正确!");
	}

	@Override
	public Map<String, Object> editMorderInfo(String merchantId, String merchantName, String[] strArr, int flag) {
		Map<String, Object> paramMap = new HashMap<>();
		if (strArr != null && strArr.length > 0 && flag > 0) {
			String orderId = strArr[0];
			paramMap.put(ORDER_ID, orderId);
			paramMap.put("merchant_no", merchantId);
			List<Morder> reOrderList = morderDao.findByProperty(Morder.class, paramMap, 1, 1);
			if (reOrderList != null && !reOrderList.isEmpty()) {
				Morder order = reOrderList.get(0);
				// if (order.getOrder_record_status() == 1 ||
				// order.getOrder_record_status() == 4) {
				// 1-修改订单信息,2-修改订单商品信息,3-订单及商品信息都修改
				switch (flag) {
				case 1:
					return editMorderInfo(order, strArr, merchantName);
				case 2:
					Map<String, Object> reOrderSubMap = editMorderSubInfo(orderId, strArr, merchantId, merchantName);
					if (!"1".equals(reOrderSubMap.get(BaseCode.STATUS.toString()))) {
						return reOrderSubMap;
					}
					return updateOrderAmount(order, merchantName);
				default:
					return ReturnInfoUtils.errorInfo("修改订单或商品标识错误,请重新输入!");
				}
				// } else {
				// return ReturnInfoUtils.errorInfo("订单当前状态不允许修改订单及商品信息!");
				// }
			} else {
				return ReturnInfoUtils.errorInfo("订单不存在,请核实订单信息!");
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数出错,请核对信息!");
	}

	/**
	 * 商户修改手工订单后,更新订单商品总金额
	 * 
	 * @param order
	 *            订单实体类
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	private Map<String, Object> updateOrderAmount(Morder order, String merchantName) {
		Map<String, Object> params = new HashMap<>();
		params.put(ORDER_ID, order.getOrder_id());
		params.put("deleteFlag", 0);
		List<MorderSub> reOrderSubList = morderDao.findByProperty(MorderSub.class, params, 0, 0);
		if (reOrderSubList != null && !reOrderSubList.isEmpty()) {
			double totalAmount = 0.0;
			for (MorderSub orderSub : reOrderSubList) {
				int count = orderSub.getQty();
				double price = orderSub.getPrice();
				totalAmount += (count * price);
			}
			order.setFCY(totalAmount);
			order.setUpdate_date(new Date());
			order.setUpdate_by(merchantName);
			order.setActualAmountPaid(totalAmount);
			if (morderDao.update(order)) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("更新订单金额错误!");
		}
		return ReturnInfoUtils.errorInfo("未找到对应的订单商品信息,请核实订单是否存在!");
	}

	/**
	 * 修改订单商品
	 * 
	 * @param order_id
	 *            订单Id
	 * @param strArr
	 *            数据
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 * @return Map
	 */
	private Map<String, Object> editMorderSubInfo(String order_id, String[] strArr, String merchantId,
			String merchantName) {
		Map<String, Object> paramMap = new HashMap<>();
		String entGoodsNo = strArr[24];
		if (StringEmptyUtils.isEmpty(entGoodsNo)) {
			return ReturnInfoUtils.errorInfo("商品自编号不能为空!");
		}
		paramMap.put(ORDER_ID, order_id);
		paramMap.put("EntGoodsNo", entGoodsNo);
		paramMap.put("merchant_no", merchantId);
		paramMap.put("seqNo", Integer.parseInt(strArr[25]));
		List<MorderSub> reOrderSubList = morderDao.findByProperty(MorderSub.class, paramMap, 1, 1);
		if (reOrderSubList != null && !reOrderSubList.isEmpty()) {
			MorderSub goodsInfo = reOrderSubList.get(0);
			goodsInfo.setSeq(Integer.parseInt(strArr[1]));
			goodsInfo.setEntGoodsNo(strArr[2]);
			goodsInfo.setHSCode(strArr[3]);
			goodsInfo.setGoodsName(strArr[4]);
			goodsInfo.setCusGoodsNo(strArr[5]);
			goodsInfo.setCIQGoodsNo(strArr[6]);
			goodsInfo.setOriginCountry(strArr[7]);
			goodsInfo.setGoodsStyle(strArr[8]);
			goodsInfo.setBarCode(strArr[9]);
			goodsInfo.setBrand(strArr[10]);
			int count = 0;
			double price = 0.0;
			try {
				count = Integer.parseInt(strArr[11]);
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("商品数量输入错误,请重新输入!");
			}
			try {
				price = Double.parseDouble(strArr[13]);
			} catch (Exception e) {
				return ReturnInfoUtils.errorInfo("商品单价输入错误,请重新输入!");
			}
			goodsInfo.setQty(count);
			goodsInfo.setUnit(strArr[12]);
			goodsInfo.setPrice(price);
			goodsInfo.setTotal(count * price);
			goodsInfo.setNetWt(Double.parseDouble(strArr[15]));
			goodsInfo.setGrossWt(Double.parseDouble(strArr[16]));
			if (StringEmptyUtils.isEmpty(strArr[17])) {
				goodsInfo.setFirstLegalCount(0.0);
			} else {
				goodsInfo.setFirstLegalCount(Double.parseDouble(strArr[17]));
			}
			if (StringEmptyUtils.isEmpty(strArr[18])) {
				goodsInfo.setSecondLegalCount(0.0);
			} else {
				goodsInfo.setSecondLegalCount(Double.parseDouble(strArr[18]));
			}

			goodsInfo.setStdUnit(strArr[19]);
			goodsInfo.setSecUnit(strArr[20]);
			if (StringEmptyUtils.isEmpty(strArr[21])) {
				goodsInfo.setNumOfPackages(0);
			} else {
				goodsInfo.setNumOfPackages(Integer.parseInt(strArr[21]));
			}
			if (StringEmptyUtils.isEmpty(strArr[22])) {
				goodsInfo.setPackageType(0);
			} else {
				goodsInfo.setPackageType(Integer.parseInt(strArr[22]));
			}

			goodsInfo.setTransportModel(strArr[23]);
			goodsInfo.setUpdateBy(merchantName);
			goodsInfo.setUpdateDate(new Date());

			if (!morderDao.update(goodsInfo)) {
				return ReturnInfoUtils.errorInfo("更新订单备案商品错误,请重试!");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("查询订单商品信息错误,请重试!");
	}

	/**
	 * 修改订单信息
	 * 
	 * @param order
	 *            订单实体
	 * @param strArr
	 *            修改信息
	 * @param merchantName
	 *            商户名称
	 * @return Map
	 */
	private Map<String, Object> editMorderInfo(Morder order, String[] strArr, String merchantName) {
		order.setFcode(strArr[1]);
		Double totalprice = 0.0;
		Double tax = 0.0;
		try {
			totalprice = Double.parseDouble(strArr[2]);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单商品总金额错误,请重新输入!");
		}
		try {
			tax = Double.parseDouble(strArr[3]);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单税费错误,请重新输入!");
		}
		order.setFCY(totalprice);
		order.setTax(tax);
		order.setActualAmountPaid(totalprice + tax);
		order.setRecipientName(strArr[5]);
		order.setRecipientID(strArr[6]);
		if (!IdcardValidator.validate18Idcard(strArr[6])) {
			return ReturnInfoUtils.errorInfo("收货人身份证号错误,请核对信息!");
		}
		order.setRecipientTel(strArr[7]);
		order.setRecipientProvincesName(strArr[8]);
		String provincesCode = strArr[9];
		if (provincesCode.length() != 6) {
			return ReturnInfoUtils.errorInfo("收货人省份编码错误,请核对信息!");
		}
		order.setRecipientProvincesCode(provincesCode);
		order.setRecipientCityName(strArr[10]);
		String cityCode = strArr[11];
		if (cityCode.length() != 6) {
			return ReturnInfoUtils.errorInfo("收货人城市编码错误,请核对信息!");
		}
		order.setRecipientCityCode(cityCode);
		order.setRecipientAreaName(strArr[12]);
		String areaCode = strArr[13];
		if (areaCode.length() != 6) {
			return ReturnInfoUtils.errorInfo("收货人区域编码,请核对信息!");
		}
		order.setRecipientAreaCode(areaCode);
		order.setRecipientAddr(strArr[14]);
		if (!IdcardValidator.validate18Idcard(strArr[15])) {
			return ReturnInfoUtils.errorInfo("下单人身份证号,请核对信息!");
		}
		order.setOrderDocId(strArr[15]);
		order.setOrderDocName(strArr[16]);
		order.setOrderDocType(strArr[17]);
		order.setOrderDocAcount(strArr[18]);
		order.setOrderDocTel(strArr[19]);
		order.setWaybill(strArr[22]);
		order.setSenderName(strArr[23]);
		// order.setStatus(orderMap.get(""));
		order.setSenderCountry(strArr[24]);
		order.setSenderAreaCode(strArr[25]);
		order.setSenderAddress(strArr[26]);
		order.setSenderTel(strArr[27]);
		order.setPostal(strArr[28]);
		order.setOldOrderId(strArr[29]);
		order.setUpdate_date(new Date());
		order.setUpdate_by(merchantName);
		try {
			order.setSerial(Integer.parseInt(strArr[31]));
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("批次号错误,请重新输入!");
		}
		// 备用字段
		/*
		 * String spareParams = strArr[32]; if
		 * (StringEmptyUtils.isNotEmpty(spareParams)) { JSONObject json =
		 * JSONObject.fromObject(spareParams);
		 * order.setSpareParams(json.toString()); String ehsEntName =
		 * json.get("ehsEntName") + "";
		 * 
		 * }
		 */

		if (!morderDao.update(order)) {
			return ReturnInfoUtils.errorInfo("更新订单备案信息错误!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> addOrderGoodsInfo(String merchantId, String merchantName, String[] strArr) {
		Map<String, Object> params = new HashMap<>();
		if (strArr != null) {
			String orderId = strArr[1];
			params.put(ORDER_ID, orderId);
			List<Morder> orderlist = morderDao.findByProperty(Morder.class, params, 1, 1);
			if (orderlist != null && !orderlist.isEmpty()) {
				Morder order = orderlist.get(0);
				MorderSub goods = new MorderSub();
				goods.setOrder_id(orderId);
				goods.setSeq(Integer.parseInt(strArr[2]));
				goods.setEntGoodsNo(strArr[3]);
				goods.setHSCode(strArr[4]);
				goods.setGoodsName(strArr[5]);
				goods.setCusGoodsNo(strArr[6]);
				goods.setCIQGoodsNo(strArr[7]);
				goods.setGoodsStyle(strArr[8]);
				goods.setBrand(strArr[9]);
				int count = Integer.parseInt(strArr[10]);
				goods.setQty(count);
				goods.setUnit(strArr[11]);
				double price = Double.parseDouble(strArr[12]);
				goods.setPrice(price);
				goods.setTotal(count * price);
				goods.setNetWt(Double.parseDouble(strArr[13]));
				goods.setGrossWt(Double.parseDouble(strArr[14]));
				goods.setStdUnit(strArr[15]);
				goods.setSecUnit(strArr[16]);
				goods.setSeqNo(Integer.parseInt(strArr[17]));
				// 国宗字段
				if (StringEmptyUtils.isNotEmpty(strArr[18])) {
					goods.setFirstLegalCount(Double.parseDouble(strArr[15]));
				}
				if (StringEmptyUtils.isNotEmpty(strArr[19])) {
					goods.setSecondLegalCount(Double.parseDouble(strArr[16]));
				}
				if (StringEmptyUtils.isNotEmpty(strArr[20])) {
					goods.setNumOfPackages(Integer.parseInt(strArr[20]));
				}
				if (StringEmptyUtils.isNotEmpty(strArr[21])) {
					goods.setPackageType(Integer.parseInt(strArr[21]));
				}
				if (StringEmptyUtils.isNotEmpty(strArr[22])) {
					goods.setTransportModel(strArr[22]);
				}
				// 企邦字段
				String spareParams = order.getSpareParams();
				JSONObject json = JSONObject.fromObject(spareParams);
				// 当企邦订单中承运商字段存在时则必须填写企邦对应的商品属性
				if (StringEmptyUtils.isNotEmpty(json.get("ehsEntName") + "")) {
					String marCode = strArr[23];
					String sku = strArr[24];
					String ebEntNo = strArr[25];
					String ebEntName = strArr[26];
					String dzknNo = strArr[27];
					if (StringEmptyUtils.isNotEmpty(marCode) && StringEmptyUtils.isNotEmpty(sku)
							&& StringEmptyUtils.isNotEmpty(ebEntNo) && StringEmptyUtils.isNotEmpty(ebEntName)
							&& StringEmptyUtils.isNotEmpty(dzknNo)) {
						json.clear();
						json.put("marCode", marCode);
						json.put("SKU", sku);
						json.put("ebEntNo", ebEntNo);
						json.put("ebEntName", ebEntName);
						json.put("DZKNNo", dzknNo);
						goods.setSpareParams(json.toString());
					} else {
						return ReturnInfoUtils.errorInfo("企邦专属字段不能为空!");
					}
					if (morderDao.add(goods)) {
						return updateOrderAmount(order, merchantName);
					}
					return ReturnInfoUtils.errorInfo("添加商品信息失败,服务器繁忙!");
				}
			}
			return ReturnInfoUtils.errorInfo("未找到订单关联的商品信息,请核对信息!");
		}
		return ReturnInfoUtils.errorInfo("商品信息参数错误,请核对信息!");
	}

	@Override
	public Map<String, Object> updateOldCreateBy() {
		Map<String, Object> params = new HashMap<>();
		List<Merchant> reList = morderDao.findByProperty(Merchant.class, null, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (Merchant merchantInfo : reList) {
				params.clear();
				String merchantId = merchantInfo.getMerchantId();
				params.put("merchant_no", merchantId);
				List<Morder> reOrderList = morderDao.findByProperty(Morder.class, params, 0, 0);

				if (reOrderList != null && !reOrderList.isEmpty()) {
					for (Morder order : reOrderList) {
						String orderId = order.getOrder_id();
						order.setCreate_by(merchantInfo.getMerchantName());
						order.setUpdate_date(new Date());
						order.setUpdate_by("system");
						if (!morderDao.update(order)) {
							return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]更新订单错误!");
						}
						params.clear();
						params.put(ORDER_ID, orderId);
						List<MorderSub> reOrderSubList = morderDao.findByProperty(MorderSub.class, params, 0, 0);
						if (reOrderSubList != null && !reOrderSubList.isEmpty()) {
							for (MorderSub orderSub : reOrderSubList) {
								if (StringEmptyUtils.isEmpty(Integer.toString(orderSub.getSeqNo()))) {
									orderSub.setSeqNo(0);
								}
								orderSub.setMerchant_no(merchantId);
								orderSub.setCreateBy(merchantInfo.getMerchantName());
								orderSub.setUpdateDate(new Date());
								orderSub.setUpdateBy("system");
								if (!morderDao.update(orderSub)) {
									return ReturnInfoUtils.errorInfo("订单号[" + order.getOrder_id() + "]更新订单商品错误!");
								}
							}
						}
					}
				}
			}

		}
		return null;
	}

	@Override
	public Map<String, Object> updateOldPaymentCreateBy() {
		Map<String, Object> params = new HashMap<>();
		List<Merchant> reList = morderDao.findByProperty(Merchant.class, null, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (Merchant merchantInfo : reList) {
				params.clear();
				String merchantId = merchantInfo.getMerchantId();
				params.put("merchant_no", merchantId);
				List<Mpay> rePayList = morderDao.findByProperty(Mpay.class, params, 0, 0);
				if (rePayList != null && !rePayList.isEmpty()) {
					for (Mpay pay : rePayList) {
						pay.setCreate_by(merchantInfo.getMerchantName());
						if (!morderDao.update(pay)) {
							return null;
						}
					}
				}
			}

		}
		return null;
	}

	@Override
	public Map<String, Object> updateManualOrderGoodsInfo(String startTime, String endTime, String merchantId,
			String merchantName, Map<String, Object> customsMap) {
		if (StringEmptyUtils.isNotEmpty(startTime) && StringEmptyUtils.isNotEmpty(endTime)
				&& StringEmptyUtils.isNotEmpty(merchantId) && StringEmptyUtils.isNotEmpty(merchantName)) {
			//
			Map<String, Object> reDatasMap = checkInfo(merchantId, merchantName, customsMap);
			if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
				return reDatasMap;
			}
			Map<String, Object> item = (Map<String, Object>) reDatasMap.get(BaseCode.DATAS.toString());
			String customsCode = item.get("customsCode") + "";
			String customsName = item.get("customsName") + "";
			// 创建商品仓库
			Map<String, Object> warehousMap = goodsRecordService.createWarehous(merchantId, merchantName, customsCode,
					customsName);
			if (!warehousMap.get(BaseCode.STATUS.toString()).equals("1")) {
				return warehousMap;
			}
			Map<String, Object> params = new HashMap<>();
			// 查询缓存中商品自编号自增Id
			int count = SerialNoUtils.getSerialNo("goodsRecordHead");
			String goodsRecordHeadSerialNo = SerialNoUtils.getSerialNo("GRH", count);
			item.put("goodsRecordHeadSerialNo", goodsRecordHeadSerialNo);
			if (!saveGoodsRecordHead(item)) {
				return ReturnInfoUtils.errorInfo("保存商品备案信息头错误,服务器繁忙!");
			}
			// 查询当前已备案成功的订单商品信息信息中(商品自编号)还未添加至商品备案信息表中的总数
			Long totalCountT = morderDao.getMOrderAndMGoodsInfoCount(merchantId, startTime, endTime, 0, 0);
			// 获取总数
			int totalCount = totalCountT.intValue();
			if (totalCount <= 0) {
				return ReturnInfoUtils.errorInfo("没有需要更新的订单信息!");
			}
			// 创建线程池
			ExecutorService threadPool = Executors.newCachedThreadPool();
			// 获取流水号
			String serialNo = "updateMOrderGoods_" + SerialNoUtils.getSerialNo("updateMOrderGoods");
			startTask(totalCount, merchantId, merchantName, startTime, endTime, serialNo, goodsRecordHeadSerialNo,
					threadPool);
			threadPool.shutdown();
			params.clear();
			params.put("serialNo", serialNo);
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			params.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return params;
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	/**
	 * 根据对应的CPU数量开辟子任务查询订单商品信息
	 * 
	 * @param totalCount
	 *            总数
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param startTime
	 *            查询开始时间
	 * @param endTime
	 *            查询结束时间
	 * @param serialNo
	 *            批次号
	 * @param goodsRecordHeadSerialNo
	 *            已备案商品信息(头部)流水号
	 * @param threadPool
	 *            线程池
	 * @return Map
	 */
	private void startTask(int totalCount, String merchantId, String merchantName, String startTime, String endTime,
			String serialNo, String goodsRecordHeadSerialNo, ExecutorService threadPool) {
		// 由于服务器CPU数量过多,故而写死为6线程
		int cpuCount = 6;
		int page = 1;
		int size = totalCount / cpuCount;
		long start = System.currentTimeMillis();
		Vector errorList = new Vector<>();
		for (int i = 0; i < cpuCount; i++) {
			Table table = morderDao.getMOrderAndMGoodsInfo(merchantId, startTime, endTime, page, size);
			if (table != null && !table.getRows().isEmpty()) {
				// 获取表单中的List<Row>数据
				List<Row> reOrderList = table.getRows();
				List<Row> lr2 = null;
				// 当最后一次的时候page+1查询CPU数未除尽的数量,然后进行统一处理
				if (page == cpuCount) {
					page += 1;
					Table table2 = morderDao.getMOrderAndMGoodsInfo(merchantId, startTime, endTime, page, size);
					lr2 = table2.getRows();
					reOrderList.addAll(lr2);
				}
				UpdateMOrderGoodsTask updateMOrderGoodsTask = new UpdateMOrderGoodsTask(reOrderList, merchantId,
						merchantName, errorList, totalCount, serialNo, this, goodsRecordHeadSerialNo);
				threadPool.submit(updateMOrderGoodsTask);
			}
			page++;
		}
		long end = System.currentTimeMillis();
		System.out.println("总计耗时：" + (end - start) + "ms");
	}

	/**
	 * 遍历查询出来商品信息,保存至已备案商品信息表中
	 * 
	 * @param dataList
	 *            订单商品信息
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param errorList
	 *            错误信息
	 * @param totalCount
	 *            总数
	 * @param serialNo
	 *            批次号
	 * @param goodsRecordHeadSerialNo
	 *            已备案商品信息(头部)流水号
	 */
	public void saveGoodsRecordContent(List<Row> dataList, String merchantId, String merchantName, List errorList,
			int totalCount, String serialNo, String goodsRecordHeadSerialNo) {
		Map<String, Object> params = new HashMap<>();
		for (int i = 0; i < dataList.size(); i++) {
			String entGoodsNo = dataList.get(i).getValue("EntGoodsNo") + "";
			String orderId = dataList.get(i).getValue(ORDER_ID) + "";
			params.clear();
			params.put("entGoodsNo", entGoodsNo);
			params.put("goodsMerchantId", merchantId);
			List<GoodsRecordDetail> reGoodsContentList = morderDao.findByProperty(GoodsRecordDetail.class, params, 0,
					0);
			if (reGoodsContentList == null) {
				String msg = "订单号[" + orderId + "]查询信息失败,服务器繁忙!";
				// RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount,
				// serialNo, "updateMOrderGoods", 6);
				continue;
			} else if (!reGoodsContentList.isEmpty()) {
				// 由于查询出来的总数已检索了未添加至备案商品信息表中,故无需添加记录,否则会造成计数错误
				// String msg = "商品自编号[" + entGoodsNo + "]已存在,无需重复添加至已备案信息!";
				// RedisInfoUtils.commonErrorInfo(msg, errorList, totalCount,
				// serialNo, "updateMOrderGoods", 6);
				// continue;
			} else {
				GoodsRecordDetail goodsRecordDetail = new GoodsRecordDetail();
				int seq = Integer.parseInt(dataList.get(i).getValue("Seq") + "");
				goodsRecordDetail.setSeq(seq);
				goodsRecordDetail.setEntGoodsNo(entGoodsNo);
				String ciqGoodsNo = dataList.get(i).getValue("CIQGoodsNo") + "";
				goodsRecordDetail.setCiqGoodsNo(ciqGoodsNo);
				String cusGoodsNo = dataList.get(i).getValue("CusGoodsNo") + "";
				goodsRecordDetail.setCusGoodsNo(cusGoodsNo);
				String goodsName = dataList.get(i).getValue("GoodsName") + "";
				goodsRecordDetail.setShelfGName(goodsName);
				goodsRecordDetail.setNcadCode("27000000");
				String hsCode = dataList.get(i).getValue("HSCode") + "";
				goodsRecordDetail.setHsCode(hsCode);
				String barCode = dataList.get(i).getValue("BarCode") + "";
				goodsRecordDetail.setBarCode(barCode);
				goodsRecordDetail.setGoodsName(goodsName);
				String goodsStyle = dataList.get(i).getValue("GoodsStyle") + "";
				goodsRecordDetail.setGoodsStyle(goodsStyle);
				String brand = dataList.get(i).getValue("Brand") + "";
				goodsRecordDetail.setBrand(brand);
				String unit = dataList.get(i).getValue("Unit") + "";
				goodsRecordDetail.setgUnit(unit);
				String stdUnit = dataList.get(i).getValue("stdUnit") + "";
				goodsRecordDetail.setStdUnit(stdUnit);
				Double price = Double.parseDouble(dataList.get(i).getValue("Price") + "");
				goodsRecordDetail.setRegPrice(price);
				goodsRecordDetail.setGiftFlag("1");
				String originCountry = dataList.get(i).getValue("OriginCountry") + "";
				goodsRecordDetail.setOriginCountry(originCountry);
				goodsRecordDetail.setQuality("合格");
				// 供应商
				goodsRecordDetail.setManufactory(merchantName);
				Double netWt = Double.parseDouble(dataList.get(i).getValue("netWt") + "");
				goodsRecordDetail.setNetWt(netWt);
				Double grossWt = Double.parseDouble(dataList.get(i).getValue("grossWt") + "");
				goodsRecordDetail.setGrossWt(grossWt);
				// 备案状态：1-备案中，2-备案成功，3-备案失败,4-未备案
				goodsRecordDetail.setStatus(2);
				// 已备案商品状态:0-已备案,待审核,1-备案审核通过,2-正常备案,3-审核不通过
				goodsRecordDetail.setRecordFlag(0);
				goodsRecordDetail.setCreateBy(merchantName);
				goodsRecordDetail.setCreateDate(new Date());
				goodsRecordDetail.setDeleteFlag(0);
				goodsRecordDetail.setGoodsMerchantId(merchantId);
				goodsRecordDetail.setGoodsMerchantName(merchantName);
				goodsRecordDetail.setGoodsSerialNo(goodsRecordHeadSerialNo);
				String spareParams = dataList.get(i).getValue("spareParams1") + "";
				if (StringEmptyUtils.isNotEmpty(spareParams)) {
					goodsRecordDetail.setSpareParams(spareParams);
				}
				if (!morderDao.add(goodsRecordDetail)) {
					String msg = "订单号[" + orderId + "]更新商品信息失败,未知错误!";
					// RedisInfoUtils.commonErrorInfo(msg, errorList,
					// totalCount, serialNo, "updateMOrderGoods", 6);
					continue;
				}
			}
			// bufferUtils.writeRedis(errorList, totalCount, serialNo,
			// "updateMOrderGoods");
		}
		// bufferUtils.writeCompletedRedis(errorList, totalCount, serialNo,
		// "updateMOrderGoods", merchantId, merchantName);
	}

	/**
	 * 检查前台传递海关信息及查询商户备案信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param customsMap
	 *            海关口岸信息
	 * @return Map
	 */
	private Map<String, Object> checkInfo(String merchantId, String merchantName, Map<String, Object> customsMap) {
		int eport = Integer.parseInt(customsMap.get("eport") + "");
		String ciqOrgCode = customsMap.get("ciqOrgCode") + "";
		String customsCode = customsMap.get("customsCode") + "";
		// 校验前台传递口岸、海关、智检编码
		Map<String, Object> reCustomsMap = goodsRecordService.checkCustomsPort(eport, customsCode, ciqOrgCode);
		if (!"1".equals(reCustomsMap.get(BaseCode.STATUS.toString()))) {
			return reCustomsMap;
		}
		CustomsPort portInfo = (CustomsPort) reCustomsMap.get(BaseCode.DATAS.toString());
		// 获取商户在对应口岸的备案信息
		Map<String, Object> merchantRecordMap = merchantUtils.getMerchantRecordInfo(merchantId, eport);
		if (!"1".equals(merchantRecordMap.get(BaseCode.STATUS.toString()))) {
			return merchantRecordMap;
		}
		MerchantRecordInfo merchantRecordInfo = (MerchantRecordInfo) merchantRecordMap.get(BaseCode.DATAS.toString());
		Map<String, Object> item = new HashMap<>();
		item.put("customsPort", portInfo.getCustomsPort());
		item.put("customsPortName", portInfo.getCustomsPortName());
		item.put("customsCode", portInfo.getCustomsCode());
		item.put("customsName", portInfo.getCustomsName());
		item.put("ciqOrgCode", portInfo.getCiqOrgCode());
		item.put("ciqOrgName", portInfo.getCiqOrgName());
		item.put("ebEntNo", merchantRecordInfo.getEbEntNo());
		item.put("ebEntName", merchantRecordInfo.getEbEntName());
		item.put("ebpEntNo", merchantRecordInfo.getEbpEntNo());
		item.put("ebpEntName", merchantRecordInfo.getEbpEntName());
		item.put("merchantId", merchantId);
		item.put("merchantName", merchantName);
		return ReturnInfoUtils.successDataInfo(item, 0);
	}

	/**
	 * 根据查询出来的海关口岸信息及商户备案信息,创建商品备案信息头
	 * 
	 * @param customsMap
	 *            海关口岸信息与商户备案信息
	 * @return boolean
	 */
	private boolean saveGoodsRecordHead(Map<String, Object> customsMap) {
		GoodsRecord goodsRecord = new GoodsRecord();
		goodsRecord.setGoodsSerialNo(customsMap.get("goodsRecordHeadSerialNo") + "");
		goodsRecord.setCustomsPort(Integer.parseInt(customsMap.get("customsPort") + ""));
		goodsRecord.setCustomsPortName(customsMap.get("customsPortName") + "");
		goodsRecord.setCustomsCode(customsMap.get("customsCode") + "");
		goodsRecord.setCustomsName(customsMap.get("customsName") + "");
		goodsRecord.setCiqOrgCode(customsMap.get("ciqOrgCode") + "");
		goodsRecord.setCiqOrgName(customsMap.get("ciqOrgName") + "");
		goodsRecord.setEbEntNo(customsMap.get("ebEntNo") + "");
		goodsRecord.setEbEntName(customsMap.get("ebEntName") + "");
		goodsRecord.setEbpEntNo(customsMap.get("ebpEntNo") + "");
		goodsRecord.setEbpEntName(customsMap.get("ebpEntName") + "");
		// 接收状态
		goodsRecord.setStatus(1);
		goodsRecord.setMerchantId(customsMap.get("merchantId") + "");
		goodsRecord.setMerchantName(customsMap.get("merchantName") + "");
		goodsRecord.setCreateBy(customsMap.get("merchantName") + "");
		goodsRecord.setCreateDate(new Date());
		goodsRecord.setDeleteFlag(0);
		return morderDao.add(goodsRecord);
	}

	@Override
	public Map<String, Object> checkGZOrder(String waybill, int serial, String dateSign, String OrderDate, Double FCY,
			Double Tax, Double ActualAmountPaid, String RecipientName, String RecipientID, String RecipientTel,
			String RecipientProvincesCode, String RecipientAddr, String OrderDocAcount, String OrderDocName,
			String OrderDocId, String OrderDocTel, String senderName, String senderCountry, String senderAreaCode,
			String senderAddress, String senderTel, String areaCode, String cityCode, String provinceCode,
			String postal, String provinceName, String cityName, String areaName, String orderId,
			JSONObject goodsInfo) {
		//
		String key = "Shop_Key_CheckGZOrder_List_" + serial;
		ConcurrentMap<String, Object> item = new ConcurrentHashMap<>();
		synchronized (LOCK) {
			byte[] redisByte = JedisUtil.get(key.getBytes());
			if (redisByte != null && redisByte.length > 0) {
				item = (ConcurrentMap<String, Object>) SerializeUtil.toObject(redisByte);
				if (item.containsKey(waybill)) {
					return updateRedisInfo(item, FCY, Tax, waybill, key);
				}
			}

			Morder morder = new Morder();
			morder.setFCY(FCY);
			morder.setTax(Tax);
			morder.setActualAmountPaid(ActualAmountPaid);
			morder.setRecipientName(RecipientName);
			morder.setRecipientID(RecipientID);
			morder.setRecipientTel(RecipientTel);
			morder.setRecipientAddr(RecipientAddr);
			// 暂时默认为下单人姓名
			morder.setOrderDocAcount(OrderDocName);
			morder.setOrderDocName(OrderDocName);
			morder.setOrderDocType("01");// 身份证
			morder.setOrderDocId(OrderDocId);
			morder.setOrderDocTel(OrderDocTel);
			morder.setDateSign(dateSign);
			morder.setSerial(serial);
			morder.setWaybill(waybill);
			morder.setFcode(FCODE);
			morder.setSenderName(senderName);
			morder.setSenderCountry(senderCountry);
			morder.setSenderAreaCode(senderAreaCode);
			morder.setSenderAddress(senderAddress);
			morder.setSenderTel(senderTel);
			morder.setPostal(postal);
			morder.setRecipientProvincesCode(provinceCode);
			morder.setRecipientCityCode(cityCode);
			morder.setRecipientAreaCode(areaCode);
			morder.setRecipientProvincesName(provinceName);
			morder.setRecipientCityName(cityName);
			morder.setRecipientAreaName(areaName);
			List<Morder> mOrderList = new ArrayList();
			mOrderList.add(morder);
			item.put(waybill, mOrderList);
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(item), 1800);
			return ReturnInfoUtils.successInfo();
		}
	}

	/**
	 * 根据缓存中找出的订单信息,更新国宗订单商品总金额与订单实际支付金额
	 * 
	 * @param item
	 *            缓存中订单信息
	 * @param FCY
	 *            订单商品金额
	 * @param Tax
	 *            订单税费
	 * @param waybill
	 *            运单号
	 * @param key
	 *            缓存Key
	 * @return Map
	 */
	private Map<String, Object> updateRedisInfo(ConcurrentMap<String, Object> item, Double FCY, Double Tax,
			String waybill, String key) {
		Map<String, Object> statusMap = new HashMap<>();
		List<Morder> newList = new ArrayList();
		List<Morder> mOrderList = (List<Morder>) item.get(waybill);
		Morder order = mOrderList.get(0);
		double newFcy = order.getFCY() + FCY;
		order.setFCY(newFcy);
		double newActualAmountPaid = newFcy + Tax;
		order.setActualAmountPaid(newActualAmountPaid);
		System.out.println("运单号-->" + waybill + ";商品金额>>" + FCY + ";订单商品总金额--->" + newFcy);
		// 覆盖原来的值
		newList.add(order);
		item.put(waybill, newList);
		JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(item), 1800);
		if (newFcy > 2000) {// 当订单金额超过2000时
			statusMap.put(BaseCode.STATUS.toString(), "10");
			statusMap.put(BaseCode.MSG.toString(), "运单号[" + waybill + "]<--关联商品总计金额超过2000,请核对金额!");
			return statusMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> checkQBOrder(String merchantId, Map<String, Object> item, String merchantName) {
		//
		String key = "Shop_Key_CheckQBOrder_List_" + item.get("serial");
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		ConcurrentMap<String, Object> concurrentMap = new ConcurrentHashMap<>();
		double orderTotalPrice = Double.parseDouble(item.get("orderTotalPrice") + "");
		double tax = Double.parseDouble(item.get("tax") + "");
		double actualAmountPaid = Double.parseDouble(item.get("actualAmountPaid") + "");
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String orderId = item.get("orderId") + "";
		if (StringEmptyUtils.isEmpty(orderId)) {// 当表单中未填写订单Id时,则系统生成
			// 查询缓存中订单自增Id
			int count = SerialNoUtils.getSerialNo("order");
			orderId = SerialNoUtils.getSerialNo("YM", count);
		}
		// 校验企邦是否已经录入已备案商品信息
		String entGoodsNo = item.get("entGoodsNo") + "";
		String marCode = item.get("marCode") + "";
		if (StringEmptyUtils.isNotEmpty(marCode)) {
			params.put("entGoodsNo", entGoodsNo.trim() + "_" + marCode.trim());
		} else {
			params.put("entGoodsNo", entGoodsNo.trim());
		}
		params.put("goodsMerchantId", merchantId);
		List<GoodsRecordDetail> goodsList = morderDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
		if (goodsList == null || goodsList.isEmpty()) {
			statusMap.put("status", -1);
			statusMap.put("msg", "商品编号[" + entGoodsNo.trim() + "]与[" + marCode.trim() + "]------>对应商品不存在,请核实!");
			return statusMap;
		}
		synchronized (LOCK) {
			byte[] redisByte = JedisUtil.get(key.getBytes());
			if (redisByte != null && redisByte.length > 0) {
				concurrentMap = (ConcurrentMap<String, Object>) SerializeUtil.toObject(redisByte);
				if (item.containsKey(orderId)) {
					return updateQBRedisInfo(item, orderTotalPrice, tax, orderId, key);
				}
			}
			Morder morder = new Morder();
			morder.setOrder_id(orderId);
			morder.setMerchant_no(merchantId);
			morder.setFCY(orderTotalPrice);
			morder.setTax(tax);
			morder.setActualAmountPaid(actualAmountPaid);
			morder.setRecipientName(item.get("recipientName") + "");
			morder.setRecipientID(item.get("orderDocId") + "");
			morder.setRecipientTel(item.get("recipientTel") + "");
			morder.setRecipientProvincesCode(item.get("provinceCode") + "");
			morder.setRecipientProvincesName(item.get("provinceName") + "");
			morder.setRecipientCityCode(item.get("cityCode") + "");
			morder.setRecipientCityName(item.get("cityName") + "");
			morder.setRecipientAreaCode(item.get("areaCode") + "");
			morder.setRecipientAreaName(item.get("areaName") + "");
			morder.setRecipientAddr(item.get("recipientAddr") + "");
			morder.setOrderDocAcount(item.get("orderDocAcount") + "");
			morder.setOrderDocName(item.get("orderDocName") + "");
			morder.setOrderDocType("01");// 身份证
			morder.setOrderDocId(item.get("orderDocId") + "");
			morder.setOrderDocTel(item.get("orderDocTel") + "");
			morder.setDateSign(dateSign);
			morder.setSerial(Integer.parseInt(item.get("serial") + ""));
			morder.setWaybill(item.get("waybillNo") + "");
			morder.setDel_flag(0);
			morder.setOrder_record_status(1);
			morder.setCreate_date(new Date());
			morder.setCreate_by(merchantName);
			morder.setFcode(FCODE);
			String randomDate = DateUtil.randomCreateDate();
			morder.setOrderDate(randomDate);
			String ehsEntName = item.get("ehsEntName") + "";
			if (StringEmptyUtils.isNotEmpty(ehsEntName)) {
				JSONObject json = new JSONObject();
				json.put("ehsEntName", ehsEntName);
				morder.setSpareParams(json.toString());
			}
			List<Morder> mOrderList = new ArrayList();
			mOrderList.add(morder);
			concurrentMap.put(orderId, mOrderList);
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(concurrentMap), 1800);
			return ReturnInfoUtils.successInfo();
		}
	}

	/**
	 * 根据缓存中找出的订单信息,更新启邦订单商品总金额与订单实际支付金额
	 * 
	 * @param item
	 *            缓存中订单信息
	 * @param orderTotalPrice
	 *            订单商品总金额
	 * 
	 * @param tax
	 *            税费
	 * @param orderId
	 *            订单Id
	 * @param key
	 *            缓存Key
	 * @return Map
	 */
	private Map<String, Object> updateQBRedisInfo(Map<String, Object> item, double orderTotalPrice, double tax,
			String orderId, String key) {
		Map<String, Object> statusMap = new HashMap<>();
		List<Morder> newList = new ArrayList();
		List<Morder> mOrderList = (List<Morder>) item.get(orderId);
		Morder order = mOrderList.get(0);
		double newFcy = order.getFCY() + orderTotalPrice;
		order.setFCY(newFcy);
		double newActualAmountPaid = newFcy + tax;
		order.setActualAmountPaid(newActualAmountPaid);
		System.out.println("订单号-->" + orderId + ";--商品金额>>" + orderTotalPrice + ";--订单商品总金额--->" + newFcy);
		// 覆盖原来的值
		newList.add(order);
		item.put(orderId, newList);
		JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(item), 1800);
		if (newFcy > 2000) {// 当订单金额超过2000时
			statusMap.put(BaseCode.STATUS.toString(), "10");
			statusMap.put(BaseCode.MSG.toString(), "订单号[" + orderId + "]<--关联商品总计金额超过2000,请核对金额!");
			return statusMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> managerDeleteMorderDatas(JSONArray json, String note, String managerId,
			String managerName) {
		if (json != null && StringEmptyUtils.isNotEmpty(note)) {
			for (int i = 0; i < json.size(); i++) {
				String orderId = String.valueOf(json.get(i));
				Map<String, Object> params = new HashMap<>();
				params.put(ORDER_ID, orderId);
				List<Morder> reMOrderList = morderDao.findByProperty(Morder.class, params, 1, 1);
				List<MorderSub> reMOrderSubList = morderDao.findByProperty(MorderSub.class, params, 0, 0);
				if (reMOrderList != null && !reMOrderList.isEmpty() && reMOrderSubList != null) {
					OldManualOrder oldManualOrder = new OldManualOrder();
					Morder mOrder = reMOrderList.get(0);
					try {
						CopyUtils.copy(mOrder, oldManualOrder);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// 注明删除原因
					oldManualOrder.setRemarks(note);
					oldManualOrder.setDeleteBy(managerName);
					oldManualOrder.setDeleteDate(new Date());
					Map<String, Object> reMOrderMap = transferMOrder(oldManualOrder);
					if (!"1".equals(reMOrderMap.get(BaseCode.STATUS.toString()))) {
						return reMOrderMap;
					}
					Map<String, Object> reMOrderSubMap = transferMOrderSub(oldManualOrder, reMOrderSubList);
					if (!"1".equals(reMOrderSubMap.get(BaseCode.STATUS.toString()))) {
						return reMOrderMap;
					}
					Map<String, Object> redelMOrder = deleteMOrder(mOrder);
					if (!"1".equals(redelMOrder.get(BaseCode.STATUS.toString()))) {
						return redelMOrder;
					}
					Map<String, Object> redelMOrderSub = deleteMOrderSub(reMOrderSubList);
					if (!"1".equals(redelMOrderSub.get(BaseCode.STATUS.toString()))) {
						return redelMOrderSub;
					}
				} else {
					return ReturnInfoUtils.errorInfo("查询订单信息或订单商品信息失败,服务器繁忙!");
				}
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("请求参数不能为空！");
	}

	/**
	 * 真实删除手工订单关联商品信息
	 * 
	 * @param reMOrderSubList
	 * @return Map
	 */
	private Map<String, Object> deleteMOrderSub(List<MorderSub> reMOrderSubList) {
		for (int i = 0; i < reMOrderSubList.size(); i++) {
			MorderSub mOrderSub = reMOrderSubList.get(i);
			if (!morderDao.delete(mOrderSub)) {
				return ReturnInfoUtils.errorInfo("删除订单商品信息失败,服务器繁忙!");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 真实删除手工订单信息
	 * 
	 * @param mOrder
	 *            手工订单信息实体类
	 * @return Map
	 */
	private Map<String, Object> deleteMOrder(Morder mOrder) {
		if (!morderDao.delete(mOrder)) {
			return ReturnInfoUtils.errorInfo("删除订单信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 转移手工订单信息
	 * 
	 * @param oldManualOrder
	 *            手工订单实体集合
	 * @return Map
	 * @throws Exception
	 */
	private Map<String, Object> transferMOrder(OldManualOrder oldManualOrder) {
		if (!morderDao.add(oldManualOrder)) {
			if (morderDao.delete(oldManualOrder)) {
				return ReturnInfoUtils.errorInfo("回滚订单信息失败,服务器繁忙!");
			}
			return ReturnInfoUtils.errorInfo("移除订单信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 转移手工订单关联商品信息
	 * 
	 * @param reMOrderList
	 * @param reMOrderSubList
	 *            手工订单商品实体集合
	 * @return Map
	 */
	private Map<String, Object> transferMOrderSub(OldManualOrder oldManualOrder, List<MorderSub> mOrderSubList) {
		List<OldManualOrderSub> cacheList = new ArrayList<>();
		for (int i = 0; i < mOrderSubList.size(); i++) {
			MorderSub manualOrderSub = mOrderSubList.get(0);
			OldManualOrderSub oldManualOrderSub = new OldManualOrderSub();
			try {
				CopyUtils.copy(manualOrderSub, oldManualOrderSub);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!morderDao.add(oldManualOrderSub)) {
				return rollBackOldManualOrderSub(oldManualOrder, cacheList);
			}
			cacheList.add(oldManualOrderSub);
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 当手工订单关联商品信息向历史记录表中添加数据时,发生错误,则先进行商品信息回滚(删除),后进行订单回滚(删除)
	 * 
	 * @param oldManualOrder
	 *            (旧数据)手工订单实体实体
	 * @param cacheList
	 *            手工订单关联商品信息(集合)
	 * @return Map
	 */
	private Map<String, Object> rollBackOldManualOrderSub(OldManualOrder oldManualOrder,
			List<OldManualOrderSub> cacheList) {
		for (int i = 0; i < cacheList.size(); i++) {
			OldManualOrderSub oldManualOrderSub = cacheList.get(0);
			if (!morderDao.delete(oldManualOrderSub)) {
				return ReturnInfoUtils.errorInfo("回滚订单商品信息失败,服务器繁忙!");
			}
		}
		if (!morderDao.delete(oldManualOrder)) {
			return ReturnInfoUtils.errorInfo("回滚订单信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.errorInfo("手工订单与商品信息回滚成功!!");
	}

	@Override
	public Map<String, Object> checkIdCardCount(String orderDocId) {
		if (StringEmptyUtils.isEmpty(orderDocId)) {
			return ReturnInfoUtils.errorInfo("请求参数错误,身份证号码不能为空!");
		}
		Map<String, Object> reTodayMap = checkTodayIdCardCount(orderDocId);
		if (!"1".equals(reTodayMap.get(BaseCode.STATUS.toString()))) {
			return reTodayMap;
		}
		Map<String, Object> reWeekMap = checkWeekIdCardCount(orderDocId);
		if (!"1".equals(reWeekMap.get(BaseCode.STATUS.toString()))) {
			return reWeekMap;
		}

		return checkMonthIdCardCount(orderDocId);
	}

	/**
	 * 检查一个月以内身份证出现次数
	 * 
	 * @param orderDocId
	 *            身份证号码
	 * @return Map
	 */
	private Map<String, Object> checkMonthIdCardCount(String orderDocId) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String endDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd ");// 格式化当前时间
		calendar.add(Calendar.MONTH, -1); // 设置为一个月前
		String startDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd");// 格式化当前时间
		Table weekCountTable = morderDao.getIdCardCount(orderDocId, startDate, endDate);
		if (weekCountTable == null) {
			return ReturnInfoUtils.errorInfo("查询一个月内身份证号码出现次数失败,服务器繁忙！");
		} else if (!weekCountTable.getRows().isEmpty()) {
			String strCount = weekCountTable.getRows().get(0).getValue("count") + "";
			int idCardCount = Integer.parseInt(strCount);
			if (idCardCount >= 8) {
				return ReturnInfoUtils
						.errorInfo("身份证号码[" + orderDocId + "]在->" + startDate + "至" + endDate + ",已经有过8次订单记录,请勿恶意刷单！");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 检查一周内身份证出现次数
	 * 
	 * @param orderDocId
	 *            下单人身份证号码
	 * @return Map
	 */
	private Map<String, Object> checkWeekIdCardCount(String orderDocId) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String endDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd ");// 格式化当前时间
		calendar.add(Calendar.WEDNESDAY, -1); // 设置为一周前
		String startDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd");// 格式化当前时间
		Table weekCountTable = morderDao.getIdCardCount(orderDocId, startDate, endDate);
		if (weekCountTable == null) {
			return ReturnInfoUtils.errorInfo("查询一周内身份证号码出现次数失败,服务器繁忙！");
		} else if (!weekCountTable.getRows().isEmpty()) {
			String strCount = weekCountTable.getRows().get(0).getValue("count") + "";
			int idCardCount = Integer.parseInt(strCount);
			if (idCardCount >= 5) {
				return ReturnInfoUtils
						.errorInfo("身份证号码[" + orderDocId + "]在->" + startDate + "至" + endDate + ",已经有过5次订单记录,请勿恶意刷单！");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验当天身份证号码出现次数
	 * 
	 * @param orderDocId
	 *            下单人身份证号码
	 * @return Map
	 */
	private Map<String, Object> checkTodayIdCardCount(String orderDocId) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String startDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd");// 格式化当前时间
		String endDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd ");// 格式化当前时间
		Table todayCountTable = morderDao.getIdCardCount(orderDocId, startDate, endDate);
		if (todayCountTable == null) {
			return ReturnInfoUtils.errorInfo("查询当天身份证号码出现次数失败,服务器繁忙！");
		} else if (!todayCountTable.getRows().isEmpty()) {
			String strCount = todayCountTable.getRows().get(0).getValue("count") + "";
			int idCardCount = Integer.parseInt(strCount);
			if (idCardCount >= 3) {
				return ReturnInfoUtils.errorInfo("身份证号码[" + orderDocId + "]在->" + startDate + ",已经有过3次订单记录,请勿恶意刷单！");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> checkRecipientTel(String recipientTel) {
		if (StringEmptyUtils.isEmpty(recipientTel)) {
			return ReturnInfoUtils.errorInfo("请求参数错误,收货人电话不能为空!");
		}
		Map<String, Object> reTodayMap = checkTodayPhoneCount(recipientTel);
		if (!"1".equals(reTodayMap.get(BaseCode.STATUS.toString()))) {
			return reTodayMap;
		}

		Map<String, Object> reWeekMap = checkWeekPhoneCount(recipientTel);
		if (!"1".equals(reWeekMap.get(BaseCode.STATUS.toString()))) {
			return reWeekMap;
		}
		return checkMonthPhoneCount(recipientTel);
	}

	/**
	 * 根据订单收货人电话号码查询今天至30天之前(一个月内)手机号码出现的次数
	 * 
	 * @param recipientTel
	 *            收货人手机号码
	 * @return Map
	 */
	private Map<String, Object> checkMonthPhoneCount(String recipientTel) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String endDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd ");// 格式化当前时间
		calendar.add(Calendar.MONTH, -1); // 设置为一个月前
		String startDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd");// 格式化当前时间
		Table weekCountTable = morderDao.getPhoneCount(recipientTel, startDate, endDate);
		if (weekCountTable == null) {
			return ReturnInfoUtils.errorInfo("查询一个月内身份证号码出现次数失败,服务器繁忙！");
		} else if (!weekCountTable.getRows().isEmpty()) {
			String strCount = weekCountTable.getRows().get(0).getValue("count") + "";
			int idCardCount = Integer.parseInt(strCount);
			if (idCardCount >= 8) {
				return ReturnInfoUtils.errorInfo(
						"收货人手机号码[" + recipientTel + "]在->" + startDate + "至" + endDate + ",已经有过8次订单记录,请勿恶意刷单！");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据订单收货人电话号码查询今天至7天之前(一周内)手机号码出现的次数
	 * 
	 * @param recipientTel
	 *            收货人手机号码
	 * @return Map
	 */
	private Map<String, Object> checkWeekPhoneCount(String recipientTel) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String endDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd ");// 格式化当前时间
		calendar.add(Calendar.WEDNESDAY, -1); // 设置为一周前
		String startDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd");// 格式化当前时间
		Table weekCountTable = morderDao.getPhoneCount(recipientTel, startDate, endDate);
		if (weekCountTable == null) {
			return ReturnInfoUtils.errorInfo("查询当天收货人手机号码出现次数失败,服务器繁忙！");
		} else if (!weekCountTable.getRows().isEmpty()) {
			String strCount = weekCountTable.getRows().get(0).getValue("count") + "";
			int idCardCount = Integer.parseInt(strCount);
			if (idCardCount >= 5) {
				return ReturnInfoUtils.errorInfo(
						"收货人手机号码[" + recipientTel + "]在->" + startDate + "至" + endDate + ",已经有过5次订单记录,请勿恶意刷单！");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据订单收货人电话号码查询今天内手机号码出现的次数
	 * 
	 * @param recipientTel
	 *            收货人手机号码
	 * @return Map
	 */
	private Map<String, Object> checkTodayPhoneCount(String recipientTel) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String startDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd");// 格式化当前时间
		String endDate = DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd ");// 格式化当前时间
		Table todayCountTable = morderDao.getPhoneCount(recipientTel, startDate, endDate);
		if (todayCountTable == null) {
			return ReturnInfoUtils.errorInfo("查询当天收货人手机号码出现次数失败,服务器繁忙！");
		} else if (!todayCountTable.getRows().isEmpty()) {
			String strCount = todayCountTable.getRows().get(0).getValue("count") + "";
			int idCardCount = Integer.parseInt(strCount);
			if (idCardCount >= 3) {
				return ReturnInfoUtils.errorInfo("收货人手机号码[" + recipientTel + "]在->" + startDate + "已经有过3次订单记录,请勿恶意刷单！");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	private static Logger logger = LogManager.getLogger(MorderServiceImpl.class);

	@Override
	public Object testLogs() {
		JSONArray json = null;
		try {
			json.size();
		} catch (Exception e) {
			logger.error("====service层=====>>>>json错误！", e);
			// logger.debug("Hello World!");
			// e.printStackTrace();
		}
		return null;
	}

}
