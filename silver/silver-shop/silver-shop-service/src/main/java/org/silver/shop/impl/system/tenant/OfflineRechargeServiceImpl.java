package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.MerchantWalletLogService;
import org.silver.shop.api.system.tenant.OfflineRechargeService;
import org.silver.shop.dao.system.tenant.OfflineRechargeDao;
import org.silver.shop.model.system.log.OfflineRechargeLog;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.OfflineRechargeContent;
import org.silver.shop.util.WalletUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = OfflineRechargeService.class)
public class OfflineRechargeServiceImpl implements OfflineRechargeService {

	@Autowired
	private OfflineRechargeDao offlineRechargeDao;
	@Autowired
	private MerchantWalletLogService merchantWalletLogService;
	@Autowired
	private WalletUtils walletUtils;

	@Override
	public Map<String, Object> getApplicationDetail(String offlineRechargeId) {
		if (StringEmptyUtils.isEmpty(offlineRechargeId)) {
			return ReturnInfoUtils.errorInfo("流水id不能为空！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("offlineRechargeId", offlineRechargeId);
		List<OfflineRechargeContent> reList = offlineRechargeDao.findByProperty(OfflineRechargeContent.class, params, 0,
				0);
		List<OfflineRechargeLog> relogList = offlineRechargeDao.findByProperty(OfflineRechargeLog.class, params, 0, 0);
		if (reList == null || relogList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			Map<String, Object> map = new HashMap<>();
			map.put("content", reList.get(0));
			map.put("log", relogList);
			return ReturnInfoUtils.successDataInfo(map);
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}

	@Override
	public Map<String, Object> managerReview(String offlineRechargeId, String managerName, String managerId,
			int reviewerFlag, String note) {
		if (StringEmptyUtils.isEmpty(offlineRechargeId)) {
			return ReturnInfoUtils.errorInfo("流水id不能为空！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("offlineRechargeId", offlineRechargeId);
		List<OfflineRechargeContent> reList = offlineRechargeDao.findByProperty(OfflineRechargeContent.class, params, 0,
				0);
		List<OfflineRechargeLog> relogList = offlineRechargeDao.findByProperty(OfflineRechargeLog.class, params, 0, 0);
		if (reList == null || relogList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			OfflineRechargeLog log = relogList.get(0);
			OfflineRechargeContent content =reList.get(0);
			log.setReviewerId(managerId);
			log.setReviewerName(managerName);
			log.setReviewDate(new Date());
			// 审核标识：1-待审核、2-通过、3-不通过
			if (reviewerFlag == 3) {
				log.setReviewerFlag(reviewerFlag);
				content.setUpdateBy(managerName);
				content.setUpdateDate(new Date());
				return updateFailureLog(log, note,content);
			} else {
				log.setReviewerFlag(reviewerFlag);
			}
			if (!offlineRechargeDao.update(log)) {
				return ReturnInfoUtils.errorInfo("审核失败，服务器繁忙！");
			}
			Map<String, Object> reUpdateMap = updateOfflineRechargeContent(content, managerName,
					"financialAudit");
			if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
				return reUpdateMap;
			}
			return addNewLog(offlineRechargeId, "运营审核", "财务审核", 1, managerName);
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}

	/**
	 * 更新失败日志记录
	 * 
	 * @param log
	 *            线下充值日志实体信息类
	 * @param note
	 *            批注说明信息
	 * @param content
	 * @return Map
	 */
	private Map<String, Object> updateFailureLog(OfflineRechargeLog log, String note, OfflineRechargeContent content) {
		if (log == null) {
			return ReturnInfoUtils.errorInfo("日志信息不能为null");
		}
		if (StringEmptyUtils.isEmpty(note)) {
			return ReturnInfoUtils.errorInfo("批注说明不能为空！");
		}
		if (content == null) {
			return ReturnInfoUtils.errorInfo("审核信息错误！");
		}
		// 审核类型：firstTrial-运营初审、financialAudit-财务审核、termination-终止、carryOut-完成
		log.setNote(note);
		content.setReviewerType("termination");
		if (!offlineRechargeDao.update(log) || !offlineRechargeDao.update(content) ) {
			return ReturnInfoUtils.errorInfo("审核失败，服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	@Override
	public Map<String, Object> financialReview(String offlineRechargeId, String managerName, String managerId,
			int reviewerFlag, String note) {
		if (StringEmptyUtils.isEmpty(offlineRechargeId) || StringEmptyUtils.isEmpty(managerName)
				|| StringEmptyUtils.isEmpty(managerId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空！");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("offlineRechargeId", offlineRechargeId);
		List<OfflineRechargeContent> reList = offlineRechargeDao.findByProperty(OfflineRechargeContent.class, params, 0,
				0);
		List<OfflineRechargeLog> relogList = offlineRechargeDao.findByProperty(OfflineRechargeLog.class, params, 0, 0);
		if (reList == null || relogList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!reList.isEmpty()) {
			// 更新财务审核日志
			OfflineRechargeLog log = relogList.get(0);
			OfflineRechargeContent content = reList.get(0);
			log.setReviewerId(managerId);
			log.setReviewerName(managerName);
			log.setReviewDate(new Date());
			// 审核标识：1-待审核、2-通过、3-不通过
			if (reviewerFlag == 3) {
				log.setReviewerFlag(reviewerFlag);
				content.setUpdateBy(managerName);
				content.setUpdateDate(new Date());
				return updateFailureLog(log, note, content);
			} else {
				log.setReviewerFlag(reviewerFlag);
			}
			if (!offlineRechargeDao.update(log)) {
				return ReturnInfoUtils.errorInfo("审核失败，服务器繁忙！");
			}
			Map<String, Object> reUpdateMap = updateOfflineRechargeContent(content, managerName, "end");
			if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
				return reUpdateMap;
			}
			Map<String, Object> reNewLogMap = addNewLog(offlineRechargeId, "财务审核", "结束", 0, managerName);
			if (!"1".equals(reNewLogMap.get(BaseCode.STATUS.toString()))) {
				return reNewLogMap;
			}
			return updateWallet(content);
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}

	private Map<String, Object> updateWallet(OfflineRechargeContent content) {
		if (content == null) {
			return ReturnInfoUtils.errorInfo("更新钱包信息时,请求参数不能为null");
		}
		Map<String, Object> reWalletMap = walletUtils.checkWallet(1, content.getApplicantId(), content.getApplicant());
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return reWalletMap;
		}
		MerchantWalletContent merchantWallet = (MerchantWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		double oldBalance = merchantWallet.getBalance();
		merchantWallet.setBalance(oldBalance + content.getRemittanceAmount());
		if (!offlineRechargeDao.update(merchantWallet)) {
			return ReturnInfoUtils.errorInfo("加款失败，服务器繁忙！");
		}
		Map<String, Object> datas = new HashMap<>();
		datas.put("merchantId", content.getApplicantId());
		datas.put("walletId", merchantWallet.getWalletId());
		datas.put("merchantName", content.getApplicant());
		datas.put("serialName", "银盟线下加款");
		datas.put("balance", oldBalance);
		datas.put("amount", content.getRemittanceAmount());
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
		datas.put("type", 2);
		datas.put("flag", "in");
		datas.put("note", "线下充值");
		datas.put("targetWalletId", "000000");
		datas.put("targetName", "银盟");
		datas.put("status", "success"); // 添加商户钱包流水日志
		Map<String, Object> reWalletLogMap = merchantWalletLogService.addWalletLog(datas);
		if (!"1".equals(reWalletLogMap.get(BaseCode.STATUS.toString()))) {
			return reWalletLogMap;
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 更新线下充值记录信息
	 * 
	 * @param content
	 *            线下充值内容
	 * @param managerName
	 *            管理员名称
	 * @param reviewerType
	 *            审核类型：firstTrial-运营初审、financialAudit-财务审核、end-结束
	 * @return Map
	 */
	private Map<String, Object> updateOfflineRechargeContent(OfflineRechargeContent content, String managerName,
			String reviewerType) {
		if (content == null) {
			return ReturnInfoUtils.errorInfo("更新线下充值审核内容不能为null！");
		}
		//
		if (StringEmptyUtils.isEmpty(reviewerType)) {
			return ReturnInfoUtils.errorInfo("更新的审核类型不能为空！");
		}
		content.setReviewerType(reviewerType);
		content.setUpdateBy(managerName);
		content.setUpdateDate(new Date());
		if (!offlineRechargeDao.update(content)) {
			return ReturnInfoUtils.errorInfo("审核失败，服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();

	}

	/**
	 * 添加新的审核日志
	 * 
	 * @param offlineRechargeId
	 *            线下充值流水id
	 * @param previousNodeName
	 *            上一个节点名称
	 * @param currentNodeName
	 *            当前节点名称
	 * @param reviewerFlag
	 *            审核标识：1-待审核、2-通过、3-不通过
	 * @param managerName
	 *            管理员名称
	 * @return
	 */
	private Map<String, Object> addNewLog(String offlineRechargeId, String previousNodeName, String currentNodeName,
			int reviewerFlag, String managerName) {
		if (StringEmptyUtils.isEmpty(offlineRechargeId)) {
			return ReturnInfoUtils.errorInfo("添加新日志时，流水id不能为空！");
		}
		OfflineRechargeLog newLog = new OfflineRechargeLog();
		newLog.setOfflineRechargeId(offlineRechargeId);
		if (StringEmptyUtils.isEmpty(previousNodeName) || StringEmptyUtils.isEmpty(currentNodeName)) {
			return ReturnInfoUtils.errorInfo("节点名称不能为空！");
		}
		newLog.setPreviousNodeName(previousNodeName);
		newLog.setCurrentNodeName(currentNodeName);
		newLog.setReviewerFlag(reviewerFlag);
		newLog.setCreateBy(managerName);
		newLog.setCreateDate(new Date());
		if (!offlineRechargeDao.add(newLog)) {
			return ReturnInfoUtils.errorInfo("提交失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}
}
