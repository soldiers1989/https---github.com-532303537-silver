package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.MerchantBusinessService;
import org.silver.shop.dao.system.tenant.MerchantBusinessDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantBusinessContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantBusinessService.class)
public class MerchantBusinessServiceImpl implements MerchantBusinessService {

	@Autowired
	private MerchantBusinessDao merchantBusinessDao;
	@Autowired
	private MerchantUtils merchantUtils;

	@Override
	public Map<String, Object> addInfo(String managerName, Map<String, Object> datasMap) {
		if (StringEmptyUtils.isEmpty(managerName) || datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		String merchantId = datasMap.get("merchantId") + "";
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<MerchantBusinessContent> reList = merchantBusinessDao.findByProperty(MerchantBusinessContent.class, params,
				0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("该商户已有业务信息,请勿重复添加!");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		////第三方标识：1-银盟(银盟商城平台),2-第三方商城平台
		if(merchant.getThirdPartyFlag() != 2){
			return ReturnInfoUtils.errorInfo("该商户不属于第三方电商，不需要添加业务信息！");
		}
		MerchantBusinessContent entity = new MerchantBusinessContent();
		entity.setMerchantId(merchantId);
		entity.setMerchantName(merchant.getMerchantName());
		String businessType = datasMap.get("businessType") + "";
		if ("all".equals(businessType) || "online".equals(businessType) || "offline".equals(businessType)) {
			entity.setBusinessType(businessType);
		} else {
			return ReturnInfoUtils.errorInfo("业务类型错误！");
		}
		String pushType = datasMap.get("pushType") + "";
		if ("all".equals(pushType) || "orderRecord".equals(pushType) || "paymentRecord".equals(pushType)) {
			entity.setPushType(pushType);
		} else {
			return ReturnInfoUtils.errorInfo("推送类型错误！");
		}
		String idCardVerifySwitch = datasMap.get("idCardVerifySwitch") + "";
		if ("on".equals(idCardVerifySwitch) || "off".equals(idCardVerifySwitch)) {
			entity.setIdCardVerifySwitch(idCardVerifySwitch);
		} else {
			return ReturnInfoUtils.errorInfo("身份证认证参数错误！");
		}
		entity.setCreateBy(managerName);
		entity.setCreateDate(new Date());
		if (!merchantBusinessDao.add(entity)) {
			return ReturnInfoUtils.errorInfo("保存失败，服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> getInfo(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("搜索参数不能为null");
		}
		String merchantId = datasMap.get("merchantId") + "";
		Map<String, Object> params = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(merchantId)) {
			params.put("merchantId", merchantId);
		}
		List<MerchantBusinessContent> reList = merchantBusinessDao.findByProperty(MerchantBusinessContent.class, params,
				0, 0);
		long count = merchantBusinessDao.findByPropertyCount(MerchantBusinessContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count);
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}

}
