package org.silver.shop.model.common.category;

import java.io.Serializable;
import java.util.Date;

/**
 *	商品第二(级)类型信息
 */
public class GoodsSecondType implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id ;
	private long firstTypeId;//第一级ID
	private String goodsSecondTypeName;//商品第二(级)类别名称
	private String createBy;//创建人
	private Date createDate;//创建时间
	private String updateBy;//更新人
	private Date updateDate;//更新时间
	private int deleteFlag;//删除标识:0-未删除,1-已删除
	private String deleteBy;//删除人
	private Date deleteDate;//删除时间
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Long getFirstTypeId() {
		return firstTypeId;
	}
	public void setFirstTypeId(Long firstTypeId) {
		this.firstTypeId = firstTypeId;
	}
	
	public String getGoodsSecondTypeName() {
		return goodsSecondTypeName;
	}
	public void setGoodsSecondTypeName(String goodsSecondTypeName) {
		this.goodsSecondTypeName = goodsSecondTypeName;
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	
	
}
