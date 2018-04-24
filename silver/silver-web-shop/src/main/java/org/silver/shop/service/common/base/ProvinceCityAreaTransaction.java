package org.silver.shop.service.common.base;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.ProvinceCityAreaService;
import org.silver.shop.model.common.base.Area;
import org.silver.shop.model.common.base.City;
import org.silver.shop.model.common.base.Province;
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
	private static Logger logger = Logger.getLogger(ProvinceCityAreaTransaction.class);
	@Reference
	private ProvinceCityAreaService provinceCityAreaService;

	/**
	 * 查询省市区三级联动
	 * 
	 * @return
	 */
	public List<Object> findProvinceCityArea() {
		List<Object> datasList = null;
		Map<String, Object> datasMap = null;
		String redisList = JedisUtil.get("Shop_Key_ProvinceCityArea_List");
		if (StringEmptyUtils.isEmpty(redisList)) {// redis缓存没有数据
			datasMap = provinceCityAreaService.getProvinceCityArea();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if (status.equals("1")) {// 当查询成功时
				datasList = (List) datasMap.get(BaseCode.DATAS.toString());
			}
			return JSONArray.fromObject(datasList);
		} else {
			// redis缓存中已有数据,直接返回数据
			return JSONArray.fromObject(redisList);
		}
	}

	/**
	 * 查询省市区封装成Map集合key=区域编码
	 * 
	 * @return
	 */
	public Object getProvinceCityArea() {
		Map<String, Object> province = null;
		byte[] redisByte = JedisUtil.get("Shop_Key_Province_Map".getBytes());
		if (redisByte != null) {
			province = (Map<String, Object>) SerializeUtil.toObject(redisByte);
			return ReturnInfoUtils.successDataInfo(JSONObject.fromObject(province));
		} else {
			try{
				Map<String, Object> datasMap = provinceCityAreaService.getProvinceCityArea2();
				String status = datasMap.get(BaseCode.STATUS.toString()) + "";
				if ("1".equals(status)) {
					return datasMap;
				}
			}catch (Exception e) {
				logger.error(Thread.currentThread().getName()+"-查询省市区信息->",e);
				return ReturnInfoUtils.errorInfo("查询省市区失败,服务器繁忙!！");
			}
			return ReturnInfoUtils.errorInfo("查询省市区失败！");
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
