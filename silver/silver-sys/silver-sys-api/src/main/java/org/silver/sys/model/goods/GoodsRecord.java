package org.silver.sys.model.goods;

import java.io.Serializable;
import java.util.Date;
/**
 * 商品备案信息存储表
 * @author zhangxin 2017/9/6
 *
 */
public class GoodsRecord implements Serializable{

	private static final long serialVersionUID = 1L;
	private long id;
	private String DeclEntNo; // 申报企业编号
	private String DeclEntName; // 申报企业名称
	private String EBEntNo; // 电商企业编号
	private String EBEntName; // 电商企业名称
	private String OpType; // 操作方式 A-新增；M-修改；D-取消备案；
	private String CustomsCode; // 主管海关代码
	private String CIQOrgCode; // 检验检疫机构代码
	private String EBPEntNo; // 电商平台企业编号
	private String EBPEntName; // 电商平台名称
	private String CurrCode; // 币制代码
	private String BusinessType; // 跨境业务类型 1-特殊监管区域BBC保税进口 2-保税仓库BBC保税进口；
	private String InputDate; // 录入日期 YYYYMMDDhhmmss
	private String DeclTime; // 申请备案时间 YYYYMMDDhhmmss
	private String IeFlag; // 进出境标志 I-进，E-出
	private String Notes;//备注
	
	private int eport;                  //口岸：1 电子口岸 2 南沙智检
	private String filePath;            //报文存储路径
	private String OrgMessageID; //原始报文编号
	private String ciqNotes;// 国检审核备注
	private String ciqStatus;// 国检审核状态
	private String cusNotes;// 海关审核备注
	private String cusStatus;// 海关审核状态
	private String status;// 发送状态  0未发送  1已发送   2发送失败   3已被接收成功   4（已接收回执）完成
	private int count;//重发计数      重发次数不能超过5次
	private int del_flag;// 0正常 1删除
	private Date create_date; // 创建时间
	private String create_by; // 创建人
	private Date update_date; // 更新时间
	private String update_by;// 更新人
	private String remarks;// 备注
	
	public String getNotes() {
		return Notes;
	}
	public void setNotes(String notes) {
		Notes = notes;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDeclEntNo() {
		return DeclEntNo;
	}
	public void setDeclEntNo(String declEntNo) {
		DeclEntNo = declEntNo;
	}
	public String getDeclEntName() {
		return DeclEntName;
	}
	public void setDeclEntName(String declEntName) {
		DeclEntName = declEntName;
	}
	public String getEBEntNo() {
		return EBEntNo;
	}
	public void setEBEntNo(String eBEntNo) {
		EBEntNo = eBEntNo;
	}
	public String getEBEntName() {
		return EBEntName;
	}
	public void setEBEntName(String eBEntName) {
		EBEntName = eBEntName;
	}
	public String getOpType() {
		return OpType;
	}
	public void setOpType(String opType) {
		OpType = opType;
	}
	public String getCustomsCode() {
		return CustomsCode;
	}
	public void setCustomsCode(String customsCode) {
		CustomsCode = customsCode;
	}
	public String getCIQOrgCode() {
		return CIQOrgCode;
	}
	public void setCIQOrgCode(String cIQOrgCode) {
		CIQOrgCode = cIQOrgCode;
	}
	public String getEBPEntNo() {
		return EBPEntNo;
	}
	public void setEBPEntNo(String eBPEntNo) {
		EBPEntNo = eBPEntNo;
	}
	public String getEBPEntName() {
		return EBPEntName;
	}
	public void setEBPEntName(String eBPEntName) {
		EBPEntName = eBPEntName;
	}
	public String getCurrCode() {
		return CurrCode;
	}
	public void setCurrCode(String currCode) {
		CurrCode = currCode;
	}
	public String getBusinessType() {
		return BusinessType;
	}
	public void setBusinessType(String businessType) {
		BusinessType = businessType;
	}
	public String getInputDate() {
		return InputDate;
	}
	public void setInputDate(String inputDate) {
		InputDate = inputDate;
	}
	public String getDeclTime() {
		return DeclTime;
	}
	public void setDeclTime(String declTime) {
		DeclTime = declTime;
	}
	public String getIeFlag() {
		return IeFlag;
	}
	public void setIeFlag(String ieFlag) {
		IeFlag = ieFlag;
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
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
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
	public int getEport() {
		return eport;
	}
	public void setEport(int eport) {
		this.eport = eport;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	

	
	
}
