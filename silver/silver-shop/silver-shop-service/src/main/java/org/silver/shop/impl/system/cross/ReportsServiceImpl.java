package org.silver.shop.impl.system.cross;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.system.cross.ReportsService;
import org.silver.shop.dao.system.cross.PaymentDao;
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

	@Override
	public Map<String, Object> getSynthesisReportDetails(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		long startTime = System.currentTimeMillis();
		Table reList = paymentDao.getPaymentReportDetails(params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			com.alibaba.fastjson.JSONArray jsonArr = Transform.tableToJson(reList).getJSONArray("rows");
			List<Object> list = new ArrayList<>();
			Map<String,Object> datasMap =null;
			for (int i = 0; i < jsonArr.size(); i++) {
				JSONObject json = JSONObject.fromObject(jsonArr.get(i));
				double fee = 0;
				params.clear();
				params.put("merchantId", StringUtil.replace(json.get("merchant_no") + ""));
				// 类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
				params.put("type", "orderRecord");
				params.put("customsCode", StringUtil.replace(json.get("customsCode") + ""));
				List<MerchantFeeContent> feeList = paymentDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
				if (feeList != null && !feeList.isEmpty()) {
					MerchantFeeContent feeContent = feeList.get(0);
					fee = feeContent.getPlatformFee();
				}
				datasMap = new HashMap<>();
				Iterator<String> sIterator = json.keys();
				while (sIterator.hasNext()) {
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
				datasMap.remove("userdata");
				list.add(datasMap);
			}
			long endTime = System.currentTimeMillis();
			System.out.println("--查询综合报表-耗时->>>" + (endTime - startTime) + "ms");
			return ReturnInfoUtils.successDataInfo(list);
		} else {
			return ReturnInfoUtils.errorInfo("暂无支付单报表数据!");
		}
	}

}
