package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 商家下的拓展商信息
 */
public class ExpadndMerchantContent implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8363332962759413626L;
	private long id;
	private String supMerchantId;// 归属主商户Id
	private String supMerchantName;// 归属主商户名称
	private String expadndMerchantCode;// 拓展商code
	private String expadndMerchantName;// 拓展商名称
	private double profit;//分润-利润
	private String loginName;//登录名称
	private String legalName;//法人姓名
	private String ysPartnerNo;// 银盛账户号
	private String remark;//
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	public long getId() {
		return id;
	}	
	public String getExpadndMerchantCode() {
		return expadndMerchantCode;
	}
	public String getExpadndMerchantName() {
		return expadndMerchantName;
	}
	public String getYsPartnerNo() {
		return ysPartnerNo;
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
	public int getDeleteFlag() {
		return deleteFlag;
	}
	public String getDeleteBy() {
		return deleteBy;
	}
	public Date getDeleteDate() {
		return deleteDate;
	}
	public void setId(long id) {
		this.id = id;
	}

	public void setExpadndMerchantCode(String expadndMerchantCode) {
		this.expadndMerchantCode = expadndMerchantCode;
	}
	public void setExpadndMerchantName(String expadndMerchantName) {
		this.expadndMerchantName = expadndMerchantName;
	}
	public void setYsPartnerNo(String ysPartnerNo) {
		this.ysPartnerNo = ysPartnerNo;
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
	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	public String getLoginName() {
		return loginName;
	}
	public String getLegalName() {
		return legalName;
	}
	public String getRemark() {
		return remark;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public void setLegalName(String legalName) {
		this.legalName = legalName;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSupMerchantId() {
		return supMerchantId;
	}
	public String getSupMerchantName() {
		return supMerchantName;
	}
	public void setSupMerchantId(String supMerchantId) {
		this.supMerchantId = supMerchantId;
	}
	public void setSupMerchantName(String supMerchantName) {
		this.supMerchantName = supMerchantName;
	}
	public double getProfit() {
		return profit;
	}
	public void setProfit(double profit) {
		this.profit = profit;
	}

}
