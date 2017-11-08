package org.silver.shop.component;

import org.silver.shop.impl.system.TimerOrderUpdate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class TimerStartUp implements InitializingBean {
	@Autowired
	private TimerOrderUpdate timedTask;

	public void close() {
		timedTask.release();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("start--timer------");
		//timedTask.reminder();

	}

}
