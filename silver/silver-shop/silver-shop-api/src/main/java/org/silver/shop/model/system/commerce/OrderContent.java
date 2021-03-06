package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 商城自用基本订单信息
 */
public class OrderContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2524545940525643500L;
	private long id;
	private String merchantId;// 商户ID
	private String merchantName;// 商户名称
	private String memberId;// 用户ID
	private String memberName;// 用户名称
	private String orderId;// 订单ID
	private double freight;// 运费
	private double orderTotalPrice;// 订单总金额
	private int status;// 订单状态：1-待付款、2-已付款,待商家处理、3-待揽件、4-快件运输中、5-快件已签收、200-交易成功、400-交易关闭
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	private String receiptId;// 用户收货地址信息ID
	private String recipientName;// 收货人姓名
	private String recipientCardId;// 收货人身份证号码
	private String recipientTel;// 收货人电话
	private String recipientCountryName;// 收货人所在国-国家名称
	private String recipientCountryCode;// 收货人所在国-国家代码
	private String recProvincesName;// 收货人省份名称
	private String recProvincesCode;// 收货人省份代码
	private String recCityName;// 收货人城市名称
	private String recCityCode;// 收货人城市代码
	private String recAreaName;// 收货人城市区名称
	private String recAreaCode;// 收货人城市区代码
	private String recipientAddr;// 收货人详细地址
	private int ehsStatus;// 物流状态：0-待处理、1-待发货、2-快件运输中、3-快件已签收
	private String wbEhsentName;// 物流公司名称
	private String wbEhsentNo;// 物流公司备案号
	private String entOrderNo;// 对接海关订单总编号
	private String reMark;//
	private String waybillNo;// (快递单号)运单编号
	private double actualAmountPaid;// 实际支付金额
	private double tax;// 税费
	private int sourceFlag;// 来源标识：1-银盟商城、2-第三方推广
	
	
	private String profitType;// 分润计算类型：1-比例，2-按固定分润数
	private double dividendRatio;// 推广商分润比例
	private double dividendAmount;// 推广商分红金额
	private double masterDividendRatio;// 主商户分红比例
	private double masterDividendAmount;// 主商户分红金额
	private double platformDividendRatio;// 平台手续费-费率
	private double platformDividendAmount;// 平台手续费
	private String expadndMerchantCode;// 推广商code
	private String expadndMerchantName;// 推广商名称

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

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public double getFreight() {
		return freight;
	}

	public void setFreight(double freight) {
		this.freight = freight;
	}

	public double getOrderTotalPrice() {
		return orderTotalPrice;
	}

	public void setOrderTotalPrice(double orderTotalPrice) {
		this.orderTotalPrice = orderTotalPrice;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public String getReMark() {
		return reMark;
	}

	public void setReMark(String reMark) {
		this.reMark = reMark;
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

	public String getRecipientCountryCode() {
		return recipientCountryCode;
	}

	public void setRecipientCountryCode(String recipientCountryCode) {
		this.recipientCountryCode = recipientCountryCode;
	}

	public String getRecProvincesCode() {
		return recProvincesCode;
	}

	public void setRecProvincesCode(String recProvincesCode) {
		this.recProvincesCode = recProvincesCode;
	}

	public String getRecCityCode() {
		return recCityCode;
	}

	public void setRecCityCode(String recCityCode) {
		this.recCityCode = recCityCode;
	}

	public String getRecAreaCode() {
		return recAreaCode;
	}

	public void setRecAreaCode(String recAreaCode) {
		this.recAreaCode = recAreaCode;
	}

	public String getRecipientAddr() {
		return recipientAddr;
	}

	public void setRecipientAddr(String recipientAddr) {
		this.recipientAddr = recipientAddr;
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

	public String getReceiptId() {
		return receiptId;
	}

	public void setReceiptId(String receiptId) {
		this.receiptId = receiptId;
	}

	public String getEntOrderNo() {
		return entOrderNo;
	}

	public void setEntOrderNo(String entOrderNo) {
		this.entOrderNo = entOrderNo;
	}

	public String getRecipientCountryName() {
		return recipientCountryName;
	}

	public void setRecipientCountryName(String recipientCountryName) {
		this.recipientCountryName = recipientCountryName;
	}

	public String getRecProvincesName() {
		return recProvincesName;
	}

	public void setRecProvincesName(String recProvincesName) {
		this.recProvincesName = recProvincesName;
	}

	public String getRecCityName() {
		return recCityName;
	}

	public void setRecCityName(String recCityName) {
		this.recCityName = recCityName;
	}

	public String getRecAreaName() {
		return recAreaName;
	}

	public void setRecAreaName(String recAreaName) {
		this.recAreaName = recAreaName;
	}

	public String getWaybillNo() {
		return waybillNo;
	}

	public void setWaybillNo(String waybillNo) {
		this.waybillNo = waybillNo;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public int getSourceFlag() {
		return sourceFlag;
	}

	public void setSourceFlag(int sourceFlag) {
		this.sourceFlag = sourceFlag;
	}

	public String getProfitType() {
		return profitType;
	}

	public double getDividendRatio() {
		return dividendRatio;
	}

	public double getDividendAmount() {
		return dividendAmount;
	}

	public double getMasterDividendRatio() {
		return masterDividendRatio;
	}

	public double getMasterDividendAmount() {
		return masterDividendAmount;
	}

	public double getPlatformDividendRatio() {
		return platformDividendRatio;
	}

	public double getPlatformDividendAmount() {
		return platformDividendAmount;
	}

	public void setProfitType(String profitType) {
		this.profitType = profitType;
	}

	public void setDividendRatio(double dividendRatio) {
		this.dividendRatio = dividendRatio;
	}

	public void setDividendAmount(double dividendAmount) {
		this.dividendAmount = dividendAmount;
	}

	public void setMasterDividendRatio(double masterDividendRatio) {
		this.masterDividendRatio = masterDividendRatio;
	}

	public void setMasterDividendAmount(double masterDividendAmount) {
		this.masterDividendAmount = masterDividendAmount;
	}

	public void setPlatformDividendRatio(double platformDividendRatio) {
		this.platformDividendRatio = platformDividendRatio;
	}

	public void setPlatformDividendAmount(double platformDividendAmount) {
		this.platformDividendAmount = platformDividendAmount;
	}

	public String getExpadndMerchantCode() {
		return expadndMerchantCode;
	}

	public String getExpadndMerchantName() {
		return expadndMerchantName;
	}

	public void setExpadndMerchantCode(String expadndMerchantCode) {
		this.expadndMerchantCode = expadndMerchantCode;
	}

	public void setExpadndMerchantName(String expadndMerchantName) {
		this.expadndMerchantName = expadndMerchantName;
	}

	public double getActualAmountPaid() {
		return actualAmountPaid;
	}

	public void setActualAmountPaid(double actualAmountPaid) {
		this.actualAmountPaid = actualAmountPaid;
	}

	
}
