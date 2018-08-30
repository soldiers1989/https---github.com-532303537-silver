package org.silver.shop.quartz;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.cross.ReportsService;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.dao.system.cross.ReportsDao;
import org.silver.shop.impl.system.manual.ManualOrderServiceImpl;
import org.silver.shop.model.system.log.SynthesisReportLog;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.model.system.tenant.MerchantIdCardCostContent;
import org.silver.util.DateUtil;
import org.silver.util.DoubleOperationUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONObject;

/**
 * 定时任务生成订单报表数据
 */
public class CreateReportQtz {

	private static Logger logger = LogManager.getLogger(Object.class);

	@Autowired
	private ReportsService reportsService;
	@Autowired
	private ReportsDao reportsDao;

	public void createReportJob() {
		System.out.println("--定时任务生成订单报表数据--");
		double fee = 0;
		double backCoverFee = 0;
		Map<String, Object> params = new HashMap<>();
		List<MerchantIdCardCostContent> merchantList = reportsDao.findByProperty(MerchantIdCardCostContent.class, null, 0, 0);
		for (MerchantIdCardCostContent merchant : merchantList) {
			params.clear();
			params.put("merchantId", merchant.getMerchantId());
			List<MerchantFeeContent> merchantFeeList = reportsDao.findByProperty(MerchantFeeContent.class, params, 0,
					0);
			if (merchantFeeList != null && !merchantFeeList.isEmpty()) {
				// 随便取一个封底手续费即可
				MerchantFeeContent feeContent = merchantFeeList.get(0);
				fee = feeContent.getPlatformFee();
				params.clear();
				params.put("merchantId", merchant.getMerchantId());
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
				System.out.println("--商户-->" + merchant.getMerchantName() + ";--费率-->" + fee + "--封底费-->"
						+ feeContent.getBackCoverFee());
			}
			params.clear();
			params.put("startDate", "2018-08-01 00:00:00");
			params.put("endDate", "2018-08-01 23:59:59");
			params.put("merchantId", merchant.getMerchantId());
			Table reList = reportsDao.getDailyReportDetails(params, fee, backCoverFee);
			JSONArray arr = Transform.tableToJson(reList).getJSONArray("rows");
			Map<String, Object> viceMap = null;
			JSONObject json = null;
			for (int i = 0; i < arr.size(); i++) {
				json = JSONObject.fromObject(arr.get(i));
				System.out.println("--->>>" + (json.toString()));
				JSONObject idCardJson = null;
				viceMap = new HashMap<>();
				viceMap.put("merchantId", merchant.getMerchantId());
				viceMap.put("date", StringUtil.replace(json.get("date") + ""));
				Table reIdcardList = reportsDao.getIdCardDetails(viceMap);
				if (reIdcardList != null && !reIdcardList.getRows().isEmpty()) {
					com.alibaba.fastjson.JSONArray idCardJsonArr = Transform.tableToJson(reIdcardList)
							.getJSONArray("rows");
					idCardJson = JSONObject.fromObject(idCardJsonArr.get(0));
					System.out.println("==身份证==>>" + idCardJson.toString());
					saveReportLog(json,idCardJson,merchant.getPlatformCost(),fee,backCoverFee);
				}
			}
		}
		return null;
	}

	/**
	 * 保存报表日志信息
	 * @param json
	 * @param idCardJson
	 * @param platformCost
	 * @param fee
	 * @param backCoverFee
	 */
	private void saveReportLog(JSONObject json, JSONObject idCardJson, double platformCost, double fee, double backCoverFee) {
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
		log.setIdCardTotalCount(
				Integer.parseInt(StringUtil.replace(idCardJson.get("idCardTotalCount") + "")));
		log.setIdCardTollCount(Integer.parseInt(StringUtil.replace(idCardJson.get("tollFlag1") + "")));
		log.setIdCardFreeCount(Integer.parseInt(StringUtil.replace(idCardJson.get("tollFlag2") + "")));
		log.setIdCardCost(platformCost);
		log.setCreateDate(new Date());
		log.setCreateBy("system");
		if (!reportsDao.add(log)) {
			System.out.println("-----保存失败！----");
		}
		System.out.println("--==保存成功=======");
	}
	
	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date date = calendar.getTime();
		String d = DateUtil.formatDate(new Date(), "yyyy-MM-dd");
		System.out.println("-->>" +d.substring(d.length()-2));
	}
}
