package org.silver.pay.model;

import java.io.Serializable;
import java.util.Date;
/**
 * 2017/9/15
 * @author zhangxin
 *
 */
public class PaymentDetail implements Serializable{

	private static final long serialVersionUID = 1L;
	private long id;
	private String OrgMessageID;        //报文编号
	private String EntPayNo;            //支付交易编号
	private String PayStatus;           //支付交易状态
	private double PayAmount;      //订单商品总额
	private String PayCurrCode;// 支付币制
	private String PayTime;// 付款时间 格式YYYYMMDDhhmmss
	private String PayerName;// 支付人姓名
	private String PayerDocumentType;// 支付人证件类型:01:身份证,02:护照,04:其他
	private String PayerDocumentNumber;// 支付人证件号码
	private String PayerPhoneNumber;// 支付人手机号
	private String EntOrderNo;// 电子订单编号
	private String EBPEntNo;// 电商平台企业编码
	private String EBPEntName;// 电商平台企业名称
	private String Notes;// 备注
	
	private String status;// 发送状态  0未发送  1已发送   2发送失败   3已被接收成功   4（已接收回执）完成
	private int count;//重发计数      重发次数不能超过5次
	private int del_flag;// 0正常 1删除
	private Date create_date; // 创建时间
	private String create_by; // 创建人
	private Date update_date; // 更新时间
	private String update_by;// 更新人
	private String remarks;// 备注
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getOrgMessageID() {
		return OrgMessageID;
	}
	public void setOrgMessageID(String orgMessageID) {
		OrgMessageID = orgMessageID;
	}
	public String getEntPayNo() {
		return EntPayNo;
	}
	public void setEntPayNo(String entPayNo) {
		EntPayNo = entPayNo;
	}
	public String getPayStatus() {
		return PayStatus;
	}
	public void setPayStatus(String payStatus) {
		PayStatus = payStatus;
	}
	public double getPayAmount() {
		return PayAmount;
	}
	public void setPayAmount(double payAmount) {
		PayAmount = payAmount;
	}
	public String getPayCurrCode() {
		return PayCurrCode;
	}
	public void setPayCurrCode(String payCurrCode) {
		PayCurrCode = payCurrCode;
	}
	public String getPayTime() {
		return PayTime;
	}
	public void setPayTime(String payTime) {
		PayTime = payTime;
	}
	public String getPayerName() {
		return PayerName;
	}
	public void setPayerName(String payerName) {
		PayerName = payerName;
	}
	public String getPayerDocumentType() {
		return PayerDocumentType;
	}
	public void setPayerDocumentType(String payerDocumentType) {
		PayerDocumentType = payerDocumentType;
	}
	public String getPayerDocumentNumber() {
		return PayerDocumentNumber;
	}
	public void setPayerDocumentNumber(String payerDocumentNumber) {
		PayerDocumentNumber = payerDocumentNumber;
	}
	public String getPayerPhoneNumber() {
		return PayerPhoneNumber;
	}
	public void setPayerPhoneNumber(String payerPhoneNumber) {
		PayerPhoneNumber = payerPhoneNumber;
	}
	public String getEntOrderNo() {
		return EntOrderNo;
	}
	public void setEntOrderNo(String entOrderNo) {
		EntOrderNo = entOrderNo;
	}
	public String getEBPEntNo() {
		return EBPEntNo;
	}
	public void setEBPEntNo(String eBPEntNo) {
		EBPEntNo = eBPEntNo;
	}
	public String getEBPEntName() {
		return EBPEntName;
	}
	public void setEBPEntName(String eBPEntName) {
		EBPEntName = eBPEntName;
	}
	public String getNotes() {
		return Notes;
	}
	public void setNotes(String notes) {
		Notes = notes;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getDel_flag() {
		return del_flag;
	}
	public void setDel_flag(int del_flag) {
		this.del_flag = del_flag;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	public String getCreate_by() {
		return create_by;
	}
	public void setCreate_by(String create_by) {
		this.create_by = create_by;
	}
	public Date getUpdate_date() {
		return update_date;
	}
	public void setUpdate_date(Date update_date) {
		this.update_date = update_date;
	}
	public String getUpdate_by() {
		return update_by;
	}
	public void setUpdate_by(String update_by) {
		this.update_by = update_by;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	
}
