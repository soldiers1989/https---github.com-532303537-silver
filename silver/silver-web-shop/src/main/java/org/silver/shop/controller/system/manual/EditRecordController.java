package org.silver.shop.controller.system.manual;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.manual.ManualService;
import org.silver.shop.service.system.manual.MdataService;
import org.silver.util.DateUtil;
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
		Map<String, Object> reqMap = new HashMap<>();
		reqMap = manualService.loadDatas(page, size, req);
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
	 * 批量导入手工订单 暂只支持有国宗、企邦
	 * 
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
		Map<String, Object> recordMap = new HashMap<>();
		Enumeration<String> itkeys = req.getParameterNames();
		String key = "";
		while (itkeys.hasMoreElements()) {
			key = itkeys.nextElement();
			String value = req.getParameter(key);
			recordMap.put(key, value);
		}
		return JSONObject.fromObject(mdataService.sendMorderRecord(recordMap, orderNoPack)).toString();
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
		datasMap.put("msg", req.getParameter("msg") + "");
		datasMap.put("messageID", req.getParameter("messageID") + "");
		datasMap.put("entOrderNo", req.getParameter("entOrderNo") + "");
		Map<String, Object> statusMap = mdataService.updateOrderRecordInfo(datasMap);
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 根据订单日期与批次号下载订单信息
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/downMOrderExcel")
	@RequiresRoles("Merchant")
	public void downMOrderExcel(HttpServletRequest req, HttpServletResponse response, String date, String serialNo) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = mdataService.downOrderExcelByDateSerialNo(req, date, serialNo);
		if ("1".equals(statusMap.get("status"))) {
			String filePath = statusMap.get("filePath") + "";
			try {
				downLoad(filePath, response, false);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	//下载
	public void downLoad(String filePath, HttpServletResponse response, boolean isOnLine) throws Exception {
		File f = new File(filePath);
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(f));
		byte[] buf = new byte[2048];
		int len = 0;
		response.reset();
		if (isOnLine) {
			URL u = new URL(filePath);
			response.setContentType(u.openConnection().getContentType());
			response.setHeader("Content-Disposition", "inline; filename=" + toUTF8(f.getName()));
		}
		// 纯下载
		else {
			response.setContentType("application/x-msdownload");
			response.setHeader("Content-Disposition", "attachment; filename=" + toUTF8(f.getName()));
		}
		OutputStream out = response.getOutputStream();
		while ((len = br.read(buf)) > 0)
			out.write(buf, 0, len);
		out.flush();
		br.close();
		out.close();
		 f.delete();
	}

	public String toUTF8(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0 && c <= 255) {
				sb.append(c);
			} else {
				byte[] b;
				try {
					b = Character.toString(c).getBytes("utf-8");
				} catch (Exception ex) {
					System.out.println(ex);
					b = new byte[0];
				}
				for (int j = 0; j < b.length; j++) {
					int k = b[j];
					if (k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 商户修改手工订单信息
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/editMorderInfo", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String editMorderInfo(HttpServletRequest req, HttpServletResponse response, String morderInfoPack,
			int length, int flag) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> datasMap = new HashMap<>();
		try {
			JSONObject json = JSONObject.fromObject(morderInfoPack);
			Map<String, Object> statusMap = mdataService.editMorderInfo(json, length, flag);
			return JSONObject.fromObject(statusMap).toString();
		} catch (Exception e) {
			datasMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			datasMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
			return JSONObject.fromObject(datasMap).toString();
		}
	}

	/**
	 * 商户查询缓存中Excel读取进度
	 * 
	 * @param req
	 * @param response
	 * @return
	 */
	@RequiresRoles("Merchant")
	@RequestMapping(value = "/readExcelRedisInfo", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String readExcelRedisInfo(HttpServletRequest req, HttpServletResponse response) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		Map<String, Object> statusMap = mdataService.readExcelRedisInfo();
		return JSONObject.fromObject(statusMap).toString();

	}
}
