package org.silver.shop.impl.system.manual;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.manual.MorderService;
import org.silver.shop.dao.system.manual.MorderDao;
import org.silver.shop.dao.system.manual.MorderSubDao;
import org.silver.shop.dao.system.manual.MuserDao;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.manual.Muser;
import org.silver.shop.util.SearchUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.JedisUtil;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

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

	private static final String FCODE = "142";

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

	private String createMorderSysNo(String sign, long id, Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String dstr = sdf.format(d);
		String nstr = id + "";
		while (nstr.length() < 5) {
			nstr = "0" + nstr;
		}
		int rstr = RandomUtils.getRandom(4);
		return sign + dstr + nstr + rstr;
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
		Map<String, Object> reDatasMap = SearchUtils.universalSearch(dataMap);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		paramMap.put("merchant_no", dataMap.get("merchant_no") + "".trim());
		paramMap.put("del_flag", 0);

		List<Morder> mlist = morderDao.findByPropertyLike(Morder.class, paramMap, null, page, size);
		long count = morderDao.findByPropertyLikeCount(Morder.class, paramMap, null);
		Map<String, Object> statusMap = new HashMap<>();
		List<Map<String, Object>> lMap = new ArrayList<>();
		if (mlist != null && mlist.size() > 0) {
			paramMap.clear();
			for (Morder m : mlist) {
				paramMap.put("order_id", m.getOrder_id());

				List<MorderSub> mslist = morderSubDao.findByProperty(paramMap, 0, 0);
				Map<String, Object> item = new HashMap<>();
				item.put("head", m);
				item.put("content", mslist);
				lMap.add(item);
			}
			statusMap.put("status", 1);
			statusMap.put("datas", lMap);
			statusMap.put("count", count);
			return statusMap;
		}
		return ReturnInfoUtils.errorInfo("暂无数据,服务器繁忙!");
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
			params.put("order_id", order_id);
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
		JSONArray datas = new JSONArray();
		datas.add(goodsInfo);
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("entGoodsNo");
		noNullKeys.add("HSCode");
		noNullKeys.add("Brand");
		// noNullKeys.add("BarCode");
		noNullKeys.add("GoodsName");
		noNullKeys.add("OriginCountry");
		noNullKeys.add("CusGoodsNo");
		noNullKeys.add("CIQGoodsNo");
		noNullKeys.add("GoodsStyle");
		noNullKeys.add("Unit");
		noNullKeys.add("Price");
		noNullKeys.add("Qty");
		Map<String, Object> checkMap = CheckDatasUtil.checkData(datas, noNullKeys);
		if ((int) checkMap.get("status") != 1) {
			return checkMap;
		}
		Map<String, Object> map = new HashMap<>();
		String orderId = goodsInfo.get("order_id") + "";
		map.put("order_id", orderId);
		long count = morderSubDao.findByPropertyCount(map);
		/*
		 * map.put("EntGoodsNo", goodsInfo.get("EntGoodsNo")); List<MorderSub>
		 * mls = morderSubDao.findByProperty(map, 0, 0); if (mls != null &&
		 * mls.size() > 0) { statusMap.put("status", -4); statusMap.put("msg",
		 * "订单【" + goodsInfo.get("order_id") + "】" + "关联的商品【" +
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
		Map<String, Object> params = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(orderIdPack);
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("订单Id信息错误,请重试!");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> item = (Map<String, Object>) jsonList.get(i);
			String orderId = item.get("orderId") + "";
			params.put("merchant_no", merchantId);
			params.put("order_id", orderId);
			List<Morder> orderlist = morderDao.findByProperty(Morder.class, params, 1, 1);
			params.clear();
			if (orderlist != null && !orderlist.isEmpty()) {
				Morder order = orderlist.get(0);
				// 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
				if (order.getOrder_record_status() == 1 || order.getOrder_record_status() == 4) {
					// 删除标识:0-未删除,1-已删除
					order.setDel_flag(1);
					if (morderDao.update(order)) {
						return ReturnInfoUtils.successInfo();
					}
					return ReturnInfoUtils.errorInfo("订单删除失败,服务器繁忙!");
					// deleteMsubByOrderId(orderId);
				} else {
					return ReturnInfoUtils.errorInfo("订单当前状态不允许删除,请联系管理员!");
				}
			}
		}
		return ReturnInfoUtils.errorInfo("订单信息参数错误,服务器繁忙!");
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
		params.clear();
		params.put("dateSign", dateSign);
		params.put("waybill", waybill);
		List<Morder> ml = morderDao.findByProperty(Morder.class, params, 1, 1);
		if (ml != null && !ml.isEmpty()) {
			Morder morder = ml.get(0);
			if (morder.getDel_flag() == 1) {
				return ReturnInfoUtils.errorInfo(morder.getOrder_id() + "<--订单已被刪除,无法再次导入,请联系管理员!");
			}
			return judgmentOrderInfo(morder, goodsInfo, ActualAmountPaid, FCY, Tax, 1);
		}
		Morder morder = new Morder();
		// 查询缓存中订单自增Id
		int count = SerialNoUtils.getRedisIdCount("order");
		morder.setOrder_id(createMorderSysNo("YM", count, new Date()));
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
		morder.setCreate_by(merchant_no);
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
		if (morderDao.add(morder)) {
			statusMap.put("status", 1);
			statusMap.put("order_id", morder.getOrder_id());
			statusMap.put("msg", "存储完毕");
			return statusMap;
		}
		return ReturnInfoUtils.errorInfo(morder.getOrder_id() + "<--订单存储失败，请稍后重试!");
	}

	/**
	 * 判断订单与订单商品信息是否存在,如果只是商品不存在则更新订单金额,如果订单与商品、序号都已存在则判定为重复导入
	 * 
	 * @param morder
	 *            订单信息
	 * @param goodsInfo
	 *            商品信息
	 * @param actualAmountPaid
	 *            总金额
	 * @param FCY
	 *            单价
	 * @param tax
	 *            税费
	 * @param flag
	 *            暂定标识1-国宗,2-企邦
	 * @param cacheList
	 * @return Map
	 */
	private Map<String, Object> judgmentOrderInfo(Morder morder, JSONObject goodsInfo, Double actualAmountPaid,
			Double FCY, Double tax, int flag) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramsMap = new HashMap<>();
		String waybill = morder.getWaybill().trim();
		String orderId = morder.getOrder_id().trim();
		paramsMap.put("seqNo", goodsInfo.get("seqNo"));
		paramsMap.put("EntGoodsNo", goodsInfo.get("entGoodsNo"));
		List<MorderSub> ms = morderDao.findByProperty(MorderSub.class, paramsMap, 0, 0);
		if (ms != null && !ms.isEmpty()) {
			MorderSub goods = ms.get(0);
			return ReturnInfoUtils.errorInfo(goods.getGoodsName() + "<--该订单与商品信息已存在,无需重复导入!");
		} else {
			morder.setActualAmountPaid(morder.getActualAmountPaid() + tax + actualAmountPaid);
			morder.setFCY(morder.getFCY() + FCY);
			if(!morderDao.update(morder)){
				return ReturnInfoUtils.errorInfo("订单号[" + orderId + "]<--订单更新总价失败,服务器繁忙!");
			}
			statusMap.put("status", 1);
			statusMap.put("order_id", morder.getOrder_id());
			statusMap.put("msg", "订单更新总价");
			if (morder.getFCY() + FCY + actualAmountPaid >= 2000) {// 当订单金额超过2000时
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
	public Map<String, Object> createQBOrder(String merchantId, Map<String, Object> item) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		List<String> cacheList = new ArrayList<>();
		double orderTotalPrice = Double.parseDouble(item.get("orderTotalPrice") + "");
		double tax = Double.parseDouble(item.get("tax") + "");
		double actualAmountPaid = Double.parseDouble(item.get("actualAmountPaid") + "");
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String orderId = item.get("orderId") + "";
		if (StringEmptyUtils.isEmpty(orderId)) {// 当表单中未填写订单Id时,则系统生成
			// 查询缓存中订单自增Id
			int count = SerialNoUtils.getRedisIdCount("order");
			orderId = createMorderSysNo("YM", count, new Date());
		}
		// 校验企邦是否已经录入已备案商品信息
		String entGoodsNo = item.get("entGoodsNo") + "";
		String marCode = item.get("marCode") + "";
		params.put("entGoodsNo", entGoodsNo.trim() + "_" + marCode.trim());
		params.put("goodsMerchantId", merchantId);
		List<GoodsRecordDetail> goodsList = morderDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
		if (goodsList == null || goodsList.isEmpty()) {
			statusMap.put("status", -1);
			statusMap.put("msg", "商品编号[" + entGoodsNo.trim() + "]与[" + marCode.trim() + "]------>对应商品不存在,请核实!");
			return statusMap;
		}
		params.clear();
		params.put("merchant_no", merchantId);
		params.put("order_id", orderId);
		List<Morder> ml = morderDao.findByProperty(Morder.class, params, 1, 1);
		Map<String, Object> reMap = null;
		if (ml != null && !ml.isEmpty()) {
			Morder morder = ml.get(0);
			// 企邦的税费暂写死为0
			return judgmentOrderInfo(morder, JSONObject.fromObject(item), actualAmountPaid, 0.0, tax, 2);
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
		morder.setCreate_by(merchantId);
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
			statusMap.put("msg", "存储失败，请稍后重试");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put("order_id", morder.getOrder_id());
		statusMap.put("msg", "存储完毕");
		return statusMap;

	}

	@Override
	public Map<String, Object> createQBOrderSub(String merchantId, Map<String, Object> item) {
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
			param.put("order_id", item.get("orderId") + "");
			String reEntGoodsNo = goods.getEntGoodsNo();
			if (StringEmptyUtils.isNotEmpty(marCode)) {
				String[] str = reEntGoodsNo.split("_");
				reEntGoodsNo = str[0];
			}
			param.put("EntGoodsNo", reEntGoodsNo);
			param.put("HSCode", goods.getHsCode());
			param.put("Brand", goods.getBrand());
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
		Map<String, Object> params = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(idPack);
		} catch (Exception e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("参数错误!");
		}
		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> datas = (Map<String, Object>) jsonList.get(i);
			String orderId = datas.get("orderId") + "";
			String goodsId = datas.get("goodsId") + "";
			params.clear();
			params.put("order_id", orderId);
			List<Morder> orderlist = morderDao.findByProperty(Morder.class, params, 1, 1);
			params.put("EntGoodsNo", goodsId);
			List<MorderSub> orderSublist = morderDao.findByProperty(MorderSub.class, params, 1, 1);
			if (orderSublist != null && !orderSublist.isEmpty()) {
				MorderSub goodsInfo = orderSublist.get(0);
				goodsInfo.setDeleteFlag(1);
				if (!morderDao.update(goodsInfo)) {
					return ReturnInfoUtils.errorInfo(goodsInfo.getGoodsName() + "<----删除失败,服务器繁忙!");
				} else {
					// 单价
					Double price = goodsInfo.getPrice();
					// 数量
					int count = goodsInfo.getQty();
					// 总价
					Double total = price * count;
					Map<String, Object> reMap = updateOrderAmount(orderlist, total);
					if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
						return reMap;
					}
				}
			} else {
				return ReturnInfoUtils.errorInfo("未找到商品信息,请核实参数是否正确!");
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 更新订单信息中订单总金额与实际支付金额
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
}
