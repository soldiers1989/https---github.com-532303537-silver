package org.silver.shop.component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.silver.shop.impl.system.TimerOrderUpdate;
import org.silver.util.DateUtil;
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
		timedTask.reminder();
	}

	
	
	public static void main(String[] args) {

       double d = 100;
       double fee = 0.006;
       double fengdi = 1; 
       if((d * fee) < fengdi){
    	   fee = fengdi;
    	   System.out.println("----"+fee );
       }
    }
}
