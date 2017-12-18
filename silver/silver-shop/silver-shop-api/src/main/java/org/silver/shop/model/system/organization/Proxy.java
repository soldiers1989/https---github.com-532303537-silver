package org.silver.shop.model.system.organization;

import java.io.Serializable;
import java.util.Date;

public class Proxy implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5879106583403029821L;
	private long id;
	private String proxyUUid;// 系统唯一标识
	private String loginAccount;// 登录账号
	private String loginPassword;
	private String proxyName;
	private String proxyPhone;
	private String proxyPhoto;// 头像
	private String proxyId;// 身份证
	private String idFront;// 身份证正面
	private String idOpposite;// 身份证反面
	private String proxyEmail;// 邮箱
	private int type;
	private int level;// 等级
	private int points;// 积分
	private int pointsTotal;// 历史总积分
	private String createBy;// 创建人
	private Date createDate;// 创建日期
	private String updateBy;// 更新人
	private Date updateDate;// 更新日期
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除日期

	private String remarks;// 备注

	public long getId() {
		return id;
	}

	public String getProxyUUid() {
		return proxyUUid;
	}

	public String getLoginAccount() {
		return loginAccount;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public String getProxyName() {
		return proxyName;
	}

	public String getProxyPhone() {
		return proxyPhone;
	}

	public String getProxyPhoto() {
		return proxyPhoto;
	}

	public String getProxyId() {
		return proxyId;
	}

	public String getIdFront() {
		return idFront;
	}

	public String getIdOpposite() {
		return idOpposite;
	}

	public String getProxyEmail() {
		return proxyEmail;
	}

	public int getType() {
		return type;
	}

	public int getLevel() {
		return level;
	}

	public int getPoints() {
		return points;
	}

	public int getPointsTotal() {
		return pointsTotal;
	}

	public String getCreateBy() {
		return createBy;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public int getDeleteFlag() {
		return deleteFlag;
	}

	public String getDeleteBy() {
		return deleteBy;
	}

	public Date getDeleteDate() {
		return deleteDate;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setProxyUUid(String proxyUUid) {
		this.proxyUUid = proxyUUid;
	}

	public void setLoginAccount(String loginAccount) {
		this.loginAccount = loginAccount;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	public void setProxyPhone(String proxyPhone) {
		this.proxyPhone = proxyPhone;
	}

	public void setProxyPhoto(String proxyPhoto) {
		this.proxyPhoto = proxyPhoto;
	}

	public void setProxyId(String proxyId) {
		this.proxyId = proxyId;
	}

	public void setIdFront(String idFront) {
		this.idFront = idFront;
	}

	public void setIdOpposite(String idOpposite) {
		this.idOpposite = idOpposite;
	}

	public void setProxyEmail(String proxyEmail) {
		this.proxyEmail = proxyEmail;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public void setPointsTotal(int pointsTotal) {
		this.pointsTotal = pointsTotal;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public void setDeleteBy(String deleteBy) {
		this.deleteBy = deleteBy;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	
}
