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
	private Double vat;//增值税 
	private Double consumptionTax;//消费税 
	private Double tariff;//关税税率暂设为0%
	private Double consolidatedTax;//综合税 跨境电商综合税率 = （消费税率+增值税率）/（1-消费税率）×70%
	private String createBy;//创建人
	private Date createDate;//创建时间
	private String updateBy;//更新人
	private Date updateDate;//更新时间
	private int serialNo;//顺序编号
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
	public Double getTariff() {
		return tariff;
	}
	public void setTariff(Double tariff) {
		this.tariff = tariff;
	}
	public Double getConsolidatedTax() {
		return consolidatedTax;
	}
	public void setConsolidatedTax(Double consolidatedTax) {
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
	public int getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(int serialNo) {
		this.serialNo = serialNo;
	}
	
}
