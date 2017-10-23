package org.silver.sys.util;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.silver.common.GZFtpConfig;
import org.silver.sys.api.CallbackService;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Reference;

@Component("receiptScanning")
public class ReceiptScanning {

	@Reference
	private CallbackService callbackService;
	
	private Timer task;

	public void timer() {
		task = new Timer();
		task.schedule(new TimerTask() {
			public void run() {
				System.out.println("-------指定任务--开始扫描海关回执文件------");
				ReceiptScanningFTP(GZFtpConfig.FTP_ID, GZFtpConfig.FTP_PORT, GZFtpConfig.FTP_USER_NAME_YM,
						GZFtpConfig.FTP_PASS_WORD_YM, "/out/", "", "f:/tools/download", 0);
			}
		}, 5000, 50000);
	}
	/**
	 * 
	 * @param url        FTP服务器连接url
	 * @param port       FTP服务器端口
	 * @param username   登录账号
	 * @param password   密码
	 * @param remotePath FTP服务器的工作目录
	 * @param fileName   指定文件名
	 * @param localPath  文件下载到指定的目录
	 * @param eport      口岸 0电子口岸  1智检
	 */
	public void ReceiptScanningFTP(String url,int port,String username,String password,String remotePath,
			String fileName, String localPath,int eport){
		try {
//			Map<String, Object> map  =FtpUtil.downFile(NSFtpConfig.FTP_ID, 
//					NSFtpConfig.FTP_PORT, 
//					NSFtpConfig.FTP_USER_NAME_YM, 
//					NSFtpConfig.FTP_PASS_WORD_YM, "/4200.IMPBA.SWBEBTRADE.REPORT/out/", "", "f:/tools/download");
			Map<String, Object> map1  =FtpUtil.downFile(url,port,username,password,remotePath,fileName,localPath);
			String path ="";
			List pathList =(List) map1.get("path");
			for (int i = 0; i < pathList.size(); i++) {
				path=(String) pathList.get(i);
				File file =new File(path);
				try {
					Map<String, Object> resultMap=DOMXMLService.getHeadBeanList(file);
					if(resultMap.size()>0){
//						CallbackServiceImpl im= new CallbackServiceImpl();
						switch (eport) {
						case 0:
							if("KJ881101".equals(resultMap.get("OrgMessageType"))){
								callbackService.AsynchronousCallback(resultMap.get("OrgMessageID")+"", 0, resultMap);
//								im.AsynchronousCallback(resultMap.get("OrgMessageID")+"", 0, resultMap);
							}else {
								callbackService.AsynchronousCallback(resultMap.get("OrgMessageID")+"", 1, resultMap);
//								im.AsynchronousCallback(resultMap.get("OrgMessageID")+"", 1, resultMap);
							}
						case 1:
							if("/4200.IMPBA.SWBEBTRADE.REPORT/out/".equals(remotePath)){
								callbackService.AsynchronousCallback(resultMap.get("OrgMessageID")+"", 1, resultMap);
							}
							if("/4200.IMPBA.SWBCARGOBACK.REPORT/out/".equals(remotePath)){
								callbackService.AsynchronousCallback(resultMap.get("OrgMessageID")+"", 0, resultMap);
							}
						}
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		ReceiptScanning rs = new ReceiptScanning();
		rs.timer();
	}
	
	
	
}
