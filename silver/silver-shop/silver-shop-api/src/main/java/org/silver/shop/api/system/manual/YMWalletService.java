package org.silver.shop.api.system.manual;

import java.util.Map;

public interface YMWalletService {
	/**
	 * 一般钱包进出账 
	 * @param merchant_no
	 * @param amount 金额
	 * @param update_by
	 * @return
	 * @throws Exception 
	 */
    public Map<String,Object> commUpdateMoney(String merchant_no,double amount,String update_by);
}
