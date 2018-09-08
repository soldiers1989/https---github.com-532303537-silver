package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.ExpadndMerchantService;
import org.silver.shop.dao.system.tenant.ExpadndMerchantDao;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.ExpadndMerchantContent;
import org.silver.shop.util.IdUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service(interfaceClass = ExpadndMerchantService.class)
public class ExpadndMerchantServiceImpl implements ExpadndMerchantService {

	@Autowired
	private ExpadndMerchantDao expadndMerchantDao;

	@Autowired
	private IdUtils idUtils;

	@Override
	public Map<String, Object> addInfo(Map<String, Object> datasMap) {
		Map<String, Object> reCheckMap = checkData("add", datasMap);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		ExpadndMerchantContent entity = new ExpadndMerchantContent();
		String supMerchantId = datasMap.get("supMerchantId") + "";
		String supMerchantName = datasMap.get("supMerchantName") + "";
		Map<String, Object> reIdMap = idUtils.createId(ExpadndMerchantContent.class, "exMerchant_");
		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		String code = reIdMap.get(BaseCode.DATAS.toString()) + "";
		entity.setSupMerchantId(supMerchantId);
		entity.setSupMerchantName(supMerchantName);
		entity.setExpadndMerchantCode(code);
		entity.setExpadndMerchantName(datasMap.get("expadndMerchantName") + "");
		entity.setLoginName(datasMap.get("loginName") + "");
		String legalName = datasMap.get("legalName") + "";
		if (StringEmptyUtils.isNotEmpty(legalName)) {
			entity.setLegalName(legalName);
		}
		try{
			entity.setProfit(Double.parseDouble(datasMap.get("profit") + ""));
		}catch (Exception e) {
			return ReturnInfoUtils.errorInfo("分润参数错误！");
		}
		entity.setYsPartnerNo(datasMap.get("ysPartnerNo") + "");
		entity.setCreateDate(new Date());
		entity.setCreateBy(supMerchantName);
		//
		entity.setDeleteFlag(0);
		if (expadndMerchantDao.add(entity)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("添加失败,服务器繁忙!");
	}

	/**
	 * 根据不同银行卡业务类型，校验数据
	 * 
	 * @param type
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> checkData(String type, Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("校验参数时，请求参数不能为空！");
		}
		List<String> noNullKeys = new ArrayList<>();
		JSONArray jsonList = null;
		switch (type) {
		case "add":// 添加用户银行卡
			noNullKeys.add("supMerchantId");
			noNullKeys.add("supMerchantName");
			noNullKeys.add("expadndMerchantName");
			noNullKeys.add("loginName");
			noNullKeys.add("ysPartnerNo");
			noNullKeys.add("profit");
			jsonList = new JSONArray();
			jsonList.add(datasMap);
			return CheckDatasUtil.checkData(jsonList, noNullKeys);
		default:
			return ReturnInfoUtils.errorInfo("校验银行卡信息时，[" + type + "]类型错误！");
		}

	}

	/**
	 * 校验商户信息是否正确
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 */
	private boolean checkMerchantInfo(String merchantId, String merchantName) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("merchantName", merchantName);
		List<Merchant> merchantList = expadndMerchantDao.findByProperty(Merchant.class, params, 1, 1);
		return merchantList != null && !merchantList.isEmpty();
	}

	@Override
	public Map<String, Object> getSubMerchantInfo() {
		List<ExpadndMerchantContent> reList = expadndMerchantDao.findByProperty(ExpadndMerchantContent.class, null, 0,
				0);
		Long count = expadndMerchantDao.findByPropertyCount(ExpadndMerchantContent.class, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count.intValue());
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Object editSubMerchantInfo(Map<String, Object> datasMap, String managerId, String managerName) {
		if (datasMap != null && !datasMap.isEmpty()) {
			Map<String, Object> params = new HashMap<>();
			String merchantId = datasMap.get("merchantId") + "";
			String merchantName = datasMap.get("merchantName") + "";
			if (!checkMerchantInfo(merchantId, merchantName)) {
				return ReturnInfoUtils.errorInfo("商户信息错误!");
			}
			String customsRecordCode = datasMap.get("customsRecordCode") + "";
			if (StringEmptyUtils.isNotEmpty(customsRecordCode) && customsRecordCode.length() == 16) {
				return ReturnInfoUtils.errorInfo("电子口岸(16位编号)错误,请核实信息!");
			}

		}
		return null;
	}

}
