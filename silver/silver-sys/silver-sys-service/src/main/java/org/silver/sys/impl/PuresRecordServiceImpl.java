package org.silver.sys.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.silver.sys.api.PuresRecordService;
import org.silver.sys.dao.PuresRecordDao;
import org.silver.sys.dao.WalletDao;
import org.silver.sys.model.pures.PuresRecord;
import org.silver.sys.model.pures.Wallet;

public class PuresRecordServiceImpl implements PuresRecordService {

	@Resource
	private WalletDao walletDao;
	@Resource
	private PuresRecordDao puresRecordDao;
	
	private final static Log logger = LogFactory.getLog(PuresRecordServiceImpl.class);
	
	@Override
	public Map<String, Object> walletRecharge(String serialNo, String walletNo, String tenantNo, String acceptanceNo,
			double money,int status) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if(serialNo!=null&&walletNo!=null&&acceptanceNo!=null&&tenantNo!=null){
			resultMap.put("status", -6);
			resultMap.put("msg", "请检查请求参数：serialNo,walletNo,acceptanceNo,acceptanceNo");
			return resultMap;
		}
		if(status==1){//充值成功
			Wallet wallet =findWallet(walletNo, tenantNo);
			if(wallet!=null){//找到对应钱包 
				boolean bln=addPuresRecord(serialNo,walletNo,tenantNo,acceptanceNo,money,status,wallet.getAmount(),wallet.getBalance(),wallet.getFrozenFund(),1);
				if(bln){//充值记录存储成功
					if(updateWallet(wallet, money,1)){
						resultMap.put("status", 1);
						resultMap.put("msg", "充值成功！");
						return resultMap;
					}else{
						resultMap.put("status",-5);
						resultMap.put("msg", "钱包总金额累加失败！");
						return resultMap;
					}
				}else{//充值记录存储失败
					logger.info("充值记录存储失败，钱包金额更新失败！");
					resultMap.put("status", -1);
					resultMap.put("msg", "充值失败！");
					return resultMap;
				}
			}else{//没找到对应钱包
				addPuresRecord(serialNo, walletNo, tenantNo, acceptanceNo, money, status, 0, 0, 0, 1);
				logger.info("没找到钱包对象！");
				resultMap.put("status", -2);
				resultMap.put("msg", "充值失败！");
				return resultMap;
			}
		}else{//充值失败
			addPuresRecord(serialNo, walletNo, tenantNo, acceptanceNo, money, status, 0, 0, 0, 1);
			logger.info("充值失败！");
			resultMap.put("status", -3);
			resultMap.put("msg", "充值失败！");
			return resultMap;
		}
	}

	@Override
	public Map<String, Object> withdrawDeposit(String serialNo, String walletNo, String tenantNo, String acceptanceNo,
			double money, String bank, String bankAccount,int status) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if(serialNo!=null&&walletNo!=null&&acceptanceNo!=null&&tenantNo!=null){
			resultMap.put("status", -6);
			resultMap.put("msg", "请检查请求参数：serialNo,walletNo,acceptanceNo,acceptanceNo");
			return resultMap;
		}
		if(status==1){
			Wallet wallet =findWallet(walletNo, tenantNo);
			if(wallet!=null){
				boolean bln=addPuresRecord(serialNo,walletNo,tenantNo,acceptanceNo,money,status,wallet.getAmount(),wallet.getBalance(),wallet.getFrozenFund(),2);
				if(bln){
					if(updateWallet(wallet, money,2)){
						resultMap.put("status", 1);
						resultMap.put("msg", "提现成功！");
						return resultMap;
					}else{
						resultMap.put("status",-5);
						resultMap.put("msg", "钱包扣款失败！");
						return resultMap;
					}
				}else{
					logger.info("提现记录存储失败，钱包金额更新失败！");
					resultMap.put("status", -1);
					resultMap.put("msg", "提现失败！");
					return resultMap;
				}
			}else{
				addPuresRecord(serialNo, walletNo, tenantNo, acceptanceNo, money, status, 0, 0, 0, 2);
				logger.info("没找到钱包对象！");
				resultMap.put("status", -2);
				resultMap.put("msg", "提现失败！");
				return resultMap;
			}
		}else{
			addPuresRecord(serialNo, walletNo, tenantNo, acceptanceNo, money, status, 0, 0, 0, 1);
			logger.info("提现失败！");
			resultMap.put("status", -3);
			resultMap.put("msg", "提现失败！");
			return resultMap;
		}
		
	}

	public Wallet findWallet( String walletNo, String tenantNo){
		Map<String , Object> params= new HashMap<String, Object>();
		params.put("walletNo",walletNo);
		params.put("tenantNo",tenantNo);
		params.put("delFlag", 0);
		List<Wallet> walletList =walletDao.findByProperty(params, 0, 0);
		if(walletList!=null&&walletList.size()>0){
			Wallet wallet =walletList.get(0);
			return wallet;
		}
		return null;
	}
	/**
	 * 
	 * @param serialNo       业务流水号
	 * @param walletNo       钱包编号
	 * @param tenantNo       商户关联号
	 * @param acceptanceNo   支付企业响应号
	 * @param money          业务资金
	 * @param status         业务状态   1成功   其他均为失败
	 * @param amount         业务发起前，钱包总金额
	 * @param balance        业务发起前，钱包可用余额
	 * @param frozenFund     业务发起前，钱包冻结金额
	 * @param type           业务类型 1 充值  2 提现
	 * @return
	 */
	public boolean addPuresRecord(String serialNo, String walletNo, String tenantNo, String acceptanceNo,
			double money,int status,double amount,double balance,double frozenFund,int type){
		PuresRecord puresRecord = new PuresRecord();
		puresRecord.setWalletNo(walletNo);
		puresRecord.setTenantNo(tenantNo);
		puresRecord.setSerialNo(serialNo);
		puresRecord.setAcceptanceNo(acceptanceNo);
		puresRecord.setMoney(money);
		puresRecord.setStatus(status);
		puresRecord.setAmount(amount);
		puresRecord.setBalance(balance);
		puresRecord.setFrozenFund(frozenFund);
		puresRecord.setType(type);
		puresRecord.setCreateDate(new Date());
		boolean bln =puresRecordDao.add(puresRecord);
		return bln;
	}
	//钱包充值，更新数据
	public boolean updateWallet(Wallet wallet,double money,int type){
		boolean bln =false;
		switch (type) {
		case 1://充值
			wallet.setAmount(wallet.getAmount()+money);
			wallet.setBalance(wallet.getBalance()+money);
			wallet.setUpdateDate(new Date());
			bln =walletDao.update(wallet);
			return bln;
		case 2://提现
			wallet.setAmount(wallet.getAmount()-money);
			wallet.setBalance(wallet.getBalance()-money);
			wallet.setUpdateDate(new Date());
			bln =walletDao.update(wallet);
		default:
			return bln;
		}
	}
	
	
	
	public static void main(String[] args) {
		System.out.println(new Date());

	}
}
