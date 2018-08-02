package org.silver.shop.service.common.base;

import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.RedisKey;
import org.silver.shop.api.common.base.CCIQService;
import org.silver.shop.model.common.base.CCIQ;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;


@Service
public class CCIQTransaction {

	@Reference
	private CCIQService cciqService;
	
	public Object getCCIQInfo() {
		byte[] redisByte = JedisUtil.get(RedisKey.SHOP_KEY_CCIQ_LIST.getBytes());
		if (redisByte != null) {
			return ReturnInfoUtils.successDataInfo((List<CCIQ>) SerializeUtil.toObject(redisByte));
		} else {// 缓存中没有数据,重新访问数据库读取数据
			Map<String, Object> datasMap = cciqService.getCCIQInfo();
			if (!"1".equals(datasMap.get(BaseCode.STATUS.toString()))) {
				return datasMap;
			}
			JedisUtil.set(RedisKey.SHOP_KEY_CCIQ_LIST.getBytes(), SerializeUtil.toBytes(datasMap.get(BaseCode.DATAS.toString())), 86400);
			return datasMap;
		}
	}

}
