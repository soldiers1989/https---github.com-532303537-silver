package org.silver.shop.component;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.DateUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 定时任务生成订单报表数据
 */
public class TimerCreateOrderReport implements InitializingBean{

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
				//resendPayment();
				test();
			}
		}, DateUtil.parseDate2("2018-06-13 16:30:00"), 6000);
	}	
	
	public void test(){
		int page = 1;
		int size = 50;
		List<Merchant> reMerchantList = orderDao.findByProperty(Merchant.class, null,page, size);
		while (reMerchantList!=null && !reMerchantList.isEmpty()) {
			if(page > 1){
				reMerchantList = orderDao.findByProperty(Merchant.class, null,page, size);
			}
			for(Merchant merchant : reMerchantList){
				
			}
		}
		System.out.println("---扫描启动-->>>");
	}
}
