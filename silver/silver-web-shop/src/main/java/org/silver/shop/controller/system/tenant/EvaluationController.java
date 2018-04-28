package org.silver.shop.controller.system.tenant;

import java.util.HashMap;
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
	@RequestMapping(value = "/addInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("商城用户进行商品评价")
	// @RequiresRoles("Member")
	public String addInfo(HttpServletRequest req, HttpServletResponse response, @RequestParam("goodsId") String goodsId,
			@RequestParam("content") String content, @RequestParam("level") double level, String memberId,
			String memberName) {
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

	public static void main(String[] args) {
		JSONObject json = JSONObject
				.fromObject(YmHttpUtil.HttpPost("http://localhost:8080/silver-web-shop/evaluation/randomMember", null));
		if (!"1".equals(json.get(BaseCode.STATUS.toString()))) {
			System.out.println("---------------------------" + json);
		}
		Map<String, Object> item = new HashMap<>();
		Map<String, Object> map = (Map<String, Object>) json.get(BaseCode.DATAS.toString());
		item.put("goodsId", "9344949001140_JZBG");
		item.put("content", "没想到这么快就到了，以后还在这买");
		Random random = new Random();
		int s = random.nextInt(9) + 2;
		String l = 4 + "." + s;
		if ("4.9".equals(l) || "4.8".equals(l) || "4.7".equals(l) || "4.6".equals(l) || "4.5".equals(l)|| "4.4".equals(l)|| "4.3".equals(l)) {
			l = "5";
		}
		item.put("level", l);
		item.put("memberId", map.get("memberId"));
		item.put("memberName", map.get("memberName"));

		System.out.println(
				"--评论结果->>" + YmHttpUtil.HttpPost("http://localhost:8080/silver-web-shop/evaluation/addInfo", item));
	}
}
