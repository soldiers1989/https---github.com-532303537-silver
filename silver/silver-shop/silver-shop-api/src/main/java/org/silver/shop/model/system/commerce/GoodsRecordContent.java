package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 备案商品信息
 */
public class GoodsRecordContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private int seq; // 商品序号 001-999
	private String entGoodsNo; // 企业商品自编号 企业的商品货号，不可重复
	private String entGoodsNoSKU; // 电商平台自定义的商品货号（SKU）
	private String eportGoodsNo;// 跨境公共平台商品备案申请号
	private String ciqGoodsNo; // 检验检疫商品备案编号
	private String cusGoodsNo; // 海关正式备案编号
	private String emsNo; // 账册号
	private String itemNo; // 项号 保税账册里的项号
	private String shelfGName; // 上架品名 在电商平台上的商品名称。
	private String ncadCode; // 行邮税号 商品综合分类表(NCAD)
	private String hsCode; // HS编码
	private String barCode; // 商品条形码 允许包含字母和数字
	private String goodsName; // 商品名称 商品中文名称
	private String goodsStyle; // 型号规格
	private String brand; // 商品品牌
	private String gUnit; // 申报计量单位 计量单位代码表(UNIT)
	private String stdUnit; // 第一法定计量单位 参照公共代码表
	private String secUnit; // 第二法定计量单位 参照公共代码表
	private long regPrice; // 单价 境物品：指无税的销售价格,
							// RMB价格;(默认存储以"分钱"为单位,如：100(分钱)=1(块钱))
	private String giftFlag; // 是否赠品 0-是，1-否，默认否
	private String originCountry;// 原产国 参照国别代码表
	private String quality; // 商品品质及说明 用文字描述
	private String qualityCertify;// 品质证明说明 商品品质证明性文字说明
	private String manufactory; // 生产厂家或供应商 此项填生成厂家或供应商名称
	private Double netWt; // 净重 单位KG；
	private Double grossWt; // 毛重 单位KG；
	private String notes; // 备注
	private int status;//备案状态：0-未备案，1-备案中，2-备案成功，3-备案失败
	private int recordFalg;//已备案商品状态:0-已备案,待审核,1-备案审核通过
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 修改人
	private Date updateDate;// 修改时间
	private int deletFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	private String  customsCode;//主管海关代码
	private String  ciqOrgCode;//检验检疫机构代码
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public String getEntGoodsNoSKU() {
		return entGoodsNoSKU;
	}
	public void setEntGoodsNoSKU(String entGoodsNoSKU) {
		this.entGoodsNoSKU = entGoodsNoSKU;
	}
	public String getEportGoodsNo() {
		return eportGoodsNo;
	}
	public void setEportGoodsNo(String eportGoodsNo) {
		this.eportGoodsNo = eportGoodsNo;
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
	public String getEmsNo() {
		return emsNo;
	}
	public void setEmsNo(String emsNo) {
		this.emsNo = emsNo;
	}
	public String getItemNo() {
		return itemNo;
	}
	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}
	public String getShelfGName() {
		return shelfGName;
	}
	public void setShelfGName(String shelfGName) {
		this.shelfGName = shelfGName;
	}
	public String getNcadCode() {
		return ncadCode;
	}
	public void setNcadCode(String ncadCode) {
		this.ncadCode = ncadCode;
	}
	public String getHsCode() {
		return hsCode;
	}
	public void setHsCode(String hsCode) {
		this.hsCode = hsCode;
	}
	public String getBarCode() {
		return barCode;
	}
	public void setBarCode(String barCode) {
		this.barCode = barCode;
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
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getgUnit() {
		return gUnit;
	}
	public void setgUnit(String gUnit) {
		this.gUnit = gUnit;
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
	public long getRegPrice() {
		return regPrice;
	}
	public void setRegPrice(long regPrice) {
		this.regPrice = regPrice;
	}
	public String getGiftFlag() {
		return giftFlag;
	}
	public void setGiftFlag(String giftFlag) {
		this.giftFlag = giftFlag;
	}
	public String getOriginCountry() {
		return originCountry;
	}
	public void setOriginCountry(String originCountry) {
		this.originCountry = originCountry;
	}
	public String getQuality() {
		return quality;
	}
	public void setQuality(String quality) {
		this.quality = quality;
	}
	public String getQualityCertify() {
		return qualityCertify;
	}
	public void setQualityCertify(String qualityCertify) {
		this.qualityCertify = qualityCertify;
	}
	public String getManufactory() {
		return manufactory;
	}
	public void setManufactory(String manufactory) {
		this.manufactory = manufactory;
	}
	public Double getNetWt() {
		return netWt;
	}
	public void setNetWt(Double netWt) {
		this.netWt = netWt;
	}
	public Double getGrossWt() {
		return grossWt;
	}
	public void setGrossWt(Double grossWt) {
		this.grossWt = grossWt;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getRecordFalg() {
		return recordFalg;
	}
	public void setRecordFalg(int recordFalg) {
		this.recordFalg = recordFalg;
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
	public String getCustomsCode() {
		return customsCode;
	}
	public void setCustomsCode(String customsCode) {
		this.customsCode = customsCode;
	}
	public String getCiqOrgCode() {
		return ciqOrgCode;
	}
	public void setCiqOrgCode(String ciqOrgCode) {
		this.ciqOrgCode = ciqOrgCode;
	}
	
	
}
