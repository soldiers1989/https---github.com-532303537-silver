package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.MerchantIdCardCostService;
import org.silver.shop.dao.system.tenant.MerchantIdCardCostDao;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.model.system.tenant.MerchantIdCardCostContent;
import org.silver.shop.util.IdUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantIdCardCostService.class)
public class MerchantIdCardCostServiceImpl implements MerchantIdCardCostService{

	@Autowired
	private MerchantIdCardCostDao merchantIdCardCostDao; 
	@Autowired
	private IdUtils idUtils;
	
	@Override
	public Map<String, Object> getIdCardCostInfo(String merchantId) {
		if(StringEmptyUtils.isEmpty(merchantId)){
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<MerchantIdCardCostContent> merchantList = merchantIdCardCostDao.findByProperty(MerchantIdCardCostContent.class, params, 1, 1);
		if (merchantList == null) {
			return ReturnInfoUtils.errorInfo("查询商户实名认证费率信息失败,服务器繁忙!");
		} else if (!merchantList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(merchantList.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("未找到商户实名认证费率信息,请联系管理员!");
		}
	}

	@Override
	public Map<String, Object> getInfo(String merchantId) {
		Map<String, Object> params = new HashMap<>();
		if(StringEmptyUtils.isNotEmpty(merchantId)){
			params.put("merchantId", merchantId);
		}
		List<MerchantIdCardCostContent> merchantList = merchantIdCardCostDao.findByProperty(MerchantIdCardCostContent.class, params,0, 01);
		if (merchantList == null) {
			return ReturnInfoUtils.errorInfo("查询商户实名认证,服务器繁忙!");
		} else if (!merchantList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(merchantList.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Map<String,Object> addInfo(Map<String, Object> datasMap) {
		if(datasMap == null || datasMap.isEmpty()){
			return ReturnInfoUtils.errorInfo("添加信息参数不能为空!");
		}
		MerchantIdCardCostContent idcardCost = new MerchantIdCardCostContent();
		Map<String, Object> reIdMap = idUtils.createId(MerchantFeeContent.class, "idCardCost_");
		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		String idCardCostNo = reIdMap.get(BaseCode.DATAS.toString()) + "";
		idcardCost.setIdCardCostNo(idCardCostNo);
		idcardCost.setMerchantId(datasMap.get("merchantId")+"");
		idcardCost.setMerchantName(datasMap.get("merchantName")+"");
		double fee = 0;
		try{
			fee = Double.parseDouble(datasMap.get("platformCost")+"");
		}catch (Exception e) {
			e.printStackTrace();
			return ReturnInfoUtils.errorInfo("费用格式错误,请重新输入!");
		}
		idcardCost.setPlatformCost(fee);
		idcardCost.setIdCardVerifySwitch(datasMap.get("idCardVerifySwitch")+"");
		idcardCost.setDeleteFlag(0);
		idcardCost.setCreateBy(datasMap.get("managerName")+"");
		idcardCost.setCreateDate(new Date());
		if(!merchantIdCardCostDao.add(idcardCost)){
			return ReturnInfoUtils.errorInfo("添加商户实名认证费率信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}
	
}
