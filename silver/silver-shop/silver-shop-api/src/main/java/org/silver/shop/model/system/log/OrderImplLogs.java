package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于记录商户(订单导入)操作的错误信息记录
 *
 */
public class OrderImplLogs implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;

	private String action; // 动作orderPush orderRecord paymentIn paymentRecord

	private String note; // 操作说明

	private Date createDate; // 操作时间

	private String createBy; // 操作人

	private String operatorId; // 操作人编号
	
	private String serialNo;// 流水号

	private String remark;//
	
	private String type ;//类型: error-错误,orderExcess-订单超额,address-地址信息,idCard-身份证,overweight-超重,phone-手机号码,member-会员信息..待续
	
	private int readingSign;//阅读标识:1-未阅读,2-已阅读
	
	public long getId() {
		return id;
	}

	public String getAction() {
		return action;
	}

	public String getNote() {
		return note;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setNote(String note) {
		this.note = note;
	}


	public Date getCreateDate() {
		return createDate;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getReadingSign() {
		return readingSign;
	}
	public void setReadingSign(int readingSign) {
		this.readingSign = readingSign;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
