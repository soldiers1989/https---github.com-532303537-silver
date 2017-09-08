package org.silver.sys.model;

import java.io.Serializable;
/**
 * 备案商品表
 * @author zhangxin  2019/9/6
 *
 */
public class GoodsInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;
	private String OrgMessageID; //原始报文编号
	private int Seq; // 商品序号    1-999
	private String EntGoodsNo;// 企业商品自编号
	private String EPortGoodsNo;// 跨境公共平台商品备案申请号
	private String CIQGoodsNo;// 检验检疫商品备案编号
	private String CusGoodsNo;// 海关正式备案编号
	private String EmsNo;// 账册号
	private String ItemNo ;// 项号
	private String ShelfGName;// 上架品名
	private String NcadCode;// 行邮税号
	private String HSCode;// HS编码
	private String BarCode;// 商品条形码
	private String GoodsName;// 商品名称
	private String GoodsStyle;// 型号规格
	private String Brand;// 品牌
	private String GUnit;// 申报计量单位
	private String StdUnit;// 第一法定计量单位
	private String SecUnit;// 第二法定计量单位
	private double RegPrice;// 单价
	private String GiftFlag;// 是否赠品:0-是，1-否，默认否
	private String OriginCountry;// 原产国
	private String Quality;// 商品品质及说明
	private String QualityCertify;// 品质证明说明
	private String Manufactory;// 生产厂家或供应商
	private double NetWt;// 净重
	private double GrossWt;// 毛重
	private String Notes;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getOrgMessageID() {
		return OrgMessageID;
	}
	public void setOrgMessageID(String orgMessageID) {
		OrgMessageID = orgMessageID;
	}
	public int getSeq() {
		return Seq;
	}
	public void setSeq(int seq) {
		Seq = seq;
	}
	public String getEntGoodsNo() {
		return EntGoodsNo;
	}
	public void setEntGoodsNo(String entGoodsNo) {
		EntGoodsNo = entGoodsNo;
	}
	public String getEPortGoodsNo() {
		return EPortGoodsNo;
	}
	public void setEPortGoodsNo(String ePortGoodsNo) {
		EPortGoodsNo = ePortGoodsNo;
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
	public String getEmsNo() {
		return EmsNo;
	}
	public void setEmsNo(String emsNo) {
		EmsNo = emsNo;
	}
	public String getItemNo() {
		return ItemNo;
	}
	public void setItemNo(String itemNo) {
		ItemNo = itemNo;
	}
	public String getShelfGName() {
		return ShelfGName;
	}
	public void setShelfGName(String shelfGName) {
		ShelfGName = shelfGName;
	}
	public String getNcadCode() {
		return NcadCode;
	}
	public void setNcadCode(String ncadCode) {
		NcadCode = ncadCode;
	}
	public String getHSCode() {
		return HSCode;
	}
	public void setHSCode(String hSCode) {
		HSCode = hSCode;
	}
	public String getBarCode() {
		return BarCode;
	}
	public void setBarCode(String barCode) {
		BarCode = barCode;
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
	public String getBrand() {
		return Brand;
	}
	public void setBrand(String brand) {
		Brand = brand;
	}
	public String getGUnit() {
		return GUnit;
	}
	public void setGUnit(String gUnit) {
		GUnit = gUnit;
	}
	public String getStdUnit() {
		return StdUnit;
	}
	public void setStdUnit(String stdUnit) {
		StdUnit = stdUnit;
	}
	public String getSecUnit() {
		return SecUnit;
	}
	public void setSecUnit(String secUnit) {
		SecUnit = secUnit;
	}
	public double getRegPrice() {
		return RegPrice;
	}
	public void setRegPrice(double regPrice) {
		RegPrice = regPrice;
	}
	public String getGiftFlag() {
		return GiftFlag;
	}
	public void setGiftFlag(String giftFlag) {
		GiftFlag = giftFlag;
	}
	public String getOriginCountry() {
		return OriginCountry;
	}
	public void setOriginCountry(String originCountry) {
		OriginCountry = originCountry;
	}
	public String getQuality() {
		return Quality;
	}
	public void setQuality(String quality) {
		Quality = quality;
	}
	public String getQualityCertify() {
		return QualityCertify;
	}
	public void setQualityCertify(String qualityCertify) {
		QualityCertify = qualityCertify;
	}
	public String getManufactory() {
		return Manufactory;
	}
	public void setManufactory(String manufactory) {
		Manufactory = manufactory;
	}
	public double getNetWt() {
		return NetWt;
	}
	public void setNetWt(double netWt) {
		NetWt = netWt;
	}
	public double getGrossWt() {
		return GrossWt;
	}
	public void setGrossWt(double grossWt) {
		GrossWt = grossWt;
	}
	public String getNotes() {
		return Notes;
	}
	public void setNotes(String notes) {
		Notes = notes;
	}
	
	
	
}
