package org.silver.shop.controller.common;

import java.util.HashMap;
import java.util.Map;

import org.silver.util.YmHttpUtil;

public class Test extends Thread {
	@Override
	public void run() {
		for(int i =0 ; i <500; i++){
			Map params = new HashMap();
			params.put("recipientAddr", "内蒙古自治区 阿拉善盟 阿拉善左旗 内蒙古自治区阿拉善盟阿拉善左旗巴镇怡和小区西门对面盛世金桥公司财务部");
			String result = YmHttpUtil.HttpPost("https://ym.191ec.com/silver-web-shop/manual/readInfo2", params);
			System.out.println(Thread.currentThread().getName()+"--第"+i+"-次>>"+result);
			
		}
	}

	public static void main(String[] args) {
		Test station1 = new Test();
		Test station2 = new Test();
		Test station3 = new Test();
		station1.start();
		station2.start();
		station3.start();
	}

}
