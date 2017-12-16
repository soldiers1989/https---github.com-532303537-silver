package org.silver.shop.model.system.manual;

import java.io.Serializable;
import java.util.Date;

public class MorderSub implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3030558475180992948L;
	private long id;
	private String order_id;// 关联的主表订单id
	private int seq;// 序号
	private String EntGoodsNo;// 商品自编号
	private String HSCode;// HS编码
	private String GoodsName;// 商品名
	private String CusGoodsNo;// 海关正式备案编号
	private String CIQGoodsNo;// 检验检疫商品备案编号
	private String OriginCountry;// 原产国
	private String GoodsStyle;// 规格型号
	private String BarCode;// 条形码
	private String Brand;// 品牌
	private int Qty;// 数量
	private String Unit;// 计量单位
	private double Price;// 单价
	private double Total;// 总价
	private Date create_date;

	private double netWt; // 净重
	private double grossWt;// 毛重
	private double firstLegalCount;// 第一法定数量
	private double secondLegalCount;//  第二法定数量
	private String stdUnit ;//  第一法定计量单位
	private String secUnit	;//  第二法定计量单位
	private int numOfPackages ;//  箱件数 （同一订单商品总件数，例如同一订单有6支护手霜和4瓶钙片则填写10，即6+4=10。)
	private int packageType ;//  包装种类
	private String transportModel;//  运输方式

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOrder_id() {
		return order_id;
	}

	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public String getEntGoodsNo() {
		return EntGoodsNo;
	}

	public void setEntGoodsNo(String entGoodsNo) {
		EntGoodsNo = entGoodsNo;
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

	public String getCusGoodsNo() {
		return CusGoodsNo;
	}

	public void setCusGoodsNo(String cusGoodsNo) {
		CusGoodsNo = cusGoodsNo;
	}

	public String getCIQGoodsNo() {
		return CIQGoodsNo;
	}

	public void setCIQGoodsNo(String cIQGoodsNo) {
		CIQGoodsNo = cIQGoodsNo;
	}

	public String getOriginCountry() {
		return OriginCountry;
	}

	public void setOriginCountry(String originCountry) {
		OriginCountry = originCountry;
	}

	public String getGoodsStyle() {
		return GoodsStyle;
	}

	public void setGoodsStyle(String goodsStyle) {
		GoodsStyle = goodsStyle;
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

	public double getPrice() {
		return Price;
	}

	public void setPrice(double price) {
		Price = price;
	}

	public double getTotal() {
		return Total;
	}

	public void setTotal(double total) {
		Total = total;
	}

	public Date getCreate_date() {
		return create_date;
	}

	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}

	public double getNetWt() {
		return netWt;
	}

	public void setNetWt(double netWt) {
		this.netWt = netWt;
	}

	public double getGrossWt() {
		return grossWt;
	}

	public void setGrossWt(double grossWt) {
		this.grossWt = grossWt;
	}

	public double getFirstLegalCount() {
		return firstLegalCount;
	}

	public void setFirstLegalCount(double firstLegalCount) {
		this.firstLegalCount = firstLegalCount;
	}

	public double getSecondLegalCount() {
		return secondLegalCount;
	}

	public void setSecondLegalCount(double secondLegalCount) {
		this.secondLegalCount = secondLegalCount;
	}

	public String getStdUnit() {
		return stdUnit;
	}

	public void setStdUnit(String stdUnit) {
		this.stdUnit = stdUnit;
	}

	public String getSecUnit() {
		return secUnit;
	}

	public void setSecUnit(String secUnit) {
		this.secUnit = secUnit;
	}

	public int getNumOfPackages() {
		return numOfPackages;
	}

	public void setNumOfPackages(int numOfPackages) {
		this.numOfPackages = numOfPackages;
	}

	public int getPackageType() {
		return packageType;
	}

	public void setPackageType(int packageType) {
		this.packageType = packageType;
	}

	public String getTransportModel() {
		return transportModel;
	}

	public void setTransportModel(String transportModel) {
		this.transportModel = transportModel;
	}

}
