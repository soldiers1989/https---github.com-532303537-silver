package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.SubMerchantService;
import org.silver.shop.dao.system.tenant.SubMerchantDao;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.SubMerchantContent;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONObject;

@Service(interfaceClass = SubMerchantService.class)
public class SubMerchantServiceImpl implements SubMerchantService {

	@Autowired
	private SubMerchantDao subMerchantDao;

	@Override
	public Map<String, Object> addSubMerchantInfo(Map<String, Object> datasMap, String managerId, String managerName) {
		if (datasMap != null && !datasMap.isEmpty()) {
			String merchantId = datasMap.get("merchantId") + "";
			String merchantName = datasMap.get("merchantName") + "";
			if (!checkMerchantInfo(merchantId, merchantName)) {
				return ReturnInfoUtils.errorInfo("商户信息错误!");
			}
			String customsRecordCode = datasMap.get("customsRecordCode") + "";
			if (StringEmptyUtils.isNotEmpty(customsRecordCode) && customsRecordCode.length() == 16) {
				return ReturnInfoUtils.errorInfo("电子口岸(16位编号)错误,请核实信息!");
			}
			SubMerchantContent subMerchant = new SubMerchantContent();
			subMerchant.setMerchantId(merchantId);
			subMerchant.setMerchantName(merchantName);
			subMerchant.setCompanyName(datasMap.get("companyName") + "");
			subMerchant.setCustomsRecordCode(customsRecordCode);
			subMerchant.setCiqRecoreCode(datasMap.get("ciqRecoreCode") + "");
			subMerchant.setMarCode(datasMap.get("marCode") + "");
			subMerchant.setCreateBy(managerName);
			subMerchant.setCreateDate(new Date());
			subMerchant.setDeleteFlag(0);
			if (subMerchantDao.add(subMerchant)) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("添加子商户信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
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
		List<Merchant> merchantList = subMerchantDao.findByProperty(Merchant.class, params, 1, 1);
		return merchantList != null && !merchantList.isEmpty();
	}

	@Override
	public Map<String, Object> getSubMerchantInfo() {
		List<SubMerchantContent> reList = subMerchantDao.findByProperty(SubMerchantContent.class, null, 0, 0);
		Long count = subMerchantDao.findByPropertyCount(SubMerchantContent.class, null);
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
