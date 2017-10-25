package org.silver.shop.impl.system.cross;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.system.cross.YsPayReceiveService;
import org.silver.shop.dao.system.cross.YsPayReceiveDao;
import org.silver.shop.impl.system.commerce.GoodsRecordServiceImpl;
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.OrderRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = YsPayReceiveService.class)
public class YsPayReceiveServiceImpl implements YsPayReceiveService {
	protected static final Logger logger = LogManager.getLogger();

	@Autowired
	private YsPayReceiveDao ysPayReceiveDao;

	@Override
	public Map<String, Object> ysPayReceive(Map<String, Object> datasMap) {
		Map<String, Object> params = new HashMap<>();
		String reOrderId = datasMap.get("out_trade_no") + "";
		params.put("orderId", reOrderId);
		List<Object> orderList = ysPayReceiveDao.findByProperty(OrderContent.class, params, 1, 1);
		if (orderList != null && orderList.size() > 0) {
			updateOrderInfo(orderList, datasMap);
		} else {

		}
		return null;
	}

	// 更新订单流水号
	private Map<String, Object> updateOrderInfo(List<Object> orderList, Map<String, Object> datasMap) {
		Map<String, Object> stautsMap = new HashMap<>();
		Map<String,Object> params = new HashMap<>();
		
		OrderRecord orderRecord = new OrderRecord();
		OrderContent orderInfo = (OrderContent) orderList.get(0);
		String merchantId = orderInfo.getMerchantId();
		String orderId = orderInfo.getGoodsId();
		
		params.put("orderId", orderId);
		
		GoodsRecordServiceImpl impl = new GoodsRecordServiceImpl();
		/*impl.getMerchantInfo(merchantId, eport);*/
		
		return null;
	}
}
