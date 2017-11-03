package org.silver.shop.service.common.base;

import java.util.List;

import org.silver.shop.api.common.base.MeteringService;
import org.silver.util.JedisUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 计量单位 Transaction
 *
 */
@Service("meteringTransaction")
public class MeteringTransaction {

	@Reference
	private MeteringService meteringService;

	public List<Object> findMetering() {
		String redisList = JedisUtil.get("Shop_Key_Metering_List");
		if(StringEmptyUtils.isEmpty(redisList)){
			List<Object> reList = meteringService.findAllMetering();
			if (reList != null && reList.size() > 0) {
				JedisUtil.setListDatas("Shop_Key_Metering_List", 3600, reList);
				return reList;
			}
		}else{
			return JSONArray.fromObject(redisList);
		}
		return null;
	}

}
