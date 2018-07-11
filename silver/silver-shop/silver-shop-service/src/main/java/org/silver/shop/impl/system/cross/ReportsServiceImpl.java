package org.silver.shop.impl.system.cross;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.system.cross.ReportsService;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.dao.system.cross.ReportsDao;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;
import com.mysql.fabric.xmlrpc.base.Array;

import net.sf.json.JSONObject;

@Service(interfaceClass = ReportsService.class)
public class ReportsServiceImpl implements ReportsService {

	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private ReportsDao reportsDao;
	
	
	@Override
	public Map<String, Object> getSynthesisReportDetails(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> viceMap = null;
		long startTime = System.currentTimeMillis();
		Table reList = paymentDao.getPaymentReportDetails(params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			com.alibaba.fastjson.JSONArray jsonArr = Transform.tableToJson(reList).getJSONArray("rows");
			Map<String, Object> pamras2 = null;
			List<Object> newlist = new ArrayList<>();
			for (int i = 0; i < jsonArr.size(); i++) {
				JSONObject json = JSONObject.fromObject(jsonArr.get(i));
				double fee = 0;
				String merchantId = StringUtil.replace(json.get("merchant_no") + "");
				pamras2 = new HashMap<>();
				pamras2.put("merchantId", merchantId);
				// 类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
				pamras2.put("type", "orderRecord");
				pamras2.put("customsCode", StringUtil.replace(json.get("customsCode") + ""));
				List<MerchantFeeContent> feeList = paymentDao.findByProperty(MerchantFeeContent.class, pamras2, 0, 0);
				if (feeList != null && !feeList.isEmpty()) {
					MerchantFeeContent feeContent = feeList.get(0);
					fee = feeContent.getPlatformFee();
				}
				JSONObject idCardJson = null;
				viceMap = new HashMap<>();
				viceMap.put("merchantId", merchantId);
				viceMap.put("date", StringUtil.replace(json.get("date")+""));
				Table reIdcardList = reportsDao.getIdCardDetails(viceMap);
				if (reIdcardList != null && !reIdcardList.getRows().isEmpty()) {
					com.alibaba.fastjson.JSONArray idCardJsonArr = Transform.tableToJson(reIdcardList)
							.getJSONArray("rows");
					idCardJson = JSONObject.fromObject(idCardJsonArr.get(0));
				}
				mergeDatas(json, fee, newlist, idCardJson);
			}
			long endTime = System.currentTimeMillis();
			System.out.println("--查询综合报表-耗时->>>" + (endTime - startTime) + "ms");
			return ReturnInfoUtils.successDataInfo(newlist);
		} else {
			return ReturnInfoUtils.errorInfo("暂无报表数据!");
		}
	}

	private void mergeDatas(JSONObject json, double fee, List<Object> newlist, JSONObject idCardJson) {
		Map<String, Object> datasMap = new HashMap<>();
		Iterator<String> sIterator = json.keys();
		while (sIterator.hasNext()) {// 合并商户手续费
			// 获得key
			String key = sIterator.next();
			// 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
			String value = StringUtil.replace(json.getString(key));
			if ("platformFee".equals(key) && StringEmptyUtils.isNotEmpty(value)) {
				datasMap.put(key, fee + Double.parseDouble(value));
			} else {
				datasMap.put(key, value);
			}
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
		if(params == null){
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Table reIdcardList = reportsDao.getIdCardCertificationDetails(params);
		if(reIdcardList == null){
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		}else if (!reIdcardList.getRows().isEmpty()) {
			com.alibaba.fastjson.JSONArray idCardJsonArr = Transform.tableToJson(reIdcardList)
					.getJSONArray("rows");
			return ReturnInfoUtils.successDataInfo(idCardJsonArr);
		}else{
			return ReturnInfoUtils.errorInfo("暂无数据");
		}
	}

}
