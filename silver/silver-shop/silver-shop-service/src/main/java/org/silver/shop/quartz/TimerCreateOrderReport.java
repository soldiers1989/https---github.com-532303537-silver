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
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.impl.system.manual.ManualOrderServiceImpl;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.DateUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.justep.baas.data.Row;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

/**
 * 定时任务生成订单报表数据
 */
public class TimerCreateOrderReport implements InitializingBean {

	private static Logger logger = LogManager.getLogger(Object.class);

	@Autowired
	private OrderDao orderDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("---定时启动-->>>");
		//reminder();
	}

	public void reminder() {
		Timer timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				createOrderReport();
			}
		}, DateUtil.parseDate2("2018-06-14 09:12:00"), 60000);
	}

	public void createOrderReport() {
		System.out.println("---------------");
		int page = 1;
		int size = 50;
		List<Merchant> reMerchantList = orderDao.findByProperty(Merchant.class, null, page, size);
		while (reMerchantList != null && !reMerchantList.isEmpty()) {
			if (page > 1) {
				reMerchantList = orderDao.findByProperty(Merchant.class, null, page, size);
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, -1);
			for (Merchant merchant : reMerchantList) {
				Map<String, Object> params = new HashMap<>();
				params.put("merchantId", merchant.getMerchantId());
				params.put("startDate", DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd"));
				params.put("endDate", DateUtil.formatDate(calendar.getTime(), "yyyy-MM-dd"));
				Table table = orderDao.getOrderDailyReportetDetails(params);
				System.out.println(merchant.getMerchantName()+"--->>"+Transform.tableToJson(table));
				/*if (table == null) {
					logger.error(merchant.getMerchantName() + "--查询报表失败--");
				} else if (!table.getRows().isEmpty()) {
					List<Row> lr = table.getRows();
					for (int i = 0; i < lr.size(); i++) {
						
					}
				}*/
			}
			page++;
			System.out.println("---->>"+page);
		}
		System.out.println("--循环结束-扫描-->>>");
	}
}
