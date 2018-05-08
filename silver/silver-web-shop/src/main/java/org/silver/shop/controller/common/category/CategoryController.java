package org.silver.shop.controller.common.category;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.common.category.CategoryTransaction;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	 * 
	 * @return HashMap
	 */
	@RequestMapping(value = "/AllGoodsCategory", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String findAllCategory(HttpServletRequest req, HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> statusMap = categoryTransaction.findAllCategory();
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
		return JSONObject.fromObject(categoryTransaction.editGoodsCategory(req)).toString();
	}

	/**
	 * 管理员查询商品类型详情
	 * 
	 * @param req
	 * @param response
	 * @param type
	 *            1-第一级,2-第二级,3-第三级
	 * @param id
	 *            类型Id
	 * @return
	 */
	@RequestMapping(value = "/getCategoryInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	public String getCategoryInfo(HttpServletRequest req, HttpServletResponse response, @RequestParam("type") int type,
			@RequestParam("id") String id) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (type == 1 || type == 2 || type == 3 && StringEmptyUtils.isNotEmpty(id)) {
			statusMap = categoryTransaction.getCategoryInfo(type, id);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 根据等级查询商品类型
	 * 
	 * @param req
	 * @param response
	 * @param type
	 *            1-第一级,2-第二级,3-第三级
	 * @param id
	 *            类型Id
	 * @return
	 */
	@RequestMapping(value = "/searchCategoryInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Manager")
	public String searchCategoryInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("type") int type) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = new HashMap<>();
		if (type == 1 || type == 2 || type == 3) {
			statusMap = categoryTransaction.searchCategoryInfo(type);
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		}
		return JSONObject.fromObject(statusMap).toString();
	}
}
