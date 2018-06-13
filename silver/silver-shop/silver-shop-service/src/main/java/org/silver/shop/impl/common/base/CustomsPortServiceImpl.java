package org.silver.shop.impl.common.base;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.dao.common.base.CustomsPortDao;
import org.silver.shop.model.common.base.CCIQ;
import org.silver.shop.model.common.base.City;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.common.base.GAC;
import org.silver.shop.model.common.base.Province;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = CustomsPortService.class)
public class CustomsPortServiceImpl implements CustomsPortService {

	@Autowired
	private CustomsPortDao customsPortDao;

	@Override
	public Map<String, Object> addCustomsPort(Map<String, Object> params, String managerId, String managerName) {
		Date date = new Date();

		String provinceName = params.get("provinceName") + "";
		String provinceCode = params.get("provinceCode") + "";
		String cityName = params.get("cityName") + "";
		String cityCode = params.get("cityCode") + "";
		int customsPort = Integer.parseInt(params.get("customsPort") + "");
		String customsPortName = params.get("customsPortName") + "";
		String customsName = params.get("customsName") + "";
		String customsCode = params.get("customsCode") + "";
		String ciqOrgName = params.get("ciqOrgName") + "";
		String ciqOrgCode = params.get("ciqOrgCode") + "";
		if (!checkProvince(provinceName, provinceCode)) {
			return ReturnInfoUtils.errorInfo("添加口岸失败,省份信息错误,请重新输入!");
		}
		if (!checkCity(cityName, cityCode)) {
			return ReturnInfoUtils.errorInfo("添加口岸失败,城市信息错误,请重新输入!");
		}
		if (!checkGAC(customsName, customsCode)) {
			return ReturnInfoUtils.errorInfo("添加口岸失败,海关关区错误,请重新输入!");
		}
		if (!checkCCIQ(ciqOrgName, ciqOrgCode)) {
			return ReturnInfoUtils.errorInfo("添加口岸失败,国检机构信息错误,请重新输入!");
		}
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
		if (customsPortDao.add(customsInfo)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("添加口岸失败,服务器繁忙!");
	}

	
	@Override
	public boolean checkCCIQ(String ciqOrgName, String ciqOrgCode) {
		if (StringEmptyUtils.isNotEmpty(ciqOrgName) && StringEmptyUtils.isNotEmpty(ciqOrgCode)) {
			Map<String, Object> params = new HashMap<>();
			params.put("CCIQName", ciqOrgName);
			params.put("code", ciqOrgCode);
			List<CCIQ> cciqList = customsPortDao.findByProperty(CCIQ.class, params, 1, 1);
			if (cciqList != null && !cciqList.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public boolean checkCCIQ( String ciqOrgCode) {
		if ( StringEmptyUtils.isNotEmpty(ciqOrgCode)) {
			Map<String, Object> params = new HashMap<>();
			params.put("code", ciqOrgCode);
			List<CCIQ> cciqList = customsPortDao.findByProperty(CCIQ.class, params, 1, 1);
			if (cciqList != null && !cciqList.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean checkGAC(String customsName, String customsCode) {
		if (StringEmptyUtils.isNotEmpty(customsName) && StringEmptyUtils.isNotEmpty(customsCode)) {
			Map<String, Object> params = new HashMap<>();
			params.put("GACName", customsName);
			params.put("code", customsCode);
			List<GAC> gacList = customsPortDao.findByProperty(GAC.class, params, 1, 1);
			if (gacList != null && !gacList.isEmpty()) {
				return true;
			}
		}
		return false;
	}

@Override
	public boolean checkGAC(  String customsCode) {
		if ( StringEmptyUtils.isNotEmpty(customsCode)) {
			Map<String, Object> params = new HashMap<>();
			params.put("code", customsCode);
			List<GAC> gacList = customsPortDao.findByProperty(GAC.class, params, 1, 1);
			if (gacList != null && !gacList.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 校验城市名称与编码是否真实存在
	 * 
	 * @param cityName
	 *            城市名称
	 * @param cityCode
	 *            城市编码
	 * @return boolean
	 */
	public boolean checkCity(String cityName, String cityCode) {
		if (StringEmptyUtils.isNotEmpty(cityName) && StringEmptyUtils.isNotEmpty(cityCode)) {
			Map<String, Object> params = new HashMap<>();
			params.put("cityName", cityName);
			params.put("cityCode", cityCode);
			List<City> cityList = customsPortDao.findByProperty(City.class, params, 1, 1);
			if (cityList != null && !cityList.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 校验省份名称与编码是否真实存在
	 * 
	 * @param provinceName
	 *            省份名称
	 * @param provinceCode
	 *            省份编码
	 * @return boolean
	 */
	public boolean checkProvince(String provinceName, String provinceCode) {
		if (StringEmptyUtils.isNotEmpty(provinceName) && StringEmptyUtils.isNotEmpty(provinceCode)) {
			Map<String, Object> params = new HashMap<>();
			params.put("provinceName", provinceName);
			params.put("provinceCode", provinceCode);
			List<Province> provinceList = customsPortDao.findByProperty(Province.class, params, 1, 1);
			if (provinceList != null && !provinceList.isEmpty()) {
				return true;
			}
		}
		return false;
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
			if (reList != null && reList.isEmpty()) {
				allCustomsList.remove(i);
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.DATAS.toString(), allCustomsList);
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public boolean deleteCustomsPort(long id) {
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		List<CustomsPort> dataList = customsPortDao.findByProperty(CustomsPort.class, params, 1, 1);
		if (dataList != null && !dataList.isEmpty()) {
			CustomsPort customs = dataList.get(0);
			return customsPortDao.delete(customs);
		}
		return false;
	}

	@Override
	public Map<String, Object> modifyCustomsPort(String managerId, String managerName, Map<String, Object> params) {
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("id", Long.parseLong(params.get("id") + ""));
		List<CustomsPort> reList = customsPortDao.findByProperty(CustomsPort.class, dataMap, 1, 1);
		if (reList != null && !reList.isEmpty()) {
			CustomsPort customs = reList.get(0);
			customs.setProvince(params.get("province") + "");
			customs.setProvinceCode(params.get("provinceCode") + "");
			customs.setCity(params.get("city") + "");
			customs.setCityCode(params.get("cityCode") + "");
			customs.setCiqOrgName(params.get("ciqOrgName") + "");
			customs.setCustomsCode(params.get("customsCode") + "");
			customs.setCustomsName(params.get("customsName") + "");
			customs.setCustomsPort(Integer.parseInt(params.get("customsPort") + ""));
			customs.setCustomsPortName(params.get("customsPortName") + "");
			if (customsPortDao.update(customs)) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("修改已开通口岸失败,服务器繁忙!");
		}
		return ReturnInfoUtils.errorInfo("查询已开通口岸失败,服务器繁忙!");
	}
}
