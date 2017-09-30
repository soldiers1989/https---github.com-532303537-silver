package org.silver.shop.model.system.commerce;

import java.util.Date;

/**
 * 商品备案流水 
 *
 */
public class GoodsRecordContent {
	private long id ;
	private String merchantId;// 商户ID
	private String customsPort;//海关口岸代码
	private String customsPortName;//海关口岸名称
	private String customsCode;// 主管海关代码
	private String customsName;// 主管海关代码名称
	private String ciqOrgCode;// 检验检疫机构代码
	private String ciqOrgName;// 检验检疫机构名称
	private String ebEntNo;// 电商企业编号
	private String ebEntName;// 电商企业名称
	private String ebpEntNo;// 电商平台企业编号
	private String ebpEntName;// 电商平台名称
	private String goodsMerchantName;//归属商戶名
	private String status;//
	
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 修改人
	private Date updateDate;// 修改时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
}
