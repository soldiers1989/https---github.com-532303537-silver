package org.silver.shop.model.system.manual;

import java.io.Serializable;
import java.util.Date;

//手工支付单(支持手工录入支付单，支持由手工订单生成支付单)
public class Mpay implements Serializable {
	/**
		 * 
		 */
	private static final long serialVersionUID = -9174880036104531544L;
	private long id;
	private String merchant_no;// 支付单所属商户
	private String trade_no;// 模拟银盛支付交易流水 系统唯一
	private String morder_id;// 关联的手工订单 id
	private double pay_amount;// 交易金额
	private String trade_status;// 固定 TRADE_SUCCESS
	private String payer_name;// 支付人姓名
	private String payer_document_type;// 支付人证件类型:01:身份证,02:护照,04:其他
	private String payer_document_number;// 支付人证件号码
	private String payer_phone_number;// 支付人手机号
	private String year;// 所属年份
	private int del_flag;// 0正常 1删除
	private Date create_date; // 创建时间
	private String create_by; // 创建人
	private Date update_date; // 更新时间
	private String update_by;// 更新人
	private String remarks;// 备注
	private String pay_status;//支付状态D-代扣(款项由消费者账户转至第三方支付企业账户)S-实扣(款项由消费者账户转至收款方账户)C-取消(退款)
	private String pay_currCode;//支付币制
	
	private int pay_record_status;//备案状态：1-未备案,2-备案中,3-备案成功、4-备案失败
	private String pay_serial_no;//服务器接收成功后返回编号
	private String pay_re_note;//服务器返回信息
	private Date pay_time;//付款时间
	private String eport;//口岸标识1-电子口岸,2-广东智检
	private String customsCode;//海关关区代码(导出表中-进/出口岸)
	private String ciqOrgCode;//国检机构代码
	private String thirdPartyId;//第三方支付单唯一标识
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTrade_no() {
		return trade_no;
	}

	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}

	public String getMerchant_no() {
		return merchant_no;
	}

	public void setMerchant_no(String merchant_no) {
		this.merchant_no = merchant_no;
	}

	public String getTrade_status() {
		return trade_status;
	}

	public void setTrade_status(String trade_status) {
		this.trade_status = trade_status;
	}

	public String getMorder_id() {
		return morder_id;
	}

	public void setMorder_id(String morder_id) {
		this.morder_id = morder_id;
	}

	public double getPay_amount() {
		return pay_amount;
	}

	public void setPay_amount(double pay_amount) {
		this.pay_amount = pay_amount;
	}

	public String getPayer_name() {
		return payer_name;
	}

	public void setPayer_name(String payer_name) {
		this.payer_name = payer_name;
	}

	public String getPayer_document_type() {
		return payer_document_type;
	}

	public void setPayer_document_type(String payer_document_type) {
		this.payer_document_type = payer_document_type;
	}

	public String getPayer_document_number() {
		return payer_document_number;
	}

	public void setPayer_document_number(String payer_document_number) {
		this.payer_document_number = payer_document_number;
	}

	public String getPayer_phone_number() {
		return payer_phone_number;
	}

	public void setPayer_phone_number(String payer_phone_number) {
		this.payer_phone_number = payer_phone_number;
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

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getPay_status() {
		return pay_status;
	}

	public void setPay_status(String pay_status) {
		this.pay_status = pay_status;
	}

	public String getPay_currCode() {
		return pay_currCode;
	}

	public void setPay_currCode(String pay_currCode) {
		this.pay_currCode = pay_currCode;
	}

	

	public int getPay_record_status() {
		return pay_record_status;
	}

	public void setPay_record_status(int pay_record_status) {
		this.pay_record_status = pay_record_status;
	}

	public String getPay_serial_no() {
		return pay_serial_no;
	}

	public void setPay_serial_no(String pay_serial_no) {
		this.pay_serial_no = pay_serial_no;
	}

	public String getPay_re_note() {
		return pay_re_note;
	}
	public void setPay_re_note(String pay_re_note) {
		this.pay_re_note = pay_re_note;
	}

	public Date getPay_time() {
		return pay_time;
	}

	public void setPay_time(Date pay_time) {
		this.pay_time = pay_time;
	}

	public String getEport() {
		return eport;
	}

	public String getCustomsCode() {
		return customsCode;
	}

	public String getCiqOrgCode() {
		return ciqOrgCode;
	}

	public void setEport(String eport) {
		this.eport = eport;
	}

	public void setCustomsCode(String customsCode) {
		this.customsCode = customsCode;
	}

	public void setCiqOrgCode(String ciqOrgCode) {
		this.ciqOrgCode = ciqOrgCode;
	}

	public String getThirdPartyId() {
		return thirdPartyId;
	}

	public void setThirdPartyId(String thirdPartyId) {
		this.thirdPartyId = thirdPartyId;
	}

}
