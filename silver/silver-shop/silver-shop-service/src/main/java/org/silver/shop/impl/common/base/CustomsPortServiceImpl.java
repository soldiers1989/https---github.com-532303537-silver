package org.silver.shop.impl.common.base;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.dao.common.base.CustomsPortDao;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = CustomsPortService.class)
public class CustomsPortServiceImpl implements CustomsPortService {

	@Autowired
	private CustomsPortDao customsPortDao;

	@Override
	public boolean addCustomsPort(String provinceName, String provinceCode, String cityName,
			String cityCode, int customsPort, String customsPortName, String customsCode, String customsName,
			String ciqOrgCode, String ciqOrgName,String managerId, String managerName) {
		Date date = new Date();
		CustomsPort customsInfo = new CustomsPort();
		customsInfo.setProvince(provinceName);
		customsInfo.setProvinceCode(provinceCode);
		customsInfo.setCity(cityName);
		customsInfo.setCityCode(cityCode);
		customsInfo.setCustomsPort(customsPort);
		customsInfo.setCustomsPortName(customsPortName);
		customsInfo.setCustomsName(customsName);
		customsInfo.setCustomsCode(customsCode);
		customsInfo.setCiqOrgCode(ciqOrgCode);
		customsInfo.setCiqOrgName(ciqOrgName);
		customsInfo.setCreateDate(date);
		customsInfo.setDeleteFlag(0);
		customsInfo.setCreateBy(managerName);
		return customsPortDao.add(customsInfo);
	}

	@Override
	public Map<String, Object> findAllCustomsPort() {
		Map<String, Object> reMap = new HashMap<>();
		List<Object> dataList = customsPortDao.findAll(CustomsPort.class, 0, 0);
		if (dataList != null && dataList.size() > 0) {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reMap.put(BaseCode.DATAS.toString(), dataList);
			reMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			reMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return reMap;
	}

	@Override
	public Map<String, Object> findMerchantCustomsPort(String merchantId, String merchantName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		Map<String, Object> reMap = findAllCustomsPort();
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			reMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return reMap;
		}
		List<Object> allCustomsList = (List) reMap.get(BaseCode.DATAS.toString());
		for (int i = 0; i < allCustomsList.size(); i++) {
			CustomsPort customsPort = (CustomsPort) allCustomsList.get(i);
			paramMap.put("customsPort", customsPort.getCustomsPort());
			paramMap.put("merchantId", merchantId);
			List<Object> reList = customsPortDao.findByProperty(MerchantRecordInfo.class, paramMap, 0, 0);
			if(reList !=null  && reList.isEmpty()){
				allCustomsList.remove(i);
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.DATAS.toString(), allCustomsList);
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

}
