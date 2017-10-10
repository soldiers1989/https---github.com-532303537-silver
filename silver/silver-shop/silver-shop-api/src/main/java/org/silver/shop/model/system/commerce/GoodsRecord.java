package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品备案流水信息
 *
 */
public class GoodsRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3669875261264998504L;

	private long id;
	private String merchantId;// 商户ID
	private String merchantName;// 商户名称
	private String goodsSerialNo;// 备案流水号
	private String customsPort;// 海关口岸代码 1:广州电子口岸(目前只支持BC业务) 2:南沙智检(支持BBC业务)
	private String customsPortName;// 海关口岸名称 
	private String customsCode;// 主管海关代码
	private String customsName;// 主管海关代码名称
	private String ciqOrgCode;// 检验检疫机构代码
	private String ciqOrgName;// 检验检疫机构名称
	private String ebEntNo;// 电商企业编号
	private String ebEntName;// 电商企业名称
	private String ebpEntNo;// 电商平台企业编号
	private String ebpEntName;// 电商平台名称
	private String status;// 备案信息接受状态：1-成功,2-失败
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 修改人
	private Date updateDate;// 修改时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间

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

	public String getGoodsSerialNo() {
		return goodsSerialNo;
	}

	public void setGoodsSerialNo(String goodsSerialNo) {
		this.goodsSerialNo = goodsSerialNo;
	}

	public String getCustomsPort() {
		return customsPort;
	}

	public void setCustomsPort(String customsPort) {
		this.customsPort = customsPort;
	}

	public String getCustomsPortName() {
		return customsPortName;
	}

	public void setCustomsPortName(String customsPortName) {
		this.customsPortName = customsPortName;
	}

	public String getCustomsCode() {
		return customsCode;
	}

	public void setCustomsCode(String customsCode) {
		this.customsCode = customsCode;
	}

	public String getCustomsName() {
		return customsName;
	}

	public void setCustomsName(String customsName) {
		this.customsName = customsName;
	}

	public String getCiqOrgCode() {
		return ciqOrgCode;
	}

	public void setCiqOrgCode(String ciqOrgCode) {
		this.ciqOrgCode = ciqOrgCode;
	}

	public String getCiqOrgName() {
		return ciqOrgName;
	}

	public void setCiqOrgName(String ciqOrgName) {
		this.ciqOrgName = ciqOrgName;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
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

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

}
