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
	private String goodsMerchantName;//商品归属商戶名
	private String goodsName;// 商品名称
	private String goodsFirstType;// 商品类型第一级
	private String goodsSecondType;// 商品类型第二级
	private String goodsThirdType;// 商品类型第三级
	private String goodsImage;// 商品展示图片
	private String goodsDetail;// 商品详情
	private String goodsBrand;// 商品品牌
	private String goodsStyle;// 商品规格
	private String goodsUnit;// 申报计量单位
	private long goodsRegPrice;// 商品价格(默认存储以"分钱"为单位,如：100(分钱)=1(块钱))
	private String goodsOriginCountry;// 商品原厂国
	private String goodsBarCode;// 商品货号(条形码)
	private String ymYear;//商品录入年份
	private String createBy;//创建人
	private Date createDate;//创建时间
	private String updateBy;//修改人
	private Date updateDate;//修改时间
	private int deletFlag;//删除标识:0-未删除,1-已删除
	private String deleteBy;//删除人
	private Date deleteDate;//删除时间
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
	public String getGoodsFirstType() {
		return goodsFirstType;
	}
	public void setGoodsFirstType(String goodsFirstType) {
		this.goodsFirstType = goodsFirstType;
	}
	public String getGoodsSecondType() {
		return goodsSecondType;
	}
	public void setGoodsSecondType(String goodsSecondType) {
		this.goodsSecondType = goodsSecondType;
	}
	public String getGoodsThirdType() {
		return goodsThirdType;
	}
	public void setGoodsThirdType(String goodsThirdType) {
		this.goodsThirdType = goodsThirdType;
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
	public long getGoodsRegPrice() {
		return goodsRegPrice;
	}
	public void setGoodsRegPrice(long goodsRegPrice) {
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
	
	public String getYmYear() {
		return ymYear;
	}
	public void setYmYear(String ymYear) {
		this.ymYear = ymYear;
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
	public int getDeletFlag() {
		return deletFlag;
	}
	public void setDeletFlag(int deletFlag) {
		this.deletFlag = deletFlag;
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
