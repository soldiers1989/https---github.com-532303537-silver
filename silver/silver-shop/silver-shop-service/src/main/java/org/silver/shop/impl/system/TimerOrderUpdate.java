package org.silver.shop.impl.system;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.stereotype.Component;


@Component("timerOrderUpdate")
public class TimerOrderUpdate {

	private Timer timer;

	public void reminder() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				System.out.println("Time's up!");
			}
		}, 3000, 5000);

	}

	public void release() {
		if (timer != null) {
			timer.cancel();
		}
	}
}
