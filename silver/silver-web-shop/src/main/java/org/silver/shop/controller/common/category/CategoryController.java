package org.silver.shop.controller.common.category;

import java.util.HashMap;
import java.util.List;
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
		Map<String, Object> statusMap = new HashMap<>();
		List datasList = categoryTransaction.findAllCategory();
		if (datasList != null && datasList.size() > 0) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.DATAS.getBaseCode(), datasList);
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return JSONObject.fromObject(statusMap).toString();
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}
}
