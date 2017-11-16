package org.silver.shop.service.common.category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.common.category.CategoryService;
import org.silver.util.JedisUtil;
import org.silver.util.StringEmptyUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 商品类型Transaction(事物)处理层
 */
@Service("categoryTransaction")
public class CategoryTransaction {

	@Reference
	private CategoryService categoryService;

	/**
	 * 查询所有商品类型,并进行对应的级联封装到Map
	 * 
	 * @return Map
	 */
	public Map<String,Object> findAllCategory() {
		Map<String, Object> datasMap = new HashMap<>();
		// 获取在redis中的所有商品类型
		String redisList = JedisUtil.get("Shop_Key_GoodsCategory_Map");
		if (StringEmptyUtils.isEmpty(redisList)) {// redis缓存没有数据
			datasMap = categoryService.findGoodsType();
			String  status= datasMap.get(BaseCode.STATUS.toString()) + "";
			if ("1".equals(status)) {
				datasMap  =   (Map) datasMap.get(BaseCode.DATAS.getBaseCode());
				// 将已查询出来的商品类型存入redis,有效期为1小时
				JedisUtil.setListDatas("Shop_Key_GoodsCategory_Map", 3600,datasMap);
			}
		} else {// redis缓存中已有数据,直接返回数据
			return JSONObject.fromObject(redisList);
		}
		return datasMap;
	} 
}
