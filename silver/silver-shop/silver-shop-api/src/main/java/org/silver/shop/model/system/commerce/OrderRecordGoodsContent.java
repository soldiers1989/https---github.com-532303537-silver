package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 *	订单备案关联商品信息 
 *
 */
public class OrderRecordGoodsContent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1166330294512912882L;
	private long id;
	private String entOrderNo;// 电商平台的原始订单编号
	private int seq; // 商品序号 001-999
	private String entGoodsNo; // 企业商品自编号 企业的商品货号，不可重复
	private String ciqGoodsNo; // 检验检疫商品备案编号
	private String cusGoodsNo; // 海关正式备案编号
	private String hsCode; // HS编码
	private String shelfGName; // 上架品名 在电商平台上的商品名称
	private String goodsName; // 商品名称 商品中文名称
	private String goodsStyle; // 型号规格
	private String goodsDescribe ;//企业商品描述
	private String originCountry;// 原产国 参照国别代码表
	private String barCode; // 商品条形码 允许包含字母和数字
	private String brand; // 商品品牌
	private long qty;//商品数量
	private String unit; // 申报计量单位 计量单位代码表(UNIT)
	private Double price; // 单价 
	private Double total; // 总价
	private String currCode;//币制
	private String notes;//备注
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
	public String getEntOrderNo() {
		return entOrderNo;
	}
	public void setEntOrderNo(String entOrderNo) {
		this.entOrderNo = entOrderNo;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public String getEntGoodsNo() {
		return entGoodsNo;
	}
	public void setEntGoodsNo(String entGoodsNo) {
		this.entGoodsNo = entGoodsNo;
	}
	public String getCiqGoodsNo() {
		return ciqGoodsNo;
	}
	public void setCiqGoodsNo(String ciqGoodsNo) {
		this.ciqGoodsNo = ciqGoodsNo;
	}
	public String getCusGoodsNo() {
		return cusGoodsNo;
	}
	public void setCusGoodsNo(String cusGoodsNo) {
		this.cusGoodsNo = cusGoodsNo;
	}
	public String getHsCode() {
		return hsCode;
	}
	public void setHsCode(String hsCode) {
		this.hsCode = hsCode;
	}
	public String getShelfGName() {
		return shelfGName;
	}
	public void setShelfGName(String shelfGName) {
		this.shelfGName = shelfGName;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public String getGoodsStyle() {
		return goodsStyle;
	}
	public void setGoodsStyle(String goodsStyle) {
		this.goodsStyle = goodsStyle;
	}
	public String getGoodsDescribe() {
		return goodsDescribe;
	}
	public void setGoodsDescribe(String goodsDescribe) {
		this.goodsDescribe = goodsDescribe;
	}
	public String getOriginCountry() {
		return originCountry;
	}
	public void setOriginCountry(String originCountry) {
		this.originCountry = originCountry;
	}
	public String getBarCode() {
		return barCode;
	}
	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public long getQty() {
		return qty;
	}
	public void setQty(long qty) {
		this.qty = qty;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Double getTotal() {
		return total;
	}
	public void setTotal(Double total) {
		this.total = total;
	}
	public String getCurrCode() {
		return currCode;
	}
	public void setCurrCode(String currCode) {
		this.currCode = currCode;
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
