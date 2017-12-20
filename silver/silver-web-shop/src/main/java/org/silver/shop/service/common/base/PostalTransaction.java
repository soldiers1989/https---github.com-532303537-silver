package org.silver.shop.service.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.PostalService;
import org.silver.shop.model.common.base.Postal;
import org.silver.shop.model.common.base.Province;
import org.silver.util.JedisUtil;
import org.silver.util.SerializeUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONObject;

@Service("postalTransaction")
public class PostalTransaction {

	@Reference
	private static PostalService postalService;

	// 查询全国邮编
	public static Object getPostal() {
		Map<String, Object> datasMap = new HashMap<>();
		List<Postal> provinceList = null;
		byte[] redisByte = JedisUtil.get("Shop_Key_Postal_List".getBytes(), 3600);
		if (redisByte != null) {
			provinceList = (List<Postal>) SerializeUtil.toObject(redisByte);
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			datasMap.put(BaseCode.DATAS.toString(), provinceList);
			return datasMap;
		} else {
			datasMap = postalService.getPostal();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				List<Object> reList = (List) datasMap.get(BaseCode.DATAS.getBaseCode());
				if (reList != null && !reList.isEmpty()) {
					// 将查询出来的数据放入到缓存中
					JedisUtil.set("Shop_Key_Postal_List".getBytes(), SerializeUtil.toBytes(reList), 3600);
				}
			}
			return datasMap;
		}
	}
}
