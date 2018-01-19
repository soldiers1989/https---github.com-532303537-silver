package org.silver.shop.controller.system.manual;

import java.io.File;
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

	// 删除指定文件夹下所有文件  
    // param path 文件夹完整绝对路径  
    public static boolean delAllFile(String path) {  
        boolean flag = false;  
        File file = new File(path);  
        if (!file.exists()) {  
            return flag;  
        }  
        if (!file.isDirectory()) {  
            return flag;  
        }  
        String[] tempList = file.list();  
        File temp = null;  
        for (int i = 0; i < tempList.length; i++) {  
            if (path.endsWith(File.separator)) {  
                temp = new File(path + tempList[i]);  
            } else {  
                temp = new File(path + File.separator + tempList[i]);  
            }  
            if (temp.isFile()) {  
                temp.delete();  
            }  
            if (temp.isDirectory()) {  
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件  
//              delFolder(path + "/" + tempList[i]);// 再删除空文件夹  
                flag = true;  
            }  
        }  
        return flag;  
    }  
	public static void main(String[] args) {
		  String fileRoot = "E:/gadd-excel";  
		  delAllFile(fileRoot);  
	            System.out.println("deleted");  
	}
}
