package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存信息 
 */
public class StockContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private String merchantId;// 商户ID
	private String goodsId;// 商品基本信息ID
	private int totalStock;// 现有存库数量
	private int sellCount;// 上架数量
	private int paymentCount;// 待支付数量
	private int paidCount;// 已支付待发货数量
	private int shippedCount;// 已发货数量
	private int doneCount;// 成交完成数量
	private double regPrice; // 单价(售卖)价格
	private double freight;// 运杂费
	private String warehouseCode;// 仓库编码 
	private String warehouseName;// 仓库名称
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间	
	private String merchantName;//商户名称
	private int sellFlag;//上下架标识：1-上架,2-下架,3-审核中
	private String goodsName;// 商品名称
	private String entGoodsNo;//商品备案ID
	
	private double originalPrice;//商品(入库)价格
	private double marketPrice;//商品(市场)价格
	private double salePrice;//商品(特卖)价格
	private double memberPrice;//商品(会员)价格
	private int readingCount;//商品浏览数量
	private Date sellDate;// 商品上架时间
	private Date dropOffDate;// 商品下架时间
	private String reMark;//备用字段
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}
	public int getTotalStock() {
		return totalStock;
	}
	public void setTotalStock(int totalStock) {
		this.totalStock = totalStock;
	}
	public int getSellCount() {
		return sellCount;
	}
	public void setSellCount(int sellCount) {
		this.sellCount = sellCount;
	}
	public int getPaymentCount() {
		return paymentCount;
	}
	public void setPaymentCount(int paymentCount) {
		this.paymentCount = paymentCount;
	}
	public int getPaidCount() {
		return paidCount;
	}
	public void setPaidCount(int paidCount) {
		this.paidCount = paidCount;
	}
	public int getShippedCount() {
		return shippedCount;
	}
	public void setShippedCount(int shippedCount) {
		this.shippedCount = shippedCount;
	}
	public int getDoneCount() {
		return doneCount;
	}
	public void setDoneCount(int doneCount) {
		this.doneCount = doneCount;
	}
	
	public Double getRegPrice() {
		return regPrice;
	}
	
	public Double getFreight() {
		return freight;
	}
	public void setFreight(Double freight) {
		this.freight = freight;
	}
	
	public String getWarehouseCode() {
		return warehouseCode;
	}
	public void setWarehouseCode(String warehouseCode) {
		this.warehouseCode = warehouseCode;
	}
	public String getWarehouseName() {
		return warehouseName;
	}
	public void setWarehouseName(String warehouseName) {
		this.warehouseName = warehouseName;
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
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public int getSellFlag() {
		return sellFlag;
	}
	public void setSellFlag(int sellFlag) {
		this.sellFlag = sellFlag;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public String getEntGoodsNo() {
		return entGoodsNo;
	}
	public void setEntGoodsNo(String entGoodsNo) {
		this.entGoodsNo = entGoodsNo;
	}
	public double getOriginalPrice() {
		return originalPrice;
	}
	public void setOriginalPrice(double originalPrice) {
		this.originalPrice = originalPrice;
	}
	public double getMarketPrice() {
		return marketPrice;
	}
	public void setMarketPrice(double marketPrice) {
		this.marketPrice = marketPrice;
	}
	public double getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(double salePrice) {
		this.salePrice = salePrice;
	}
	public double getMemberPrice() {
		return memberPrice;
	}
	public void setMemberPrice(double memberPrice) {
		this.memberPrice = memberPrice;
	}
	public void setFreight(double freight) {
		this.freight = freight;
	}
	
	public void setRegPrice(double regPrice) {
		this.regPrice = regPrice;
	}
	public String getReMark() {
		return reMark;
	}
	public void setReMark(String reMark) {
		this.reMark = reMark;
	}
	public int getReadingCount() {
		return readingCount;
	}
	public void setReadingCount(int readingCount) {
		this.readingCount = readingCount;
	}
	public Date getSellDate() {
		return sellDate;
	}
	public Date getDropOffDate() {
		return dropOffDate;
	}
	public void setSellDate(Date sellDate) {
		this.sellDate = sellDate;
	}
	public void setDropOffDate(Date dropOffDate) {
		this.dropOffDate = dropOffDate;
	}
	
}
