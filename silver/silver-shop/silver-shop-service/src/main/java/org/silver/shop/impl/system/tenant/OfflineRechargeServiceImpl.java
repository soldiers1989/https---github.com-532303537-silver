package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.MerchantWalletLogService;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.api.system.tenant.OfflineRechargeService;
import org.silver.shop.dao.system.tenant.OfflineRechargeDao;
import org.silver.shop.model.system.log.OfflineRechargeLog;
import org.silver.shop.model.system.tenant.OfflineRechargeContent;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;

@Service(interfaceClass = OfflineRechargeService.class)
public class OfflineRechargeServiceImpl implements OfflineRechargeService {

	@Autowired
	private OfflineRechargeDao offlineRechargeDao;
	@Autowired
	private MerchantWalletLogService merchantWalletLogService;
	
	@Override
	public Map<String, Object> merchantApplication(Map<String, Object> datasMap) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		JSONArray arr = new JSONArray();
		arr.add(datasMap);
		Map<String, Object> reCheckMap = checkData(arr);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		OfflineRechargeContent content = new OfflineRechargeContent();
		long idCount = offlineRechargeDao.findByPropertyCount(OfflineRechargeContent.class, null);
		if (idCount < 0) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		}
		String offlineRechargeId = SerialNoUtils.getSerialNo("OR", idCount);
		content.setOfflineRechargeId(offlineRechargeId);
		content.setApplicantId(datasMap.get("merchantId") + "");
		content.setApplicant(datasMap.get("merchantName") + "");
		content.setBeneficiaryAccount(datasMap.get("beneficiaryAccount") + "");
		content.setBeneficiaryName(datasMap.get("beneficiaryName") + "");
		content.setBeneficiaryBank(datasMap.get("beneficiaryBank") + "");
		double amount;
		try {
			amount = Double.parseDouble(datasMap.get("remittanceAmount") + "");
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("提交失败，金额错误！");
		}
		if (amount < 0.01) {
			return ReturnInfoUtils.errorInfo("提交失败，汇款金额不能低于0.01元！");
		}
		content.setRemittanceAmount(amount);
		content.setRemittanceAccount(datasMap.get("remittanceAccount") + "");
		content.setRemittanceName(datasMap.get("remittanceName") + "");
		content.setRemittanceBank(datasMap.get("remittanceBank") + "");
		String remittanceDate = datasMap.get("remittanceDate") + "";
		try {
			content.setRemittanceDate(DateUtil.parseDate(remittanceDate, "yyyy-MM-dd"));
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("汇款时间错误！");
		}
		content.setRemittanceReceipt(datasMap.get("remittanceReceipt") + "");
		// 审核类型：firstTrial-运营初审、financialAudit-财务审核、end-结束
		content.setReviewerType("firstTrial");
		content.setCreateBy(datasMap.get("merchantName") + "");
		content.setCreateDate(new Date());
		if (!offlineRechargeDao.add(content)) {
			return ReturnInfoUtils.errorInfo("提交失败,服务器繁忙！");
		}
		OfflineRechargeLog logs = new OfflineRechargeLog();
		logs.setOfflineRechargeId(offlineRechargeId);
		logs.setCurrentNodeName("运营审核");
		// 审核标识：1-待审核、2-审核通过、3-审核不通过
		logs.setReviewerFlag(1);
		logs.setCreateBy(datasMap.get("merchantName") + "");
		logs.setCreateDate(new Date());
		if (!offlineRechargeDao.add(logs)) {
			return ReturnInfoUtils.errorInfo("提交失败,服务器繁忙！");
		}
		return ReturnInfoUtils.successInfo();
	}

	private Map<String, Object> checkData(JSONArray jsonList) {
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("beneficiaryAccount");
		noNullKeys.add("beneficiaryName");
		noNullKeys.add("beneficiaryBank");
		noNullKeys.add("remittanceAmount");
		noNullKeys.add("remittanceAccount");
		noNullKeys.add("remittanceName");
		noNullKeys.add("remittanceBank");
		noNullKeys.add("remittanceDate");
		noNullKeys.add("remittanceReceipt");
		return CheckDatasUtil.changeOfflineRechargeMsg(jsonList, noNullKeys);
	}

	@Override
	public Map<String, Object> getApplication(Map<String, Object> datasMap, int page, int size) {
		if (datasMap == null || datasMap.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		Table table = offlineRechargeDao.getApplication(datasMap, page, size);
		if (table == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙！");
		} else if (!table.getRows().isEmpty()) {
			return ReturnInfoUtils.successDataInfo(Transform.tableToJson(table).getJSONArray("rows"));
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据！");
		}
	}

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
			map.put("log", relogList.get(0));
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
			log.setReviewerId(managerId);
			log.setReviewerName(managerName);
			log.setReviewDate(new Date());
			// 审核标识：1-待审核、2-通过、3-不通过
			if (reviewerFlag == 3) {
				if(StringEmptyUtils.isEmpty(note)){
					return ReturnInfoUtils.errorInfo("批注不能为空！");
				}
				log.setReviewerFlag(reviewerFlag);
				log.setNote(note);
			} else {
				log.setReviewerFlag(reviewerFlag);
			}
			if (!offlineRechargeDao.update(log)) {
				return ReturnInfoUtils.errorInfo("审核失败，服务器繁忙！");
			}
			OfflineRechargeContent content = reList.get(0);
			// 审核类型：firstTrial-运营初审、financialAudit-财务审核、end-结束
			content.setReviewerType("financialAudit");
			content.setUpdateBy(managerName);
			content.setUpdateDate(new Date());
			if (!offlineRechargeDao.update(content)) {
				return ReturnInfoUtils.errorInfo("审核失败，服务器繁忙！");
			}
			OfflineRechargeLog newLog = new OfflineRechargeLog();
			newLog.setOfflineRechargeId(offlineRechargeId);
			newLog.setPreviousNodeName("运营审核");
			newLog.setCurrentNodeName("财务审核");
			// 审核标识：1-待审核、2-通过、3-不通过
			newLog.setReviewerFlag(1);
			newLog.setCreateBy(managerName);
			newLog.setCreateDate(new Date());
			if (!offlineRechargeDao.add(newLog)) {
				return ReturnInfoUtils.errorInfo("提交失败,服务器繁忙！");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}

	@Override
	public Map<String, Object> financialReview(String offlineRechargeId, String managerName, String managerId,
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
			log.setReviewerId(managerId);
			log.setReviewerName(managerName);
			log.setReviewDate(new Date());
			// 审核标识：1-待审核、2-通过、3-不通过
			if (reviewerFlag == 3) {
				if(StringEmptyUtils.isEmpty(note)){
					return ReturnInfoUtils.errorInfo("批注不能为空！");
				}
				log.setReviewerFlag(reviewerFlag);
				log.setNote(note);
			} else {
				log.setReviewerFlag(reviewerFlag);
			}
			if (!offlineRechargeDao.update(log)) {
				return ReturnInfoUtils.errorInfo("审核失败，服务器繁忙！");
			}
			OfflineRechargeContent content = reList.get(0);
			// 审核类型：firstTrial-运营初审、financialAudit-财务审核、end-结束
			content.setReviewerType("end");
			content.setUpdateBy(managerName);
			content.setUpdateDate(new Date());
			if (!offlineRechargeDao.update(content)) {
				return ReturnInfoUtils.errorInfo("审核失败，服务器繁忙！");
			}
			OfflineRechargeLog newLog = new OfflineRechargeLog();
			newLog.setOfflineRechargeId(offlineRechargeId);
			newLog.setPreviousNodeName("财务审核");
			newLog.setCurrentNodeName("结束");
			// 审核标识：1-待审核、2-通过、3-不通过
			newLog.setReviewerFlag(0);
			newLog.setCreateBy(managerName);
			newLog.setCreateDate(new Date());
			
			Map<String, Object> datas = new HashMap<>();
			datas.put("merchantId", content.getApplicantId());
			datas.put(WALLET_ID, merchantWallet.getWalletId());
			datas.put("merchantName", content.getApplicant());
			datas.put("serialName", "订单申报-手续费");
			datas.put("balance", balance);
			datas.put("amount", serviceFee);
			datas.put("type", 4);
			datas.put("flag", "in");
			datas.put("note", "线下充值");
			datas.put("targetWalletId", agentWallet.getWalletId());
			datas.put("targetName", agentWallet.getAgentName());
			//datas.put("count", jsonList.size());
			datas.put("status", "success");
			// 添加商户钱包流水日志
			Map<String, Object> reWalletLogMap = merchantWalletLogService.addWalletLog(datas);
			if (!"1".equals(reWalletLogMap.get(BaseCode.STATUS.toString()))) {
				return reWalletLogMap;
			}
			if (!offlineRechargeDao.add(newLog)) {
				return ReturnInfoUtils.errorInfo("提交失败,服务器繁忙！");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("暂无数据！");
	}
}
