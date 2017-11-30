package org.silver.shop.controller.common.category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.common.category.CategoryTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
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
	 * @return HashMap
	 */
	@RequestMapping(value = "/AllGoodsCategory", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String findAllCategory(HttpServletResponse req, HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object>  statusMap = categoryTransaction.findAllCategory();
		return JSONObject.fromObject(statusMap).toString();
	}
	
	@RequestMapping(value = "/addGoodsCategory", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员添加商品类型")
	public String addGoodsCategory(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = categoryTransaction.addGoodsCategory(req);
		return JSONObject.fromObject(statusMap).toString();
	}
	
	@RequestMapping(value = "/deleteGoodsCategory", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员删除商品类型")
	public String deleteGoodsCategory(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = categoryTransaction.deleteGoodsCategory(req);
		return JSONObject.fromObject(statusMap).toString();
	}
	
	@RequestMapping(value = "/editGoodsCategory", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	@ApiOperation("管理员修改商品类型")
	public String editGoodsCategory(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = categoryTransaction.editGoodsCategory(req);
		return JSONObject.fromObject(statusMap).toString();
	}
}
