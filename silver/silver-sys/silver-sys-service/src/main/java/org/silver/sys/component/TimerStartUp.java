package org.silver.sys.component;

import org.silver.sys.util.TimedTaskGoodsRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class TimerStartUp implements InitializingBean {
	@Autowired
	private TimedTaskGoodsRecord TimedTask;

	public void close() {
		TimedTask.release();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("start--timer");
//		TimedTask.timer();

	}

}
