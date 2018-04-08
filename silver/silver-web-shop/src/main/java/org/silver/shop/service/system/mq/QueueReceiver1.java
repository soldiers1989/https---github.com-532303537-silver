package org.silver.shop.service.system.mq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.stereotype.Component;

@Component("queueReceiver1")
public class QueueReceiver1 implements MessageListener{
    int count=0;    
	@Override
	public void onMessage(Message message) {
		 TextMessage textmessage = (TextMessage)message;
		try {
			count++;
			System.out.println(Thread.currentThread().getName()+textmessage.getText()+"***"+count);
			
		} catch (JMSException e) {
			
			e.printStackTrace();
		}
		
	}

}
