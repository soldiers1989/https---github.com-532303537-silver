package org.silver.shop.task;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.shop.api.system.tenant.MemberWalletService;

/**
 * 创建支付单时商户进行钱包扣款子任务
 */
public class WalletTransferTask implements Callable<Object> {

	private static Logger logger = LogManager.getLogger(Object.class);

	private String memberId;// 用户Id
	private String merchantId; // 商户Id
	private String tradeNo;// 交易流水号
	private MemberWalletService memberWalletService;//
	private Double amount;// 金额

	/**
	 * 商户从关联的用户储备资金中扣款
	 * @param memberId
	 *            用户Id
	 * @param merchantId
	 *            商户Id
	 * @param tradeNo
	 *            交易流水号
	 * @param memberWalletService
	 * @param amount
	 *            支付金额
	 */
	public WalletTransferTask(String memberId, String merchantId, String tradeNo,
			MemberWalletService memberWalletService, double amount) {
		this.memberId = memberId;
		this.merchantId = merchantId;
		this.tradeNo = tradeNo;
		this.memberWalletService = memberWalletService;
		this.amount = amount;
	}

	@Override
	public Map<String, Object> call() {
		try {
			memberWalletService.reserveAmountTransfer(memberId, merchantId, tradeNo, amount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("--生成支付单时用户储备资金清算--", e);
		}
		return null;
	}
}
