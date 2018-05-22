package org.silver.shop.controller.system.tenant;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.service.system.tenant.EvaluationTransaction;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商品评论Controller
 */
@Controller
@RequestMapping("/evaluation")
public class EvaluationController {

	@Autowired
	private EvaluationTransaction evaluationTransaction;

	/**
	 * 商城前台获取商品评价信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商城前台获取商品评价信息")
	public String getInfo(HttpServletRequest req, HttpServletResponse response, @RequestParam("goodsId") String goodsId,
			@RequestParam("size") int size, @RequestParam("page") int page) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(goodsId)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("商品Id不能为空!")).toString();
		}
		return JSONObject.fromObject(evaluationTransaction.getInfo(goodsId, page, size)).toString();
	}

	/**
	 * 商城前台获取商品评价信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/temporary-addInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商城用户进行商品评价")
	// @RequiresRoles("Member")
	public String temporaryAddInfo(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("goodsId") String goodsId, @RequestParam("content") String content,
			@RequestParam("level") double level, String memberId, String memberName) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(goodsId)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("商品Id不能为空!")).toString();
		}
		return JSONObject.fromObject(evaluationTransaction.addInfo(goodsId, content, level, memberId, memberName))
				.toString();
	}

	/**
	 * 临时接口--随机获取用户信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/randomMember", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("临时接口--随机获取用户信息")
	public String randomMember(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(evaluationTransaction.randomMember()).toString();
	}

	@RequestMapping(value = "/brushEvaluation", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("临时接口--评论商品")
	public String brushEvaluation(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("goodsId") String goodsId, @RequestParam("content") String content) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		JSONObject json = JSONObject
				.fromObject(YmHttpUtil.HttpPost("http://localhost:8080/silver-web-shop/evaluation/randomMember", null));
		if (!"1".equals(json.get(BaseCode.STATUS.toString()))) {
			System.out.println("---------------------------" + json);
		}
		Map<String, Object> item = new HashMap<>();
		Map<String, Object> map = (Map<String, Object>) json.get(BaseCode.DATAS.toString());
		// goodsId
		// 9344949001140_JZBG
		item.put("goodsId", goodsId);
		item.put("content", content);
		Random random = new Random();
		int s = random.nextInt(9) + 2;
		String l = 4 + "." + s;
		if ("4.9".equals(l) || "4.8".equals(l) || "4.7".equals(l) || "4.6".equals(l) || "4.5".equals(l)
				|| "4.4".equals(l) || "4.3".equals(l)) {
			l = "5";
		}
		item.put("level", l);
		item.put("memberId", map.get("memberId"));
		item.put("memberName", map.get("memberName"));
		String resources = YmHttpUtil.HttpPost("http://localhost:8080/silver-web-shop/evaluation/addInfo", item);
		return JSONObject.fromObject(resources).toString();
	}

	/**
	 * 用户对订单中商品进行评价
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/addEvaluation", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商城用户进行商品评价")
	@RequiresRoles("Member")
	public String addEvaluation(HttpServletRequest req, HttpServletResponse response,String goodsInfoPack ,String entOrderNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		if (StringEmptyUtils.isEmpty(entOrderNo)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单Id不能为空!")).toString();
		}
		if (StringEmptyUtils.isEmpty(goodsInfoPack)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("订单商品信息不能为空!")).toString();
		}
		return JSONObject.fromObject(evaluationTransaction.addEvaluation(entOrderNo,goodsInfoPack)).toString();
	}

	/**
	 * 商户后台获取所有商品评价信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/merchantGetInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商户后台获取所有商品评价信息")
	@RequiresRoles("Merchant")
	public String merchantGetInfo(HttpServletRequest req, HttpServletResponse response, String goodsName,
			String memberName) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(evaluationTransaction.merchantGetInfo(goodsName, memberName)).toString();
	}

	/**
	 * 管理员获取所有商品评价信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/managerGetInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员获取所有商品评价信息")
	@RequiresRoles("Manager")
	public String managerGetInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			String value = req.getParameter(key);
			datasMap.put(key, value);
		}
		return JSONObject.fromObject(evaluationTransaction.managerGetInfo(datasMap)).toString();
	}
	
	/**
	 * 管理员删除商品评价信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/managerDeleteInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("管理员删除商品评价信息")
	@RequiresRoles("Manager")
	public String managerDeleteInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		List<String> idList = new ArrayList<>();
		Enumeration<String> isKeys = req.getParameterNames();
		while (isKeys.hasMoreElements()) {
			String key = isKeys.nextElement();
			idList.add(req.getParameter(key));
		}
		if(idList.isEmpty()){
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("流水Id不能为空!")).toString();
		}
		return JSONObject.fromObject(evaluationTransaction.managerDeleteInfo(idList)).toString();
	}
}
