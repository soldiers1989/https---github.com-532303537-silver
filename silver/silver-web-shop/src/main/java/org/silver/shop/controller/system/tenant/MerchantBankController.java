package org.silver.shop.controller.system.tenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.service.system.tenant.MerchantBankInfoTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 商户银行Controller
 */
@Controller
@RequestMapping("/merchantBank")
public class MerchantBankController {

	@Autowired
	private MerchantBankInfoTransaction merchantBankInfoTransaction;

	/**
	 * 添加银行卡信息
	 * 
	 * @param bankName
	 * @param bankAccount
	 * @param type
	 *            默认选择：1-默认选中,2-备用
	 * @return
	 */
	@RequestMapping(value = "/addMerchantBankInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ApiOperation("添加银行卡信息")
	@RequiresRoles("Merchant")
	@ResponseBody
	public String addMerchantBankInfo(@RequestParam("bankName") String bankName,
			@RequestParam("bankAccount") String bankAccount, @RequestParam("defaultFalg") int defaultFalg) {
		Map<String, Object> statusMap = new HashMap<>();
		if (bankName != null && bankAccount != null) {
			boolean flag = merchantBankInfoTransaction.addMerchantBankInfo(bankName, bankAccount, defaultFalg);
			if (flag) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), 1);
				statusMap.put(BaseCode.MSG.getBaseCode(), "保存银行卡信息成功！");
				return JSONObject.fromObject(statusMap).toString();
			}
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.UNKNOWN.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}

	/**
	 * 获取商户银行卡信息
	 * 
	 * @param page
	 *            页面
	 * @param size
	 *            条数
	 * @return Map
	 */
	@RequestMapping(value = "/getMerchantBankInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("获取商户银行卡信息")
	public String getMerchantBankInfo(@RequestParam("page") int page, @RequestParam("size") int size) {
		Map<String, Object> reMap = new HashMap<>();
		List<Object> bankInfoList = merchantBankInfoTransaction.findMerchantBankInfo(page, size);
		if (!bankInfoList.isEmpty()) {
			reMap.put(BaseCode.STATUS.getBaseCode(), 1);
			reMap.put(BaseCode.DATAS.getBaseCode(), bankInfoList);
			reMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return JSONObject.fromObject(reMap).toString();
		}
		reMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
		reMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
		return JSONObject.fromObject(reMap).toString();
	}

	/**
	 * 设置默认银行卡
	 * @param id 
	 * @return
	 */
	@RequestMapping(value = "/selectMerchantBankInfoDefault", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("设置默认银行卡")
	public String selectMerchantBankInfoDefault(@RequestParam(value = "id") long id) {
		Map<String, Object> statusMap = new HashMap<>();
		if (id > 0) {
			statusMap = merchantBankInfoTransaction.selectMerchantBank(id);
			return JSONObject.fromObject(statusMap).toString();
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.UNKNOWN.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}

	@RequestMapping(value = "/deleteMerchantBankInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@RequiresRoles("Merchant")
	@ApiOperation("删除银行卡信息")
	public String deleteMerchantBankInfo(@RequestParam(value = "id") long id) {
		Map<String, Object> statusMap = new HashMap<>();
		if (id > 0) {
			statusMap = merchantBankInfoTransaction.deleteBankInfo(id);
			return JSONObject.fromObject(statusMap).toString();
		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.UNKNOWN.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.UNKNOWN.getMsg());
		return JSONObject.fromObject(statusMap).toString();
	}
}
