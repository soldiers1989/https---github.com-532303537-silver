package org.silver.shop.model.system.manual;

import java.io.Serializable;
import java.util.Date;

/**
 * 手工录入订单暂存表
 */
public class Morder implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -2472565382093818009L;
	private long id;
	private String merchant_no;// 所属商户ID
	private String order_id;
	private String Fcode;// 币种
	private double FCY;// 订单商品总金额
	private double Tax;// 税费
	private double ActualAmountPaid;// 实际支付金额
	private String RecipientName;// 收货人姓名
	private String RecipientAddr;// 收货人地址
	private String RecipientID;// 收货人身份证号
	private String RecipientTel;// 收货人电话
	private String RecipientProvincesCode;// 收货人省份编码
	private String RecipientCityCode;// 收货人城市编码
	private String RecipientAreaCode;// 收货人区域编码
	private String OrderDocAcount;// 下单人账号
	private String OrderDocName;// 下单人姓名
	private String OrderDocType;// 下单人证件类型 固定为01 01:身份证、02:护照、04:其他
	private String OrderDocId;// 下单人证件号
	private String OrderDocTel;// 下单人电话
	private String OrderDate;// 订单日期 YYYYMMDDHHMMSS
	private String trade_no;// 模拟银盛支付交易流水 系统唯一
	private String dateSign;// 日期标记YYYYMMDD
	private String waybill;// 运单号
	private int serial;// 批次号
	private int status;// 订单推送至网关接收状态： 0-未发起,1-已发起,2-接收成功,3-接收失败
	private int del_flag;// 删除标识:0-未删除,1-已删除
	private Date create_date;// 创建日期
	private String create_by;// 创建人
	private Date update_date;// 更新日期
	private String update_by;// 更新人
	private String remarks;//
	/**
	 * 备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
	 * 2018-6-14更新为申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
	 */
	private int order_record_status;
	private String order_serial_no;// 服务器接收成功后返回编号
	private String order_re_note;// 服务器返回信息
	private String RecipientProvincesName;// 收货人省份名称
	private String RecipientCityName;// 收货人城市名称
	private String RecipientAreaName;// 收货人区域名称

	private String senderName; // 发货人姓名
	private String senderCountry;// 发货人国家代码
	private String senderAreaCode;// 发货人区域代码 国外填 000000
	private String senderAddress;// 发货人地址
	private String senderTel;// 发货人电话
	private String postal;// 邮编
	private String oldOrderId;// 原导入表单中订单Id
	private String spareParams;// 备用时段,用于存放不供货商的多余字段信息,存储格式为JSON

	private String eport;// 口岸标识1-电子口岸,2-广东智检
	private String customsCode;// 海关关区代码(导出表中-进/出口岸)
	private String ciqOrgCode;// 国检机构代码
	private String thirdPartyId;// 第三方订单唯一标识
	private int idcardCertifiedFlag;// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
	private int backCoverFlag;// 手续费封底标识：1-不封底计算、2-100封底计算
	private double platformFee;// 商户对应的订单服务费率
	private String orderPayerId;// 订单付款人id
	private String orderPayerName;// 订单付款人名称
	private int orderStatus;// 订单状态：1-待付款、2-已付款,待商家处理、3-待揽件、4-快件运输中、5-快件已签收、200-交易成功、400-交易关闭
	private String orderSourceType;// 订单录入系统类型：online-线上(商城真实下单)、offline-线下(后台批量导入)
	private double freight;// 订单运费
	private double otherPayment;// 抵付金额
	private String otherPayNotes;// 抵付说明:如果填写抵付金额时，此项必填。
	private String resendThirdPartyStatus;// 第三方重发状态：success-成功，failure-失败
	private Date declareDate;// 订单申报时间

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMerchant_no() {
		return merchant_no;
	}

	public void setMerchant_no(String merchant_no) {
		this.merchant_no = merchant_no;
	}

	public String getOrder_id() {
		return order_id;
	}

	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}

	public String getFcode() {
		return Fcode;
	}

	public void setFcode(String fcode) {
		Fcode = fcode;
	}

	public double getFCY() {
		return FCY;
	}

	public void setFCY(double fCY) {
		FCY = fCY;
	}

	public double getTax() {
		return Tax;
	}

	public void setTax(double tax) {
		Tax = tax;
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

	public String getRecipientID() {
		return RecipientID;
	}

	public void setRecipientID(String recipientID) {
		RecipientID = recipientID;
	}

	public String getRecipientTel() {
		return RecipientTel;
	}

	public void setRecipientTel(String recipientTel) {
		RecipientTel = recipientTel;
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

	public String getWaybill() {
		return waybill;
	}

	public void setWaybill(String waybill) {
		this.waybill = waybill;
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

	public int getSerial() {
		return serial;
	}

	public void setSerial(int serial) {
		this.serial = serial;
	}

	public String getDateSign() {
		return dateSign;
	}

	public void setDateSign(String dateSign) {
		this.dateSign = dateSign;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
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

	public String getTrade_no() {
		return trade_no;
	}

	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}

	public int getOrder_record_status() {
		return order_record_status;
	}

	public void setOrder_record_status(int order_record_status) {
		this.order_record_status = order_record_status;
	}

	public String getOrder_serial_no() {
		return order_serial_no;
	}

	public void setOrder_serial_no(String order_serial_no) {
		this.order_serial_no = order_serial_no;
	}

	public String getOrder_re_note() {
		return order_re_note;
	}

	public void setOrder_re_note(String order_re_note) {
		this.order_re_note = order_re_note;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderCountry() {
		return senderCountry;
	}

	public void setSenderCountry(String senderCountry) {
		this.senderCountry = senderCountry;
	}

	public String getSenderAreaCode() {
		return senderAreaCode;
	}

	public void setSenderAreaCode(String senderAreaCode) {
		this.senderAreaCode = senderAreaCode;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	public String getSenderTel() {
		return senderTel;
	}

	public void setSenderTel(String senderTel) {
		this.senderTel = senderTel;
	}

	public String getRecipientCityCode() {
		return RecipientCityCode;
	}

	public String getRecipientAreaCode() {
		return RecipientAreaCode;
	}

	public String getPostal() {
		return postal;
	}

	public void setRecipientCityCode(String recipientCityCode) {
		RecipientCityCode = recipientCityCode;
	}

	public void setRecipientAreaCode(String recipientAreaCode) {
		RecipientAreaCode = recipientAreaCode;
	}

	public void setPostal(String postal) {
		this.postal = postal;
	}

	public String getRecipientProvincesName() {
		return RecipientProvincesName;
	}

	public String getRecipientCityName() {
		return RecipientCityName;
	}

	public String getRecipientAreaName() {
		return RecipientAreaName;
	}

	public void setRecipientProvincesName(String recipientProvincesName) {
		RecipientProvincesName = recipientProvincesName;
	}

	public void setRecipientCityName(String recipientCityName) {
		RecipientCityName = recipientCityName;
	}

	public void setRecipientAreaName(String recipientAreaName) {
		RecipientAreaName = recipientAreaName;
	}

	public String getOldOrderId() {
		return oldOrderId;
	}

	public void setOldOrderId(String oldOrderId) {
		this.oldOrderId = oldOrderId;
	}

	public String getSpareParams() {
		return spareParams;
	}

	public void setSpareParams(String spareParams) {
		this.spareParams = spareParams;
	}

	public String getCustomsCode() {
		return customsCode;
	}

	public void setCustomsCode(String customsCode) {
		this.customsCode = customsCode;
	}

	public String getThirdPartyId() {
		return thirdPartyId;
	}

	public void setThirdPartyId(String thirdPartyId) {
		this.thirdPartyId = thirdPartyId;
	}

	public String getEport() {
		return eport;
	}

	public String getCiqOrgCode() {
		return ciqOrgCode;
	}

	public void setEport(String eport) {
		this.eport = eport;
	}

	public void setCiqOrgCode(String ciqOrgCode) {
		this.ciqOrgCode = ciqOrgCode;
	}

	public int getIdcardCertifiedFlag() {
		return idcardCertifiedFlag;
	}

	public int getBackCoverFlag() {
		return backCoverFlag;
	}

	public void setIdcardCertifiedFlag(int idcardCertifiedFlag) {
		this.idcardCertifiedFlag = idcardCertifiedFlag;
	}

	public void setBackCoverFlag(int backCoverFlag) {
		this.backCoverFlag = backCoverFlag;
	}

	public double getPlatformFee() {
		return platformFee;
	}

	public void setPlatformFee(double platformFee) {
		this.platformFee = platformFee;
	}

	public String getOrderPayerId() {
		return orderPayerId;
	}

	public String getOrderPayerName() {
		return orderPayerName;
	}

	public void setOrderPayerId(String orderPayerId) {
		this.orderPayerId = orderPayerId;
	}

	public void setOrderPayerName(String orderPayerName) {
		this.orderPayerName = orderPayerName;
	}

	public int getOrderStatus() {
		return orderStatus;
	}

	public String getOrderSourceType() {
		return orderSourceType;
	}

	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}

	public void setOrderSourceType(String orderSourceType) {
		this.orderSourceType = orderSourceType;
	}

	public double getFreight() {
		return freight;
	}

	public double getOtherPayment() {
		return otherPayment;
	}

	public String getOtherPayNotes() {
		return otherPayNotes;
	}

	public String getResendThirdPartyStatus() {
		return resendThirdPartyStatus;
	}

	public void setFreight(double freight) {
		this.freight = freight;
	}

	public void setOtherPayment(double otherPayment) {
		this.otherPayment = otherPayment;
	}

	public void setOtherPayNotes(String otherPayNotes) {
		this.otherPayNotes = otherPayNotes;
	}

	public void setResendThirdPartyStatus(String resendThirdPartyStatus) {
		this.resendThirdPartyStatus = resendThirdPartyStatus;
	}

	public Date getDeclareDate() {
		return declareDate;
	}

	public void setDeclareDate(Date declareDate) {
		this.declareDate = declareDate;
	}

}
