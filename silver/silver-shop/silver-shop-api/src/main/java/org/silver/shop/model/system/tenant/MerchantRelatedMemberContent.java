package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 *	商户关联的用户，关系表 
 */
public class MerchantRelatedMemberContent implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1189082016194482832L;
	private Long id;
	private String merchantId;//商户Id
	private String memberId;//用户Id
	private String remark;//
	private String createBy;//创建人
	private Date createDate;//创建时间
	
	public Long getId() {
		return id;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getMemberId() {
		return memberId;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getRemark() {
		return remark;
	}
	public String getCreateBy() {
		return createBy;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	
}
