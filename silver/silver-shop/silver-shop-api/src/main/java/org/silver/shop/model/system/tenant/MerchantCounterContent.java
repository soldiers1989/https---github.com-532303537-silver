package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

public class MerchantCounterContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1903941845018809059L;
	private long id;//
	private String counterId;// 专柜id
	private String merchantId;// 商户id
	private String merchantName;// 商户名称
	private String headContent;// 专柜名称
	private String description;// 描述
	private String counterURL;// 专柜url
	private String logo;// 专柜图标
	private String imge;// 图片
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

	public String getCounterId() {
		return counterId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public String getHeadContent() {
		return headContent;
	}

	public String getDescription() {
		return description;
	}

	public String getCounterURL() {
		return counterURL;
	}

	public String getLogo() {
		return logo;
	}

	public String getImge() {
		return imge;
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

	public void setCounterId(String counterId) {
		this.counterId = counterId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public void setHeadContent(String headContent) {
		this.headContent = headContent;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCounterURL(String counterURL) {
		this.counterURL = counterURL;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public void setImge(String imge) {
		this.imge = imge;
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

}
