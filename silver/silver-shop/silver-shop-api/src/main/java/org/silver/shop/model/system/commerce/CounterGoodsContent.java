package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 专柜商品信息
 */
public class CounterGoodsContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6444936134745717595L;
	private long id;//
	private String serialNo;// 序号、唯一标识
	private String counterId;// 专柜id
	private String counterOwnerId;// 专柜所属人id
	private String counterOwnerName;// 专柜所属人名称
	private String goodsMerchantId;// 商品归属商户id
	private String goodsMerchantName;// 商品归属商户名称
	private String entGoodsNo; // 企业商品自编号 企业的商品货号，不可重复
	private String goodsName;// 商品名称
	private double regPrice;// 单价
	private int popularizeFlag;// 推广标识：1-允许分销、2-不允许分销
	private String profitType;// 分润计算类型：1-比例，2-按固定分润数
	private double popularizeProfit;// 推广分润
	private String goodsImage;// 商品主图
	private String remark;//
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间

	public long getId() {
		return id;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public String getCounterId() {
		return counterId;
	}

	

	public String getEntGoodsNo() {
		return entGoodsNo;
	}

	public int getPopularizeFlag() {
		return popularizeFlag;
	}

	public double getPopularizeProfit() {
		return popularizeProfit;
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

	public void setId(long id) {
		this.id = id;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public void setCounterId(String counterId) {
		this.counterId = counterId;
	}

	

	public void setEntGoodsNo(String entGoodsNo) {
		this.entGoodsNo = entGoodsNo;
	}

	public void setPopularizeFlag(int popularizeFlag) {
		this.popularizeFlag = popularizeFlag;
	}

	public void setPopularizeProfit(double popularizeProfit) {
		this.popularizeProfit = popularizeProfit;
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

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public double getRegPrice() {
		return regPrice;
	}

	public void setRegPrice(double regPrice) {
		this.regPrice = regPrice;
	}

	public String getCounterOwnerId() {
		return counterOwnerId;
	}

	public String getCounterOwnerName() {
		return counterOwnerName;
	}

	public void setCounterOwnerId(String counterOwnerId) {
		this.counterOwnerId = counterOwnerId;
	}

	public void setCounterOwnerName(String counterOwnerName) {
		this.counterOwnerName = counterOwnerName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getGoodsImage() {
		return goodsImage;
	}

	public void setGoodsImage(String goodsImage) {
		this.goodsImage = goodsImage;
	}

	public String getProfitType() {
		return profitType;
	}

	public void setProfitType(String profitType) {
		this.profitType = profitType;
	}

	public String getGoodsMerchantId() {
		return goodsMerchantId;
	}

	public String getGoodsMerchantName() {
		return goodsMerchantName;
	}

	public void setGoodsMerchantId(String goodsMerchantId) {
		this.goodsMerchantId = goodsMerchantId;
	}

	public void setGoodsMerchantName(String goodsMerchantName) {
		this.goodsMerchantName = goodsMerchantName;
	}

}
