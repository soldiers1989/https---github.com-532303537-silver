package org.silver.shop.controller.system.log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.shop.service.system.log.StockReviewLogTransaction;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@RequestMapping("/stockReviewLog")
@Controller
public class StockReviewLogController {

	@Autowired
	private StockReviewLogTransaction stockReviewLogTransaction;

	@RequestMapping(value = "/merchantGetLog", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	@ResponseBody
	@ApiOperation("商户查看库存审核日志")
	public String merchantGetLog(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("entGoodsNo") String entGoodsNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		return JSONObject.fromObject(stockReviewLogTransaction.merchantGetLog(entGoodsNo)).toString();
	}
}
