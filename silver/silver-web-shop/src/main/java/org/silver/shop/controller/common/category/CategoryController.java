package org.silver.shop.controller.common.category;

import java.util.HashMap;
import java.util.Map;

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
	
	@RequestMapping(value="/findAll" ,produces = "application/json; charset=utf-8")
	@ResponseBody
	public String findAllCategory() {
		Map<String, HashMap<String, Object>> reMap = categoryTransaction.findAllCategoryTpye();
		return JSONObject.fromObject(reMap).toString();
	}
}
