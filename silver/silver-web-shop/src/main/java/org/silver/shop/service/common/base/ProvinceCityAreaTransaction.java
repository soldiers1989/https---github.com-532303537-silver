package org.silver.shop.service.common.base;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.common.base.ProvinceCityAreaService;
import org.silver.shop.service.system.manual.ManualOrderTransaction;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 省市区 Transaction
 */
@Service("provinceCityAreaTransaction")
public class ProvinceCityAreaTransaction {

	private static Logger logger = LogManager.getLogger(ProvinceCityAreaTransaction.class);
	@Reference
	private ProvinceCityAreaService provinceCityAreaService;

	/**
	 * 查询省市区三级联动
	 * 
	 * @return
	 */
	public Map<String, Object> findProvinceCityArea() {
		//省市区List缓存
		String redisList = JedisUtil.get("SHOP_KEY_PROVINCE_CITY_AREA_LIST");
		if (StringEmptyUtils.isEmpty(redisList)) {// redis缓存没有数据
			return provinceCityAreaService.getProvinceCityArea();
		} else {
			// redis缓存中已有数据,直接返回数据
			return ReturnInfoUtils.successDataInfo(JSONArray.fromObject(redisList));
		}
	}

	/**
	 * 查询省市区封装成Map集合key=区域编码
	 * 
	 * @return
	 */
	public Object getProvinceCityArea() {
		//省市区Map缓存
		byte[] redisByte = JedisUtil.get("SHOP_KEY_PROVINCE_CITY_AREA_POSTAL_MAP".getBytes());
		if (redisByte != null) {
			return ReturnInfoUtils.successDataInfo(JSONObject.fromObject(SerializeUtil.toObject(redisByte)));
		} else {
			try {
				return  provinceCityAreaService.getAllProvinceCityAreaPostal();
			} catch (Exception e) {
				logger.error(Thread.currentThread().getName() + "-查询省市区信息错误->", e);
				return ReturnInfoUtils.errorInfo("查询省市区失败,服务器繁忙!！");
			}
		}
	}

	public Object editProvinceCityAreaInfo(HttpServletRequest req, int flag) {
		Map<String, Object> params = new HashMap<>();
		Enumeration<String> isKey = req.getParameterNames();
		while (isKey.hasMoreElements()) {
			String key = isKey.nextElement();
			String value = req.getParameter(key);
			params.put(key, value);
		}
		params.remove("flag");
		return provinceCityAreaService.editProvinceCityAreaInfo(params, flag);
	}
}
