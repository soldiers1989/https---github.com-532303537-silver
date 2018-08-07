package org.silver.shop.model.system.tenant;

import java.io.Serializable;

/**
 * 商户关联在银盛注册的账号信息
 */
public class MerchantYsContent implements Serializable {

	private long id;
	private String merchantId;// 商户id
	private String merchantName;// 商户名称
	private String yinShengNo;//
	
}
