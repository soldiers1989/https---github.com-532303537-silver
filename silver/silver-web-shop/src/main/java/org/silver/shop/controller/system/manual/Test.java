package org.silver.shop.controller.system.manual;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

import org.silver.util.MD5;

public class Test extends Thread {

	// 通过构造方法给线程名字赋值
	public Test(String name) {
		super(name);// 给线程名字赋值
	}

	// 为了保持票数的一致，票数要静态
	// static int tick = 20;

	// 创建一个静态钥匙
	static Object ob = "aa";// 值是任意的
	static AtomicInteger  tick2 = new AtomicInteger(100);
	static int tick = 20;

	// 重写run方法，实现买票操作
	@Override
	public void run() {

		while (tick > 0) {
			//synchronized (ob) {// 这个很重要，必须使用一个锁，
				// 进去的人会把钥匙拿在手上，出来后才把钥匙拿让出来
				if (tick > 0) {
					System.out.println(getName() + "卖出了第" + tick + "张票");
					tick--;
					// tick.getAndDecrement();
				} else {
					System.out.println("票卖完了");
				}
		//	}
			try {
				sleep(1000);// 休息一秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {
		// 实例化站台对象，并为每一个站台取名字
		/*Test station1 = new Test("窗口1");
		Test station2 = new Test("窗口2");
		Test station3 = new Test("窗口3");

		// 让每一个站台对象各自开始工作
		station1.start();
		station2.start();
		station3.start();*/
		
		String time = "1515392647125";
		String appkey="cf285ec49c724019a297c0dc6941df5d";
		String acessToken = "jbTEF04KL350s0bCOmGL8q8p1dawGXHTPq9SKKk8l5gzAuWbmdYqjhdpgyyHnPco";
		String merchantId ="YM_MerchantId_00030";
		String orderId = "GAC_20170003615105451078091831";
		Double amount = 0.02;
		String notify_url = "www.google.cn";
		String str = "YM_MerchantId_00030GAC_201700036151054510780918310.02www.google.cn1515392647125";
		String client_sign="";
		//YM_MerchantId_00030GAC_201700036151054510780918310.02www.google.cn1515392647125
		try {
			System.out.println(MD5.getMD5(( appkey+ acessToken + str + notify_url + time).getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
	}
}
