package org.silver.shop.service.system.cross;

import java.util.Map;

import org.silver.shop.api.system.cross.YsPayReceiveService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;


@Service
public class YsPayReceiveTransaction {
	@Reference
	public YsPayReceiveService ysPayReceiveService;
	
	public Map<String,Object> ysPayReceive(Map<String,Object> datasMap){
		return ysPayReceiveService.ysPayReceive(datasMap);
	}

	//银盛支付回调钱包充值成功后
	public Map<String, Object> walletRechargeReceive(Map datasMap) {
		return ysPayReceiveService.walletRechargeReceive(datasMap);
	}

	//银盟发起商户资金代付后,银盛支付成功回调
	public Map<String, Object> dfReceive(Map params) {
		return ysPayReceiveService.dfReceive(params);
	}
}
