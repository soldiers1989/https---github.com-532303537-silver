package org.silver.shop.controller.system.organization;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/merchant")
public class MerchantController {

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public String login(@RequestParam("account") String account, @RequestParam("loginPassword") String loginPassword) {
		Map<String, Object> reMap = null;

		return null;
	}

	@RequestMapping(value="/register")
	public Map<String, Object> merchantRegister(HttpServletRequest req,HttpServletResponse resp) {
		Map<String,Object> reMap = null;
		return null;
	}
}
