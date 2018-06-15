package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单历史报表信息
 */
public class OrderHistoryReports implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4943745152500778710L;
	
	private long id;
	private String merchantId;// 商户Id
	private String merchantName;// 商户名称
	private int totalCount;// 总订单数
	private double totalAmount;// 总金额
	private double platformFee;// 商户口岸费率
	private int backCoverCount;// 100封底订单数量
	private double backCoverFee;// 封底订单手续费
	private int orderNormalCount;// 正常计费订单数量
	private double orderNormalAmount;// 订单正常数量的金额
	private double orderNormalFee;// 订单正常手续费
	private String customsCode;// 海关代码
	private String ciqOrgCode;// 国检检疫机构代码
	private String type;// 类型:总计-total、小计-subtotal
	private Date createDate;// 创建日期
	public long getId() {
		return id;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public double getTotalAmount() {
		return totalAmount;
	}
	public double getPlatformFee() {
		return platformFee;
	}
	public int getBackCoverCount() {
		return backCoverCount;
	}
	public double getBackCoverFee() {
		return backCoverFee;
	}
	public int getOrderNormalCount() {
		return orderNormalCount;
	}
	public double getOrderNormalAmount() {
		return orderNormalAmount;
	}
	public double getOrderNormalFee() {
		return orderNormalFee;
	}
	public String getCustomsCode() {
		return customsCode;
	}
	public String getCiqOrgCode() {
		return ciqOrgCode;
	}
	public String getType() {
		return type;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public void setPlatformFee(double platformFee) {
		this.platformFee = platformFee;
	}
	public void setBackCoverCount(int backCoverCount) {
		this.backCoverCount = backCoverCount;
	}
	public void setBackCoverFee(double backCoverFee) {
		this.backCoverFee = backCoverFee;
	}
	public void setOrderNormalCount(int orderNormalCount) {
		this.orderNormalCount = orderNormalCount;
	}
	public void setOrderNormalAmount(double orderNormalAmount) {
		this.orderNormalAmount = orderNormalAmount;
	}
	public void setOrderNormalFee(double orderNormalFee) {
		this.orderNormalFee = orderNormalFee;
	}
	public void setCustomsCode(String customsCode) {
		this.customsCode = customsCode;
	}
	public void setCiqOrgCode(String ciqOrgCode) {
		this.ciqOrgCode = ciqOrgCode;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	
}
