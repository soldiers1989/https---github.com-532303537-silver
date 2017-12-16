package org.silver.shop.impl.system.manual;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.silver.shop.api.system.manual.YMWalletService;
import org.silver.shop.dao.system.manual.YMWalletDao;
import org.silver.shop.model.system.manual.YMWallet;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=YMWalletService.class)
public class YMWalletServiceImpl implements YMWalletService{

	@Resource
	private YMWalletDao yMWalletDao;
	
	@Override
	public Map<String, Object> commUpdateMoney(String merchant_no, double amount, String update_by)  {
		
		 Map<String, Object> params = new HashMap<>();
		 params.put("merchant_no", merchant_no);
		 List<YMWallet> lymwallet=yMWalletDao.findByProperty(params, 1, 1);
		 if(lymwallet!=null&&lymwallet.size()>0){
			 YMWallet entity=lymwallet.get(0);
			 entity.setTotal_fund(entity.getTotal_fund()+amount);
			 entity.setAvailable_balance(entity.getAvailable_balance()+amount);
			 entity.setUpdate_date(new Date());
			 entity.setUpdate_by(update_by);
			 if(yMWalletDao.update(entity)){
				 params.put("status", 1);
				 params.put("msg", "电子钱包出入账成功");
				 return params;
			 }
		
			 params.put("status", -1);
			 params.put("msg", "电子钱包出入账失败");
			 return params;
		}
		 params.put("status", -4);
		 params.put("msg", "系统内部错误，未找到商户电子钱包");
		 return params;
	}

	
}
