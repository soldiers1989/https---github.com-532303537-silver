package org.silver.sys.model.order;

import java.io.Serializable;
import java.util.Date;
/**
 * 订单信息存储表
 * @author zhangxin 2017/8/25
 *
 */
public class OrderRecord implements Serializable {


	private static final long serialVersionUID = 1L;
	
	private long id;
	private String OrgMessageID;        //报文编号
	private String EntOrderNo;          //企业电子订单编号
	private String OrderStatus;         //电子订单状态
	private String PayStatus;           //支付状态
	private double OrderGoodTotal;      //订单商品总额
	private String OrderGoodTotalCurr;  //订单商品总额币制
	private double Freight;             //订单运费
	private double Tax;                 //税款
	private double OtherPayment;        //抵押金额
	private String OtherPayNotes;       //低付说明
	private double OtherCharges;        //其它费用
	private double ActualAmountPaid;    //实际支付金额
	private String RecipientName; // 收货人名称
	private String RecipientAddr; // 收货人地址
	private String RecipientTel; // 收货人电话
	private String RecipientCountry; // 收货人所在国
	private String RecipientProvincesCode; // 收货人行政区代码
	private String OrderDocAcount; //下单人账户
	private String OrderDocName;  //下单人姓名
	private String OrderDocType;  //下单人证件类型
	private String OrderDocId;   //下单人证件号码
	private String OrderDocTel;  //下单人电话
	private String OrderDate;    //下单日期
	private String BatchNumbers; //商品批次
	private String InvoiceType;  //发票类型
	private String InvoiceNo;    //发票编号
	private String InvoiceTitle; //发票抬头
	private String InvoiceIdentifyId; // 纳税人标示号
	private String InvoiceDesc;  //发票内容
	private double InvoiceAmount;//发票金额
	private String InvoiceDate;  //开票日期
	private String InvoiceNotes; //备注
	private String EHSEntNo;     //物流企业代码
	private String EHSEntName;   //物流企业名称
	private String WaybillNo;    //电子运单编号 
	private String PayEntNo;     //支付企业代码
	private String PayEntName;   //支付企业名称
	private String PayNo;        //支付交易编号
	
	private String ciqNotes;// 国检审核备注
	private String ciqStatus;// 国检审核备注
	private String cusNotes;// 海关审核备注
	private String cusStatus;// 海关审核备注

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
	public String getEntOrderNo() {
		return EntOrderNo;
	}
	public void setEntOrderNo(String entOrderNo) {
		EntOrderNo = entOrderNo;
	}
	public String getOrgMessageID() {
		return OrgMessageID;
	}
	public void setOrgMessageID(String orgMessageID) {
		OrgMessageID = orgMessageID;
	}
	public String getOrderStatus() {
		return OrderStatus;
	}
	public void setOrderStatus(String orderStatus) {
		OrderStatus = orderStatus;
	}
	public String getPayStatus() {
		return PayStatus;
	}
	public void setPayStatus(String payStatus) {
		PayStatus = payStatus;
	}
	public double getOrderGoodTotal() {
		return OrderGoodTotal;
	}
	public void setOrderGoodTotal(double orderGoodTotal) {
		OrderGoodTotal = orderGoodTotal;
	}
	public String getOrderGoodTotalCurr() {
		return OrderGoodTotalCurr;
	}
	public void setOrderGoodTotalCurr(String orderGoodTotalCurr) {
		OrderGoodTotalCurr = orderGoodTotalCurr;
	}
	public double getFreight() {
		return Freight;
	}
	public void setFreight(double freight) {
		Freight = freight;
	}
	public double getTax() {
		return Tax;
	}
	public void setTax(double tax) {
		Tax = tax;
	}
	public double getOtherPayment() {
		return OtherPayment;
	}
	public void setOtherPayment(double otherPayment) {
		OtherPayment = otherPayment;
	}
	public String getOtherPayNotes() {
		return OtherPayNotes;
	}
	public void setOtherPayNotes(String otherPayNotes) {
		OtherPayNotes = otherPayNotes;
	}
	public double getOtherCharges() {
		return OtherCharges;
	}
	public void setOtherCharges(double otherCharges) {
		OtherCharges = otherCharges;
	}
	public double getActualAmountPaid() {
		return ActualAmountPaid;
	}
	public void setActualAmountPaid(double actualAmountPaid) {
		ActualAmountPaid = actualAmountPaid;
	}
	public String getRecipientName() {
		return RecipientName;
	}
	public void setRecipientName(String recipientName) {
		RecipientName = recipientName;
	}
	public String getRecipientAddr() {
		return RecipientAddr;
	}
	public void setRecipientAddr(String recipientAddr) {
		RecipientAddr = recipientAddr;
	}
	public String getRecipientTel() {
		return RecipientTel;
	}
	public void setRecipientTel(String recipientTel) {
		RecipientTel = recipientTel;
	}
	public String getRecipientCountry() {
		return RecipientCountry;
	}
	public void setRecipientCountry(String recipientCountry) {
		RecipientCountry = recipientCountry;
	}
	public String getRecipientProvincesCode() {
		return RecipientProvincesCode;
	}
	public void setRecipientProvincesCode(String recipientProvincesCode) {
		RecipientProvincesCode = recipientProvincesCode;
	}
	public String getOrderDocAcount() {
		return OrderDocAcount;
	}
	public void setOrderDocAcount(String orderDocAcount) {
		OrderDocAcount = orderDocAcount;
	}
	public String getOrderDocName() {
		return OrderDocName;
	}
	public void setOrderDocName(String orderDocName) {
		OrderDocName = orderDocName;
	}
	public String getOrderDocType() {
		return OrderDocType;
	}
	public void setOrderDocType(String orderDocType) {
		OrderDocType = orderDocType;
	}
	public String getOrderDocId() {
		return OrderDocId;
	}
	public void setOrderDocId(String orderDocId) {
		OrderDocId = orderDocId;
	}
	public String getOrderDocTel() {
		return OrderDocTel;
	}
	public void setOrderDocTel(String orderDocTel) {
		OrderDocTel = orderDocTel;
	}
	public String getOrderDate() {
		return OrderDate;
	}
	public void setOrderDate(String orderDate) {
		OrderDate = orderDate;
	}
	public String getBatchNumbers() {
		return BatchNumbers;
	}
	public void setBatchNumbers(String batchNumbers) {
		BatchNumbers = batchNumbers;
	}
	public String getInvoiceType() {
		return InvoiceType;
	}
	public void setInvoiceType(String invoiceType) {
		InvoiceType = invoiceType;
	}
	public String getInvoiceNo() {
		return InvoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		InvoiceNo = invoiceNo;
	}
	public String getInvoiceTitle() {
		return InvoiceTitle;
	}
	public void setInvoiceTitle(String invoiceTitle) {
		InvoiceTitle = invoiceTitle;
	}
	public String getInvoiceIdentifyId() {
		return InvoiceIdentifyId;
	}
	public void setInvoiceIdentifyId(String invoiceIdentifyId) {
		InvoiceIdentifyId = invoiceIdentifyId;
	}
	public String getInvoiceDesc() {
		return InvoiceDesc;
	}
	public void setInvoiceDesc(String invoiceDesc) {
		InvoiceDesc = invoiceDesc;
	}
	public double getInvoiceAmount() {
		return InvoiceAmount;
	}
	public void setInvoiceAmount(double invoiceAmount) {
		InvoiceAmount = invoiceAmount;
	}
	public String getInvoiceDate() {
		return InvoiceDate;
	}
	public void setInvoiceDate(String invoiceDate) {
		InvoiceDate = invoiceDate;
	}
	public String getInvoiceNotes() {
		return InvoiceNotes;
	}
	public void setInvoiceNotes(String invoiceNotes) {
		InvoiceNotes = invoiceNotes;
	}
	public String getEHSEntNo() {
		return EHSEntNo;
	}
	public void setEHSEntNo(String eHSEntNo) {
		EHSEntNo = eHSEntNo;
	}
	public String getEHSEntName() {
		return EHSEntName;
	}
	public void setEHSEntName(String eHSEntName) {
		EHSEntName = eHSEntName;
	}
	public String getWaybillNo() {
		return WaybillNo;
	}
	public void setWaybillNo(String waybillNo) {
		WaybillNo = waybillNo;
	}
	public String getPayEntNo() {
		return PayEntNo;
	}
	public void setPayEntNo(String payEntNo) {
		PayEntNo = payEntNo;
	}
	public String getPayEntName() {
		return PayEntName;
	}
	public void setPayEntName(String payEntName) {
		PayEntName = payEntName;
	}
	public String getPayNo() {
		return PayNo;
	}
	public void setPayNo(String payNo) {
		PayNo = payNo;
	}
	public String getCiqNotes() {
		return ciqNotes;
	}
	public void setCiqNotes(String ciqNotes) {
		this.ciqNotes = ciqNotes;
	}
	public String getCiqStatus() {
		return ciqStatus;
	}
	public void setCiqStatus(String ciqStatus) {
		this.ciqStatus = ciqStatus;
	}
	public String getCusNotes() {
		return cusNotes;
	}
	public void setCusNotes(String cusNotes) {
		this.cusNotes = cusNotes;
	}
	public String getCusStatus() {
		return cusStatus;
	}
	public void setCusStatus(String cusStatus) {
		this.cusStatus = cusStatus;
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
