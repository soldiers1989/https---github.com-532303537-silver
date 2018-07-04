package org.silver.shop.impl.system.manual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.system.tenant.MerchantIdCardCostService;
import org.silver.shop.dao.system.tenant.MerchantIdCardCostDao;
import org.silver.shop.model.system.tenant.MerchantIdCardCostContent;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantIdCardCostService.class)
public class MerchantIdCardCostServiceImpl implements MerchantIdCardCostService{

	@Autowired
	private MerchantIdCardCostDao merchantIdCardCostDao; 
	
	@Override
	public Map<String, Object> getIdCardCostInfo(String merchantId) {
		if(StringEmptyUtils.isEmpty(merchantId)){
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<MerchantIdCardCostContent> merchantList = merchantIdCardCostDao.findByProperty(MerchantIdCardCostContent.class, params, 1, 1);
		if (merchantList == null) {
			return ReturnInfoUtils.errorInfo("查询商户实名认证信息失败,服务器繁忙!");
		} else if (!merchantList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(merchantList.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("未找到商户实名认证费率信息,请联系管理员!");
		}
	}
	
}
