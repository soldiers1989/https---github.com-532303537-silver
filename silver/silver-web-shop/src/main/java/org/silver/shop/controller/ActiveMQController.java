package org.silver.shop.controller;

import java.util.Calendar;
import java.util.Date;

import org.silver.shop.mq.ShopQueueSender;
import org.silver.util.DateUtil;
import org.silver.util.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/MQ")
public class ActiveMQController {

	@Autowired
	private ShopQueueSender QueueSender;

	@RequestMapping(value = "/send")
	@ResponseBody
	public String send(String queueName, String message, String type) {
		try {
			if (type == null) {
				QueueSender.send(queueName, message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "net err";
		}
		return message;
	}

	public static void main(String[] args) {
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MINUTE, -20);
		System.out.println(DateUtil.formatDate(c.getTime(), "yyyy-MM-dd HH:mm:ss"));
		int i = 5;
		if(i >5){
			System.out.println("-------------");
		}else{
			System.out.println("=======");
		}
	}
}
