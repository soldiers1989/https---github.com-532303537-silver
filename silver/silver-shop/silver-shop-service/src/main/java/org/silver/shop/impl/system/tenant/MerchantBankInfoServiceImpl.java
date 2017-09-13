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
				MerchantBankInfo bankInfo = (MerchantBankInfo)reList.get(0);
				// 将原有的默认选中银行卡改为2-备用
				bankInfo.setDefaultFalg(2);
				merchantBankInfoDao.update(bankInfo);
			}
			merchantBankInfo.setMerchantId(id);
			merchantBankInfo.setBankName(bankName);
			merchantBankInfo.setBankAccount(bankAccount);
			merchantBankInfo.setDefaultFalg(defaultFalg);
			merchantBankInfo.setCreateDate(createDate);
			merchantBankInfo.setCreateBy(name);
			flag = merchantBankInfoDao.add(merchantBankInfo);
			return flag;
		} else {// 2备用银行卡
			merchantBankInfo.setMerchantId(id);
			merchantBankInfo.setBankName(bankName);
			merchantBankInfo.setBankAccount(bankAccount);
			merchantBankInfo.setDefaultFalg(defaultFalg);
			merchantBankInfo.setCreateDate(createDate);
			merchantBankInfo.setCreateBy(name);
			flag = merchantBankInfoDao.add(merchantBankInfo);
			return flag;
		}
	}

	@Override
	public boolean selectMerchantBank(int id) {
		
		return false;
	}

}
