package org.silver.shop.impl.system.tenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.system.tenant.SubMerchantService;
import org.silver.shop.dao.system.tenant.SubMerchantDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.SubMerchantContent;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONObject;

@Service(interfaceClass = SubMerchantService.class)
public class SubMerchantServiceImpl implements SubMerchantService {

	@Autowired
	private SubMerchantDao subMerchantDao;

	@Override
	public Map<String, Object> addSubMerchantInfo(JSONObject json) {
		Map<String, Object> params = new HashMap<>();
		if (json != null) {
			String merchantId = json.get("merchantId") + "";
			params.put("merchantId", merchantId);
			List<Merchant> reMerchantList = subMerchantDao.findByProperty(Merchant.class, params, 1, 1);
			if (reMerchantList != null && !reMerchantList.isEmpty()) {
				Merchant merchant = reMerchantList.get(0);
				String merchantName = merchant.getMerchantName();
				SubMerchantContent subMerchant = new SubMerchantContent();
				subMerchant.setMerchantId(merchantId);
				subMerchant.setMerchantName(merchantName);
				//subMerchant.setCompanyName(companyName);
				
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

}
