package org.silver.sys.model.repeat;

import java.io.Serializable;
import java.util.Date;
/**
 * 把海关与国检审核结果通知第三方，传输失败后，存储记录
 * @author Administrator
 *
 */
public class Audit implements Serializable{

	private static final long serialVersionUID = 1L;
	private long id;
	private String orgMessageID;   //原始报文编号
	private String orgMessageType; //原始报文类型
	private String url; //回调URL
	private String content;//审核结果
	
	private int delFlag;//0正常   1删除
	private Date createDate; //创建时间
	private String createBy; //创建人
	private Date updateDate; //更新时间
	private String updateBy;//更新人
	private String remarks;//备注
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getOrgMessageID() {
		return orgMessageID;
	}
	public void setOrgMessageID(String orgMessageID) {
		this.orgMessageID = orgMessageID;
	}
	public String getOrgMessageType() {
		return orgMessageType;
	}
	public void setOrgMessageType(String orgMessageType) {
		this.orgMessageType = orgMessageType;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public int getDelFlag() {
		return delFlag;
	}
	public void setDelFlag(int delFlag) {
		this.delFlag = delFlag;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getUpdateBy() {
		return updateBy;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
}
