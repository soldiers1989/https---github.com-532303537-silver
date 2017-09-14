package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.system.tenant.MerchantBankInfoService;
import org.silver.shop.dao.system.tenant.MerchantBankInfoDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantBankInfo;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantBankInfoService.class)
public class MerchantBankInfoServiceImpl implements MerchantBankInfoService {

	@Autowired
	private MerchantBankInfoDao merchantBankInfoDao;

	@Override
	public List<Object> findMerchantBankInfo(Map dataMap, int page, int size) {
		return merchantBankInfoDao.findByProperty(MerchantBankInfo.class, dataMap, page, size);
	}

	@Override
	public boolean addMerchantBankInfo(Object entity, String bankName, String bankAccount, int defaultFalg) {
		MerchantBankInfo merchantBankInfo = new MerchantBankInfo();
		Date createDate = new Date();
		boolean flag = false;
		String id = ((Merchant) entity).getMerchantId();
		String name = ((Merchant) entity).getMerchantName();
		if (defaultFalg == 1) {// 默认选中银行卡
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("merchantId", id);
			dataMap.put("defaultFalg", 1);
			List reList = merchantBankInfoDao.findByProperty(MerchantBankInfo.class, dataMap, 0, 0);
			if (!reList.isEmpty()) {
				MerchantBankInfo bankInfo = (MerchantBankInfo) reList.get(0);
				// 将原有的默认选中银行卡改为2-备用
				bankInfo.setDefaultFlag(2);
				merchantBankInfoDao.update(bankInfo);
			}
			merchantBankInfo.setMerchantId(id);
			merchantBankInfo.setBankName(bankName);
			merchantBankInfo.setBankAccount(bankAccount);
			merchantBankInfo.setDefaultFlag(defaultFalg);
			merchantBankInfo.setCreateDate(createDate);
			merchantBankInfo.setCreateBy(name);
			flag = merchantBankInfoDao.add(merchantBankInfo);
			return flag;
		} else {// 2备用银行卡
			merchantBankInfo.setMerchantId(id);
			merchantBankInfo.setBankName(bankName);
			merchantBankInfo.setBankAccount(bankAccount);
			merchantBankInfo.setDefaultFlag(defaultFalg);
			merchantBankInfo.setCreateDate(createDate);
			merchantBankInfo.setCreateBy(name);
			flag = merchantBankInfoDao.add(merchantBankInfo);
			return flag;
		}
	}

	@Override
	public boolean selectMerchantBank(long id, String merchantId) {
		boolean flag = false;
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		params.put("merchantId", merchantId);
		// 根据前台传递的ID与商户id查询银行卡数据
		List<Object> bankList = merchantBankInfoDao.findByProperty(MerchantBankInfo.class, params, 1, 1);
		params.clear();
		// 放入商户ID与银行卡默认选中的值去找寻商户已选中的银行卡
		params.put("merchantId", merchantId);
		params.put("defaultFalg", 1);
		List<Object> oldBankList = merchantBankInfoDao.findByProperty(MerchantBankInfo.class, params, 0, 0);
		if (!bankList.isEmpty() && !oldBankList.isEmpty()) {
			MerchantBankInfo bankInfo2 = (MerchantBankInfo) bankList.get(0);
			long reId = bankInfo2.getId();
			String reMerchantId = bankInfo2.getMerchantId();
			// 判断前端值与数据库查询出来的值是否一致
			if (id == reId && merchantId.equals(reMerchantId.trim())) {
				bankInfo2.setId(id);
				bankInfo2.setMerchantId(merchantId);
				bankInfo2.setDefaultFlag(1);
				flag = merchantBankInfoDao.update(bankInfo2);
				if (flag) {
					MerchantBankInfo bankInfo = (MerchantBankInfo) oldBankList.get(0);
					// 将原有的默认选中银行卡改为2-备用
					bankInfo.setDefaultFlag(2);
					flag = merchantBankInfoDao.update(bankInfo);
				}
			}
		}
		return flag;
	}

	@Override
	public boolean deleteMerchantBankInfo(long id, String merchantId) {
		Map<String, Object> params = new HashMap<>();
		boolean flag = false;
		params.put("id", id);
		params.put("merchantId", merchantId);
		// 根据前台传递的ID与商户id查询银行卡数据
		List<Object> bankList = merchantBankInfoDao.findByProperty(MerchantBankInfo.class, params, 1, 1);
		if (!bankList.isEmpty()) {
			MerchantBankInfo bankInfo = (MerchantBankInfo) bankList.get(0);
			long reId = bankInfo.getId();
			String reMerchantId = bankInfo.getMerchantId();
			// 判断前端值与数据库查询出来的值是否一致
			if (id == reId && merchantId.equals(reMerchantId.trim())) {
				flag = merchantBankInfoDao.delete(bankInfo);
				long reFalg = bankInfo.getDefaultFlag();
				if (flag && reFalg == 1) {
					params.clear();
					params.put("merchantId", merchantId);
					List<Object> reBankList = merchantBankInfoDao.findByProperty(MerchantBankInfo.class, params, 0, 0);
					MerchantBankInfo reBankInfo = (MerchantBankInfo) reBankList.get(0);
					// 将查询出来的第一张银行卡改为1-默认
					reBankInfo.setDefaultFlag(1);
					flag = merchantBankInfoDao.update(reBankInfo);
				}
				return flag;
			}
		}
		return flag;
	}
}
