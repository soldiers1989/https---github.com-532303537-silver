package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 * 购物车实体
 */
public class ShopCarContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5044519365949043508L;
	private long id;
	private String memberId;// 用户ID
	private String memberName;// 用户名称
	private String merchantId;// 商户ID
	private String merchantName;// 商户名称
	private String goodsBaseId;// 商品基本信息ID
	private String goodsName;// 商品名称
	private String goodsImage;// 商品展示图片
	private String goodsStyle;// 商品规格
	private int count;// 商品数量
	private double regPrice;// 单价
	private double totalPrice;// 总价

	private int sellCount;// 库存(上架)数量
	private int flag;// 购物车商品选中标识：1-选中,2-未选中
	private String entGoodsNo;//商品(海关备案返回)ID
	private double vat;//增值税 
	private double consumptionTax;//消费税 
	private double consolidatedTax;//综合税 跨境电商综合税率 = （消费税率+增值税率）/（1-消费税率）×70%
	private double tariff;//关税税率暂设为0%
	private double courierFee;// (国内快递)运费
	private int taxFlag;//计算税费标识：1-计算税费,2-不计税费
	private int courierFeeFlag;//计算(国内快递)物流费标识：1-无运费,2-手动设置运费
	private String reMark;//备用字段 
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
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
	public int getSellCount() {
		return sellCount;
	}

	public void setSellCount(int sellCount) {
		this.sellCount = sellCount;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getEntGoodsNo() {
		return entGoodsNo;
	}

	public void setEntGoodsNo(String entGoodsNo) {
		this.entGoodsNo = entGoodsNo;
	}

	public double getVat() {
		return vat;
	}

	public void setVat(double vat) {
		this.vat = vat;
	}

	public double getConsumptionTax() {
		return consumptionTax;
	}

	public void setConsumptionTax(double consumptionTax) {
		this.consumptionTax = consumptionTax;
	}

	public double getConsolidatedTax() {
		return consolidatedTax;
	}

	public void setConsolidatedTax(double consolidatedTax) {
		this.consolidatedTax = consolidatedTax;
	}

	public double getTariff() {
		return tariff;
	}

	public void setTariff(double tariff) {
		this.tariff = tariff;
	}

	public double getCourierFee() {
		return courierFee;
	}

	public void setCourierFee(double courierFee) {
		this.courierFee = courierFee;
	}

	public String getReMark() {
		return reMark;
	}

	public void setReMark(String reMark) {
		this.reMark = reMark;
	}

	public int getTaxFlag() {
		return taxFlag;
	}

	public void setTaxFlag(int taxFlag) {
		this.taxFlag = taxFlag;
	}

	public int getCourierFeeFlag() {
		return courierFeeFlag;
	}

	public void setCourierFeeFlag(int courierFeeFlag) {
		this.courierFeeFlag = courierFeeFlag;
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
	
}
