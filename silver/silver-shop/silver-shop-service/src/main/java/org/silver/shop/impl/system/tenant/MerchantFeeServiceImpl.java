package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.common.base.CustomsPortService;
import org.silver.shop.api.system.tenant.MerchantFeeService;
import org.silver.shop.dao.system.tenant.MerchantFeeDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantFeeService.class)
public class MerchantFeeServiceImpl implements MerchantFeeService {

	@Autowired
	private MerchantFeeDao merchantFeeDao;
	@Autowired
	private CustomsPortService customsPortService;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private IdUtils<MerchantFeeContent> idUtils;

	@Override
	public Map<String, Object> addMerchantServiceFee(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		String merchantId = datasMap.get("merchantId") + "";
		String customsName = datasMap.get("customsName") + "";
		String customsCode = datasMap.get("customsCode") + "";
		String ciqOrgName = datasMap.get("ciqOrgName") + "";
		String ciqOrgCode = datasMap.get("ciqOrgCode") + "";
		String type = datasMap.get("type") + "";
		int customsPort = 0;
		try {
			customsPort = Integer.parseInt(datasMap.get("customsPort") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("参数格式错误！");
		}
		Map<String, Object> reCheckCustomsMap = checkCustomsInfo(customsPort, customsName, customsCode, ciqOrgCode,
				ciqOrgName);
		if (!"1".equals(reCheckCustomsMap.get(BaseCode.STATUS.toString()))) {
			return reCheckCustomsMap;
		}
		Map<String, Object> checkFeeMap = checkFee(datasMap);
		if (!"1".equals(checkFeeMap.get(BaseCode.STATUS.toString()))) {
			return checkFeeMap;
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		// 查询当前用户Id是否已添加过该口岸、海关、国检信息
		Map<String, Object> reCheckMerchantMap = checkMerchantFeeInfo(merchantId, customsName, customsCode, ciqOrgCode,
				ciqOrgName, type);
		if (!"1".equals(reCheckMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMerchantMap;
		}
		return saveMerchantFeeContent(merchant, datasMap);
	}

	/**
	 * 保存商户口岸费率信息
	 * 
	 * @param merchant
	 *            商户信息
	 * @param datasMap
	 *            口岸信息参数
	 * @return Map
	 */
	private Map<String, Object> saveMerchantFeeContent(Merchant merchant, Map<String, Object> datasMap) {
		if (merchant == null || datasMap == null) {
			return ReturnInfoUtils.errorInfo("保存参数不能为null");
		}
		String customsPortName = datasMap.get("customsPortName") + "";
		String customsName = datasMap.get("customsName") + "";
		String customsCode = datasMap.get("customsCode") + "";
		String ciqOrgName = datasMap.get("ciqOrgName") + "";
		String ciqOrgCode = datasMap.get("ciqOrgCode") + "";
		String managerName = datasMap.get("managerName") + "";
		String type = datasMap.get("type") + "";
		String status = datasMap.get("status") + "";
		int customsPort = 0;
		int backCoverFlag = 0;
		try {
			customsPort = Integer.parseInt(datasMap.get("customsPort") + "");
			backCoverFlag = Integer.parseInt(datasMap.get("backCoverFlag") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("参数格式错误！");
		}
		Map<String, Object> reIdMap = idUtils.createId(MerchantFeeContent.class, "merchantFee_");
		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		MerchantFeeContent merchantFee = new MerchantFeeContent();
		String merchantFeeId = reIdMap.get(BaseCode.DATAS.toString()) + "";
		merchantFee.setMerchantFeeId(merchantFeeId);
		merchantFee.setMerchantId(merchant.getMerchantId());
		merchantFee.setMerchantName(merchant.getMerchantName());
		merchantFee.setCustomsPort(customsPort);
		merchantFee.setCustomsPortName(customsPortName);
		merchantFee.setCustomsCode(customsCode);
		merchantFee.setCustomsName(customsName);
		merchantFee.setCiqOrgCode(ciqOrgCode);
		merchantFee.setCiqOrgName(ciqOrgName);
		double platformFee = 0;
		try {
			platformFee = Double.parseDouble(datasMap.get("platformFee") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,平台服务费率参数格式错误,请重新输入!");
		}
		merchantFee.setPlatformFee(platformFee);
		// 类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
		merchantFee.setType(type);
		merchantFee.setStatus(status);
		merchantFee.setCreateBy(managerName);
		merchantFee.setCreateDate(new Date());
		// 封底标识：1-不封底计算、2-封底计算
		merchantFee.setBackCoverFlag(backCoverFlag);
		String backCoverFee = datasMap.get("backCoverFee") + "";
		Map<String, Object> reMap = addBackCoverFee(merchantFee, backCoverFee, backCoverFlag);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		if (merchantFeeDao.add(merchantFee)) {
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙!");

	}

	/**
	 * 添加封底服务费率
	 * 
	 * @param merchantFee
	 *            商户口岸费率信息
	 * @param backCoverFee
	 *            封底服务费
	 * @param backCoverFlag
	 *            封底标识：1-不封底计算、2-封底计算
	 * @return Map
	 */
	private Map<String, Object> addBackCoverFee(MerchantFeeContent merchantFee, String backCoverFee,
			int backCoverFlag) {
		if (merchantFee == null || backCoverFlag < 0) {
			return ReturnInfoUtils.errorInfo("添加封底服务费率，参数错误！");
		}
		if (backCoverFlag == 2) {
			if (StringEmptyUtils.isEmpty(backCoverFee)) {
				return ReturnInfoUtils.errorInfo("封底手续费不能为空！");
			} else {
				try {
					merchantFee.setBackCoverFee(Double.parseDouble(backCoverFee));
				} catch (Exception e) {
					return ReturnInfoUtils.errorInfo("封底手续费错误！");
				}
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验该商户是否已添加过海关、国检信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param customsName
	 *            海关名称
	 * @param customsCode
	 *            海关代码
	 * @param ciqOrgCode
	 *            国检检疫机构代码
	 * @param ciqOrgName
	 *            国检检疫机构名称
	 * @param type
	 *            类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
	 */
	private Map<String, Object> checkMerchantFeeInfo(String merchantId, String customsName, String customsCode,
			String ciqOrgCode, String ciqOrgName, String type) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("customsName", customsName);
		params.put("customsCode", customsCode);
		params.put("ciqOrgCode", ciqOrgCode);
		params.put("ciqOrgName", ciqOrgName);
		params.put("type", type);
		List<MerchantFeeContent> reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户口岸信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("该商户已添加过该口岸信息,请勿重复添加!");
		} else {
			return ReturnInfoUtils.successInfo();
		}
	}

	/**
	 * 根据口岸编码校验海关，国检信息是否系统真实存在
	 * 
	 * @param customsPort
	 *            海关口岸编码 1-广州电子口岸 ,2-广东智检
	 * @param customsName
	 *            海关名称名称
	 * @param customsCode
	 *            海关关区代码
	 * @param ciqOrgCode
	 *            国检检疫机构代码
	 * @param ciqOrgName
	 *            国检检疫机构名称
	 * @return Map
	 */
	private Map<String, Object> checkCustomsInfo(int customsPort, String customsName, String customsCode,
			String ciqOrgCode, String ciqOrgName) {
		switch (customsPort) {
		case 1:
			if (!customsPortService.checkGAC(customsName, customsCode)) {
				return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,海关关区错误,请重新输入!");
			}
			if (!customsPortService.checkCCIQ(ciqOrgName, ciqOrgCode)) {
				return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,国检机构信息错误,请重新输入!");
			}
			break;
		case 2:
			if (!"000069".equals(ciqOrgCode) || !"南沙局本部".equals(ciqOrgName)) {
				return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,国检机构信息错误,请重新输入!");
			}
			break;
		default:
			return ReturnInfoUtils.errorInfo("添加商户口岸费率失败,海关或国检信息错误,请重新输入!");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验商户口岸费率
	 * 
	 * @param datasMap
	 *            校验参数
	 * @return Map
	 */
	private Map<String, Object> checkFee(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("参数不能为空！");
		}
		// 类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
		String type = datasMap.get("type") + "";
		if (StringEmptyUtils.isEmpty(datasMap.get("platformFee"))) {
			return ReturnInfoUtils.errorInfo("服务费率不能为空,请重新输入!");
		}
		double platformFee;
		try {
			platformFee = Double.parseDouble(datasMap.get("platformFee") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("服务费参数格式错误,请重新输入!");
		}
		if ("goodsRecord".equals(type) && platformFee < 0.0001) {
			return ReturnInfoUtils.errorInfo("商品备案费率不能低于万一,请重新输入!");
		}
		if ("orderRecord".equals(type) && platformFee < 0.001) {
			return ReturnInfoUtils.errorInfo("订单申报费率不能低于千一,请重新输入!");
		}
		if ("paymentRecord".equals(type) && platformFee < 0.002) {
			return ReturnInfoUtils.errorInfo("支付单申报费率不能低于千二,请重新输入!");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
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
		// 状态：1-启用、2-禁用
		params.put("status", "1");
		// 删除标识:0-未删除,1-已删除
		params.put("deleteFlag", 0);
		List<MerchantFeeContent> reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
		long count = merchantFeeDao.findByPropertyCount(MerchantFeeContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询已开通海关口岸信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList,count);
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
		int customsPort = 0;
		int backCoverFlag = 0;
		double platformFee = 0;
		try {
			customsPort = Integer.parseInt(datasMap.get("customsPort") + "");
			backCoverFlag = Integer.parseInt(datasMap.get("backCoverFlag") + "");
			platformFee = Double.parseDouble(datasMap.get("platformFee") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("请求参数格式错误,请重新输入!");
		}
		String customsPortName = datasMap.get("customsPortName") + "";
		String customsName = datasMap.get("customsName") + "";
		String customsCode = datasMap.get("customsCode") + "";
		String ciqOrgName = datasMap.get("ciqOrgName") + "";
		String ciqOrgCode = datasMap.get("ciqOrgCode") + "";
		String managerName = datasMap.get("managerName") + "";
		String type = datasMap.get("type") + "";
		String status = datasMap.get("status") + "";
		Map<String, Object> checkFeeMap = checkFee(datasMap);
		if (!"1".equals(checkFeeMap.get(BaseCode.STATUS.toString()))) {
			return checkFeeMap;
		}
		Map<String, Object> reMerchantFeeMap = getMerchantFeeInfo(merchantFeeId);
		if (!"1".equals(reMerchantFeeMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantFeeMap;
		}
		MerchantFeeContent merchantFee = (MerchantFeeContent) reMerchantFeeMap.get(BaseCode.DATAS.toString());
		merchantFee.setCustomsPort(customsPort);
		merchantFee.setCustomsPortName(customsPortName);
		merchantFee.setCustomsCode(customsCode);
		merchantFee.setCustomsName(customsName);
		merchantFee.setCiqOrgCode(ciqOrgCode);
		merchantFee.setCiqOrgName(ciqOrgName);
		merchantFee.setPlatformFee(platformFee);
		// 类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
		merchantFee.setType(type);
		//
		merchantFee.setStatus(status);
		merchantFee.setUpdateBy(managerName);
		merchantFee.setUpdateDate(new Date());
		// 封底标识：1-不封底计算、2-封底计算
		merchantFee.setBackCoverFlag(backCoverFlag);
		String backCoverFee = datasMap.get("backCoverFee") + "";
		Map<String, Object> reMap = addBackCoverFee(merchantFee, backCoverFee, backCoverFlag);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		if (merchantFeeDao.update(merchantFee)) {
			if(backCoverFlag == 2){
				// 同步支付单或者订单口岸的封底手续费
				Map<String, Object> reSynMap = synUpdateAnotherOne(merchantFee.getMerchantId(), type, customsCode, ciqOrgCode,
						backCoverFee);
				if (!"1".equals(reSynMap.get(BaseCode.STATUS.toString()))) {
					return reSynMap;
				}
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("修改失败,服务器繁忙!");
	}

	/**
	 * 
	 * @param type
	 *            类型：goodsRecord-商品备案、orderRecord-订单申报、paymentRecord-支付单申报
	 * @param customsCode
	 *            海关代码
	 * @param ciqOrgCode
	 *            国检检疫机构代码
	 * @param merchantId
	 *            商户id
	 * @param d
	 * @return Map
	 */
	private Map<String, Object> synUpdateAnotherOne(String merchantId, String type, String customsCode,
			String ciqOrgCode, String backCoverFee) {
		Map<String, Object> params = new HashMap<>();
		params.put("merchantId", merchantId);
		params.put("customsCode", customsCode);
		params.put("ciqOrgCode", ciqOrgCode);
		List<MerchantFeeContent> reList = null;
		switch (type) {
		case "orderRecord":
			// 当修改类型为订单时，则同步修改的是支付单申报的口岸费率信息
			params.put("type", "paymentRecord");
			reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
			break;
		case "paymentRecord":
			// 当修改类型为支付单时，则同步修改的是订单申报的口岸费率信息
			params.put("type", "orderRecord");
			reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
			break;
		default:
			return ReturnInfoUtils.errorInfo("同步更新时，错误类型！");
		}
		if (reList != null && !reList.isEmpty()) {
			MerchantFeeContent content = reList.get(0);
			content.setBackCoverFee(Double.parseDouble(backCoverFee));
			if (merchantFeeDao.update(content)) {
				return ReturnInfoUtils.successInfo();
			}
		}else{
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("同步更新失败！");
	}

	@Override
	public Map<String, Object> getServiceFee(Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalMerchantFeeSearch(datasMap);
		if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
			return reDatasMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		paramMap.put("deleteFlag", 0);
		List<MerchantFeeContent> reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, paramMap, page, size);
		long count = merchantFeeDao.findByPropertyCount(MerchantFeeContent.class, paramMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户费用信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据");
		}
	}

	@Override
	public Map<String, Object> getCustomsFee(String merchantId) {
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();

		params.put("merchantId", merchantId);
		// 状态：1-启用、2-禁用
		params.put("status", "1");
		params.put("deleteFlag", 0);
		List<MerchantFeeContent> reList = merchantFeeDao.findByProperty(MerchantFeeContent.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			Map<String, Object> cacheMap = new HashMap<>();
			for (MerchantFeeContent feeContent : reList) {
				String customsCode = feeContent.getCustomsCode();
				String ciqOrgCode = feeContent.getCiqOrgCode();
				if (cacheMap.containsKey(customsCode + "_" + ciqOrgCode)) {
					double fee = feeContent.getPlatformFee();
					MerchantFeeContent reFeeContent = (MerchantFeeContent) cacheMap.get(customsCode + "_" + ciqOrgCode);
					feeContent.setPlatformFee(fee + reFeeContent.getPlatformFee());
					cacheMap.put(customsCode + "_" + ciqOrgCode, feeContent);
				} else {
					cacheMap.put(customsCode + "_" + ciqOrgCode, feeContent);
				}
			}
			return ReturnInfoUtils.successDataInfo(cacheMap);
		} else {
			return ReturnInfoUtils.errorInfo("没有已开通海关口岸信息,请联系管理员!");
		}
	}

}
