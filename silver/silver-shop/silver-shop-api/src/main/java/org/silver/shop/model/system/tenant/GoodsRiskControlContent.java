package org.silver.shop.model.system.tenant;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品风控信息实体类
 */
public class GoodsRiskControlContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6970653525587003998L;
	private long id;
	private String riskNo;// 风控流水号
	private String entGoodsNo;// 商品自编号
	private String hsCode;// HS编码
	private String goodsName;// 商品名称
	private String goodsStyle;// 规格
	private String goodsBrand;// 品牌
	private Double regPrice;// 商城商品单价
	private Double platformOnePrice;// 平台一价格
	private Double platformTwoPrice;// 平台三价格
	private Double platformThreePrivce;// 平台四价格
	private Double platformFourPrice;// 平台五价格
	private Double referencePrice;// 参考价格
	private String remake;//
	private String note;//
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
	public String getRiskNo() {
		return riskNo;
	}
	public String getEntGoodsNo() {
		return entGoodsNo;
	}
	public String getHsCode() {
		return hsCode;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public String getGoodsStyle() {
		return goodsStyle;
	}
	public String getGoodsBrand() {
		return goodsBrand;
	}
	public Double getRegPrice() {
		return regPrice;
	}
	public Double getPlatformOnePrice() {
		return platformOnePrice;
	}
	public Double getPlatformTwoPrice() {
		return platformTwoPrice;
	}
	public Double getPlatformThreePrivce() {
		return platformThreePrivce;
	}
	public Double getPlatformFourPrice() {
		return platformFourPrice;
	}
	public Double getReferencePrice() {
		return referencePrice;
	}
	public String getRemake() {
		return remake;
	}
	public String getNote() {
		return note;
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
	public void setRiskNo(String riskNo) {
		this.riskNo = riskNo;
	}
	public void setEntGoodsNo(String entGoodsNo) {
		this.entGoodsNo = entGoodsNo;
	}
	public void setHsCode(String hsCode) {
		this.hsCode = hsCode;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public void setGoodsStyle(String goodsStyle) {
		this.goodsStyle = goodsStyle;
	}
	public void setGoodsBrand(String goodsBrand) {
		this.goodsBrand = goodsBrand;
	}
	public void setRegPrice(Double regPrice) {
		this.regPrice = regPrice;
	}
	public void setPlatformOnePrice(Double platformOnePrice) {
		this.platformOnePrice = platformOnePrice;
	}
	public void setPlatformTwoPrice(Double platformTwoPrice) {
		this.platformTwoPrice = platformTwoPrice;
	}
	public void setPlatformThreePrivce(Double platformThreePrivce) {
		this.platformThreePrivce = platformThreePrivce;
	}
	public void setPlatformFourPrice(Double platformFourPrice) {
		this.platformFourPrice = platformFourPrice;
	}
	public void setReferencePrice(Double referencePrice) {
		this.referencePrice = referencePrice;
	}
	public void setRemake(String remake) {
		this.remake = remake;
	}
	public void setNote(String note) {
		this.note = note;
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
