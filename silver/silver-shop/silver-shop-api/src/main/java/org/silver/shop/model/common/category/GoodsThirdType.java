package org.silver.shop.model.common.category;

import java.io.Serializable;
import java.util.Date;

/**
 *	商品第三(级)类型信息
 */
public class GoodsThirdType implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private long firstTypeId;//第一级ID
	private long secondTypeId;//第二级ID
	private String goodsThirdTypeName;//商品第三(级)类别名称
	private long vat;//增值税 
	private long consumptionTax;//消费税 
	private long tariff;//关税税率暂设为0%
	private long consolidatedTax;//综合税 跨境电商综合税率 = （消费税率+增值税率）/（1-消费税率）×70%
	private String createBy;//创建人
	private Date createDate;//创建时间
	private String updateBy;//更新人
	private Date updateDate;//更新时间
	private int deletFlag;//删除标识:0-未删除,1-已删除
	private String deleteBy;//删除人
	private Date deleteDate;//删除时间
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getFirstTypeId() {
		return firstTypeId;
	}
	public void setFirstTypeId(long firstTypeId) {
		this.firstTypeId = firstTypeId;
	}
	public long getSecondTypeId() {
		return secondTypeId;
	}
	public void setSecondTypeId(long secondTypeId) {
		this.secondTypeId = secondTypeId;
	}
	
	public String getGoodsThirdTypeName() {
		return goodsThirdTypeName;
	}
	public void setGoodsThirdTypeName(String goodsThirdTypeName) {
		this.goodsThirdTypeName = goodsThirdTypeName;
	}
	public long getVat() {
		return vat;
	}
	public void setVat(long vat) {
		this.vat = vat;
	}
	public long getConsumptionTax() {
		return consumptionTax;
	}
	public void setConsumptionTax(long consumptionTax) {
		this.consumptionTax = consumptionTax;
	}
	public long getConsolidatedTax() {
		return consolidatedTax;
	}
	public void setConsolidatedTax(long consolidatedTax) {
		this.consolidatedTax = consolidatedTax;
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getDeletFlag() {
		return deletFlag;
	}
	public void setDeletFlag(int deletFlag) {
		this.deletFlag = deletFlag;
	}
	public long getTariff() {
		return tariff;
	}
	public void setTariff(long tariff) {
		this.tariff = tariff;
	}
	
	
}
