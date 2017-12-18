package org.silver.shop.impl.system.organization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.ProxyService;
import org.silver.shop.dao.system.organization.ProxyDao;
import org.silver.shop.impl.system.tenant.MerchantWalletServiceImpl;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.organization.Proxy;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.ProxyWalletContent;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = ProxyService.class)
public class ProxyServiceImpl implements ProxyService {

	@Autowired
	private ProxyDao proxyDao;


	@Override
	public List<Object> findProxyBy(String account) {
		Map<String, Object> params = new HashMap<>();
		params.put("loginAccount", account);
		return proxyDao.findByProperty(Proxy.class, params, 0, 0);
	}

	@Override
	public Map<String, Object> getProxyMerchantInfo(String proxyUUid) {
		Map<String, Object> params = new HashMap<>();
		params.put("proxyParentId", proxyUUid);
		List<Merchant> list = proxyDao.findByProperty(Merchant.class, params, 0, 0);
		long count = proxyDao.findByPropertyCount(Merchant.class, params);
		if (list == null) {
			params.clear();
			params.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			params.put(BaseCode.MSG.toString(), "查询失败,服务器繁忙！");
		} else if(!list.isEmpty()){
			params.clear();
			for (Merchant merchant : list) {
				merchant.setLoginPassword("");
			}
			params.put(BaseCode.DATAS.toString(), list);
			params.put(BaseCode.TOTALCOUNT.toString(), count);
			params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			params.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		
		}else{
			params.clear();
			params.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			params.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return params;
	}

	

}
