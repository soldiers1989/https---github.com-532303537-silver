package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

import org.silver.shop.model.system.tenant.MerchantWalletContent.Builder;

public class ProxyWalletContent implements Serializable{
	public ProxyWalletContent() {

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6910869733492890837L;
	private long id;
	private String walletId;// 钱包Id
	private String proxyId;// 代理商Id
	private String proxyName;// 代理商名称
	private double balance;// 余额
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	

	public String getProxyId() {
		return proxyId;
	}

	public String getProxyName() {
		return proxyName;
	}

	public void setProxyId(String proxyId) {
		this.proxyId = proxyId;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
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

	public static class Builder {
		private long id;
		private String walletId = null;// 钱包Id
		private String proxyId = null;// 代理商Id
		private String proxyName = null;// 代理商名称
		private double balance = 0.0;// 余额
		private String createBy = null;// 创建人
		private Date createDate = null;// 创建日期
		private String updateBy = null;// 更新人
		private Date updateDate = null;// 更新日期

		public Builder(String walletId) {
			this.walletId = walletId;
		}
		public Builder proxyId(String proxyId) {
			this.proxyId = proxyId;
			return this;
		}

		public Builder proxyName(String proxyName) {
			this.proxyName = proxyName;
			return this;
		}
		public void setProxyId(String proxyId) {
			this.proxyId = proxyId;
		}
		public void setProxyName(String proxyName) {
			this.proxyName = proxyName;
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

		public ProxyWalletContent build() {
			return new ProxyWalletContent(this);
		}
	}

	private ProxyWalletContent(Builder b) {
		id = b.id;
		walletId = b.walletId;
		proxyId = b.proxyId;
		proxyName = b.proxyName;
		balance = b.balance;
		createBy = b.createBy;
		createDate = b.createDate;
		updateBy = b.updateBy;
		updateDate = b.updateDate;
	}
}
