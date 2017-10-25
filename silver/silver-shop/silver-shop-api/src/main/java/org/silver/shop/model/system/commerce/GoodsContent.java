package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品基本信息
 */
public class GoodsContent implements Serializable {

	/**
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private String goodsId;// 商品ID(兼商品自编号)
	private String goodsMerchantId;//商品归属商戶id
	private String goodsMerchantName;//商品归属商戶名
	private String goodsName;// 商品名称
	private String goodsFirstTypeId;// 商品类型第一级
	private String goodsSecondTypeId;// 商品类型第二级
	private String goodsThirdTypeId;// 商品类型第三级
	private String goodsImage;// 商品展示图片
	private String goodsDetail;// 商品详情
	private String goodsBrand;// 商品品牌
	private String goodsStyle;// 商品规格
	private String goodsUnit;// 申报计量单位
	private Double goodsRegPrice;// 商品价格
	private String goodsOriginCountry;// 商品原厂国
	private String goodsBarCode;// 商品货号(条形码)
	private String goodsYear;//商品录入年份
	private String createBy;//创建人
	private Date createDate;//创建时间
	private String updateBy;//修改人
	private Date updateDate;//修改时间
	private int deleteFlag;//删除标识:0-未删除,1-已删除
	private String deleteBy;//删除人
	private Date deleteDate;//删除时间
	
	
	private String goodsFirstTypeName;//商品类型第一级名称
	private String goodsSecondTypeName;// 商品类型第二级名称
	private String goodsThirdTypeName;// 商品类型第三级名称
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}
	public String getGoodsMerchantName() {
		return goodsMerchantName;
	}
	public void setGoodsMerchantName(String goodsMerchantName) {
		this.goodsMerchantName = goodsMerchantName;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	
	public String getGoodsImage() {
		return goodsImage;
	}
	public void setGoodsImage(String goodsImage) {
		this.goodsImage = goodsImage;
	}
	public String getGoodsDetail() {
		return goodsDetail;
	}
	public void setGoodsDetail(String goodsDetail) {
		this.goodsDetail = goodsDetail;
	}
	public String getGoodsBrand() {
		return goodsBrand;
	}
	public void setGoodsBrand(String goodsBrand) {
		this.goodsBrand = goodsBrand;
	}
	public String getGoodsStyle() {
		return goodsStyle;
	}
	public void setGoodsStyle(String goodsStyle) {
		this.goodsStyle = goodsStyle;
	}
	public String getGoodsUnit() {
		return goodsUnit;
	}
	public void setGoodsUnit(String goodsUnit) {
		this.goodsUnit = goodsUnit;
	}
	
	public Double getGoodsRegPrice() {
		return goodsRegPrice;
	}
	public void setGoodsRegPrice(Double goodsRegPrice) {
		this.goodsRegPrice = goodsRegPrice;
	}
	public String getGoodsOriginCountry() {
		return goodsOriginCountry;
	}
	public void setGoodsOriginCountry(String goodsOriginCountry) {
		this.goodsOriginCountry = goodsOriginCountry;
	}
	public String getGoodsBarCode() {
		return goodsBarCode;
	}
	public void setGoodsBarCode(String goodsBarCode) {
		this.goodsBarCode = goodsBarCode;
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
	public String getGoodsYear() {
		return goodsYear;
	}
	public void setGoodsYear(String goodsYear) {
		this.goodsYear = goodsYear;
	}
	public String getGoodsMerchantId() {
		return goodsMerchantId;
	}
	public void setGoodsMerchantId(String goodsMerchantId) {
		this.goodsMerchantId = goodsMerchantId;
	}
	public String getGoodsFirstTypeId() {
		return goodsFirstTypeId;
	}
	public void setGoodsFirstTypeId(String goodsFirstTypeId) {
		this.goodsFirstTypeId = goodsFirstTypeId;
	}
	public String getGoodsSecondTypeId() {
		return goodsSecondTypeId;
	}
	public void setGoodsSecondTypeId(String goodsSecondTypeId) {
		this.goodsSecondTypeId = goodsSecondTypeId;
	}
	public String getGoodsThirdTypeId() {
		return goodsThirdTypeId;
	}
	public void setGoodsThirdTypeId(String goodsThirdTypeId) {
		this.goodsThirdTypeId = goodsThirdTypeId;
	}
	public String getGoodsFirstTypeName() {
		return goodsFirstTypeName;
	}
	public void setGoodsFirstTypeName(String goodsFirstTypeName) {
		this.goodsFirstTypeName = goodsFirstTypeName;
	}
	public String getGoodsSecondTypeName() {
		return goodsSecondTypeName;
	}
	public void setGoodsSecondTypeName(String goodsSecondTypeName) {
		this.goodsSecondTypeName = goodsSecondTypeName;
	}
	public String getGoodsThirdTypeName() {
		return goodsThirdTypeName;
	}
	public void setGoodsThirdTypeName(String goodsThirdTypeName) {
		this.goodsThirdTypeName = goodsThirdTypeName;
	}
	
	
	
}
