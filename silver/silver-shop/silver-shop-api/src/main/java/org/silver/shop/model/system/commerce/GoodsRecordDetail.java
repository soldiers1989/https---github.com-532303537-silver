package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 备案商品详细信息
 */
public class GoodsRecordDetail implements Serializable {

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
	private String shelfGName; // 上架品名 在电商平台上的商品名称
	private String ncadCode; // 行邮税号 商品综合分类表(NCAD)
	private String hsCode; // HS编码
	private String barCode; // 商品条形码 允许包含字母和数字
	private String goodsName; // 商品名称 商品中文名称
	private String goodsStyle; // 型号规格
	private String brand; // 商品品牌
	private String gUnit; // 申报计量单位 计量单位代码表(UNIT)
	private String stdUnit; // 第一法定计量单位 参照公共代码表
	private String secUnit; // 第二法定计量单位 参照公共代码表
	private Double regPrice; // 单价 境物品：指无税的销售价格, RMB价格
	private String giftFlag; // 是否赠品 0-是，1-否，默认否
	private String originCountry;// 原产国 参照国别代码表
	private String quality; // 商品品质及说明 用文字描述
	private String qualityCertify;// 品质证明说明 商品品质证明性文字说明
	private String manufactory; // 生产厂家或供应商 此项填生成厂家或供应商名称
	private Double netWt; // 净重 单位KG
	private Double grossWt; // 毛重 单位KG
	private String notes; // 备注
	private int status;//备案状态：0-未备案，1-备案中，2-备案成功，3-备案失败
	private int recordFlag;//已备案商品状态:0-已备案,待审核,1-备案审核通过
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 修改人
	private Date updateDate;// 修改时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	private String goodsMerchantId;//归属商户ID
	private String goodsMerchantName;//商品归属商戶名
	private String goodsSerialNo;// 备案所属商品流水号
	private String reSerialNo;//商品备案信息接受后返回流水号
	private String goodsDetailId;//商品基本信息ID
	private String reNote;//备案商品返回信息
	
	private String spareGoodsName;// (备用字段,可修改)商品名称
	private String spareGoodsFirstTypeId;// (备用字段,可修改)商品类型第一级Id
	private String spareGoodsFirstTypeName;//(备用字段,可修改)商品类型第一级名称
	private String spareGoodsSecondTypeId;// (备用字段,可修改)商品类型第二级Id
	private String spareGoodsSecondTypeName;// (备用字段,可修改)商品类型第二级名称
	private String spareGoodsThirdTypeId;// (备用字段,可修改)商品类型第三级Id
	private String spareGoodsThirdTypeName;// (备用字段,可修改)商品类型第三级名称
	private String spareGoodsImage;// (备用字段,可修改)商品展示图片
	private String spareGoodsDetail;// (备用字段,可修改)商品详情
	private String spareGoodsBrand;// (备用字段,可修改)商品品牌
	private String spareGoodsStyle;// (备用字段,可修改)商品规格
	private String spareGoodsUnit;// (备用字段,可修改)申报计量单位
	private String spareGoodsOriginCountry;// (备用字段,可修改)商品原厂国
	private String spareGoodsBarCode;// (备用字段,可修改)商品货号(条形码)
	
	private int taxFlag;//计算税费标识：1-计算税费,2-不计税费;默认为：1
	private int freightFlag;//计算(国内快递)物流费标识：1-无运费,2-手动设置运费;默认为：1
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
	
	public Double getRegPrice() {
		return regPrice;
	}
	public void setRegPrice(Double regPrice) {
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
	
	public int getRecordFlag() {
		return recordFlag;
	}
	public void setRecordFlag(int recordFlag) {
		this.recordFlag = recordFlag;
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
	
	public String getGoodsMerchantName() {
		return goodsMerchantName;
	}
	public void setGoodsMerchantName(String goodsMerchantName) {
		this.goodsMerchantName = goodsMerchantName;
	}
	public String getGoodsSerialNo() {
		return goodsSerialNo;
	}
	public void setGoodsSerialNo(String goodsSerialNo) {
		this.goodsSerialNo = goodsSerialNo;
	}
	public String getGoodsMerchantId() {
		return goodsMerchantId;
	}
	public void setGoodsMerchantId(String goodsMerchantId) {
		this.goodsMerchantId = goodsMerchantId;
	}
	public String getReSerialNo() {
		return reSerialNo;
	}
	public void setReSerialNo(String reSerialNo) {
		this.reSerialNo = reSerialNo;
	}
	public String getGoodsDetailId() {
		return goodsDetailId;
	}
	public void setGoodsDetailId(String goodsDetailId) {
		this.goodsDetailId = goodsDetailId;
	}
	public String getReNote() {
		return reNote;
	}
	public void setReNote(String reNote) {
		this.reNote = reNote;
	}
	public String getSpareGoodsName() {
		return spareGoodsName;
	}
	public void setSpareGoodsName(String spareGoodsName) {
		this.spareGoodsName = spareGoodsName;
	}
	public String getSpareGoodsFirstTypeId() {
		return spareGoodsFirstTypeId;
	}
	public void setSpareGoodsFirstTypeId(String spareGoodsFirstTypeId) {
		this.spareGoodsFirstTypeId = spareGoodsFirstTypeId;
	}
	public String getSpareGoodsFirstTypeName() {
		return spareGoodsFirstTypeName;
	}
	public void setSpareGoodsFirstTypeName(String spareGoodsFirstTypeName) {
		this.spareGoodsFirstTypeName = spareGoodsFirstTypeName;
	}
	public String getSpareGoodsSecondTypeId() {
		return spareGoodsSecondTypeId;
	}
	public void setSpareGoodsSecondTypeId(String spareGoodsSecondTypeId) {
		this.spareGoodsSecondTypeId = spareGoodsSecondTypeId;
	}
	public String getSpareGoodsSecondTypeName() {
		return spareGoodsSecondTypeName;
	}
	public void setSpareGoodsSecondTypeName(String spareGoodsSecondTypeName) {
		this.spareGoodsSecondTypeName = spareGoodsSecondTypeName;
	}
	public String getSpareGoodsThirdTypeId() {
		return spareGoodsThirdTypeId;
	}
	public void setSpareGoodsThirdTypeId(String spareGoodsThirdTypeId) {
		this.spareGoodsThirdTypeId = spareGoodsThirdTypeId;
	}
	public String getSpareGoodsThirdTypeName() {
		return spareGoodsThirdTypeName;
	}
	public void setSpareGoodsThirdTypeName(String spareGoodsThirdTypeName) {
		this.spareGoodsThirdTypeName = spareGoodsThirdTypeName;
	}
	public String getSpareGoodsImage() {
		return spareGoodsImage;
	}
	public void setSpareGoodsImage(String spareGoodsImage) {
		this.spareGoodsImage = spareGoodsImage;
	}
	public String getSpareGoodsDetail() {
		return spareGoodsDetail;
	}
	public void setSpareGoodsDetail(String spareGoodsDetail) {
		this.spareGoodsDetail = spareGoodsDetail;
	}
	public String getSpareGoodsBrand() {
		return spareGoodsBrand;
	}
	public void setSpareGoodsBrand(String spareGoodsBrand) {
		this.spareGoodsBrand = spareGoodsBrand;
	}
	public String getSpareGoodsStyle() {
		return spareGoodsStyle;
	}
	public void setSpareGoodsStyle(String spareGoodsStyle) {
		this.spareGoodsStyle = spareGoodsStyle;
	}
	public String getSpareGoodsUnit() {
		return spareGoodsUnit;
	}
	public void setSpareGoodsUnit(String spareGoodsUnit) {
		this.spareGoodsUnit = spareGoodsUnit;
	}
	
	public String getSpareGoodsOriginCountry() {
		return spareGoodsOriginCountry;
	}
	public void setSpareGoodsOriginCountry(String spareGoodsOriginCountry) {
		this.spareGoodsOriginCountry = spareGoodsOriginCountry;
	}
	public String getSpareGoodsBarCode() {
		return spareGoodsBarCode;
	}
	public void setSpareGoodsBarCode(String spareGoodsBarCode) {
		this.spareGoodsBarCode = spareGoodsBarCode;
	}
	public int getTaxFlag() {
		return taxFlag;
	}
	public void setTaxFlag(int taxFlag) {
		this.taxFlag = taxFlag;
	}
	public int getFreightFlag() {
		return freightFlag;
	}
	public void setFreightFlag(int freightFlag) {
		this.freightFlag = freightFlag;
	}
	
}
