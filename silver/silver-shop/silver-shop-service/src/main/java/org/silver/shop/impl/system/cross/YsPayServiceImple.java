package org.silver.shop.impl.system.cross;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.commerce.OrderService;
import org.silver.shop.api.system.cross.YsPayService;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.commerce.OrderContent;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = YsPayService.class)
public class YsPayServiceImple implements YsPayService {

	@Autowired
	private OrderDao orderDao;

	@Override
	public Map<String, Object> checkOrderInfo(String memberId, String orderId) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("orderId", orderId);
		List<Object> orderList = orderDao.findByProperty(OrderContent.class, params, 1, 1);
		if(orderList!=null && orderList.size() >0){
			OrderContent order = (OrderContent) orderList.get(0);
			params.clear();
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			params.put("orderTotalPrice", order.getTotalPrice());
			params.put("orderId", order.getOrderId());
			return params;
		}
		params.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
		params.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		return params;
	}

}
