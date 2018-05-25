package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.MerchantFeeService;
import org.silver.shop.dao.system.tenant.MerchantFeeDao;
import org.silver.shop.impl.common.base.CustomsPortServiceImpl;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.util.IdUtils;
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
	@Autowired
	private IdUtils<MerchantFeeContent> idUtils;

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
			checkFee(datasMap);
			Map<String, Object> reIdMap = idUtils.createId(MerchantFeeContent.class, "merchantFee_");
			if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
				return reIdMap;
			}
			MerchantFeeContent merchantFee = new MerchantFeeContent();
			String merchantFeeId = reIdMap.get(BaseCode.DATAS.toString()) + "";
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
			merchantFee.setPlatformFee(Double.parseDouble(datasMap.get("platformFee") + ""));
			// 类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
			merchantFee.setType(type);
			merchantFee.setStatus(status);
			merchantFee.setCreateBy(managerName);
			merchantFee.setCreateDate(new Date());
			if (merchantFeeDao.add(merchantFee)) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙!");
		}
		return ReturnInfoUtils.errorInfo("请求参数不能为空!");
	}

	/**
	 * 校验商户口岸费率
	 * @param datasMap 校验参数
	 * @return Map
	 */
	private Map<String, Object> checkFee(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("参数不能为空！");
		}
		//类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
		String type = datasMap.get("type") + "";
		if (StringEmptyUtils.isEmpty(datasMap.get("platformFee"))) {
			return ReturnInfoUtils.errorInfo("服务费率不能为空,请重新输入!");
		}
		double platformFee;
		try{
			 platformFee = Double.parseDouble(datasMap.get("platformFee") + "");
		}catch (Exception e) {
			return ReturnInfoUtils.errorInfo("服务费参数格式错误,请重新输入!");
		}
		if ("goodsRecord".equals(type) && platformFee < 0.0001) {
			return ReturnInfoUtils.errorInfo("商品备案费率不能低于万一,请重新输入!");
		}
		if ("orderRecord".equals(type) && platformFee < 0.0001) {
			return ReturnInfoUtils.errorInfo("订单申报费率不能低于万一,请重新输入!");
		}
		if ("paymentRecord".equals(type) && platformFee < 0.002) {
			return ReturnInfoUtils.errorInfo("订单申报费率不能低于千二,请重新输入!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据流水Id查询商户口岸费率信息
	 * 
	 * @param merchantFeeId
	 *            流水Id
	 * @return Map
	 */
	public Map<String, Object> getMerchantFeeInfo(String merchantFeeId) {
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
	public Map<String, Object> getMerchantServiceFee(String merchantId, String type) {
		if (StringEmptyUtils.isEmpty(merchantId) || StringEmptyUtils.isEmpty(type)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("type", type);
		params.put("deleteFlag", 0);
		List<MerchantFeeContent> reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询已开通海关口岸信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList);
		} else {
			return ReturnInfoUtils.errorInfo("没有已开通海关口岸信息,请联系管理员!");
		}
	}

	@Override
	public Map<String, Object> editMerchantServiceFee(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数错误!");
		}
		String merchantFeeId = datasMap.get("merchantFeeId") + "";
		Map<String, Object> reMerchantFeeMap = getMerchantFeeInfo(merchantFeeId);
		if (!"1".equals(reMerchantFeeMap.get(BaseCode.STATUS.toString()))) {
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
	public Map<String, Object> getServiceFee(String merchantId) {
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

}
