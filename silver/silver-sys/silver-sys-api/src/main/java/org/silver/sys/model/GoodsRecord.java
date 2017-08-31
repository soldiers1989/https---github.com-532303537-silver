package org.silver.sys.model;

import java.io.Serializable;
import java.util.Date;
/**
 * 商品备案信息存储表
 * @author zhangxin
 *
 */
public class GoodsRecord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String declEntNo; // 申报企业编号
	private String declEntName; // 申报企业名称
	private String ebEntNo; // 电商企业编号
	private String ebEntName; // 电商企业名称
	private String opType; // 操作方式 A-新增；M-修改；D-取消备案；
	private String customsCode; // 主管海关代码
	private String ciqOrgCode; // 检验检疫机构代码
	private String ebpEntNo; // 电商平台企业编号
	private String ebpEntName; // 电商平台名称
	private String currCode; // 币制代码
	private String businessType; // 跨境业务类型 1-特殊监管区域BBC保税进口 2-保税仓库BBC保税进口；
	private String inputDate; // 录入日期 YYYYMMDDhhmmss
	private String declTime; // 申请备案时间 YYYYMMDDhhmmss
	private String ieFlag; // 进出境标志 I-进，E-出
	private Long seq; // 商品序号    1-999
	private String entGoodsNo;// 企业商品自编号
	private String eportGoodsNo;// 跨境公共平台商品备案申请号
	private String ciqGoodsNo;// 检验检疫商品备案编号
	private String cusGoodsNo;// 海关正式备案编号
	private String emsNo;// 账册号
	private String itemNo;// 项号
	private String shelfGname;// 上架品名
	private String ncadCode;// 行邮税号
	private String hsCode;// HS编码
	private String barCode;// 商品条形码
	private String goodsName;// 商品名称
	private String goodsStyle;// 型号规格
	private String brand;// 品牌
	private String gunit;// 申报计量单位
	private String stdUnit;// 第一法定计量单位
	private String secUnit;// 第二法定计量单位
	private double regPrice;// 单价
	private String giftFlag;// 是否赠品:0-是，1-否，默认否
	private String originCountry;// 原产国
	private String quality;// 商品品质及说明
	private String qualityCertify;// 品质证明说明
	private String manufactory;// 生产厂家或供应商
	private double netwt;// 净重
	private double grosswt;// 毛重
	private String OrgMessageID; //原始报文编号
	private String ciqNotes;// 国检审核备注
	private String ciqStatus;// 国检审核状态
	private String cusNotes;// 海关审核备注
	private String cusStatus;// 海关审核状态
	private String status;// 发送状态
	private int del_flag;// 0正常 1删除
	private Date create_date; // 创建时间
	private String create_by; // 创建人
	private Date update_date; // 更新时间
	private String update_by;// 更新人
	private String remarks;// 备注
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDeclEntNo() {
		return declEntNo;
	}
	public void setDeclEntNo(String declEntNo) {
		this.declEntNo = declEntNo;
	}
	public String getDeclEntName() {
		return declEntName;
	}
	public void setDeclEntName(String declEntName) {
		this.declEntName = declEntName;
	}
	public String getEbEntNo() {
		return ebEntNo;
	}
	public void setEbEntNo(String ebEntNo) {
		this.ebEntNo = ebEntNo;
	}
	public String getEbEntName() {
		return ebEntName;
	}
	public void setEbEntName(String ebEntName) {
		this.ebEntName = ebEntName;
	}
	public String getOpType() {
		return opType;
	}
	public void setOpType(String opType) {
		this.opType = opType;
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
	public String getEbpEntNo() {
		return ebpEntNo;
	}
	public void setEbpEntNo(String ebpEntNo) {
		this.ebpEntNo = ebpEntNo;
	}
	public String getEbpEntName() {
		return ebpEntName;
	}
	public void setEbpEntName(String ebpEntName) {
		this.ebpEntName = ebpEntName;
	}
	public String getCurrCode() {
		return currCode;
	}
	public void setCurrCode(String currCode) {
		this.currCode = currCode;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	public String getInputDate() {
		return inputDate;
	}
	public void setInputDate(String inputDate) {
		this.inputDate = inputDate;
	}
	public String getDeclTime() {
		return declTime;
	}
	public void setDeclTime(String declTime) {
		this.declTime = declTime;
	}
	public String getIeFlag() {
		return ieFlag;
	}
	public void setIeFlag(String ieFlag) {
		this.ieFlag = ieFlag;
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
	public String getShelfGname() {
		return shelfGname;
	}
	public void setShelfGname(String shelfGname) {
		this.shelfGname = shelfGname;
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
	public String getGunit() {
		return gunit;
	}
	public void setGunit(String gunit) {
		this.gunit = gunit;
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
	public double getRegPrice() {
		return regPrice;
	}
	public void setRegPrice(double regPrice) {
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
	public double getNetwt() {
		return netwt;
	}
	public void setNetwt(double netwt) {
		this.netwt = netwt;
	}
	public double getGrosswt() {
		return grosswt;
	}
	public void setGrosswt(double grosswt) {
		this.grosswt = grosswt;
	}
	public String getOrgMessageID() {
		return OrgMessageID;
	}
	public void setOrgMessageID(String orgMessageID) {
		OrgMessageID = orgMessageID;
	}
	public String getCiqNotes() {
		return ciqNotes;
	}
	public void setCiqNotes(String ciqNotes) {
		this.ciqNotes = ciqNotes;
	}
	public String getCiqStatus() {
		return ciqStatus;
	}
	public void setCiqStatus(String ciqStatus) {
		this.ciqStatus = ciqStatus;
	}
	public String getCusNotes() {
		return cusNotes;
	}
	public void setCusNotes(String cusNotes) {
		this.cusNotes = cusNotes;
	}
	public String getCusStatus() {
		return cusStatus;
	}
	public void setCusStatus(String cusStatus) {
		this.cusStatus = cusStatus;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getDel_flag() {
		return del_flag;
	}
	public void setDel_flag(int del_flag) {
		this.del_flag = del_flag;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	public String getCreate_by() {
		return create_by;
	}
	public void setCreate_by(String create_by) {
		this.create_by = create_by;
	}
	public Date getUpdate_date() {
		return update_date;
	}
	public void setUpdate_date(Date update_date) {
		this.update_date = update_date;
	}
	public String getUpdate_by() {
		return update_by;
	}
	public void setUpdate_by(String update_by) {
		this.update_by = update_by;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	
}
