package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 商户费用实体类
 */
public class MerchantFeeContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3679696160927798741L;
	private long id;
	private String merchantId;// 商户Id
	private String merchantName;// 商户名称
	private String provinceCode;// 省份编码
	private String provinceName;// 省份名称
	private String cityCode;// 城市编码
	private String cityName;// 城市名称
	private int customsPort;// 海关口岸 1-广州电子口岸 ,2-广东智检
	private String customsPortName;// 海关口岸名称
	private String customsCode;// 主管海关代码(同仓库编码)
	private String customsName;// 主管海关代码名称
	private String ciqOrgCode;// 检验检疫机构代码
	private String ciqOrgName;// 检验检疫机构名称
	private double platformFee;// 平台服务费-暂定为千分之几
	private String type; //类型：goodsRecord(商品备案)、orderRecord(订单备案)、paymentRecord(支付单备案)
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

	public String getMerchantId() {
		return merchantId;
	}

	public String getMerchantName() {
		return merchantName;
	}


	public String getProvinceCode() {
		return provinceCode;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public String getCityCode() {
		return cityCode;
	}

	public String getCityName() {
		return cityName;
	}

	public int getCustomsPort() {
		return customsPort;
	}

	public String getCustomsPortName() {
		return customsPortName;
	}

	public String getCustomsCode() {
		return customsCode;
	}

	public String getCustomsName() {
		return customsName;
	}

	public String getCiqOrgCode() {
		return ciqOrgCode;
	}

	public String getCiqOrgName() {
		return ciqOrgName;
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

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}


	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public void setCustomsPort(int customsPort) {
		this.customsPort = customsPort;
	}

	public void setCustomsPortName(String customsPortName) {
		this.customsPortName = customsPortName;
	}

	public void setCustomsCode(String customsCode) {
		this.customsCode = customsCode;
	}

	public void setCustomsName(String customsName) {
		this.customsName = customsName;
	}

	public void setCiqOrgCode(String ciqOrgCode) {
		this.ciqOrgCode = ciqOrgCode;
	}

	public void setCiqOrgName(String ciqOrgName) {
		this.ciqOrgName = ciqOrgName;
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

	public double getPlatformFee() {
		return platformFee;
	}

	public String getType() {
		return type;
	}

	public void setPlatformFee(double platformFee) {
		this.platformFee = platformFee;
	}

	public void setType(String type) {
		this.type = type;
	}

}
