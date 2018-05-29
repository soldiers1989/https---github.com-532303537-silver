package org.silver.shop.service.common.base;


import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.silver.common.BaseCode;
import org.silver.common.LoginType;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

import net.sf.json.JSONArray;

@Service
public class CustomsPortTransaction {

	@Reference
	private CustomsPortService customsPortService;
	/**
	 * 缓存系统已开通口岸键
	 */
	private static final String SHOP_PORT_ALLCUSTOMSPORT_LIST = "Shop_Port_AllCustomsPort_List";

	// 添加口岸下已开通的 海关及国检名称与编码
	public Map<String, Object> addCustomsPort(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		String managerId = managerInfo.getManagerId();
		Map<String, Object> reMap = customsPortService.addCustomsPort(params, managerId, managerName);
		if ("1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			Map<String, Object> datasMap = customsPortService.findAllCustomsPort();
			// 将查询出来的口岸数据放入缓存中
			JedisUtil.set(SHOP_PORT_ALLCUSTOMSPORT_LIST.getBytes(),
					SerializeUtil.toBytes(datasMap.get(BaseCode.DATAS.toString())), 3600);
			return ReturnInfoUtils.successInfo();
		}
		return reMap;
	}

	// 查询所有已开通的口岸及关联的海关
	public Map<String, Object> findAllCustomsPort() {
		byte[] redisByte = JedisUtil.get(SHOP_PORT_ALLCUSTOMSPORT_LIST.getBytes());
		if (redisByte != null) {
			return ReturnInfoUtils.successDataInfo(JSONArray.fromObject(SerializeUtil.toObject(redisByte)));
		} else {// 缓存中没有数据,重新访问数据库读取数据
			Map<String, Object> datasMap = customsPortService.findAllCustomsPort();
			if (!datasMap.get(BaseCode.STATUS.toString()).equals("1")) {
				return datasMap;
			}
			// 将查询出来的口岸数据放入缓存中
			JedisUtil.set(SHOP_PORT_ALLCUSTOMSPORT_LIST.getBytes(),
					SerializeUtil.toBytes(datasMap.get(BaseCode.DATAS.toString())), 3600);
			return datasMap;
		}
	}

	// 商户查询当前已备案的海关及智检信息
	public Map<String, Object> findMerchantCustomsPort() {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Merchant merchantInfo = (Merchant) currentUser.getSession().getAttribute(LoginType.MERCHANTINFO.toString());
		String merchantId = merchantInfo.getMerchantId();
		String merchantName = merchantInfo.getMerchantName();
		return customsPortService.findMerchantCustomsPort(merchantId, merchantName);
	}

	public Map<String, Object> deleteCustomsPort(long id) {
		if (customsPortService.deleteCustomsPort(id)) {
			Map<String, Object> datasMap = customsPortService.findAllCustomsPort();
			// 将查询出来的口岸数据放入缓存中
			JedisUtil.set(SHOP_PORT_ALLCUSTOMSPORT_LIST.getBytes(),
					SerializeUtil.toBytes(datasMap.get(BaseCode.DATAS.toString())), 3600);
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("删除失败,服务器繁忙!");
	}

	public Object modifyCustomsPort(Map<String, Object> params) {
		Subject currentUser = SecurityUtils.getSubject();
		// 获取商户登录时,shiro存入在session中的数据
		Manager managerInfo = (Manager) currentUser.getSession().getAttribute(LoginType.MANAGERINFO.toString());
		String managerName = managerInfo.getManagerName();
		String managerId = managerInfo.getManagerId();
		return customsPortService.modifyCustomsPort(managerId, managerName, params);
	}
}
