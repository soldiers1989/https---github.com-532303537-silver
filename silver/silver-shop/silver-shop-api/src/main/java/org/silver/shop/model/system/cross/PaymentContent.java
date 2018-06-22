package org.silver.shop.model.system.cross;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付实体类
 */
public class PaymentContent implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long  id;//
	private String memberId;//用户ID
	private String memberName;//用户名称
	private String merchantId;//商户ID
	private String merchantName;//商户ID
	private String entPayNo;// 支付交易编号
	private String payStatus;//D-代扣(款项由消费者账户转至第三方支付企业账户)S-实扣(款项由消费者账户转至收款方账户)C-取消(退款)
	private Double payAmount;// 支付企业的订单交易金额
	private String payCurrCode;// 币制参照币制代码表
	private Date payTime;// 付款时间
	private String payerName;//支付人姓名
	private int payerDocumentType;//支付人证件类型01:身份证02:护照04:其他
	private String payerDocumentNumber;// 支付人证件号码
	private String payerPhoneNumber;// 支付人手机号
	private String entOrderNo;//电子订单编号
	private String notes;//备注
	private int payRecord;// 支付备案状态：1-备案中，2-备案成功，3-备案失败
	private int payFalg;// 支付方式0:在线支付，1:货到付款
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	private String reNote;//回调信息
	private String reSerialNo;//支付单接受后返回流水号
	//private String rePayStatus;//银盛支付回调状态：success-成功、
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getMemberName() {
		return memberName;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getEntPayNo() {
		return entPayNo;
	}
	public void setEntPayNo(String entPayNo) {
		this.entPayNo = entPayNo;
	}
	public String getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}
	public void setPayAmount(Double payAmount) {
		this.payAmount = payAmount;
	}
	
	public Double getPayAmount() {
		return payAmount;
	}
	public String getPayCurrCode() {
		return payCurrCode;
	}
	public void setPayCurrCode(String payCurrCode) {
		this.payCurrCode = payCurrCode;
	}
	public Date getPayTime() {
		return payTime;
	}
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}
	public String getPayerName() {
		return payerName;
	}
	public void setPayerName(String payerName) {
		this.payerName = payerName;
	}
	public int getPayerDocumentType() {
		return payerDocumentType;
	}
	public void setPayerDocumentType(int payerDocumentType) {
		this.payerDocumentType = payerDocumentType;
	}
	public String getPayerDocumentNumber() {
		return payerDocumentNumber;
	}
	public void setPayerDocumentNumber(String payerDocumentNumber) {
		this.payerDocumentNumber = payerDocumentNumber;
	}
	public String getPayerPhoneNumber() {
		return payerPhoneNumber;
	}
	public void setPayerPhoneNumber(String payerPhoneNumber) {
		this.payerPhoneNumber = payerPhoneNumber;
	}
	public String getEntOrderNo() {
		return entOrderNo;
	}
	public void setEntOrderNo(String entOrderNo) {
		this.entOrderNo = entOrderNo;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public int getPayRecord() {
		return payRecord;
	}
	public void setPayRecord(int payRecord) {
		this.payRecord = payRecord;
	}
	
	public int getPayFalg() {
		return payFalg;
	}
	public void setPayFalg(int payFalg) {
		this.payFalg = payFalg;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getUpdateBy() {
		return updateBy;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	
	public int getDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	public String getDeleteBy() {
		return deleteBy;
	}
	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}
	public Date getDeleteDate() {
		return deleteDate;
	}
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	public String getReNote() {
		return reNote;
	}
	public void setReNote(String reNote) {
		this.reNote = reNote;
	}
	public String getReSerialNo() {
		return reSerialNo;
	}
	public void setReSerialNo(String reSerialNo) {
		this.reSerialNo = reSerialNo;
	}
	
	
}
