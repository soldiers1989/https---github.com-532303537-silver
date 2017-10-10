package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 商戶海关(口岸)备案编码信息
 */
public class MerchantRecordInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private String merchantId;// 商户ID
	private int eport;//口岸:1-广州电子口岸,2-广东智检
	private String ebEntNo;// 电商企业编号
	private String ebEntName;// 电商企业名称
	private String ebpEntNo;// 电商平台企业编号
	private String ebpEntName;// 电商平台名称
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期

	private String internetDomainName;//电商平台互联网域名
	
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

	public int getEport() {
		return eport;
	}

	public void setEport(int eport) {
		this.eport = eport;
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

	public String getInternetDomainName() {
		return internetDomainName;
	}

	public void setInternetDomainName(String internetDomainName) {
		this.internetDomainName = internetDomainName;
	}

	
}
