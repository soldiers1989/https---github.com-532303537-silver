package org.silver.shop.controller;

import java.util.Map;

import org.silver.shop.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value="/tests")
public class IndexController {
   
	@Autowired
	private TestService  testService;
	
	@RequestMapping(value="/a")
	@ResponseBody
	public String showTest(){
		Map<String,Object> reqMap =testService.test();
		return JSONObject.fromObject(reqMap).toString();
	}
}
