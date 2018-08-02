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
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.justep.baas.data.Row;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

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
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		// 获取前一天的日期
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date dayBefore = calendar.getTime();
		Map<String, Object> params = new HashMap<>();
		// 一天前的日期字符串
		String strDayBefore = DateUtil.formatDate(dayBefore, "yyyy-MM-dd");
		params.put("date", strDayBefore);
		// 查询昨日的订单报表详情
		Table reList = reportsDao.getOrderReportDetail(params);
		if (reList == null) {
			// return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.getRows().isEmpty()) {
			Map<String,Object> reMap = reportsService.oldGetSynthesisReport(Transform.tableToJson(reList).getJSONArray("rows"));
			if(!"1".equals(reMap.get(BaseCode.STATUS.toString()))){
				
			}
			List<Object> reportList = (List<Object>) reMap.get(BaseCode.DATAS.toString());
			for(int i=0;i<reportList.size(); i++){
				System.out.println("--reportList-->>"+reportList.get(i).toString());
			}
			
			params.clear();
			calendar.clear();
			calendar.setTime(new Date());
			calendar.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天 
			calendar.set(Calendar.HOUR_OF_DAY, 00);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			params.put("startDate",  calendar.getTime());
			params.put("endDate", DateUtil.parseDate(strDayBefore, "yyyy-MM-dd"));
			// 查询整月的历史报表信息进行累计
			List<SynthesisReportLog> reReportList = reportsDao.findByPropertyLike(SynthesisReportLog.class, params, null, 0, 0);
			
			
		} else {

		}
		System.out.println("----定时生成报表----");
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
