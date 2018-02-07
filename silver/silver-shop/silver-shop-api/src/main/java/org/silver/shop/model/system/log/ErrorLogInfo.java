package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于记录商户操作的错误信息记录
 *
 */
public class ErrorLogInfo implements Serializable {

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
	
	private int type ;//类型：1-错误,2-警告订单超额,3-详细地址信息错误,4-身份证校验不通过.....待续
	
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

	public int getType() {
		return type;
	}

	public int getReadingSign() {
		return readingSign;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setReadingSign(int readingSign) {
		this.readingSign = readingSign;
	}

}
