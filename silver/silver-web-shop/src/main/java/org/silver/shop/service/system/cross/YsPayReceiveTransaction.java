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
}
