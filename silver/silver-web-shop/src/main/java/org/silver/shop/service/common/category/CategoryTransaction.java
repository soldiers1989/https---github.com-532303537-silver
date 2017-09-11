package org.silver.shop.service.common.category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.common.category.CategoryService;
import org.silver.shop.model.common.category.GoodsFirstType;
import org.silver.shop.model.common.category.GoodsSecondType;
import org.silver.shop.model.common.category.GoodsThirdType;
import org.silver.util.JedisUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

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
	 * @return Map
	 */
	public Map<String, HashMap<String, Object>> findAllCategory() {
		// 获取在redis中的所有商品类型
		String redisMap = JedisUtil.get("Shop_Nav_AllGoodsCategory");
		Map<String, HashMap<String, Object>> reMap = null;
		if (redisMap == null || "".equals(redisMap)) {// redis缓存没有数据,重新读取
			reMap = categoryService.findGoodsType();
			// 将已查询出来的商品类型存入redis,有效期为1小时
			JedisUtil.set("Shop_Nav_AllGoodsCategory", 3600, reMap);
			//redis缓存没有数据,重新读取之后返回
			return reMap;
		} else {// redis缓存中已有数据,直接返回数据
			return JSONObject.fromObject(redisMap);
		}
	}
}
