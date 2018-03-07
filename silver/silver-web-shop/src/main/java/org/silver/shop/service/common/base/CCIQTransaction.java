package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CCIQService;
import org.silver.shop.model.common.base.CCIQ;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;

@Service
public class CCIQTransaction {

	@Reference
	private CCIQService cciqService;
	
	public Object getCCIQInfo() {
		Map<String, Object> datasMap = new HashMap<>();
		String key = "Shop_Key_Allcciq_List";
		List<CCIQ> gacList = null;
		byte[] redisByte = JedisUtil.get(key.getBytes(), 3600);
		if (redisByte != null) {
			gacList = (List<CCIQ>) SerializeUtil.toObject(redisByte);
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.DATAS.toString(), JSONArray.fromObject(gacList));
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return datasMap;
		} else {// 缓存中没有数据,重新访问数据库读取数据
			datasMap = cciqService.getCCIQInfo();
			if (!datasMap.get(BaseCode.STATUS.toString()).equals("1")) {
				return datasMap;
			}
			gacList = (List<CCIQ>) datasMap.get(BaseCode.DATAS.toString());
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(gacList), 3600);
			return datasMap;
		}
	}

}
