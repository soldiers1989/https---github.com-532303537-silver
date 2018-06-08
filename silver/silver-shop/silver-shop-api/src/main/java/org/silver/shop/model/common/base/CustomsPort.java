package org.silver.shop.model.common.base;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统已开通的口岸-海关-国检信息实体类
 */
public class CustomsPort implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private String province;// 省份名称
	private String provinceCode;// 省份编码
	private String city;// 城市名称
	private String cityCode;// 城市编码
	private int customsPort;// 海关口岸 1-广州电子口岸 ,2-广东智检
	private String customsPortName;// 海关口岸名称
	private String customsCode;// 主管海关代码(同仓库编码)
	private String customsName;// 主管海关代码名称
	private String ciqOrgCode;// 检验检疫机构代码
	private String ciqOrgName;// 检验检疫机构名称
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
	public void setId(long id) {
		this.id = id;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	public int getCustomsPort() {
		return customsPort;
	}
	public void setCustomsPort(int customsPort) {
		this.customsPort = customsPort;
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
	public String getCustomsPortName() {
		return customsPortName;
	}
	public void setCustomsPortName(String customsPortName) {
		this.customsPortName = customsPortName;
	}
	public String getProvinceCode() {
		return provinceCode;
	}
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}
	public String getCityCode() {
		return cityCode;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

}
