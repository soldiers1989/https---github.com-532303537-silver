package org.silver.pay.util;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;

import org.silver.common.GZFtpConfig;
import org.silver.common.NSFtpConfig;
import org.silver.pay.dao.PaymentDetailDao;
import org.silver.pay.dao.PaymentHeadDao;
import org.silver.pay.impl.GZPayEportServiceImpl;
import org.silver.pay.impl.ZJPayEportServiceImpl;
import org.silver.pay.model.PaymentDetail;
import org.silver.pay.model.PaymentHead;

import org.springframework.stereotype.Component;

@Component("timedTaskPayment")
public class TimedTaskPayment {

	@Resource
	private PaymentHeadDao paymentHeadDao;
	@Resource
	private PaymentDetailDao paymentDetailDao;
	
	private Timer task;

	public void timer() {
		task = new Timer();
		task.schedule(new TimerTask() {
			public void run() {
				System.out.println("-------设定要指定任务--------");
				findOutFromPaymentRecordToFTP(2);
			}
		}, 5000, 50000);
	}
	
	public void release() {
		if (task != null) {
			task.cancel();
		}
	}
	
	public void findOutFromPaymentRecordToFTP(int status) {
		List<PaymentHead> paymentHeadList = findOutFromPaymentHead(status);
		PaymentHead paymentHead = null;
		boolean bln  ;
		if (paymentHeadList == null) {
			return;
		}
		for (int i = 0; i < paymentHeadList.size(); i++) {
			paymentHead=paymentHeadList.get(i);
			if(paymentHead.getFilePath()!=null){
				if (1 == paymentHead.getEport() && paymentHead.getCount() <= 10) {// 电子口岸
					File file = new File(paymentHead.getFilePath());
					if (file.exists()) {//判断文件是否存在
						bln=paymentRecordToFtp(GZFtpConfig.FTP_ID, GZFtpConfig.FTP_PORT, GZFtpConfig.FTP_USER_NAME_YS,
								GZFtpConfig.FTP_PASS_WORD_YS,"/in/", paymentHead.getFilePath());
						if(bln){//上传成功，更新数据
							updatePaymentHead(paymentHead);
						}else{//上传失败，更新数据
							uploadFailedPaymentHead(paymentHead);
						}
					}else{//当根据路径找不到文件时，就重新生成XML报文再上传
						String orgMessageID = paymentHead.getOrgMessageID();
						gzAccordingToIdProductionDocuments(orgMessageID, paymentHead);
					}
				}
				if (2 == paymentHead.getEport() && paymentHead.getCount() <= 10) {// 智检口岸
					File file = new File(paymentHead.getFilePath());
					if (file.exists()) {//判断文件是否存在
						bln=paymentRecordToFtp(NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT, NSFtpConfig.FTP_USER_NAME_YS,
								NSFtpConfig.FTP_PASS_WORD_YS,NSFtpConfig.FTP_PAY_ROUTE_IN, paymentHead.getFilePath());
						if(bln){
							updatePaymentHead(paymentHead);
						}else{
							uploadFailedPaymentHead(paymentHead);
						}
					}else{//当根据路径找不到文件时，就重新生成XML报文再上传
						String orgMessageID = paymentHead.getOrgMessageID();
						zjAccordingToIdProductionDocuments(orgMessageID, paymentHead);
					}
				}
			}else{
				if (1 == paymentHead.getEport() && paymentHead.getCount() <= 10) {// 电子口岸
					String orgMessageID = paymentHead.getOrgMessageID();
					gzAccordingToIdProductionDocuments(orgMessageID, paymentHead);
				}
				if (2 == paymentHead.getEport() && paymentHead.getCount() <= 10) {// 智检口岸
					String orgMessageID = paymentHead.getOrgMessageID();
					zjAccordingToIdProductionDocuments(orgMessageID, paymentHead);
				}
			}
		}
	
		
	}
	public void gzAccordingToIdProductionDocuments(String orgMessageID,PaymentHead paymentHead){
		boolean bln;
		List<PaymentDetail> paymentDetailList = findOutFromPaymentDetail(orgMessageID);
		GZPayEportServiceImpl impl = new GZPayEportServiceImpl();
		Map<String, Object> statusMap = impl.convertPaymentRecordChangeToXML(paymentHead, paymentDetailList);
		if (1==Integer.valueOf(statusMap.get("status")+"")) {
			bln=paymentRecordToFtp(GZFtpConfig.FTP_ID, GZFtpConfig.FTP_PORT, GZFtpConfig.FTP_USER_NAME_YS,
					GZFtpConfig.FTP_PASS_WORD_YS,"/in/",statusMap.get("path")+"");
			if(bln){//上传成功，更新数据
				updatePaymentHead(paymentHead);
			}else{//上传失败，更新数据
				uploadFailedPaymentHead(paymentHead);
			}
		}
	}
	
	public void zjAccordingToIdProductionDocuments(String orgMessageID,PaymentHead paymentHead){
		List<PaymentDetail> paymentDetailList = findOutFromPaymentDetail(orgMessageID);
		boolean bln;
		ZJPayEportServiceImpl zjimpl= new ZJPayEportServiceImpl();
		Map<String, Object> statusMap =zjimpl.createPayRecordChangeToXML(paymentHead, paymentDetailList);
		if (1==Integer.valueOf(statusMap.get("status")+"")) {
			bln=paymentRecordToFtp(NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT, NSFtpConfig.FTP_USER_NAME_YS,
					NSFtpConfig.FTP_PASS_WORD_YS,NSFtpConfig.FTP_PAY_ROUTE_IN, statusMap.get("path")+"");
			if(bln){
				updatePaymentHead(paymentHead);
			}else{
				uploadFailedPaymentHead(paymentHead);
			}
		}
	}
	
	
	public boolean paymentRecordToFtp(String url, int port, String username, String password, String remotePath,
			String filePath) {
		boolean bl = false;
		File file = new File(filePath);
		try {
			FtpUtil.upload(url, port, username, password, remotePath, file);
			bl = true;
		} catch (Exception e) {
			e.printStackTrace();
			return bl;
		}
		return bl;
	}

	public void uploadFailedPaymentHead(PaymentHead paymentHead) {
		paymentHead.setCount(paymentHead.getCount() + 1);
		paymentHead.setUpdate_date(new Date());
		paymentHead.setUpdate_by("System");
		paymentHead.setRemarks("重发失败" + paymentHead.getCount() + 1);
		try {
			paymentHeadDao.update(paymentHead);
			System.out.println("上传失败，修改重发次数");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 重发成功，更新数据
	public void updatePaymentHead(PaymentHead paymentHead) {
		paymentHead.setStatus(1);
		paymentHead.setCount(paymentHead.getCount() + 1);
		paymentHead.setUpdate_date(new Date());
		paymentHead.setUpdate_by("System");
		paymentHead.setRemarks("重发成功");
		try {
			paymentHeadDao.update(paymentHead);
			System.out.println("修改状态成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 查出发送失败的商品备案
	public List<PaymentHead> findOutFromPaymentHead(int status) {
		Map<String, Object> params = new HashMap<>();
		params.put("del_flag", 0);
		params.put("status", status);// 状态 0未发送 1已发送 2发送失败 3已被接收成功 4（已接收回执）完成
		List<PaymentHead> paymentHeadList = paymentHeadDao.findByProperty(params, 0, 0);
		if (paymentHeadList.size() > 0) {
			return paymentHeadList;
		}
		return null;
	}

	// 根据MessageID查出关联的商品
	public List<PaymentDetail> findOutFromPaymentDetail(String orgMessageID) {
		Map<String, Object> params = new HashMap<>();
		params.put("OrgMessageID", orgMessageID);
		List<PaymentDetail> paymentDetailList = paymentDetailDao.findByProperty(params, 0, 0);
		if (paymentDetailList.size() > 0) {
			return paymentDetailList;
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
