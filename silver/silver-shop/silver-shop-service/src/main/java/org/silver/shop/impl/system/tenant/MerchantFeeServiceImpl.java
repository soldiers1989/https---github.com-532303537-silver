package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.MerchantFeeService;
import org.silver.shop.dao.system.tenant.MerchantFeeDao;
import org.silver.shop.impl.common.base.CustomsPortServiceImpl;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.util.MerchantUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantFeeService.class)
public class MerchantFeeServiceImpl implements MerchantFeeService {

	@Autowired
	private MerchantFeeDao merchantFeeDao;
	@Autowired
	private CustomsPortServiceImpl customsPortServiceImpl;
	@Autowired
	private MerchantUtils merchantUtils;

	@Override
	public Map<String, Object> addMerchantServiceFee(Map<String, Object> datasMap) {
		if (datasMap != null) {
			String merchantId = datasMap.get("merchantId") + "";
			String provinceName = datasMap.get("provinceName") + "";
			String provinceCode = datasMap.get("provinceCode") + "";
			String cityName = datasMap.get("cityName") + "";
			String cityCode = datasMap.get("cityCode") + "";
			int customsPort = Integer.parseInt(datasMap.get("customsPort") + "");
			String customsPortName = datasMap.get("customsPortName") + "";
			String customsName = datasMap.get("customsName") + "";
			String customsCode = datasMap.get("customsCode") + "";
			String ciqOrgName = datasMap.get("ciqOrgName") + "";
			String ciqOrgCode = datasMap.get("ciqOrgCode") + "";
			String managerName = datasMap.get("managerName") + "";
			double platformFee = Double.parseDouble(datasMap.get("platformFee") + "");
			String type = datasMap.get("type") + "";
			String status = datasMap.get("status") + "";
			if (!customsPortServiceImpl.checkProvince(provinceName, provinceCode)) {
				return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,省份信息错误,请重新输入!");
			}
			if (!customsPortServiceImpl.checkCity(cityName, cityCode)) {
				return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,城市信息错误,请重新输入!");
			}
			// if (!customsPortServiceImpl.checkGAC(customsName, customsCode)) {
			// return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,海关关区错误,请重新输入!");
			// }
			// if (!customsPortServiceImpl.checkCCIQ(ciqOrgName, ciqOrgCode)) {
			// return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,国检机构信息错误,请重新输入!");
			// }
			Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
			if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
				return reMerchantMap;
			}
			Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
			long id = merchantFeeDao.findLastId(MerchantFeeContent.class);
			if (id < 0) {
				return ReturnInfoUtils.errorInfo("查询流水Id失败,服务器繁忙!");
			}
			String merchantFeeId = SerialNoUtils.getNotRandomSerialNo("merchantFee_", id);
			MerchantFeeContent merchantFee = new MerchantFeeContent();
			merchantFee.setMerchantFeeId(merchantFeeId);
			merchantFee.setMerchantId(merchant.getMerchantId());
			merchantFee.setMerchantName(merchant.getMerchantName());
			merchantFee.setProvinceCode(provinceCode);
			merchantFee.setProvinceName(provinceName);
			merchantFee.setCityCode(cityCode);
			merchantFee.setCityName(cityName);
			merchantFee.setCustomsPort(customsPort);
			merchantFee.setCustomsPortName(customsPortName);
			merchantFee.setCustomsCode(customsCode);
			merchantFee.setCustomsName(customsName);
			merchantFee.setCiqOrgCode(ciqOrgCode);
			merchantFee.setCiqOrgName(ciqOrgName);
			merchantFee.setPlatformFee(platformFee);
			merchantFee.setType(type);
			merchantFee.setStatus(status);
			merchantFee.setCreateBy(managerName);
			merchantFee.setCreateDate(new Date());
			if (merchantFeeDao.add(merchantFee)) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	/**
	 * 根据商户费用流水Id查询商户平台费用信息
	 * 
	 * @param merchantFeeId
	 *            流水Id
	 * @return Map
	 */
	private Map<String, Object> getMerchantFeeInfo(String merchantFeeId) {
		if (StringEmptyUtils.isEmpty(merchantFeeId)) {
			return ReturnInfoUtils.errorInfo("Id不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("merchantFeeId", merchantFeeId);
		List<MerchantFeeContent> reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户费用信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("未找到商户费用信息!");
		}
	}

	@Override
	public Map<String, Object> getMerchantServiceFee(String merchantId) {
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		List<MerchantFeeContent> reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户费用信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList);
		} else {
			return ReturnInfoUtils.errorInfo("未找到商户费用信息!");
		}
	}

	@Override
	public Map<String, Object> editMerchantServiceFee(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数错误!");
		}
		String merchantFeeId = datasMap.get("merchantFeeId") + "";
		Map<String,Object> reMerchantFeeMap = getMerchantFeeInfo(merchantFeeId);
		if(!"1".equals(reMerchantFeeMap.get(BaseCode.STATUS.toString()))){
			return reMerchantFeeMap;
		}
		String provinceName = datasMap.get("provinceName") + "";
		String provinceCode = datasMap.get("provinceCode") + "";
		String cityName = datasMap.get("cityName") + "";
		String cityCode = datasMap.get("cityCode") + "";
		int customsPort = Integer.parseInt(datasMap.get("customsPort") + "");
		String customsPortName = datasMap.get("customsPortName") + "";
		String customsName = datasMap.get("customsName") + "";
		String customsCode = datasMap.get("customsCode") + "";
		String ciqOrgName = datasMap.get("ciqOrgName") + "";
		String ciqOrgCode = datasMap.get("ciqOrgCode") + "";
		String managerName = datasMap.get("managerName") + "";
		double platformFee = Double.parseDouble(datasMap.get("platformFee") + "");
		String type = datasMap.get("type") + "";
		String status = datasMap.get("status") + "";
		if (!customsPortServiceImpl.checkProvince(provinceName, provinceCode)) {
			return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,省份信息错误,请重新输入!");
		}
		if (!customsPortServiceImpl.checkCity(cityName, cityCode)) {
			return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,城市信息错误,请重新输入!");
		}
		MerchantFeeContent merchantFee = (MerchantFeeContent) reMerchantFeeMap.get(BaseCode.DATAS.toString());
		merchantFee.setMerchantFeeId(merchantFeeId);
		merchantFee.setProvinceCode(provinceCode);
		merchantFee.setProvinceName(provinceName);
		merchantFee.setCityCode(cityCode);
		merchantFee.setCityName(cityName);
		merchantFee.setCustomsPort(customsPort);
		merchantFee.setCustomsPortName(customsPortName);
		merchantFee.setCustomsCode(customsCode);
		merchantFee.setCustomsName(customsName);
		merchantFee.setCiqOrgCode(ciqOrgCode);
		merchantFee.setCiqOrgName(ciqOrgName);
		merchantFee.setPlatformFee(platformFee);
		merchantFee.setType(type);
		merchantFee.setStatus(status);
		merchantFee.setUpdateBy(managerName);
		merchantFee.setUpdateDate(new Date());
		if (merchantFeeDao.update(merchantFee)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("修改失败,服务器繁忙!");
	}

	@Override
	public Map<String, Object> getServiceFee(String merchantId, String type) {
		if(StringEmptyUtils.isEmpty(merchantId)|| StringEmptyUtils.isEmpty(type)){
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String,Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("type", type);
		params.put("deleteFlag", 0);
		List<MerchantFeeContent> reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户费用信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList);
		} else {
			return ReturnInfoUtils.errorInfo("商户没有开通口岸信息,请联系管理员!");
		}
	}

}
