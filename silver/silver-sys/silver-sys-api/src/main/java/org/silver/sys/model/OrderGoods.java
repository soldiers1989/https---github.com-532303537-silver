package org.silver.sys.model;

import java.io.Serializable;
/**
 * 订单关联商品明细表
 * @author zhangxin 2017/8/25
 *
 */
public class OrderGoods implements Serializable {

	private static final long serialVersionUID = 1L;
	private long id;
	private String OrgMessageID; //报文编号
	private String EntOrderNo;   //企业电子订单编号
	private int Seq;	         //商品序号
	private String EntGoodsNo;   //企业商品自编号
	private String CIQGoodsNo;   //检验检疫商品备案编号
	private String CusGoodsNo;   //海关正式备案号
	private String HSCode;       //HS编码
	private String GoodsName;    //企业商品名称
	private String GoodsStyle;   //规格型号
	private String GoodsDescribe;//企业商品描述
	private String OriginCountry;//原产国
	private String BarCode;	     //商品条形码
	private String Brand;        //品牌
	private int Qty;	         //数量
	private String Unit;         //计量单位
	private Double Price;	     //单价
	private Double Total;        //总价
	private String CurrCode;     //币制
	private String Notes;        //备注
	private String sku;          //填启邦提供的商品备案信息中的客户SKU
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getEntGoodsNo() {
		return EntGoodsNo;
	}
	public void setEntGoodsNo(String entGoodsNo) {
		EntGoodsNo = entGoodsNo;
	}
	public String getOrgMessageID() {
		return OrgMessageID;
	}
	public void setOrgMessageID(String orgMessageID) {
		OrgMessageID = orgMessageID;
	}
	public String getEntOrderNo() {
		return EntOrderNo;
	}
	public void setEntOrderNo(String entOrderNo) {
		EntOrderNo = entOrderNo;
	}
	public int getSeq() {
		return Seq;
	}
	public void setSeq(int seq) {
		Seq = seq;
	}
	public String getCIQGoodsNo() {
		return CIQGoodsNo;
	}
	public void setCIQGoodsNo(String cIQGoodsNo) {
		CIQGoodsNo = cIQGoodsNo;
	}
	public String getCusGoodsNo() {
		return CusGoodsNo;
	}
	public void setCusGoodsNo(String cusGoodsNo) {
		CusGoodsNo = cusGoodsNo;
	}
	public String getHSCode() {
		return HSCode;
	}
	public void setHSCode(String hSCode) {
		HSCode = hSCode;
	}
	public String getGoodsName() {
		return GoodsName;
	}
	public void setGoodsName(String goodsName) {
		GoodsName = goodsName;
	}
	public String getGoodsStyle() {
		return GoodsStyle;
	}
	public void setGoodsStyle(String goodsStyle) {
		GoodsStyle = goodsStyle;
	}
	public String getGoodsDescribe() {
		return GoodsDescribe;
	}
	public void setGoodsDescribe(String goodsDescribe) {
		GoodsDescribe = goodsDescribe;
	}
	public String getOriginCountry() {
		return OriginCountry;
	}
	public void setOriginCountry(String originCountry) {
		OriginCountry = originCountry;
	}
	public String getBarCode() {
		return BarCode;
	}
	public void setBarCode(String barCode) {
		BarCode = barCode;
	}
	public String getBrand() {
		return Brand;
	}
	public void setBrand(String brand) {
		Brand = brand;
	}
	public int getQty() {
		return Qty;
	}
	public void setQty(int qty) {
		Qty = qty;
	}
	public String getUnit() {
		return Unit;
	}
	public void setUnit(String unit) {
		Unit = unit;
	}
	public Double getPrice() {
		return Price;
	}
	public void setPrice(Double price) {
		Price = price;
	}
	public Double getTotal() {
		return Total;
	}
	public void setTotal(Double total) {
		Total = total;
	}
	public String getCurrCode() {
		return CurrCode;
	}
	public void setCurrCode(String currCode) {
		CurrCode = currCode;
	}
	public String getNotes() {
		return Notes;
	}
	public void setNotes(String notes) {
		Notes = notes;
	}
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	
	
	
}
