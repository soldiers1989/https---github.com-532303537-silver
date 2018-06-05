package org.silver.shop.service.common.base;

import java.util.List;

import org.silver.shop.api.common.base.MeteringService;
import org.silver.shop.model.common.base.Metering;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

/**
 * 计量单位 Transaction
 *
 */
@Service("meteringTransaction")
public class MeteringTransaction {

	@Reference
	private MeteringService meteringService;

	public List findMetering() {
		List<Metering> meteringList = null;
		byte[] redisByte = JedisUtil.get("SHOP_KEY_METERING_LIST".getBytes(), 3600);
		if (redisByte != null) {
			meteringList = (List<Metering>) SerializeUtil.toObject(redisByte);
			return meteringList;
		} else {
			List<Object> reList = meteringService.findAllMetering();
			if (reList != null && !reList.isEmpty()) {
				// 将查询出来的数据放入到缓存中
				JedisUtil.set("SHOP_KEY_METERING_LIST".getBytes(), SerializeUtil.toBytes(reList), 3600);
				return reList;
			}
		}
		return null;
	}
}