package org.silver.sys.model;

import java.io.Serializable;
import java.util.Date;
/**
 * 订单信息存储表
 * @author zhangxin 2017/8/25
 *
 */
public class OrderRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String declEntNo;           //申报企业编号
	private String declEntName;         //申报企业名称
	private String ebEntNo;             //电商企业编号
	private String ebEntName;           //电商企业名称
	private String ebpEntNo;            //电商平台企业编码
	private String ebpEntName;          //电商平台企业名称
	private String internetDomainName;  //电商平台互联网域名
	private Date declTime;              //申报时间
	private String opType;              //操作方式
	private String ieFlag;              //进出口标示
	private String customsCode;         //主管海关代码 
	private String ciqOrgCode;          //检验检疫机构代码
	private String entOrderNo; // 企业电子订单编号
	private String orderStatus; // 电子订单状态
	private String payStatus; // 支付状态
	private Double orderGoodTotal; // 订单商品总额
	private String orderGoodTotalCurr; // 订单商品总额币制
	private Double freight; // 订单运费
	private Double tax; // 税款
	private Double otherPayment; // 抵押金额
	private String otherPayNotes; // 低付说明
	private Double otherCharges; // 其它费用
	private Double actualAmountPaid; // 实际支付金额
	private String recipientName; // 收货人名称
	private String recipientAddr; // 收货人地址
	private String recipientTel; // 收货人电话
	private String recipientCountry; // 收货人所在国
	private String recipientProvincesCode; // 收货人行政区代码
	private String orderDocAcount; // 下单人账户
	private String orderDocName; // 下单人姓名
	private String orderDocType; // 下单人证件类型
	private String orderDocId; // 下单人证件号码
	private String orderDocTel; // 下单人电话
	private Date orderDate; // 下单日期
	private String batchNumbers; // 商品批次
	private String invoiceType; // 发票类型
	private String invoiceNo; // 发票编号
	private String invoiceTitle; // 发票抬头
	private String invoiceIdentifyId; // 纳税人标示号
	private String invoiceDesc; // 发票内容
	private Double invoiceAmount; // 发票金额
	private Date invoiceDate; // 开票日期
	private String invoiceNotes; // 备注
	private String ehsEntNo;     //物流企业代码
	private String ehsEntName;   //物流企业名称
	private String waybillNo;    //电子运单编号 
	private String payEntNo;     //支付企业代码
	private String payEntName;   //支付企业名称
	private String payNo;        //支付交易编号
	
	private String OrgMessageID; //原始报文编号
	private String ciqNotes;// 国检审核备注
	private String ciqStatus;// 国检审核备注
	private String cusNotes;// 海关审核备注
	private String cusStatus;// 海关审核备注
	private String status;// 发送状态
	private int del_flag;// 0正常 1删除
	private Date create_date; // 创建时间
	private String create_by; // 创建人
	private Date update_date; // 更新时间
	private String update_by;// 更新人
	private String remarks;// 备注
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDeclEntNo() {
		return declEntNo;
	}
	public void setDeclEntNo(String declEntNo) {
		this.declEntNo = declEntNo;
	}
	public String getDeclEntName() {
		return declEntName;
	}
	public void setDeclEntName(String declEntName) {
		this.declEntName = declEntName;
	}
	public String getEbEntNo() {
		return ebEntNo;
	}
	public void setEbEntNo(String ebEntNo) {
		this.ebEntNo = ebEntNo;
	}
	public String getEbEntName() {
		return ebEntName;
	}
	public void setEbEntName(String ebEntName) {
		this.ebEntName = ebEntName;
	}
	public String getEbpEntNo() {
		return ebpEntNo;
	}
	public void setEbpEntNo(String ebpEntNo) {
		this.ebpEntNo = ebpEntNo;
	}
	public String getEbpEntName() {
		return ebpEntName;
	}
	public void setEbpEntName(String ebpEntName) {
		this.ebpEntName = ebpEntName;
	}
	public String getInternetDomainName() {
		return internetDomainName;
	}
	public void setInternetDomainName(String internetDomainName) {
		this.internetDomainName = internetDomainName;
	}
	public Date getDeclTime() {
		return declTime;
	}
	public void setDeclTime(Date declTime) {
		this.declTime = declTime;
	}
	public String getOpType() {
		return opType;
	}
	public void setOpType(String opType) {
		this.opType = opType;
	}
	public String getIeFlag() {
		return ieFlag;
	}
	public void setIeFlag(String ieFlag) {
		this.ieFlag = ieFlag;
	}
	public String getCustomsCode() {
		return customsCode;
	}
	public void setCustomsCode(String customsCode) {
		this.customsCode = customsCode;
	}
	public String getCiqOrgCode() {
		return ciqOrgCode;
	}
	public void setCiqOrgCode(String ciqOrgCode) {
		this.ciqOrgCode = ciqOrgCode;
	}
	public String getEntOrderNo() {
		return entOrderNo;
	}
	public void setEntOrderNo(String entOrderNo) {
		this.entOrderNo = entOrderNo;
	}
	public String getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}
	public String getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(String payStatus) {
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
	public String getOtherPayNotes() {
		return otherPayNotes;
	}
	public void setOtherPayNotes(String otherPayNotes) {
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
	public String getRecipientAddr() {
		return recipientAddr;
	}
	public void setRecipientAddr(String recipientAddr) {
		this.recipientAddr = recipientAddr;
	}
	public String getRecipientTel() {
		return recipientTel;
	}
	public void setRecipientTel(String recipientTel) {
		this.recipientTel = recipientTel;
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
	public String getOrderDocType() {
		return orderDocType;
	}
	public void setOrderDocType(String orderDocType) {
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
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	public String getBatchNumbers() {
		return batchNumbers;
	}
	public void setBatchNumbers(String batchNumbers) {
		this.batchNumbers = batchNumbers;
	}
	public String getInvoiceType() {
		return invoiceType;
	}
	public void setInvoiceType(String invoiceType) {
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
	public String getInvoiceIdentifyId() {
		return invoiceIdentifyId;
	}
	public void setInvoiceIdentifyId(String invoiceIdentifyId) {
		this.invoiceIdentifyId = invoiceIdentifyId;
	}
	public String getInvoiceDesc() {
		return invoiceDesc;
	}
	public void setInvoiceDesc(String invoiceDesc) {
		this.invoiceDesc = invoiceDesc;
	}
	public Double getInvoiceAmount() {
		return invoiceAmount;
	}
	public void setInvoiceAmount(Double invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getInvoiceNotes() {
		return invoiceNotes;
	}
	public void setInvoiceNotes(String invoiceNotes) {
		this.invoiceNotes = invoiceNotes;
	}
	public String getEhsEntNo() {
		return ehsEntNo;
	}
	public void setEhsEntNo(String ehsEntNo) {
		this.ehsEntNo = ehsEntNo;
	}
	public String getEhsEntName() {
		return ehsEntName;
	}
	public void setEhsEntName(String ehsEntName) {
		this.ehsEntName = ehsEntName;
	}
	public String getWaybillNo() {
		return waybillNo;
	}
	public void setWaybillNo(String waybillNo) {
		this.waybillNo = waybillNo;
	}
	public String getPayEntNo() {
		return payEntNo;
	}
	public void setPayEntNo(String payEntNo) {
		this.payEntNo = payEntNo;
	}
	public String getPayEntName() {
		return payEntName;
	}
	public void setPayEntName(String payEntName) {
		this.payEntName = payEntName;
	}
	public String getPayNo() {
		return payNo;
	}
	public void setPayNo(String payNo) {
		this.payNo = payNo;
	}
	public String getOrgMessageID() {
		return OrgMessageID;
	}
	public void setOrgMessageID(String orgMessageID) {
		OrgMessageID = orgMessageID;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
