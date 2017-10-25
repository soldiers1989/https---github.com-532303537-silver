package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单(备案报文)实体
 */
public class OrderRecordContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private String merchantId;// 商户ID
	private String merchantName;//商户名称
	private String memberId;// 用户ID
	private String memberName;//用户名称
	private String entOrderNo;// 电商平台的原始订单编号
	private int orderStatus;// 电子订单状态0-订单确认,1-订单完成,2-订单取消
	private int payStatus;// 支付状态0-已付款,1-未付款(待支付) 2：支付失败，3：支付超时
	private Double orderGoodTotal;// 订单商品总金额
	private String orderGoodTotalCurr;// 订单商品总额币制-参照币制代码表
	private Double freight;// 运费：运杂费，无则填“0”
	private Double tax;// 税款-按照货款金额计算的税款，无则填“0”
	private Double otherPayment;// 抵付金额优惠减免金额，无则填“0”
	private Double otherPayNotes;// 抵付说明抵付情况说明。如果填写抵付金额时，此项必填。
	private Double otherCharges;// 其它费用无则填“0”
	private Double actualAmountPaid;// 实际支付金额 货款+运费+税款-优惠金额，与支付保持一致
	private String recipientName;// 收货人姓名,同运单
	private String recipientCardId;// 收货人身份证号码
	private String recipientTel;// 收货人电话,同运单
	private String recipientAddr;// 收货人地址,同运单
	private String recipientCountry;// 收货人所在国-国家代码
	private String recipientProvincesCode;// 收货人行政区代码
	private String orderDocAcount;// 下单人账号(用户名)
	private String orderDocName;// 下单人姓名
	private int orderDocType;// 下单人证件类型-购买者证件号码类型01:身份证、02:护照、04:其他
	private String orderDocId;// 下单人证件号默认为身份证号
	private String orderDocTel;// 下单人电话购买者联系电话号码
	private String batchNumbers;// 商品批次号
	private int invoiceType;// 发票类型1- 电子发票2- 普通发票（纸质）3- 专用发票（纸质）0- 其它
	private String invoiceNo;// 发票编号国税发票编号
	private String invoiceTitle;// 发票抬头消费方企业/个人名称
	private String invoiceIdentifyID;// 纳税人标识号-消费方纳税标识号
	private String invoiceDesc;// 发票内容多项时取首项或合并填写
	private String invoiceAmount;// 发票金额开票总金额，应与订单实际支付金额一致
	private Date invoiceDate;// 开票日期
	private String notes;// 备注
	private Date payTime;// 付款时间
	private String orderRecordStatus;// 订单备案状态：1-备案中，2-备案成功，3-备案失败
	private int ehsStatus;// 物流状态：0-未发货 1-待出仓2-已发货3-已签收
	private String wbEhsentName;// 物流公司名称
	private String wbEhsentNo;// 物流公司备案号
	private String entPayNo;// 支付交易编号
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	
	private String orderSerialNo;// 订单所属商品流水号
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	
	public String getEntOrderNo() {
		return entOrderNo;
	}
	public void setEntOrderNo(String entOrderNo) {
		this.entOrderNo = entOrderNo;
	}
	public int getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}
	public int getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(int payStatus) {
		this.payStatus = payStatus;
	}
	
	public Double getOrderGoodTotal() {
		return orderGoodTotal;
	}
	public void setOrderGoodTotal(Double orderGoodTotal) {
		this.orderGoodTotal = orderGoodTotal;
	}
	public String getOrderGoodTotalCurr() {
		return orderGoodTotalCurr;
	}
	public void setOrderGoodTotalCurr(String orderGoodTotalCurr) {
		this.orderGoodTotalCurr = orderGoodTotalCurr;
	}
	public Double getFreight() {
		return freight;
	}
	public void setFreight(Double freight) {
		this.freight = freight;
	}
	public Double getTax() {
		return tax;
	}
	public void setTax(Double tax) {
		this.tax = tax;
	}
	public Double getOtherPayment() {
		return otherPayment;
	}
	public void setOtherPayment(Double otherPayment) {
		this.otherPayment = otherPayment;
	}
	public Double getOtherPayNotes() {
		return otherPayNotes;
	}
	public void setOtherPayNotes(Double otherPayNotes) {
		this.otherPayNotes = otherPayNotes;
	}
	public Double getOtherCharges() {
		return otherCharges;
	}
	public void setOtherCharges(Double otherCharges) {
		this.otherCharges = otherCharges;
	}
	public Double getActualAmountPaid() {
		return actualAmountPaid;
	}
	public void setActualAmountPaid(Double actualAmountPaid) {
		this.actualAmountPaid = actualAmountPaid;
	}
	public String getRecipientName() {
		return recipientName;
	}
	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}
	public String getRecipientCardId() {
		return recipientCardId;
	}
	public void setRecipientCardId(String recipientCardId) {
		this.recipientCardId = recipientCardId;
	}
	public String getRecipientTel() {
		return recipientTel;
	}
	public void setRecipientTel(String recipientTel) {
		this.recipientTel = recipientTel;
	}
	public String getRecipientAddr() {
		return recipientAddr;
	}
	public void setRecipientAddr(String recipientAddr) {
		this.recipientAddr = recipientAddr;
	}
	public String getRecipientCountry() {
		return recipientCountry;
	}
	public void setRecipientCountry(String recipientCountry) {
		this.recipientCountry = recipientCountry;
	}
	public String getRecipientProvincesCode() {
		return recipientProvincesCode;
	}
	public void setRecipientProvincesCode(String recipientProvincesCode) {
		this.recipientProvincesCode = recipientProvincesCode;
	}
	public String getOrderDocAcount() {
		return orderDocAcount;
	}
	public void setOrderDocAcount(String orderDocAcount) {
		this.orderDocAcount = orderDocAcount;
	}
	public String getOrderDocName() {
		return orderDocName;
	}
	public void setOrderDocName(String orderDocName) {
		this.orderDocName = orderDocName;
	}
	public int getOrderDocType() {
		return orderDocType;
	}
	public void setOrderDocType(int orderDocType) {
		this.orderDocType = orderDocType;
	}
	
	public String getOrderDocId() {
		return orderDocId;
	}
	public void setOrderDocId(String orderDocId) {
		this.orderDocId = orderDocId;
	}
	public String getOrderDocTel() {
		return orderDocTel;
	}
	public void setOrderDocTel(String orderDocTel) {
		this.orderDocTel = orderDocTel;
	}
	public String getBatchNumbers() {
		return batchNumbers;
	}
	public void setBatchNumbers(String batchNumbers) {
		this.batchNumbers = batchNumbers;
	}
	public int getInvoiceType() {
		return invoiceType;
	}
	public void setInvoiceType(int invoiceType) {
		this.invoiceType = invoiceType;
	}
	public String getInvoiceNo() {
		return invoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}
	public String getInvoiceTitle() {
		return invoiceTitle;
	}
	public void setInvoiceTitle(String invoiceTitle) {
		this.invoiceTitle = invoiceTitle;
	}
	public String getInvoiceIdentifyID() {
		return invoiceIdentifyID;
	}
	public void setInvoiceIdentifyID(String invoiceIdentifyID) {
		this.invoiceIdentifyID = invoiceIdentifyID;
	}
	public String getInvoiceDesc() {
		return invoiceDesc;
	}
	public void setInvoiceDesc(String invoiceDesc) {
		this.invoiceDesc = invoiceDesc;
	}
	public String getInvoiceAmount() {
		return invoiceAmount;
	}
	public void setInvoiceAmount(String invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Date getPayTime() {
		return payTime;
	}
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}
	public String getOrderRecordStatus() {
		return orderRecordStatus;
	}
	public void setOrderRecordStatus(String orderRecordStatus) {
		this.orderRecordStatus = orderRecordStatus;
	}
	public int getEhsStatus() {
		return ehsStatus;
	}
	public void setEhsStatus(int ehsStatus) {
		this.ehsStatus = ehsStatus;
	}
	public String getWbEhsentName() {
		return wbEhsentName;
	}
	public void setWbEhsentName(String wbEhsentName) {
		this.wbEhsentName = wbEhsentName;
	}
	public String getWbEhsentNo() {
		return wbEhsentNo;
	}
	public void setWbEhsentNo(String wbEhsentNo) {
		this.wbEhsentNo = wbEhsentNo;
	}
	public String getEntPayNo() {
		return entPayNo;
	}
	public void setEntPayNo(String entPayNo) {
		this.entPayNo = entPayNo;
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
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
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
	public String getOrderSerialNo() {
		return orderSerialNo;
	}
	public void setOrderSerialNo(String orderSerialNo) {
		this.orderSerialNo = orderSerialNo;
	}

	

}
