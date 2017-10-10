package org.silver.sys.util;

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
import org.silver.sys.dao.GoodsInfoDao;
import org.silver.sys.dao.GoodsRecordDao;
import org.silver.sys.dao.OrderGoodsDao;
import org.silver.sys.dao.OrderHeadDao;
import org.silver.sys.dao.OrderRecordDao;
import org.silver.sys.impl.GZEportServiceImpl;
import org.silver.sys.impl.ZJEportServiceImpl;
import org.silver.sys.model.goods.GoodsInfo;
import org.silver.sys.model.goods.GoodsRecord;
import org.silver.sys.model.order.OrderGoods;
import org.silver.sys.model.order.OrderHead;
import org.silver.sys.model.order.OrderRecord;
import org.springframework.stereotype.Component;

@Component("timedTaskGoodsRecord")
public class TimedTaskGoodsRecord {

	@Resource
	private GoodsRecordDao goodsRecordDao;
	@Resource
	private GoodsInfoDao goodsInfoDao;
	

	private Timer task;

	public void timer() {
		task = new Timer();
		task.schedule(new TimerTask() {
			public void run() {
				System.out.println("-------设定要指定任务--------");
				findOutFromGoodsRecordToFTP("2");
			}
		}, 5000, 50000);
	}

	public void findOutFromGoodsRecordToFTP(String status) {
		List<GoodsRecord> goodsRecordList = findOutFromGoodsRecord(status);
		GoodsRecord goodsRecord = null;
		boolean bln;
		if (goodsRecordList == null) {
			return;
		}
		for (int i = 0; i < goodsRecordList.size(); i++) {
			// 拿到需要重发的商品备案订单
			goodsRecord = goodsRecordList.get(i);
			if (goodsRecord.getFilePath() != null) {// 判断文件路径不为空时
				if (1 == goodsRecord.getEport() && goodsRecord.getCount() <= 10) {// 电子口岸
					File file = new File(goodsRecord.getFilePath());
					if (file.exists()) {// 判断文件是否存在
						bln=findOutFromGoodsRecordToFtp(GZFtpConfig.FTP_ID, GZFtpConfig.FTP_PORT,
								GZFtpConfig.FTP_USER_NAME_YM, GZFtpConfig.FTP_PASS_WORD_YM, "/in/",
								goodsRecord.getFilePath());
						if(bln){
							updateGoodsRecord(goodsRecord);
						}else{
							uploadFailedGoodsRecord(goodsRecord);
						}
					} else {
						String orgMessageID = goodsRecord.getOrgMessageID();
						gzAccordingToIdProductionDocuments(orgMessageID, goodsRecord);
					}
				}
				if (2 == goodsRecord.getEport() && goodsRecord.getCount() < 10) {// 智检口岸
					File file = new File(goodsRecord.getFilePath());
					if (file.exists()) {// 判断文件是否存在
						bln=findOutFromGoodsRecordToFtp(NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT, NSFtpConfig.FTP_USER_NAME_YM,
								NSFtpConfig.FTP_PASS_WORD_YM, NSFtpConfig.FTP_GOODS_ROUTE_IN, goodsRecord.getFilePath());
						if(bln){
							updateGoodsRecord(goodsRecord);
						}else{
							uploadFailedGoodsRecord(goodsRecord);
						}
					}else{
						String orgMessageID = goodsRecord.getOrgMessageID();
						zjAccordingToIdProductionDocuments(orgMessageID, goodsRecord);
					}
				}
			} else {// 当文件路径为空时
				if (goodsRecord.getEport() == 1 && goodsRecord.getCount() <= 10) {// 判断口岸
					String orgMessageID = goodsRecord.getOrgMessageID();
					gzAccordingToIdProductionDocuments(orgMessageID, goodsRecord);
				}
				if (2 == goodsRecord.getEport() && goodsRecord.getCount() < 10) {
					String orgMessageID = goodsRecord.getOrgMessageID();
					zjAccordingToIdProductionDocuments(orgMessageID, goodsRecord);
				}
			}
		}

	}
	public void gzAccordingToIdProductionDocuments(String orgMessageID,GoodsRecord goodsRecord){
		boolean bln;
		List<GoodsInfo> goodsInfoList = findOutFromGoodsInfo(orgMessageID);
		GZEportServiceImpl impl = new GZEportServiceImpl();
		Map<String, Object> statusMap = impl.convertGoodsRecordIntoXML(goodsRecord, goodsInfoList);
		if (1 == Integer.valueOf(statusMap.get("status") + "")) {
			bln=findOutFromGoodsRecordToFtp(GZFtpConfig.FTP_ID, GZFtpConfig.FTP_PORT,
					GZFtpConfig.FTP_USER_NAME_YM, GZFtpConfig.FTP_PASS_WORD_YM, "/in/",
					statusMap.get("path") + "");
		    if(bln){
		    	updateGoodsRecord(goodsRecord);
		    }else{
		    	uploadFailedGoodsRecord(goodsRecord);
		    }
		}
	}
	
	public void zjAccordingToIdProductionDocuments(String orgMessageID,GoodsRecord goodsRecord){
		boolean bln;
		List<GoodsInfo> goodsInfoList = findOutFromGoodsInfo(orgMessageID);
		ZJEportServiceImpl impl = new ZJEportServiceImpl();
		Map<String, Object> statusMap = impl.zjCreateGoodsRecordXML(goodsRecord, goodsInfoList, "A");
		System.out.println("重新生成报文成功！" + statusMap.get("status"));
		if (Integer.valueOf(statusMap.get("status") + "") == 1) {
			bln=findOutFromGoodsRecordToFtp(NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT,
					NSFtpConfig.FTP_USER_NAME_YM, NSFtpConfig.FTP_PASS_WORD_YM,
					NSFtpConfig.FTP_GOODS_ROUTE_IN, statusMap.get("path") + "");
			if(bln){
				updateGoodsRecord(goodsRecord);
			}else{
				uploadFailedGoodsRecord(goodsRecord);
			}	
			
		}
	}

	public boolean findOutFromGoodsRecordToFtp(String url, int port, String username, String password,
			String remotePath, String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			try {
				FtpUtil.upload(url, port, username, password, remotePath, file);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public void uploadFailedGoodsRecord(GoodsRecord goodsRecord) {
		goodsRecord.setCount(goodsRecord.getCount() + 1);
		goodsRecord.setUpdate_date(new Date());
		goodsRecord.setUpdate_by("System");
		goodsRecord.setRemarks("重发失败" + goodsRecord.getCount() + 1);
		try {
			goodsRecordDao.update(goodsRecord);
			System.out.println("上传失败，修改重发次数");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 重发成功，更新数据
	public void updateGoodsRecord(GoodsRecord goodsRecord) {
		goodsRecord.setStatus(1);
		goodsRecord.setCount(goodsRecord.getCount() + 1);
		goodsRecord.setUpdate_date(new Date());
		goodsRecord.setUpdate_by("System");
		goodsRecord.setRemarks("重发成功");
		try {
			goodsRecordDao.update(goodsRecord);
			System.out.println("修改状态成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 查出发送失败的商品备案
	public List<GoodsRecord> findOutFromGoodsRecord(String status) {
		Map<String, Object> params = new HashMap<>();
		params.put("del_flag", 0);
		params.put("status", status);// 状态 0未发送 1已发送 2发送失败 3已被接收成功 4（已接收回执）完成
		List<GoodsRecord> goodsList = goodsRecordDao.findByProperty(params, 0, 0);
		if (goodsList.size() > 0) {
			return goodsList;
		}
		return null;
	}

	// 根据MessageID查出关联的商品
	public List<GoodsInfo> findOutFromGoodsInfo(String orgMessageID) {
		Map<String, Object> params = new HashMap<>();
		params.put("OrgMessageID", orgMessageID);
		List<GoodsInfo> goodsInfoList = goodsInfoDao.findByProperty(params, 0, 0);
		if (goodsInfoList.size() > 0) {
			return goodsInfoList;
		}
		return null;
	}

	

	public void release() {
		if (task != null) {
			task.cancel();
		}
	}

	public static void main(String[] args) {

		TimedTaskGoodsRecord obj = new TimedTaskGoodsRecord();
		obj.timer();

	}
}
