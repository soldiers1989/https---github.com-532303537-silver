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
import org.silver.sys.dao.OrderGoodsDao;
import org.silver.sys.dao.OrderHeadDao;
import org.silver.sys.dao.OrderRecordDao;
import org.silver.sys.impl.GZEportServiceImpl;
import org.silver.sys.impl.ZJEportServiceImpl;
import org.silver.sys.model.order.OrderGoods;
import org.silver.sys.model.order.OrderHead;
import org.silver.sys.model.order.OrderRecord;
import org.springframework.stereotype.Component;

@Component("timedTaskOrderRecord")
public class TimedTaskOrderRecord {

	@Resource
	private OrderHeadDao orderHeadDao;
	@Resource
	private OrderRecordDao orderRecordDao;
	@Resource
	private OrderGoodsDao orderGoodsDao;

	private Timer task;

	public void timer() {
		task = new Timer();
		task.schedule(new TimerTask() {
			public void run() {
				System.out.println("-------设定要指定任务--------");
				findOutFromOrderRecordToFTP("2");
			}
		}, 5000, 50000);
	}

	public void release() {
		if (task != null) {
			task.cancel();
		}
	}

	public void findOutFromOrderRecordToFTP(String status) {
		List<OrderHead> orderHeadList = findOutFromOrderHead(status);
		OrderHead orderHead = null;
		boolean bln;
		if (orderHeadList == null) {
			return;
		}
		for (int i = 0; i < orderHeadList.size(); i++) {
			// 拿到需要重发的商品备案订单
			orderHead = orderHeadList.get(i);
			if (orderHead.getFilePath() != null) {// 判断文件路径不为空时
				if (1 == orderHead.getEport() && orderHead.getCount() <= 10) {// 电子口岸
					File file = new File(orderHead.getFilePath());
					if (file.exists()) {// 当根据文件路径，能找到文件
						bln = findOutFromOrderRecordToFtp(GZFtpConfig.FTP_ID, GZFtpConfig.FTP_PORT,
								GZFtpConfig.FTP_USER_NAME_YM, GZFtpConfig.FTP_PASS_WORD_YM, "/in/",
								orderHead.getFilePath());
						if (bln) {
							updateOrderRecord(orderHead);
						} else {
							uploadFailedOrderRecord(orderHead);
						}
					} else {// 根据文件路径，找不到文件
						String orgMessageID =orderHead.getOrgMessageID();
						gzFindOrderByMessageID(orgMessageID, orderHead);
					}
				}
				if (2 == orderHead.getEport() && orderHead.getCount() <= 10) {// 智检口岸
					File file = new File(orderHead.getFilePath());
					if (file.exists()) {// 当根据文件路径，能找到文件
						bln = findOutFromOrderRecordToFtp(NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT,
								NSFtpConfig.FTP_USER_NAME_YM, NSFtpConfig.FTP_PASS_WORD_YM,
								NSFtpConfig.FTP_ORDER_ROUTE_IN,orderHead.getFilePath());
						if (bln) {
							updateOrderRecord(orderHead);
						} else {
							uploadFailedOrderRecord(orderHead);
						}
					} else {// 根据文件路径，找不到文件
						String orgMessageID =orderHead.getOrgMessageID();
						zjFindOrderByMessageID(orgMessageID, orderHead);
					}
					
				}
			} else {// 当文件路径为空时
				if (1 == orderHead.getEport() && orderHead.getCount() <= 10) {// 电子口岸
					String orgMessageID =orderHead.getOrgMessageID();
					gzFindOrderByMessageID(orgMessageID, orderHead);
				}
				if (2 == orderHead.getEport() && orderHead.getCount() <= 10) {// 智检口岸
					String orgMessageID =orderHead.getOrgMessageID();
					zjFindOrderByMessageID(orgMessageID, orderHead);
				}
			}

		}
	}
	
	public void gzFindOrderByMessageID(String orgMessageID,OrderHead orderHead){
		boolean bln;
		List<OrderRecord> orderRecordList = findOutFromOrderRecord(orgMessageID);
		List<OrderGoods> orderGoodsList = findOutFromOrderGoods(orgMessageID);
		GZEportServiceImpl impl = new GZEportServiceImpl();
		Map<String, Object> statusMap = impl.convertOrderRecordIntoXML(orderHead, orderRecordList, orderGoodsList);
		if (1 == Integer.valueOf(statusMap.get("status") + "")) {
			bln = findOutFromOrderRecordToFtp(GZFtpConfig.FTP_ID, GZFtpConfig.FTP_PORT,
					GZFtpConfig.FTP_USER_NAME_YM, GZFtpConfig.FTP_PASS_WORD_YM, "/in/",
					statusMap.get("path")+"");
			if (bln) {
				updateOrderRecord(orderHead);
			} else {
				uploadFailedOrderRecord(orderHead);
			}
		}
	}
	public void zjFindOrderByMessageID(String orgMessageID,OrderHead orderHead){
		boolean bln;
		List<OrderRecord> orderRecordList = findOutFromOrderRecord(orgMessageID);
		List<OrderGoods> orderGoodsList = findOutFromOrderGoods(orgMessageID);
		ZJEportServiceImpl impl = new ZJEportServiceImpl();
		Map<String, Object> statusMap = impl.zjCreateOrderRecordXML(orderHead, orderRecordList, orderGoodsList);
		if (1 == Integer.valueOf(statusMap.get("status") + "")) {
			bln = findOutFromOrderRecordToFtp(NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT,
					NSFtpConfig.FTP_USER_NAME_YM, NSFtpConfig.FTP_PASS_WORD_YM,
					NSFtpConfig.FTP_ORDER_ROUTE_IN,statusMap.get("path") + "");
			if (bln) {
				updateOrderRecord(orderHead);
			} else {
				uploadFailedOrderRecord(orderHead);
			}
		}
	}

	// 查出发送失败的订单备案
	public List<OrderHead> findOutFromOrderHead(String status) {
		Map<String, Object> params = new HashMap<>();
		params.put("del_flag", 0);
		params.put("status", status);// 状态 0未发送 1已发送 2发送失败 3已被接收成功 4（已接收回执）完成
		List<OrderHead> orderHeadList = orderHeadDao.findByProperty(params, 0, 0);
		if (orderHeadList.size() > 0) {
			return orderHeadList;
		}
		return null;
	}

	public List<OrderRecord> findOutFromOrderRecord(String orgMessageID) {
		Map<String, Object> params = new HashMap<>();
		params.put("del_flag", 0);
		params.put("OrgMessageID", orgMessageID);
		List<OrderRecord> orderRecordList = orderRecordDao.findByProperty(params, 0, 0);
		if (orderRecordList.size() > 0) {
			return orderRecordList;
		}
		return null;
	}

	public List<OrderGoods> findOutFromOrderGoods(String orgMessageID) {
		Map<String, Object> params = new HashMap<>();
		params.put("OrgMessageID", orgMessageID);
		List<OrderGoods> orderGoodsList = orderGoodsDao.findByProperty(params, 0, 0);
		if (orderGoodsList.size() > 0) {
			return orderGoodsList;
		}
		return null;
	}

	public boolean findOutFromOrderRecordToFtp(String url, int port, String username, String password,
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

	public void uploadFailedOrderRecord(OrderHead orderHead) {
		orderHead.setCount(orderHead.getCount() + 1);
		orderHead.setUpdate_date(new Date());
		orderHead.setUpdate_by("System");
		orderHead.setRemarks("重发失败" + orderHead.getCount() + 1);
		try {
			orderHeadDao.update(orderHead);
			System.out.println("上传失败，修改重发次数");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 重发成功，更新数据
	public void updateOrderRecord(OrderHead orderHead) {
		orderHead.setStatus(1);
		orderHead.setCount(orderHead.getCount() + 1);
		orderHead.setUpdate_date(new Date());
		orderHead.setUpdate_by("System");
		orderHead.setRemarks("重发成功");
		try {
			orderHeadDao.update(orderHead);
			System.out.println("修改状态成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
