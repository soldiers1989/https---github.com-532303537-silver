package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

import org.silver.shop.model.system.tenant.MemberWalletContent.Builder;

/**
 * 商户钱包
 *
 */
public class MerchantWalletContent implements Serializable {
	public MerchantWalletContent(){
		
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -6910869733492890837L;
	private long id;
	private String walletId;// 钱包Id
	private String merchantId;// 商户Id
	private String merchantName;// 商户名称
	private double balance;// 余额
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private Double reserveAmount;//储备金额
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

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public String getWalletId() {
		return walletId;
	}

	public void setWalletId(String walletId) {
		this.walletId = walletId;
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

	
	public Double getReserveAmount() {
		return reserveAmount;
	}

	public void setReserveAmount(Double reserveAmount) {
		this.reserveAmount = reserveAmount;
	}


	public static class Builder {
		private long id;
		private String walletId = null;// 钱包Id
		private String merchantId = null;// 商户Id
		private String merchantName = null;// 商户名称
		private double balance = 0.0;// 余额
		private String createBy = null;// 创建人
		private Date createDate = null;// 创建日期
		private String updateBy = null;// 更新人
		private Date updateDate = null;// 更新日期
		private double reserveAmount = 0.0;// 储备金额
		public Builder(String walletId) {
			this.walletId = walletId;
		}

		public Builder merchantId(String merchantId) {
			this.merchantId = merchantId;
			return this;
		}

		public Builder merchantName(String merchantName) {
			this.merchantName = merchantName;
			return this;
		}

		public Builder balance(double balance) {
			this.balance = balance;
			return this;
		}

		public Builder createBy(String createBy) {
			this.createBy = createBy;
			return this;
		}

		public Builder createDate(Date createDate) {
			this.createDate = createDate;
			return this;
		}

		public Builder updateBy(String updateBy) {
			this.updateBy = updateBy;
			return this;
		}

		public Builder updateDate(Date updateDate) {
			this.updateDate = updateDate;
			return this;
		}
		
		public MerchantWalletContent build(){
			return new MerchantWalletContent(this);
		}
		public Builder reserveAmount(double reserveAmount) {
			this.reserveAmount = reserveAmount;
			return this;
		}
	}

	private MerchantWalletContent(Builder b) {
		id = b.id;
		walletId = b.walletId;
		merchantId = b.merchantId;
		merchantName = b.merchantName;
		balance = b.balance;
		createBy = b.createBy;
		createDate = b.createDate;
		updateBy = b.updateBy;
		updateDate = b.updateDate;
		reserveAmount = b.reserveAmount;
	}
}
