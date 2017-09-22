package org.silver.shop.model.common.category;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品HS编码实体类
 */
public class HsCode implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String hsCode;//HS编码
	private String typeName;//名称
	private Double vat;//增值税 
	private Double consumptionTax;//消费税 
	private Double consolidatedTax;//综合税 跨境电商综合税率 = （消费税率+增值税率）/（1-消费税率）×70%
	private Double tariff;//关税税率暂设为0%
	private String notes;//备注
	private String createBy;//创建人
	private Date createDate;//创建时间
	private String updateBy;//更新人
	private Date updateDate;//更新时间
	private int deleteFlag;//删除标识:0-未删除,1-已删除
	private String deleteBy;//删除人
	private Date deleteDate;//删除时间
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getHsCode() {
		return hsCode;
	}
	public void setHsCode(String hsCode) {
		this.hsCode = hsCode;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	public Double getVat() {
		return vat;
	}
	public void setVat(Double vat) {
		this.vat = vat;
	}
	public Double getConsumptionTax() {
		return consumptionTax;
	}
	public void setConsumptionTax(Double consumptionTax) {
		this.consumptionTax = consumptionTax;
	}
	public Double getConsolidatedTax() {
		return consolidatedTax;
	}
	public void setConsolidatedTax(Double consolidatedTax) {
		this.consolidatedTax = consolidatedTax;
	}
	public Double getTariff() {
		return tariff;
	}
	public void setTariff(Double tariff) {
		this.tariff = tariff;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
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
	
	
}
