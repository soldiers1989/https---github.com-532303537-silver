package org.silver.shop.controller.common.category;

import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.common.category.CategoryTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

/**
 * 商品类型Controller
 */
@Controller
@RequestMapping("/category")
public class CategoryController {

	@Autowired
	private CategoryTransaction categoryTransaction;

	/**
	 * 查询所有商品类型
	 * 
	 * @return HashMap
	 */
	@RequestMapping(value = "/AllGoodsCategory", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String findAllCategory() {
		Map<String, Object> datasMap = new HashMap<>();
		Map<String, HashMap<String, Object>> reMap = categoryTransaction.findAllCategory();
		if (!reMap.isEmpty()) {
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			datasMap.put(BaseCode.DATAS.getBaseCode(), reMap);
			datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return JSONObject.fromObject(datasMap).toString();
		}
		datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
		datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
		return JSONObject.fromObject(datasMap).toString();
	}
}
