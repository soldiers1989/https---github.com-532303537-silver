package org.silver.shop.service.common.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.common.category.CategoryService;
import org.silver.util.JedisUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

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
	public List findAllCategory() {
		List<Map<String, Object>> datasList = null;
		Map<String, Object> datasMap = null;
		// 获取在redis中的所有商品类型
		String redisList = JedisUtil.get("Shop_Nav_AllGoodsCategory");
		if (redisList == null   ||redisList.equals("null")) {// redis缓存没有数据
			datasMap = categoryService.findGoodsType();
			String status = datasMap.get(BaseCode.STATUS.toString()) + "";
			if (status.equals("1")) {
				datasList = (List) datasMap.get(BaseCode.DATAS.getBaseCode());
				// 将已查询出来的商品类型存入redis,有效期为1小时
				JedisUtil.setListDatas("Shop_Nav_AllGoodsCategory", 3600, datasList);
			}
		} else {// redis缓存中已有数据,直接返回数据
			return JSONArray.fromObject(redisList);
		}
		return datasList;
	} 
}
