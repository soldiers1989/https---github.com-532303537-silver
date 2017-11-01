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
	public Map<String, Object> checkOrderInfo( String entOrderNo) {
		Map<String, Object> params = new HashMap<>();
	
		params.put("entOrderNo", entOrderNo);
		List<Object> orderList = orderDao.findByProperty(OrderContent.class, params, 0, 0);
		//统计订单总金额
		double orderTotalPrice = 0;
		if(orderList!=null && orderList.size() >0){
			for(int i=0;i<orderList.size();i++){
				OrderContent order = (OrderContent) orderList.get(i);
				orderTotalPrice += order.getOrderTotalPrice();
			}
			params.clear();
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			params.put("orderTotalPrice", orderTotalPrice);
			return params;
		}
		params.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
		params.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		return params;
	}
}
