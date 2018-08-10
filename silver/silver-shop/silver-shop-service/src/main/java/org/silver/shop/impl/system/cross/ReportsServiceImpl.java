package org.silver.shop.impl.system.cross;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.cross.ReportsService;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.dao.system.cross.ReportsDao;
import org.silver.shop.model.system.log.SynthesisReportLog;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONObject;

@Service(interfaceClass = ReportsService.class)
public class ReportsServiceImpl implements ReportsService {

	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private ReportsDao reportsDao;

	@Override
	public Map<String, Object> getSynthesisReportDetails(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		String type = datasMap.get("type") + "";
		switch (type) {
		case "day":
			return getDayReportInfo(datasMap);
		case "week":

			break;
		case "month":
			return getMonthReportInfo(datasMap);
		default:
			Table list = paymentDao.getPaymentReportDetails(datasMap);
			return analysisInfo(list);
		}
		return null;
	}

	private Map<String, Object> getDayReportInfo(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("获取天报表时，请求参数不能为null");
		}
		String strDate = datasMap.get("date") + "";
		if (strDate.length() != 10 || DateUtil.parseDate(strDate, "yyyy-MM-dd") == null) {
			return ReturnInfoUtils.errorInfo("日期错误！");
		}
		Date date = DateUtil.parseDate(strDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		datasMap.put("startDate", DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"));
		calendar.clear();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		datasMap.put("endDate", DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"));
		Table reList = reportsDao.getPaymentReportDetails(datasMap);
		return analysisInfo(reList);
	}

	/**
	 * 分析查询数据,根据不同的结果，返回对应的结果信息
	 * 
	 * @param reList
	 * @return
	 */
	private Map<String, Object> analysisInfo(Table table) {
		if (table == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!table.getRows().isEmpty()) {
			return oldGetSynthesisReport(Transform.tableToJson(table).getJSONArray("rows"));
		} else {
			return ReturnInfoUtils.errorInfo("暂无报表数据!");
		}

	}

	/**
	 * 获取月报表数据
	 * 
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> getMonthReportInfo(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("获取月份报表时，请求参数不能为null");
		}
		String strDate = datasMap.get("date") + "";
		if (strDate.length() != 7 || DateUtil.parseDate(strDate, "yyyy-MM") == null) {
			return ReturnInfoUtils.errorInfo("月份错误！");
		}
		Date date = DateUtil.parseDate(strDate + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		datasMap.put("startDate", DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"));
		calendar.clear();
		calendar.setTime(date);
		// 获取当前月的最后一天
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		datasMap.put("endDate", DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss"));
		Table reList = reportsDao.getPaymentReportDetails(datasMap);
		return analysisInfo(reList);
	}

	@Override
	public Map<String, Object> oldGetSynthesisReport(JSONArray jsonArray) {
		if (jsonArray == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		Map<String, Object> viceMap = null;
		long startTime = System.currentTimeMillis();
		Map<String, Object> pamras2 = null;
		List<Object> newlist = new ArrayList<>();
		Map<String, Object> orMap = null;
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject json = JSONObject.fromObject(jsonArray.get(i));
			String merchantId = StringUtil.replace(json.get("merchant_no") + "");
			pamras2 = new HashMap<>();
			pamras2.put("merchantId", merchantId);
			// 类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
			pamras2.put("customsCode", StringUtil.replace(json.get("customsCode") + ""));
			List<Map<String, Object>> orList = new ArrayList<>();
			orMap = new HashMap<>();
			orMap.put("type", "orderRecord");
			orList.add(orMap);
			orMap = new HashMap<>();
			orMap.put("type", "paymentRecord");
			orList.add(orMap);
			List<MerchantFeeContent> feeList = paymentDao.findByPropertyOr(MerchantFeeContent.class, pamras2, orList, 0,
					0);
			double fee = 0;
			if (feeList != null && !feeList.isEmpty()) {
				for (MerchantFeeContent feeContent : feeList) {
					fee += feeContent.getPlatformFee();
				}
			}
			JSONObject idCardJson = null;
			viceMap = new HashMap<>();
			viceMap.put("merchantId", merchantId);
			viceMap.put("date", StringUtil.replace(json.get("date") + ""));
			Table reIdcardList = reportsDao.getIdCardDetails(viceMap);
			if (reIdcardList != null && !reIdcardList.getRows().isEmpty()) {
				com.alibaba.fastjson.JSONArray idCardJsonArr = Transform.tableToJson(reIdcardList).getJSONArray("rows");
				idCardJson = JSONObject.fromObject(idCardJsonArr.get(0));
			}
			mergeDatas(json, fee, newlist, idCardJson);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("--查询报表耗时-->>" + (endTime - startTime) + "ms");
		return ReturnInfoUtils.successDataInfo(newlist);
	}

	/**
	 * 将订单数据与身份证实名认证数据进行合并
	 * 
	 * @param json
	 *            订单参数
	 * @param fee
	 *            费率
	 * @param newlist
	 *            合并后的集合
	 * @param idCardJson
	 *            身份证认证参数
	 */
	private void mergeDatas(JSONObject json, double fee, List<Object> newlist, JSONObject idCardJson) {
		Map<String, Object> datasMap = new HashMap<>();
		Iterator<String> sIterator = json.keys();
		while (sIterator.hasNext()) {// 合并商户手续费
			// 获得key
			String key = sIterator.next();
			// 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
			String value = StringUtil.replace(json.getString(key));
			if (fee > 0) {
				datasMap.put("platformFee", fee);
			}
			datasMap.put(key, value);
		}
		if (idCardJson != null && !idCardJson.isEmpty()) {
			Iterator<String> sIterator2 = idCardJson.keys();
			while (sIterator2.hasNext()) {// 身份证实名手续费
				// 获得key
				String key = sIterator2.next();
				// 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
				String value = StringUtil.replace(idCardJson.getString(key));
				datasMap.put(key, value);
			}
		}
		datasMap.remove("userdata");
		newlist.add(datasMap);
	}

	@Override
	public Map<String, Object> getIdCardCertification(Map<String, Object> params) {
		if (params == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Table reIdcardList = reportsDao.getIdCardCertificationDetails(params);
		if (reIdcardList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reIdcardList.getRows().isEmpty()) {
			com.alibaba.fastjson.JSONArray idCardJsonArr = Transform.tableToJson(reIdcardList).getJSONArray("rows");
			return ReturnInfoUtils.successDataInfo(idCardJsonArr);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据");
		}
	}

	@Override
	public Map<String, Object> tmpUpdate() {
		Map<String, Object> params = new HashMap<>();
		params.put("startDate", "2018-07-01 00:00:00");
		params.put("endDate", "2018-08-09 23:59:59");
		Table reList = paymentDao.getPaymentReportDetails(params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			Map<String, Object> reMap = oldGetSynthesisReport(Transform.tableToJson(reList).getJSONArray("rows"));
			if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
				return reMap;
			}
			List<Object> reportList = (List<Object>) reMap.get(BaseCode.DATAS.toString());
			JSONObject json = null;
			for (int i = 0; i < reportList.size(); i++) {
				json = json.fromObject(reportList.get(i));
				if ("MerchantId_00074".equals(json.get("merchant_no"))) {
					System.out.println("---商户名称-不做处理->" + json.get("merchantName"));
					continue;
				}
				System.out.println("---->" + json.toString());
				// {date=2018-07-02, merchant_no=MerchantId_00076,
				// amount=861.21, totalCount=4, platformFee=0.006,
				// merchantName=上海峰赞实业有限公司, idCardTotalCount=4, tollFlag1=0,
				// tollFlag2=4, normalAmount=861.21, idCardCost=0.5,
				// backCoverCount=0, customsCode=5165}

				String date = json.get("date") + "";
				SynthesisReportLog report = new SynthesisReportLog();
				report.setMerchantId(json.get("merchant_no") + "");
				report.setMerchantName(json.get("merchantName") + "");
				report.setDate(DateUtil.parseDate(date, "yyyy-MM-dd"));
				report.setTotalCount(Integer.parseInt(json.get("totalCount") + ""));
				report.setAmount(Double.parseDouble(json.get("amount") + ""));
				// 订单服务费率
				double platformFee = Double.parseDouble(json.get("platformFee") + "");
				report.setPlatformFee(platformFee);
				// 低于100的订单数量
				int backCoverCount = Integer.parseInt(json.get("backCoverCount") + "");
				report.setBackCoverCount(backCoverCount);
				// 正常计算的订单金额
				double normalAmount = Double.parseDouble(json.get("normalAmount") + "");
				report.setNormalAmount(normalAmount);
				report.setIdCardTotalCount(Integer.parseInt(json.get("idCardTotalCount") + ""));
				// 身份证收费数量
				int tollCount = Integer.parseInt(json.get("tollFlag1") + "");
				report.setIdCardTollCount(tollCount);
				report.setIdCardFreeCount(Integer.parseInt(json.get("tollFlag2") + ""));
				// 身份证认证费率
				double idCardCost = Double.parseDouble(json.get("idCardCost") + "");
				report.setIdCardCost(idCardCost);
				report.setCreateBy("system");
				report.setCreateDate(new Date());
				/*
				 * if (!reportsDao.add(report)) {
				 * System.out.println("-保存失败！->"); }
				 */
			}
		} else {
			return ReturnInfoUtils.errorInfo("暂无报表数据!");
		}
		return null;
	}

	public static void main(String[] args) {

		System.out.println("--->?>>>" + DateUtil.parseDate("2018-07", "yyyy-MM"));
	}
}
