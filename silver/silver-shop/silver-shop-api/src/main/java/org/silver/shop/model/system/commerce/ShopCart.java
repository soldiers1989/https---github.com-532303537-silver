package org.silver.shop.model.system.commerce;

import java.io.Serializable;

/**
 * 购物车实体
 */
public class ShopCart implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5044519365949043508L;
	private long id;
	private String memberId;//用户ID
	private String memberName;//用户名称
	private String merchantId;//商户ID
	private String merchantName;//商户名称
	private String goodsBaseId;//商品基本信息ID
	private String goodsName;//商品名称
	private String goodsImage;// 商品展示图片
	private String goodsStyle;//商品规格
	private int count;//商品数量
	private double regPrice;//单价
	private double totalPrice;//总价
	
	private String goodsRecordId;//商品备案流水ID

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getGoodsBaseId() {
		return goodsBaseId;
	}

	public void setGoodsBaseId(String goodsBaseId) {
		this.goodsBaseId = goodsBaseId;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getGoodsImage() {
		return goodsImage;
	}

	public void setGoodsImage(String goodsImage) {
		this.goodsImage = goodsImage;
	}

	public String getGoodsStyle() {
		return goodsStyle;
	}

	public void setGoodsStyle(String goodsStyle) {
		this.goodsStyle = goodsStyle;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getRegPrice() {
		return regPrice;
	}

	public void setRegPrice(double regPrice) {
		this.regPrice = regPrice;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getGoodsRecordId() {
		return goodsRecordId;
	}

	public void setGoodsRecordId(String goodsRecordId) {
		this.goodsRecordId = goodsRecordId;
	}
	
	
	
}
