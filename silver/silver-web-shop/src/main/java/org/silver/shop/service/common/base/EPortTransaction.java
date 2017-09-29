package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.EPortService;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

/**
 * 商品备案的口岸 Transaction
 *
 */
@Service("ePortTransaction")
public class EPortTransaction {

	@Reference
	private EPortService ePortService;

	public Map<String, Object> checkEPortName(String customsPortName) {
		Map<String, Object> datasMap = new HashMap<>();
		List<Object> reList = ePortService.checkEportName(customsPortName);
		if (reList != null && reList.size() > 0) {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.toString(), "口岸名已存在,请重新输入！");
		} else {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), "口岸名可以使用！");
		}
		return datasMap;
	}

	public Map<String,Object> addEPort(String customsPort, String customsPortName, String cityCode) {
		Map<String,Object> datasMap = new HashMap<>();
		datasMap = ePortService.addEPort(customsPort,customsPortName,cityCode);
		String status = datasMap.get(BaseCode.STATUS.toString())+"";
		if(status.equals("1")){
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		}else{
			datasMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return datasMap;
	}

	public void findEPort() {
		Map<String,Object> datasMap = new HashMap<>();
		ePortService.findEPort();
	}

}
