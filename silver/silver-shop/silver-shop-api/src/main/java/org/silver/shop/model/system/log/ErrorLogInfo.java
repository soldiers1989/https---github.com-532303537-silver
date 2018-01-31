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

	private Date operationTime; // 操作时间

	private String operator; // 操作人

	private String operatorId; // 操作人编号
	
	private String serialNo;// 流水号

	private String remark;//
	
	public long getId() {
		return id;
	}

	public String getAction() {
		return action;
	}

	public String getNote() {
		return note;
	}

	public Date getOperationTime() {
		return operationTime;
	}

	public String getOperator() {
		return operator;
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

	public void setOperationTime(Date operationTime) {
		this.operationTime = operationTime;
	}

	public void setOperator(String operator) {
		this.operator = operator;
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

}
