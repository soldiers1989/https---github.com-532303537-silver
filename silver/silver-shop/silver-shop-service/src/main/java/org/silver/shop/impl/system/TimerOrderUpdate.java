package org.silver.shop.impl.system;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import org.silver.common.StatusCode;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.impl.system.commerce.GoodsRecordServiceImpl;
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.OrderGoodsContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("timerOrderUpdate")
public class TimerOrderUpdate {

	private static final Logger logger = LogManager.getLogger(GoodsRecordServiceImpl.class);

	@Autowired
	private OrderDao orderDao;

	private Timer timer;

	public void reminder() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				updateOrderInfo();
			}
			//30分钟启动一次
		}, 3000, 60000 * 3);
	}

	private final Map<String, Object> updateOrderInfo() {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Date nowDate = new Date(); // 当前时间
		Calendar calendar = Calendar.getInstance(); // 得到日历
		calendar.setTime(nowDate);// 把当前时间赋给日历
		calendar.add(Calendar.MINUTE, -30); // 设置30分钟之前
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(calendar.getTime()); // 设置时间格式
		paramMap.put("status", 1);
		List<Object> reOrderList = orderDao.searchTimOutOrder(OrderContent.class, paramMap, time);
		if (reOrderList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		} else if (!reOrderList.isEmpty()) {
			for (int i = 0; i < reOrderList.size(); i++) {
				OrderContent orderInfo = (OrderContent) reOrderList.get(i);
				String orderId = orderInfo.getEntOrderNo();
				// 搜索订单下的商品信息,并更新库存数量
				searchOrderGoodsAndUpdateStock(orderId);
				orderInfo.setStatus(4);
				if(!orderDao.update(orderInfo)){
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
					return statusMap;
				}
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return null;
	}

	private final Map<String, Object> searchOrderGoodsAndUpdateStock(String orderId) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("entOrderNo", orderId);
		List<Object> reOrderGoodsList = orderDao.findByProperty(OrderGoodsContent.class, paramMap, 0, 0);
		if (reOrderGoodsList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
		} else if (!reOrderGoodsList.isEmpty()) {
			for (int i = 0; i < reOrderGoodsList.size(); i++) {
				OrderGoodsContent orderGoodsInfo = (OrderGoodsContent) reOrderGoodsList.get(i);
				String goodsId = orderGoodsInfo.getEntGoodsNo();
				paramMap.clear();
				paramMap.put("entGoodsNo", goodsId);
				List<Object> reStockList = orderDao.findByProperty(StockContent.class, paramMap, 1, 1);
				int goodsCount = Integer.parseInt(Long.toString(orderGoodsInfo.getGoodsCount()));
				if (reStockList != null && !reStockList.isEmpty()) {
					StockContent stockInfo = (StockContent) reStockList.get(0);
					// 数据库中旧的待支付数量
					int oldPaymentCount = stockInfo.getPaymentCount();
					int oldSellCount = stockInfo.getSellCount();
					stockInfo.setPaymentCount(oldPaymentCount -goodsCount );
					stockInfo.setSellCount(oldSellCount + goodsCount);
					if(!orderDao.update(stockInfo)){
						statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
						return statusMap;
					}
				}
			}
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return null;
	}

	public void release() {
		if (timer != null) {
			timer.cancel();
		}
	}
}
