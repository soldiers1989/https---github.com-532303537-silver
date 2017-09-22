package org.silver.common;

public class GZEportCode {
	/** 申报企业编号*/
	public static final String DECL_ENT_NO = "C010000000537118";
	/** 申报企业名称*/
	public static final String DECL_ENT_NAME = "广州银盟信息科技有限公司";
	/** 电商企业编号*/
	public static final String PAY_ENT_NO = "C000010000803304";
	/** 电商企业名称*/
	public static final String PAY_ENT_NAME = "银盛支付服务股份有限公司";
	/** 币制代码*/
	public static final String CURR_CODE = "142";
	/** 报文发送者标识*/
	public static final String SENDER = "YINMENG";
	/** 报文发送者标识*/
	public static final String SENDER_PAY = "YINSHENGPAY";
	/** 报文接收人标识*/
	public static final String RECEIVER = "KJPUBLICPT";
	/** 业务类型-单向海关申报填CUS*/
	public static final String FUNCTION_CODE_CUS = "CUS";
	/** 业务类型-单向国检申报填CIQ*/
	public static final String FUNCTION_CODE_CIQ = "CIQ";
	/** 业务类型-同时发送时填写“BOTH”*/
	public static final String FUNCTION_CODE_BOTH = "BOTH";
	/** 报文类型-商品备案*/
	public static final String MESSAGE_TYPE_GOOD = "KJ881101";
	/** 报文类型-订单备案*/
	public static final String MESSAGE_TYPE_ORDER = "KJ881111";
	/** 报文类型-支付备案*/
	public static final String MESSAGE_TYPE_PAY = "KJ881112";
	/** 报文类型-清单备案*/
	public static final String MESSAGE_TYPE_LIST = "KJ881110";
	/** 版本号*/
	public static final String VERSION = "3.0";
	
}
