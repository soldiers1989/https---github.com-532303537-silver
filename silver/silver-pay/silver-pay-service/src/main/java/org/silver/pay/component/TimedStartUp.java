package org.silver.pay.component;

import org.silver.pay.util.TimedTaskPayment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class TimedStartUp implements InitializingBean{

	@Autowired
	private TimedTaskPayment TimedTask;

	public void close() {
		TimedTask.release();
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("start--payment--timer");
//		TimedTask.timer();
	}

}
