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
import org.silver.shop.api.system.tenant.MerchantIdCardCostService;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.dao.system.cross.ReportsDao;
import org.silver.shop.model.system.log.SynthesisReportLog;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.model.system.tenant.MerchantIdCardCostContent;
import org.silver.util.DateUtil;
import org.silver.util.DoubleOperationUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONObject;

@Service(interfaceClass = ReportsService.class)
public class ReportsServiceImpl implements ReportsService {

	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private ReportsDao reportsDao;
	@Autowired
	private MerchantIdCardCostService merchantIdCardCostService;

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
			return ReturnInfoUtils.warnInfo();
		} else if (!table.getRows().isEmpty()) {
			return oldGetSynthesisReport(Transform.tableToJson(table).getJSONArray("rows"));
		} else {
			return ReturnInfoUtils.noDatas();
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
			// 封底手续费
			double backCoverFee = 0;
			if (feeList != null && !feeList.isEmpty()) {
				for (MerchantFeeContent feeContent : feeList) {
					fee += feeContent.getPlatformFee();
					backCoverFee = feeContent.getBackCoverFee();
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
			mergeDatas(json, fee, newlist, idCardJson, backCoverFee);
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
	 * @param backCoverFee
	 *            封底手续费
	 */
	private void mergeDatas(JSONObject json, double fee, List<Object> newlist, JSONObject idCardJson,
			double backCoverFee) {
		Map<String, Object> datasMap = new HashMap<>();
		Iterator<String> sIterator = json.keys();
		while (sIterator.hasNext()) {// 合并商户手续费
			// 获得key
			String key = sIterator.next();
			// 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
			String value = StringUtil.replace(json.getString(key));
			if (fee > 0) {
				datasMap.put("platformFee", fee);
				//
				datasMap.put("backCoverFee", backCoverFee);
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
	public Map<String, Object> tmpCreate(String merchantId) {
		double fee = 0;
		double backCoverFee = 0;
		Map<String, Object> params = new HashMap<>();
		// MerchantId_00074 深圳市前海九米信息技术有限公司
		// MerchantId_00089 深圳市承和润文化传播股份有限公司
		if(StringEmptyUtils.isNotEmpty(merchantId)){
			params.put("merchantId", merchantId);
		}
		List<MerchantIdCardCostContent> merchantList = reportsDao.findByProperty(MerchantIdCardCostContent.class,
				params, 0, 0);
		for (MerchantIdCardCostContent idCardCost : merchantList) {
			params.clear();
			params.put("merchantId", idCardCost.getMerchantId());
			List<MerchantFeeContent> merchantFeeList = reportsDao.findByProperty(MerchantFeeContent.class, params, 0,
					0);
			if (merchantFeeList != null && !merchantFeeList.isEmpty()) {
				// 随便取一个封底手续费即可
				MerchantFeeContent feeContent = merchantFeeList.get(0);
				fee = feeContent.getPlatformFee();
				params.clear();
				params.put("merchantId", idCardCost.getMerchantId());
				params.put("customsCode", feeContent.getCustomsCode());
				params.put("ciqOrgCode", feeContent.getCiqOrgCode());
				// 类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
				if ("orderRecord".equals(feeContent.getType())) {
					params.put("type", "paymentRecord");
				} else {
					params.put("type", "orderRecord");
				}
				List<MerchantFeeContent> merchantFeeList2 = reportsDao.findByProperty(MerchantFeeContent.class, params,
						0, 0);
				if (merchantFeeList2 != null && !merchantFeeList2.isEmpty()) {
					MerchantFeeContent fee2 = merchantFeeList2.get(0);
					fee = DoubleOperationUtil.add(fee, fee2.getPlatformFee());
				}
				backCoverFee = feeContent.getBackCoverFee();
			}
			System.out.println("--商户-->" + idCardCost.getMerchantName() + ";--费率-->" + fee + "--封底费-->" + backCoverFee);
			params.clear();
			params.put("startDate", "2018-09-04 00:00:00");
			params.put("endDate", "2018-09-04 23:59:59");
			params.put("merchantId", idCardCost.getMerchantId());
			Table reList = reportsDao.getDailyReportDetails(params, fee, backCoverFee);
			JSONArray arr = Transform.tableToJson(reList).getJSONArray("rows");
			Map<String, Object> viceMap = null;
			JSONObject json = null;
			for (int i = 0; i < arr.size(); i++) {
				json = JSONObject.fromObject(arr.get(i));
				System.out.println("--->>>" + (json.toString()));
				JSONObject idCardJson = null;
				viceMap = new HashMap<>();
				viceMap.put("merchantId", idCardCost.getMerchantId());
				viceMap.put("date", StringUtil.replace(json.get("date") + ""));
				Table reIdcardList = reportsDao.getIdCardDetails(viceMap);
				if (reIdcardList != null && !reIdcardList.getRows().isEmpty()) {
					com.alibaba.fastjson.JSONArray idCardJsonArr = Transform.tableToJson(reIdcardList)
							.getJSONArray("rows");
					idCardJson = JSONObject.fromObject(idCardJsonArr.get(0));
					System.out.println("==身份证==>>" + idCardJson.toString());
					saveReportLog(json, idCardJson, idCardCost.getPlatformCost(), fee, backCoverFee);
				}
			}
		}
		return null;
	}

	private void saveReportLog(JSONObject json, JSONObject idCardJson, double platformCost, double fee,
			double backCoverFee) {
		SynthesisReportLog log = new SynthesisReportLog();
		log.setMerchantId(StringUtil.replace(json.get("merchant_no") + ""));
		log.setMerchantName(StringUtil.replace(json.get("merchantName") + ""));
		log.setDate(DateUtil.parseDate(StringUtil.replace(json.get("date") + ""), "yyyy-MM-dd"));
		log.setTotalCount(Integer.parseInt(StringUtil.replace(json.get("totalCount") + "")));
		log.setAmount(Double.parseDouble(StringUtil.replace(json.get("amount") + "")));
		log.setPlatformFee(fee);
		log.setBackCoverCount(Integer.parseInt(StringUtil.replace(json.get("backCoverCount") + "")));
		log.setBackCoverFee(backCoverFee);
		log.setNormalAmount(Double.parseDouble(StringUtil.replace(json.get("normalAmount") + "")));
		log.setIdCardTotalCount(Integer.parseInt(StringUtil.replace(idCardJson.get("idCardTotalCount") + "")));
		log.setIdCardTollCount(Integer.parseInt(StringUtil.replace(idCardJson.get("idCardTollCount") + "")));
		log.setIdCardFreeCount(Integer.parseInt(StringUtil.replace(idCardJson.get("idCardFreeCount") + "")));
		log.setIdCardCost(platformCost);
		log.setCreateDate(new Date());
		log.setCreateBy("system");
		if (!reportsDao.add(log)) {
			System.out.println("-----保存失败！----");
		}
		System.out.println("--==保存成功=======");
	}

	@Override
	public Map<String, Object> getSynthesisReportInfo(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		String type = datasMap.get("type") + "";
		datasMap.remove("type");
		switch (type) {
		case "day":
			return getDayReport(datasMap);
		case "week":

			break;
		case "month":
			return getMonthReport(datasMap);
		default:
			return getReport(datasMap);
		}
		return null;
	}

	/**
	 * 自定义时间范围查询报表数据
	 * 
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> getReport(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("获取月份报表时，请求参数不能为null");
		}
		String startDate = datasMap.get("startDate") + "";
		if (DateUtil.parseDate(startDate, "yyyy-MM-dd HH:mm:ss") == null) {
			return ReturnInfoUtils.errorInfo("开始时间错误！");
		}
		String endDate = datasMap.get("endDate") + "";
		if (DateUtil.parseDate(endDate, "yyyy-MM-dd HH:mm:ss") == null) {
			return ReturnInfoUtils.errorInfo("结束时间错误！");
		}
		datasMap.put("startDate", DateUtil.parseDate(startDate, "yyyy-MM-dd HH:mm:ss"));
		datasMap.put("endDate", DateUtil.parseDate(endDate, "yyyy-MM-dd HH:mm:ss"));
		return getReportInfo(datasMap, 0, 0);
	}

	/**
	 * 根据商户id与日期、查询商户历史报表数据
	 * @param datasMap  日期+商户id参数
	 * @param page 页数
	 * @param size 数目
	 * @return Map
	 */
	private Map<String, Object> getReportInfo(Map<String, Object> datasMap, int page, int size) {
		List<SynthesisReportLog> reList = reportsDao.getReportInfo(datasMap, page, size);
		if (reList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (reList.isEmpty()) {
			return ReturnInfoUtils.noDatas();
		} else {
			return ReturnInfoUtils.successDataInfo(reList);
		}
	}

	/**
	 * 获取月报表-历史记录
	 * 
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> getMonthReport(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("获取月份报表时，请求参数不能为null");
		}
		String strDate = datasMap.get("date") + "";
		if (strDate.length() != 7 || DateUtil.parseDate(strDate, "yyyy-MM") == null) {
			return ReturnInfoUtils.errorInfo("日期错误！");
		}
		Date date = DateUtil.parseDate(strDate + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		datasMap.put("startDate", calendar.getTime());
		calendar.clear();
		calendar.setTime(date);
		// 获取当前月的最后一天
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		datasMap.put("endDate", calendar.getTime());
		return getReportInfo(datasMap, 0, 0);
	}

	/**
	 * 获取指定某一天的历史报表数据
	 * 
	 * @param datasMap
	 *            查询参数
	 * @return Map
	 */
	private Map<String, Object> getDayReport(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("获取天报表时，请求参数不能为null");
		}
		String strDate = datasMap.get("date") + "";
		if (strDate.length() != 10 || DateUtil.parseDate(strDate, "yyyy-MM-dd") == null) {
			return ReturnInfoUtils.errorInfo("日期错误！");
		}
		// 判断查询日期是否为今天
		if (DateUtil.isThisTime(DateUtil.parseDate(strDate, "yyyy-MM-dd").getTime(), "yyyy-MM-dd")) {
			// 当查询时间为今天时，则查询实时数据
			return getDayReportInfo(datasMap);
		}
		Date date = DateUtil.parseDate(strDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		datasMap.put("date", calendar.getTime());
		List<SynthesisReportLog> reportList = reportsDao.findByProperty(SynthesisReportLog.class, datasMap, 0, 0);
		if (reportList == null) {
			return ReturnInfoUtils.warnInfo();
		} else if (reportList.isEmpty()) {
			return ReturnInfoUtils.noDatas();
		} else {
			return ReturnInfoUtils.successDataInfo(reportList);
		}
	}
}
