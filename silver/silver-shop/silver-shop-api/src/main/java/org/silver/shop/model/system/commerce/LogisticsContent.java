package org.silver.shop.model.system.commerce;

import java.io.Serializable;
import java.util.Date;

/**
 *	物流信息实体 
 */
public class LogisticsContent implements Serializable{
	 
	private long id;//
	
	private String logisticsCompanyCode;//物流企业编码
	private String logisticsCompanyName;//物流公司名称
	private String courierCompanyCode;//快递公司编码
	private String courierCompanyName;//快递公司名称
	private String note;//
	private String remark;//
	private String createBy;// 创建人
	private Date createDate;// 创建时间
	private String updateBy;// 更新人
	private Date updateDate;// 更新时间
	private int deleteFlag;// 删除标识:0-未删除,1-已删除
	private String deleteBy;// 删除人
	private Date deleteDate;// 删除时间
	
}
