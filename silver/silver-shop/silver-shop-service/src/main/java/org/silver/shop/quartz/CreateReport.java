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
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.dao.system.cross.ReportsDao;
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
public class CreateReport  {

	private static Logger logger = LogManager.getLogger(Object.class);
	
	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private ReportsDao reportsDao;
	
	public void createReportJob() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		//获取前一天的日期
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date date = calendar.getTime();
		
		
	}
	
	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date date = calendar.getTime();
		System.out.println("-->>"+DateUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss"));
	}
}
