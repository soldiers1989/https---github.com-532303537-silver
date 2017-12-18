package org.silver.shop.service.system.manual;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.api.system.manual.MuserService;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.utils.ExcelUtil;
import org.silver.util.FileUpLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("mdataService")
public class MdataService {

	@Reference
	private MuserService muserService;
	@Reference
	private MpayService mpayService;
	@Autowired
	private FileUpLoadService fileUpLoadService;

	public Map<String, Object> addEntity(String merchant_no, String[] strs) {
		return muserService.addEntity(merchant_no, strs);

	}

	public Map<String, Object> loadMuserDatas(String merchant_no, int page, int size) {

		return muserService.loadMuserDatas(merchant_no, page, size);
	}

	public Map<String, Object> delMubySysno(String merchant_no, String muser_sys_no) {

		return muserService.delMubySysno(merchant_no, muser_sys_no);
	}

	public Map<String, Object> groupAddmu(String merchant_no, HttpServletRequest req) {
		Map<String, Object> reqMap = fileUpLoadService.universalDoUpload(req, "/gadd-excel/", ".xls", false, 400, 400,
				null);
		List<Map<String, Object>> errl = new ArrayList<Map<String, Object>>();
		if ((int) reqMap.get("status") == 1) {
			List<String> list = (List<String>) reqMap.get("datas");
			File f = new File("/gadd-excel/" + list.get(0));
			ExcelUtil excel = new ExcelUtil();
			excel.open(f);
			readMuserSheet(0, excel, errl, merchant_no);// 读取订单工作表
			excel.closeExcel();
			// excel.getFile().delete();
			f.delete();
			reqMap.clear();
			reqMap.put("status", 1);
			reqMap.put("msg", "导入完成");
			reqMap.put("err", errl);
			return reqMap;
		}
		return null;
	}

	private Map<String, Object> readMuserSheet(int sheet, ExcelUtil excel, List<Map<String, Object>> errl,
			String merchant_no) {
		Map<String, Object> statusMap = new HashMap<>();
		String[] strs = new String[15];
		for (int r = 1; r <= excel.getRowCount(sheet); r++) {
			if (excel.getColumnCount(r) == 0) {
				break;
			}
			for (int c = 0; c < excel.getColumnCount(r); c++) {
				String value = excel.getCell(sheet, r, c);
				if (c == 0 && "".equals(value)) {
					statusMap.put("status", 1);
					statusMap.put("msg", "导入完成");
					statusMap.put("err", errl);
					return statusMap;
				}
				if (c <= 7) {
					strs[c] = value;
				} else {
					break;
				}

			}
			Map<String, Object> item = muserService.addEntity(merchant_no, strs);
			if ((int) item.get("status") != 1) {
				Map<String, Object> errMap = new HashMap<String, Object>();
				errMap.put("msg", "【人员录入工作表】第" + (r + 1) + "行-->" + item.get("msg"));
				errl.add(errMap);
			}
		}
		statusMap.put("status", 1);
		statusMap.put("msg", "导入完成");
		statusMap.put("err", errl);
		return statusMap;

	}

	public Map<String, Object> groupCreateMpay(List<String> orderIDs) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		return mpayService.groupCreateMpay(merchantId, orderIDs);
	}

	public Object sendMpayRecord(Map<String, Object> recordMap, String tradeNoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		String proxyParentId = merchantInfo.getProxyParentId();
		String proxyParentName = merchantInfo.getProxyParentName();
		return mpayService.sendMpayByRecord(merchantId, recordMap, tradeNoPack,proxyParentId,merchantName,proxyParentName);
	}

	public Object sendMorderRecord(Map<String, Object> recordMap, String orderNoPack) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		// 获取登录后的商户账号
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		String proxyParentId = merchantInfo.getProxyParentId();
		String proxyParentName = merchantInfo.getProxyParentName();
		return mpayService.sendMorderRecord(merchantId, recordMap, orderNoPack,proxyParentId,merchantName,proxyParentName);
	}

	// 更新订单信息
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap) {
		return mpayService.updateOrderRecordInfo(datasMap);
	}

	public Map<String, Object> updatePayRecordInfo(Map<String, Object> datasMap) {
		return mpayService.updatePayRecordInfo(datasMap);
	}
}
