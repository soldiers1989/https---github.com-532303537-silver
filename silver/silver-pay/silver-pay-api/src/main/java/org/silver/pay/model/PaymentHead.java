package org.silver.pay.model;

import java.io.Serializable;
import java.util.Date;
/**
 * 支付报文头
 * @author zhangxin 2017/9/15
 *
 */
public class PaymentHead implements Serializable {

	private static final long serialVersionUID = 1L;
	private long id;
	private String OrgMessageID;        //原始报文编号 唯一标识，关联
	private String DeclEntNo;           //申报企业编号
	private String DeclEntName;         //申报企业名称
	private String PayEntNo;            //支付企业编号
	private String PayEntName;          //支付企业名称
	private String DeclTime;            //申报时间
	private String OpType;              //操作方式
	private String CustomsCode;         //主管海关代码 
	private String CIQOrgCode;          //检验检疫机构代码
	
	private String filePath;            //报文存储路径
	private int del_flag;// 0正常 1删除
	private Date create_date; // 创建时间
	private String create_by; // 创建人
	private Date update_date; // 更新时间
	private String update_by;// 更新人
	private String remarks;// 备注
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
	public String getPayEntNo() {
		return PayEntNo;
	}
	public void setPayEntNo(String payEntNo) {
		PayEntNo = payEntNo;
	}
	public String getPayEntName() {
		return PayEntName;
	}
	public void setPayEntName(String payEntName) {
		PayEntName = payEntName;
	}
	public String getDeclTime() {
		return DeclTime;
	}
	public void setDeclTime(String declTime) {
		DeclTime = declTime;
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
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
