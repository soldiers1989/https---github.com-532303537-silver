package org.silver.shop.controller.system.manual;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.shop.service.system.manual.MdataService;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import net.sf.json.JSONObject;

@RestController
@RequestMapping(value = "/manual")
public class EditRecordController {

	@Autowired
	private ManualService manualService;

	@Autowired
	private MdataService mdataService;

	@RequestMapping(value = "/addMorder", produces = "application/json; charset=utf-8")
	public String addMorder(HttpServletResponse resp, HttpServletRequest req, int length) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<String, Object>();
		Enumeration<String> es = req.getParameterNames();
		int H = 0;
		int n = 0;
		int nIndex = 0;
		String[] head = new String[15];
		String[][] body = new String[length][15];
		while (es.hasMoreElements()) {
			String key = es.nextElement();
			String value = req.getParameter(key);
			if (key.contains("head")) {
				if (value != null && !"".equals(value.trim())) {
					head[H] = value;
				}
				H++;
			} else {
				if (key.contains("body[" + n + "]")) {
					body[n][nIndex] = value;
					nIndex++;
					if (nIndex > 13) {
						n++;
						nIndex = 0;
					}
				}
			}
		}
		if (manualService.saveDatas("YM20170000015078659178651922", head, length, body)) {
			reqMap.put("status", 1);
			reqMap.put("msg", "手工订单入库成功");
			return JSONObject.fromObject(reqMap).toString();
		}
		reqMap.put("status", -1);
		reqMap.put("msg", "手工订单入库出错，请重试");
		JSONObject.fromObject(reqMap).toString();
		return JSONObject.fromObject(reqMap).toString();
	}

	@RequestMapping(value = "/loadMorderDatas", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	public String loadMorderDatas(HttpServletResponse resp, HttpServletRequest req, int page, int size) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<String, Object>();
		reqMap = manualService.loadDatas(page, size);
		return JSONObject.fromObject(reqMap).toString();
	}

	@RequestMapping(value = "/groupAdd", produces = "application/json; charset=utf-8")
	public String groupAdd(HttpServletResponse resp, HttpServletRequest req) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		String autoMSHR = req.getParameter("AutoMatchSHR");// 是否自动匹配收货人 标识为1自动匹配
		String autoMXDR = req.getParameter("AutoMatchXDR");// 是否自动匹配下单人 标识为1自动匹配

		Map<String, Object> reqMap = manualService.groupAdd("YM20170000015078659178651922", req, autoMSHR, autoMXDR);
		return JSONObject.fromObject(reqMap).toString();

	}

	@RequestMapping(value = "/delete", produces = "application/json; charset=utf-8")
	public String delete(HttpServletResponse resp, HttpServletRequest req, String order_id) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = manualService.deleteByOrderId("YM20170000015078659178651922", order_id);
		return JSONObject.fromObject(reqMap).toString();

	}

	/********************** 手工录入人员信息 ************************/
	@RequestMapping(value = "/addMuser", produces = "application/json; charset=utf-8")
	public String addMuser(HttpServletResponse resp, HttpServletRequest req, int length) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<String, Object>();
		String[] strs = new String[length];
		String value = "";
		for (int i = 0; i < length; i++) {
			value = req.getParameter(i + "");
			if (value != null && !value.trim().equals("")) {
				strs[i] = value;
			} else {
				reqMap.put("status", -3);
				reqMap.put("msg", "信息不完整，请重新填写");
				return JSONObject.fromObject(reqMap).toString();
			}
		}
		reqMap = mdataService.addEntity("YM20170000015078659178651922", strs);
		return JSONObject.fromObject(reqMap).toString();

	}

	@RequestMapping(value = "/loadMuserDatas", produces = "application/json; charset=utf-8")
	public String loadMusers(HttpServletResponse resp, HttpServletRequest req, int page, int size) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<String, Object>();

		reqMap = mdataService.loadMuserDatas("YM20170000015078659178651922", page, size);
		return JSONObject.fromObject(reqMap).toString();

	}

	@RequestMapping(value = "/delMubySysno", produces = "application/json; charset=utf-8")
	public String delMubySysno(HttpServletResponse resp, HttpServletRequest req, String muser_sys_no) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<String, Object>();

		reqMap = mdataService.delMubySysno("YM20170000015078659178651922", muser_sys_no);
		return JSONObject.fromObject(reqMap).toString();

	}

	@RequestMapping(value = "/groupAddmu", produces = "application/json; charset=utf-8")
	public String groupAddmu(HttpServletResponse resp, HttpServletRequest req) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<String, Object>();
		reqMap = mdataService.groupAddmu("YM20170000015078659178651922", req);
		return JSONObject.fromObject(reqMap).toString();

	}

	/**
	 * 批量导入手工订单
	 *  暂只支持有国宗、企邦
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/groupAddOrder", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	public String groupAddOrder(HttpServletResponse resp, HttpServletRequest req) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = manualService.groupAddOrder(req);
		return JSONObject.fromObject(reqMap).toString();

	}

	/********************************** 模拟生成支付信息 *********************************/

	/**
	 * 根据订单号生成支付单（支持批量）
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/createMpayByOID", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	public String createMpayByOID(HttpServletResponse resp, HttpServletRequest req, int length) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<>();
		Enumeration<String> itkeys = req.getParameterNames();
		List<String> orderIDs = new ArrayList<>();
		String key = "";
		while (itkeys.hasMoreElements()) {
			key = itkeys.nextElement();
			String value = req.getParameter(key).trim();
			orderIDs.add(value);
		}
		orderIDs.remove(length);
		if (!orderIDs.isEmpty()) {
			return JSONObject.fromObject(mdataService.groupCreateMpay(orderIDs)).toString();
		}
		reqMap.put("status", -3);
		reqMap.put("msg", "缺少订单编号，生成失败");
		return JSONObject.fromObject(reqMap).toString();
	}

	/**
	 * 发起支付单备案
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/sendMpayRecord", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	public String sendMpayRecord(HttpServletResponse resp, HttpServletRequest req, String tradeNoPack) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<>();
		Map<String, Object> recordMap = new HashMap<>();
		Enumeration<String> itkeys = req.getParameterNames();
		String key = "";
		while (itkeys.hasMoreElements()) {
			key = itkeys.nextElement();
			String value = req.getParameter(key);
			recordMap.put(key, value);
		}
		if (!recordMap.isEmpty()) {
			return JSONObject.fromObject(mdataService.sendMpayRecord(recordMap, tradeNoPack)).toString();
		}
		reqMap.put("status", -3);
		reqMap.put("msg", "缺少支付流水号");
		return JSONObject.fromObject(reqMap).toString();
	}

	/**
	 * 发起订单备案
	 * 
	 * @param resp
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/sendMorderRecord", produces = "application/json; charset=utf-8")
	@RequiresRoles("Merchant")
	public String sendMorderRecord(HttpServletResponse resp, HttpServletRequest req, String orderNoPack) {
		String originHeader = req.getHeader("Origin");
		resp.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		resp.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> reqMap = new HashMap<>();
		Map<String, Object> recordMap = new HashMap<>();
		Enumeration<String> itkeys = req.getParameterNames();
		String key = "";
		while (itkeys.hasMoreElements()) {
			key = itkeys.nextElement();
			String value = req.getParameter(key);
			recordMap.put(key, value);
		}
		if (!recordMap.isEmpty()) {
			return JSONObject.fromObject(mdataService.sendMorderRecord(recordMap, orderNoPack)).toString();
		}
		reqMap.put("status", -3);
		reqMap.put("msg", "缺少支付流水号");
		return JSONObject.fromObject(reqMap).toString();
	}

	/**
	 * 备案网关异步回馈订单备案信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/reOrderNotifyMsg", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String reOrderNotifyMsg(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		datasMap.put("status", req.getParameter("status") + "");
		datasMap.put("errMsg", req.getParameter("errMsg") + "");
		datasMap.put("messageID", req.getParameter("messageID") + "");
		datasMap.put("entOrderNo", req.getParameter("entOrderNo") + "");
		Map<String, Object> statusMap = mdataService.updateOrderRecordInfo(datasMap);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 备案网关异步回馈支付单信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/rePayNotifyMsg", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String rePayNotifyMsg(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		datasMap.put("status", req.getParameter("status") + "");
		datasMap.put("errMsg", req.getParameter("errMsg") + "");
		datasMap.put("messageID", req.getParameter("messageID") + "");
		datasMap.put("entPayNo", req.getParameter("entPayNo") + "");
		Map<String, Object> statusMap = mdataService.updatePayRecordInfo(datasMap);
		return JSONObject.fromObject(statusMap).toString();
	}

	public static void main(String[] args) {
		for(int i = 0 ; i<5 ; i++){
			for(int x = 0 ; x <5 ; x++){
				if(x == 1){
					break;
				}
				System.out.println("x --->"+x);
			}
			System.out.println("---------------"+i);
		}
	}
}
