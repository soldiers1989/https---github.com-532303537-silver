package org.silver.shop.impl.system.organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.system.organization.MerchantService;
import org.silver.shop.dao.system.organization.MerchantDao;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.RecordInfo;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantService.class)
public class MerchantServiceImpl implements MerchantService {
	static List<Map> list = null;
	static {
		// 初始化银盟自己商户备案信息
		list = new ArrayList<>();
		Map<String, Object> record1 = new HashMap<>();
		Map<String, Object> record2 = new HashMap<>();
		record1.put("eport", 0);//  1-广州电子口岸(目前只支持BC业务) 
		record1.put("ebEntNo", "C010000000537118");
		record1.put("ebEntName", "广州银盟信息科技有限公司");
		record1.put("ebpEntNo", "C010000000537118");
		record1.put("ebpEntName", "广州银盟信息科技有限公司");

		record2.put("eport", 1);// 2-南沙智检(支持BBC业务)
		record2.put("ebEntNo", "1509007917");
		record2.put("ebEntName", "广州银盟信息科技有限公司");
		record2.put("ebpEntNo", "1509007917");
		record2.put("ebpEntName", "广州银盟信息科技有限公司");
		list.add(record1);
		list.add(record2);
	}
	@Autowired
	private MerchantDao<Merchant> merchantDao;

	@Override
	public boolean merchantRegister(Merchant entity) {
		return merchantDao.saveMerchantContent(entity);
	}

	@Override
	public List<Object> checkMerchantName(Map dataMap) {
		return merchantDao.checkMerchantName(Merchant.class, dataMap, 1, 1);
	}

	@Override
	public String findMerchantBy(String account) {
		return null;
	}

	@Override
	public Long findOriginalMerchantId() {
		return (long) merchantDao.findLastId();
	}

	@Override
	public boolean addMerchantRecordInfo(RecordInfo entity, String type) {
		boolean flag = false;
		if (type.equals("1")) {// 银盟自己商户备案信息插入
			for (int x = 0; x < list.size(); x++) {
				Map<String, Object> listMap = list.get(x);
				entity.setEport((int) listMap.get("eport"));
				entity.setEbEntNo(listMap.get("ebEntNo") + "");
				entity.setEbEntName(listMap.get("ebEntName") + "");
				entity.setEbpEntNo(listMap.get("ebpEntNo") + "");
				entity.setEbpEntName(listMap.get("ebpEntName") + "");
				flag = merchantDao.savenMerchantRecordInfo(entity);
			}
		} else {// 第三方商戶备案信息插入
			flag = merchantDao.savenMerchantRecordInfo(entity);
		}
		return flag;
	}

}
