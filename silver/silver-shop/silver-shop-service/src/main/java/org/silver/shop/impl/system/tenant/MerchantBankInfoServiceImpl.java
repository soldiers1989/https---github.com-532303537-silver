package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.loader.custom.Return;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.MerchantBankInfoService;
import org.silver.shop.dao.system.tenant.MerchantBankInfoDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantBankContent;
import org.silver.shop.model.system.tenant.MerchantFeeContent;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.MerchantUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MerchantBankInfoService.class)
public class MerchantBankInfoServiceImpl implements MerchantBankInfoService {

	@Autowired
	private MerchantBankInfoDao merchantBankInfoDao;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private IdUtils idUtils;

	@Override
	public Map<String, Object> getMerchantBankInfo(String merchantId, int page, int size, int defaultFlag) {
		if (StringEmptyUtils.isEmpty(merchantId)) {
			return ReturnInfoUtils.errorInfo("商户id不能为空!");
		}
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Map<String, Object> pamras = new HashMap<>();
		// 选中标识：1-默认选中,2-备用
		if (defaultFlag > 0) {
			pamras.put("defaultFlag", defaultFlag);
		}
		pamras.put("merchantId", merchantId);
		List<MerchantBankContent> reList = merchantBankInfoDao.findByProperty(MerchantBankContent.class, pamras, page,
				size);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询商户银行卡信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList);
		} else {
			return ReturnInfoUtils.errorInfo("未找到商户银行卡信息!");
		}
	}

	@Override
	public boolean selectMerchantBank(long id, String merchantId) {
		return false;
	}

	@Override
	public boolean deleteMerchantBankInfo(long id, String merchantId) {

		return false;
	}

	@Override
	public Map<String, Object> managerAddBankInfo(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null！");
		}
		String merchantId = datasMap.get("merchantId") + "";
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			return reMerchantMap;
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		datasMap.put("merchantName", merchant.getMerchantName());
		return addMerchantBank(datasMap, datasMap.get("managerName") + "");
	}

	/**
	 * 添加商户银行卡
	 * 
	 * @param datasMap
	 *            银行卡信息
	 * @param createBy
	 *            添加人
	 * @return Map
	 */
	private Map<String, Object> addMerchantBank(Map<String, Object> datasMap, String createBy) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null！");
		}
		Map<String, Object> reCheckMap = checkBankInfo(datasMap);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()) + "")) {
			return reCheckMap;
		}
		Map<String, Object> reIdMap = idUtils.createId(MerchantBankContent.class, "merchantBankId_");
		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		String merchantBankId = reIdMap.get(BaseCode.DATAS.toString()) + "";
		MerchantBankContent entity = new MerchantBankContent();
		entity.setMerchantBankId(merchantBankId);
		entity.setMerchantId(datasMap.get("merchantId") + "");
		entity.setMerchantName(datasMap.get("merchantName") + "");
		entity.setBankProvince(datasMap.get("bankProvince") + "");
		entity.setBankCity(datasMap.get("bankCity") + "");
		entity.setBankName(datasMap.get("bankName") + "");
		entity.setBankAccountNo(datasMap.get("bankAccountNo") + "");
		entity.setBankAccountName(datasMap.get("bankAccountName") + "");
		// 银行卡账户类型 私人(personal) 对公(corporate)
		entity.setBankAccountType(datasMap.get("bankAccountType") + "");
		// 银行卡类别 借记卡(debit) 信用卡(credit) 单位结算卡(unit)
		String bankCardType = datasMap.get("bankCardType") + "";
		if ("debit".equals(bankCardType) || "credit".equals(bankCardType) || "unit".equals(bankCardType)) {
			entity.setBankCardType(bankCardType);
		} else {
			return ReturnInfoUtils.errorInfo("银行卡类别错误，请重新输入！");
		}
		int defaultFlag = 0;
		try {
			defaultFlag = Integer.parseInt(datasMap.get("defaultFlag") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("选中标识错误！");
		}
		entity.setDefaultFlag(defaultFlag);
		entity.setCreateBy(createBy);
		entity.setCreateDate(new Date());
		if (!merchantBankInfoDao.add(entity)) {
			return ReturnInfoUtils.errorInfo("添加失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 校验银行卡信息是否正确
	 * 
	 * @param datasMap
	 * @return
	 */
	private Map<String, Object> checkBankInfo(Map<String, Object> datasMap) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("检查银行卡参数不能为null！");
		}
		JSONArray jsonArr = new JSONArray();
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("merchantId");
		noNullKeys.add("bankProvince");
		noNullKeys.add("bankCity");
		noNullKeys.add("bankName");
		noNullKeys.add("bankAccountNo");
		noNullKeys.add("bankAccountName");
		noNullKeys.add("bankAccountType");
		noNullKeys.add("bankCardType");
		noNullKeys.add("defaultFlag");
		jsonArr.add(datasMap);
		return CheckDatasUtil.checkData(jsonArr, noNullKeys);
	}

}
