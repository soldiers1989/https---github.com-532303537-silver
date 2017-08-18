package org.silver.shop.service;


import org.silver.shop.dao.UserDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AopTest {
	 public static void main(String[] args) {  
	        
			ApplicationContext ctx = new ClassPathXmlApplicationContext("benx.xml");  
	        final UserDao config = (UserDao) ctx.getBean("config");  
	        Thread t = new Thread(){
	        	@Override
	        	public void run() {
	        		// TODO Auto-generated method stub
	        		super.run();
	        		   for (int i=0;i<10000;i++){
	        			   System.out.println( config.findAll(1, 5).size()==2);
	     	        	  System.out.println( config.findAllCount()==3);
	     	  	       
	     	        }
	        	}
	        };
	       
	        Thread t2 = new Thread(){
	        	@Override
	        	public void run() {
	        		// TODO Auto-generated method stub
	        		super.run();
	        		   for (int i=0;i<10000;i++){
	        			   System.out.println( config.findAll(1, 5).size()==2);
	     	        	  System.out.println( config.findAllCount()==3);
	     	  	       
	     	        }
	        	}
	        };
	     
	        Thread t3 = new Thread(){
	        	@Override
	        	public void run() {
	        		// TODO Auto-generated method stub
	        		super.run();
	        		   for (int i=0;i<10000;i++){
	     	        	  System.out.println( config.findAllCount()==3);
	     	  	        System.out.println( config.findAll(1, 5).size()==2);
	     	        }
	        	}
	        };
	        
	        Thread t4 = new Thread(){
	          	@Override
	          	public void run() {
	          		// TODO Auto-generated method stub
	          		super.run();
	          		   for (int i=0;i<10000;i++){
	       	        	  System.out.println( config.findAllCount()==3);
	       	  	        System.out.println( config.findAll(1, 5).size()==2);
	       	        }
	          	}
	          };
	          
	          t.start();
	          t2.start();
	          t3.start();
	          t4.start();
	         
	    }  
	   
	
	 
	}  
	  



















	interface HelloWorld {  
	    void sayHelloWorld();  
	    void updateMethod(int a ,int b);
	}  
	@DataSourcesKey("read")
	class HelloWorldImpl implements HelloWorld {  
	  
		@DataSourcesKey("read")
	    public void sayHelloWorld() {  
			
			
	        System.out.println("Hello World!");  
	        //制造异常  
	        String str = null;
	        try{
	        	  str.substring(1);
	        }catch (Exception e) {
	        	System.out.println(e.getCause());
				// TODO: handle exception
			}
	        
	  
	    }

		@Override
		@DataSourcesKey
		public void updateMethod(int a,int b) {
			System.out.println("update!");
			
		}  
}
