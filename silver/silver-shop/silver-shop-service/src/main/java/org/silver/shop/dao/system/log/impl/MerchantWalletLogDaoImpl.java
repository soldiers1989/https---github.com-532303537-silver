package org.silver.shop.dao.system.log.impl;

import org.silver.common.BaseCode;
import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.system.log.MerchantWalletLogDao;
import org.springframework.stereotype.Repository;

@Repository("merchantWalletLogDao")
public class MerchantWalletLogDaoImpl extends BaseDaoImpl implements MerchantWalletLogDao {

	public static void test(int i ) {
		try {
			if(i ==1){
				System.out.println("------");
				throw new Exception();
			}
			System.out.println("=======000");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		test(2);
	}
}
