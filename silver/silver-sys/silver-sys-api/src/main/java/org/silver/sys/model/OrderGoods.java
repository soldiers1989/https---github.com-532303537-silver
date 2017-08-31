package org.silver.sys.model;

import java.io.Serializable;
/**
 * 订单关联商品明细表
 * @author zhangxin 2017/8/25
 *
 */
public class OrderGoods implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String entOrderNo; // 企业电子订单编号
	private Long seq;	         //商品序号
	private String entGoodsNo;   //企业商品自编号
	private String ciqGoodsNo;   //检验检疫商品备案编号
	private String cusGoodsNo;   //海关正式备案号
	private String hsCode;       //HS编码
	private String goodsName;    //企业商品名称
	private String goodsStyle;   //规格型号
	private String goodsDescribe;//企业商品描述
	private String originCountry;//原产国
	private String barCode;	     //商品条形码
	private String brand;        //品牌
	private Long qty;	         //数量
	private String unit;         //计量单位
	private Double price;	         //单价
	private Double total;        //总价
	private String currCode;     //币制
	private String notes;        //备注
	private String sku;          //填启邦提供的商品备案信息中的客户SKU
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEntOrderNo() {
		return entOrderNo;
	}
	public void setEntOrderNo(String entOrderNo) {
		this.entOrderNo = entOrderNo;
	}
	public Long getSeq() {
		return seq;
	}
	public void setSeq(Long seq) {
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
	public Long getQty() {
		return qty;
	}
	public void setQty(Long qty) {
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
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
