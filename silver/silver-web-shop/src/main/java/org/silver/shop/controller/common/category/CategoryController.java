package org.silver.shop.controller.common.category;

import java.util.HashMap;
import java.util.Map;

import org.silver.common.StatusCode;
import org.silver.shop.service.common.category.CategoryTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.coobird.thumbnailator.tasks.StreamThumbnailTask;
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
			datasMap.put("datas", reMap);
			datasMap.put("status", StatusCode.SUCCESS.getStatus());
			datasMap.put("msg", StatusCode.SUCCESS.getMsg());
		} else {
			datasMap.put("status", StatusCode.NO_DATAS.getStatus());
			datasMap.put("msg", StatusCode.NO_DATAS.getMsg());
		}
		return JSONObject.fromObject(datasMap).toString();
	}
}
