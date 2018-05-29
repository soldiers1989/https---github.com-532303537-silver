package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.GACService;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.common.base.GAC;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;

@Service
public class GACTransaction {

	@Reference
	private GACService gacService;

	public Map<String, Object> getGACInfo() {
		Map<String, Object> datasMap = new HashMap<>();
		String key = "Shop_Key_Allgac_List";
		byte[] redisByte = JedisUtil.get(key.getBytes(), 3600);
		if (redisByte != null) {
			return ReturnInfoUtils.successDataInfo((List<GAC>) SerializeUtil.toObject(redisByte));
		} else {// 缓存中没有数据,重新访问数据库读取数据
			datasMap = gacService.getGACInfo();
			if (!"1".equals(datasMap.get(BaseCode.STATUS.toString()))) {
				return datasMap;
			}
			JedisUtil.set(key.getBytes(), SerializeUtil.toBytes(datasMap.get(BaseCode.DATAS.toString())), 3600);
			return datasMap;
		}
	}

}
