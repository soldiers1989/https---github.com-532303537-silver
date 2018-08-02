package org.silver.shop.model.system.log;

import java.io.Serializable;
import java.util.Date;

/**
 * 综合报表记录实体类
 */
public class SynthesisReportLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1585430342755550712L;
	private long id;
	private String merchantId;// 商户id
	private String merchantName;// 商户名称
	private Date date;// 日期
	private int totalCount;// 订单总数
	private double amount;// 订单总金额
	private double platformFee;// 订单申报服务费率
	private int backCoverCount;// 100封底数量
	private double normalAmount;// 正常价格的订单金额
	private int idCardTotalCount;// 身份证总数
	private int idCardTollCount;// 身份证收费数量
	private int idCardFreeCount;// 免费数量
	private double idCardCost;// 实名认证费率/每笔
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private String remark;// 
	public long getId() {
		return id;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public Date getDate() {
		return date;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public double getAmount() {
		return amount;
	}
	public double getPlatformFee() {
		return platformFee;
	}
	public int getBackCoverCount() {
		return backCoverCount;
	}
	public double getNormalAmount() {
		return normalAmount;
	}
	public int getIdCardTotalCount() {
		return idCardTotalCount;
	}
	
	public double getIdCardCost() {
		return idCardCost;
	}
	public String getCreateBy() {
		return createBy;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public String getUpdateBy() {
		return updateBy;
	}
	public Date getUpdateDate() {
		return updateDate;
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
	public void setDate(Date date) {
		this.date = date;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public void setPlatformFee(double platformFee) {
		this.platformFee = platformFee;
	}
	public void setBackCoverCount(int backCoverCount) {
		this.backCoverCount = backCoverCount;
	}
	public void setNormalAmount(double normalAmount) {
		this.normalAmount = normalAmount;
	}
	public void setIdCardTotalCount(int idCardTotalCount) {
		this.idCardTotalCount = idCardTotalCount;
	}

	public void setIdCardCost(double idCardCost) {
		this.idCardCost = idCardCost;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getIdCardTollCount() {
		return idCardTollCount;
	}
	public int getIdCardFreeCount() {
		return idCardFreeCount;
	}
	public void setIdCardTollCount(int idCardTollCount) {
		this.idCardTollCount = idCardTollCount;
	}
	public void setIdCardFreeCount(int idCardFreeCount) {
		this.idCardFreeCount = idCardFreeCount;
	}
	
	
}
