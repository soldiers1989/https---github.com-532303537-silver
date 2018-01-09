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
	// 创建一个静态钥匙
	private static Object lock = "lock";// 值是任意的

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
		Map<String, Object> blurryMap = (Map<String, Object>) reDatasMap.get("blurry");
		List<Map<String, Object>> errorList = (List<Map<String, Object>>) reDatasMap.get("error");
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
		statusMap.put("status", -1);
		return statusMap;
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
		noNullKeys.add("EntGoodsNo");
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
		mosb.setEntGoodsNo(goodsInfo.get("EntGoodsNo") + "");
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
		String packageType = goodsInfo.get("packageType") + "";
		String transportModel = goodsInfo.get("transportModel") + "";
		String numOfPackages = goodsInfo.get("numOfPackages") + "";

		if (StringEmptyUtils.isNotEmpty(firstLegalCount) && StringEmptyUtils.isNotEmpty(secondLegalCount)
				&& StringEmptyUtils.isNotEmpty(packageType) && StringEmptyUtils.isNotEmpty(transportModel)
				&& StringEmptyUtils.isNotEmpty(numOfPackages)) {
			mosb.setFirstLegalCount(goodsInfo.getDouble("firstLegalCount"));
			mosb.setSecondLegalCount(goodsInfo.getDouble("secondLegalCount"));
			mosb.setPackageType(goodsInfo.getInt("packageType"));
			mosb.setTransportModel(goodsInfo.getString("transportModel"));
			mosb.setNumOfPackages(goodsInfo.getInt("numOfPackages"));
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
	public Map<String, Object> deleteByOrderId(String marchant_no, String order_id) {
		Map<String, Object> params = new HashMap<>();
		params.put("marchant_no", marchant_no);
		params.put("order_id", order_id);
		List<Morder> mlist = morderDao.findByProperty(Morder.class, params, 1, 1);
		params.clear();
		if (mlist != null && mlist.size() > 0) {
			if (deleteMsubByOrderId(order_id)) {
				if (morderDao.delete(mlist.get(0))) {
					params.put("status", 1);
					params.put("msg", "移除数据成功");
					return params;
				}
			}
		}
		params.put("status", -1);
		params.put("msg", "移除数据失败，请重试");
		return params;
	}

	private boolean deleteMsubByOrderId(String order_id) {
		return morderSubDao.deleteRecordsByOrderId(order_id);

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
		/*
		 * params.put("order_id", orderId); params.put("Qty",
		 * goodsInfo.getInt("Qty")); params.put("EntGoodsNo",
		 * goodsInfo.getString("EntGoodsNo")); params.put("Price",
		 * goodsInfo.getDouble("Price")); params.put("GoodsStyle",
		 * goodsInfo.getString("GoodsStyle")); List<MorderSub> msList =
		 * morderDao.findByProperty(MorderSub.class, params, 1, 1); if(msList
		 * !=null && !msList.isEmpty()){ System.out.println(
		 * "=================订单与订单商品已存在，请勿重复下单================");
		 * statusMap.put(BaseCode.STATUS.toString(),StatusCode.FORMAT_ERR.
		 * getStatus()); statusMap.put(BaseCode.MSG.toString(),
		 * "订单与订单商品已存在，请勿重复下单"); return statusMap; }
		 */
		params.clear();
		params.put("dateSign", dateSign);
		params.put("waybill", waybill);
		List<Morder> ml = morderDao.findByProperty(Morder.class, params, 1, 1);
		if (ml != null && ml.size() > 0) {
			Morder morder = ml.get(0);
			if (ml.get(0).getFCY() + ActualAmountPaid < 2000) {
				morder.setActualAmountPaid(morder.getActualAmountPaid() + Tax + ActualAmountPaid);
				morder.setFCY(morder.getFCY() + FCY);
				morderDao.update(morder);
				statusMap.put("status", 1);
				statusMap.put("order_id", morder.getOrder_id());
				statusMap.put("msg", "订单更新总价");
				return statusMap;
			} else {
				statusMap.put("status", -6);
				statusMap.put("msg", "订单总金额超过2000，请重新下单");
				return statusMap;
			}
		} else {
			Morder morder = new Morder();
			// 查询缓存中订单自增Id
			int count = getRedisIdCount(dateSign);
			/*
			 * long count = morderDao.findByPropertyCount(Morder.class, params);
			 * if (count < 0) { statusMap.put("status", -5);
			 * statusMap.put("msg", "存储失败，系统内部错误"); return statusMap; }
			 */
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
			String randomDate = randomCreateOrderDate();
			morder.setOrderDate(randomDate);
			if (morderDao.add(morder)) {
				statusMap.put("status", 1);
				statusMap.put("order_id", morder.getOrder_id());
				statusMap.put("msg", "存储完毕");
				return statusMap;
			}
			statusMap.put("status", -1);
			statusMap.put("msg", "存储失败，请稍后重试");
			return statusMap;
		}
	}

	/**
	 * 获取当前日期下订单自增数
	 * 
	 * @param dateSign
	 *            日期格式:yyyyMMdd
	 * @return int
	 */
	private int getRedisIdCount(String dateSign) {
		Map<String, Object> statusMap = new HashMap<>();
		int orderCount = 1;
		String key = "Shop_Key_OrderIdCount_Int" + dateSign;
		synchronized (lock) {
			// 读取缓存中的信息
			byte[] redisByte = JedisUtil.get(key.getBytes(), 86400);
			if (redisByte != null && redisByte.length > 0) {
				Map<String, Object> datasMap = (Map<String, Object>) SerializeUtil.toObject(redisByte);
				System.out.println("---->>>>>>>>>>>>>>"+datasMap.get("orderCount"));
				orderCount = Integer.parseInt(datasMap.get("orderCount") + "");
				orderCount++;
			} 
			statusMap.put("orderCount", orderCount);
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(statusMap), 86400);
			return orderCount;
		}
	}

	/**
	 * 随机生成一个3-5天前的日期
	 * 
	 * @return
	 */
	private String randomCreateOrderDate() {
		java.util.Random random = new java.util.Random();// 定义随机类
		int result = random.nextInt(5);// 返回[0,10)集合中的整数，注意不包括10
		Date dNow = new Date(); // 当前时间
		Calendar calendar2 = Calendar.getInstance(); // 得到日历
		calendar2.setTime(dNow);// 将当前时间赋给日历
		calendar2.add(Calendar.DATE, -2);
		dNow = calendar2.getTime();//
		Calendar calendar = Calendar.getInstance(); // 得到日历
		calendar.setTime(dNow);// 把当前时间赋给日历
		calendar.add(Calendar.DATE, -(result + 2));
		calendar.setTime(calendar.getTime());
		Date dBefore = calendar.getTime(); // 得到随机3-5天时间
		return DateUtil.randomDate(dBefore, dNow);
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
		double orderTotalPrice = Double.parseDouble(item.get("orderTotalPrice") + "");
		double tax = Double.parseDouble(item.get("tax") + "");
		double actualAmountPaid = Double.parseDouble(item.get("actualAmountPaid") + "");
		String dateSign = DateUtil.formatDate(new Date(), "yyyyMMdd");
		String orderId = item.get("orderId") + "";
		if (StringEmptyUtils.isEmpty(orderId)) {// 当表单中未填写订单Id时,则系统生成
			// 查询缓存中订单自增Id
			int count = getRedisIdCount(dateSign);
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
		if (ml != null && !ml.isEmpty()) {
			Morder morder = ml.get(0);
			// 已存在的订单总金额,加上新增的订单金额超过2000 则提示不生成
			if (morder.getFCY() + orderTotalPrice < 2000) {
				morder.setActualAmountPaid(morder.getActualAmountPaid() + tax + actualAmountPaid);
				morder.setFCY(morder.getFCY() + orderTotalPrice);
				morderDao.update(morder);
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put("order_id", morder.getOrder_id());
				statusMap.put("msg", "订单更新总价");
				return statusMap;
			} else {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
				statusMap.put("msg", "订单总金额超过2000，请重新下单");
				return statusMap;
			}
		} else {
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
			String randomDate = randomCreateOrderDate();
			morder.setOrderDate(randomDate);
			String ehsEntName = item.get("ehsEntName") + "";
			if (StringEmptyUtils.isNotEmpty(ehsEntName)) {
				JSONObject json = new JSONObject();
				json.put("ehsEntName", ehsEntName);
				morder.setSpareParams(json.toString());
			}
			if (morderDao.add(morder)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put("order_id", morder.getOrder_id());
				statusMap.put("msg", "存储完毕");
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put("msg", "存储失败，请稍后重试");
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> createQBOrderSub(String merchantId, Map<String, Object> item) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		String entGoodsNo = item.get("entGoodsNo") + "";
		String marCode = item.get("marCode") + "";
		String goodsName = item.get("goodsName") + "";
		params.put("entGoodsNo", entGoodsNo.trim() + "_" + marCode.trim());

		params.put("goodsMerchantId", merchantId);
		List<GoodsRecordDetail> goodsList = morderDao.findByProperty(GoodsRecordDetail.class, params, 1, 1);
		if (goodsList != null && !goodsList.isEmpty()) {
			GoodsRecordDetail goods = goodsList.get(0);
			JSONObject param = new JSONObject();
			param.put("order_id", item.get("orderId") + "");
			String reEntGoodsNo = goods.getEntGoodsNo();
			String[] str = reEntGoodsNo.split("_");
			reEntGoodsNo = str[0];
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
			String spareParam = goods.getSpareParams();
			if (StringEmptyUtils.isNotEmpty(spareParam)) {
				JSONObject json = JSONObject.fromObject(spareParam);
				String sku = json.get("SKU") + "";
				param.put("SKU", sku);
				param.put("marCode", marCode.trim());
			}
			return createNewSub(param);
		} else {
			statusMap.put("status", -1);
			statusMap.put("msg", goodsName + "------>该商品不存在,请核实!");
			return statusMap;
		}
	}
}
