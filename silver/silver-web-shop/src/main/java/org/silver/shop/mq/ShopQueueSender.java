package org.silver.shop.mq;

import java.io.File;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;  
import javax.jms.Message;  
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;  
import org.springframework.jms.core.MessageCreator;  
import org.springframework.stereotype.Component;  
  

/**
 * 商城自用MQ消息队列发起类
 */
@Component("shopQueueSender")  
public class ShopQueueSender {  
  
   @Autowired  
   @Qualifier("jmsQueueTemplate")  
   private JmsTemplate jmsTemplate;  
     
   /** 
    * Description: <br/> 
    * 发送消息到指定的队列(目标) 
    * @param queueName  队列名称 
    * @param message    消息内容 
    */  
   public void send(String queueName, final String message){  
       jmsTemplate.send(queueName, new MessageCreator() {  
           public Message createMessage(Session session) throws JMSException {
               return session.createTextMessage(message);  
           }  
       });  
   } 
   
   public void sendFile(String queueName, final String message){  
	   jmsTemplate.send(queueName, new MessageCreator() {
		@Override
		public Message createMessage(Session arg0) throws JMSException {
			
			File f = new File("C://Users//Administrator//Desktop//学生注册接口文档.txt");
			ActiveMQObjectMessage msg = (ActiveMQObjectMessage) arg0.createObjectMessage();  
	        msg.setObject(f);  
			return msg;
			
		}
	});
   } 
   
   public static void main(String[] args) {
	   ApplicationContext context = new ClassPathXmlApplicationContext("mq.xml");
	   ConnectionFactory connectionFactory =(ConnectionFactory) context.getBean("connectionFactory");
	   JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
	   jmsTemplate.send("test.queue", new MessageCreator(){

		@Override
		public Message createMessage(Session arg0) throws JMSException {
			// TODO Auto-generated method stub
			return arg0.createTextMessage("6703");  
		}
		   
	   });
	  
	  
	   
	  
}
}  

