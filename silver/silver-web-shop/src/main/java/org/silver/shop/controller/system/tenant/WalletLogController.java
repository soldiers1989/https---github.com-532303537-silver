package org.silver.shop.controller.system.tenant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.util.ExcelUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 钱包日志Controller
 */
@Controller
@RequestMapping("/walletLog")
public class WalletLogController {
	
	public static void main(String[] args) {
		File file = new File("C:/Users/Lenovo/Desktop/国宗表单/国宗原订单/2018-02/78428656574申报资料 -溢装1件.xlsx");
		ExcelUtil excel = new ExcelUtil();
		excel.open(file);
		String value = excel.getCell(0, 1, 54);
		System.out.println(value);
	}
}
