package org.silver.shop.impl.system.organization;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.organization.AgentService;
import org.silver.shop.dao.system.organization.AgentDao;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.util.IdUtils;
import org.silver.shop.util.SearchUtils;
import org.silver.shop.util.WalletUtils;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = AgentService.class)
public class AgentServiceImpl implements AgentService {

	/**
	 * 代理商名称
	 */
	private static final String AGENT_NAME = "agentName";

	@Autowired
	private AgentDao agentDao;
	@Autowired
	private IdUtils<AgentBaseContent> idUtils;
	@Autowired
	private WalletUtils walletUtils;

	@Override
	public Map<String, Object> addAgentBaseInfo(Map<String, Object> datasMap, String managerId, String managerName) {
		if (datasMap != null && !datasMap.isEmpty()) {
			Map<String, Object> reIdMap = idUtils.createId(AgentBaseContent.class, "AgentId_");
			if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
				return reIdMap;
			}
			String agentId = (String) reIdMap.get(BaseCode.DATAS.toString());
			AgentBaseContent agentBase = new AgentBaseContent();
			agentBase.setAgentId(agentId);
			String agentName = (datasMap.get(AGENT_NAME) + "").trim();
			if (!checkAgentName(agentName)) {
				return ReturnInfoUtils.errorInfo("代理商名称已存在,请重新输入!");
			}
			agentBase.setAgentName(agentName);
			MD5 md5 = new MD5();
			agentBase.setLoginPassword(md5.getMD5ofStr(datasMap.get("loginPassword") + ""));
			// 代理商状态：1：注销 2：正常
			agentBase.setAgentStatus("2");
			// 暂设置等级为1
			agentBase.setAgentLevel("1");
			Map<String, Object> reCheckMap = checkCommissionRate(datasMap, agentBase);
			if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
				return reCheckMap;
			}
			agentBase.setCreateBy(managerName);
			agentBase.setCreateDate(new Date());
			agentBase.setDeleteFlag(0);
			if (agentDao.add(agentBase)) {
				return walletUtils.checkWallet(3, agentId, agentName);
			}
			return ReturnInfoUtils.errorInfo("添加商户代理商信息失败,请重试!");
		}
		return ReturnInfoUtils.errorInfo("代理商参数不能为空!");
	}

	/**
	 * 校验代理商品备案,订单申报,支付单申报 佣金率
	 * @param datasMap 参数
	 * @param agentBase 代理商基本信息实体类
	 * @return Map
	 */
	private Map<String, Object> checkCommissionRate(Map<String, Object> datasMap, AgentBaseContent agentBase) {
		if (datasMap == null) {
			return ReturnInfoUtils.errorInfo("校验代理商佣金率参数不能为空!");
		}
		if (StringEmptyUtils.isEmpty(datasMap.get("goodsRecordCommissionRate"))) {
			return ReturnInfoUtils.errorInfo("商品备案佣金率不能为空!");
		}
		double goodsRecordCommissionRate = Double.parseDouble(datasMap.get("goodsRecordCommissionRate") + "");
		if (goodsRecordCommissionRate < 0.0001) {
			return ReturnInfoUtils.errorInfo("商品备案佣金率不能低于万一,请重新输入!");
		}
		agentBase.setGoodsRecordCommissionRate(goodsRecordCommissionRate);
		if (StringEmptyUtils.isEmpty(datasMap.get("orderCommissionRate"))) {
			return ReturnInfoUtils.errorInfo("订单申报佣金率不能为空!");
		}
		double orderCommissionRate = Double.parseDouble(datasMap.get("orderCommissionRate") + "");
		if (orderCommissionRate < 0.0001) {
			return ReturnInfoUtils.errorInfo("订单佣金率不能低于万一,请重新输入!");
		}
		agentBase.setOrderCommissionRate(orderCommissionRate);
		if (StringEmptyUtils.isEmpty(datasMap.get("paymentCommissionRate"))) {
			return ReturnInfoUtils.errorInfo("支付单申报佣金率不能为空!");
		}
		double paymentCommissionRate = Double.parseDouble(datasMap.get("paymentCommissionRate") + "");
		if (paymentCommissionRate < 0.002) {
			return ReturnInfoUtils.errorInfo("支付单佣金率不能低于千二,请重新输入!");
		}
		agentBase.setPaymentCommissionRate(paymentCommissionRate);
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据代理商名称,查询代理商名称是否在数据库表中已存在
	 * @param agentName 代理商名称
	 * @return boolean
	 */
	private boolean checkAgentName(String agentName) {
		Map<String, Object> params = new HashMap<>();
		params.put(AGENT_NAME, agentName);
		List<AgentBaseContent> reList = agentDao.findByProperty(AgentBaseContent.class, params, 0, 0);
		return reList != null && reList.isEmpty();
	}

	@Override
	public List<Object> findAngetBy(String account) {
		if (StringEmptyUtils.isNotEmpty(account)) {
			Map<String, Object> params = new HashMap<>();
			params.put(AGENT_NAME, account);
			return agentDao.findByProperty(AgentBaseContent.class, params, 0, 0);
		}
		return null;
	}

	@Override
	public Map<String, Object> getAllAgentInfo(Map<String, Object> datasMap, int page, int size) {
		if (datasMap != null && page >= 0 && size >= 0) {
			Map<String, Object> reDatasMap = SearchUtils.universalAgentSearch(datasMap);
			Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
			List<AgentBaseContent> reList = agentDao.findByProperty(AgentBaseContent.class, paramMap, page, size);
			long totalCount = agentDao.findByPropertyCount(AgentBaseContent.class, paramMap);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {
				Map<String, Object> item = new HashMap<>();
				Map<String, Object> params = new HashMap<>();
				for (AgentBaseContent agentBase : reList) {
					params.clear();
					String agentId = agentBase.getAgentId();
					agentBase.setLoginPassword("");
					params.put("agentParentId", agentId);
					List<Merchant> reMerchantLis = agentDao.findByProperty(Merchant.class, params, 0, 0);
					item.put("head", agentBase);
					item.put("content", reMerchantLis);
				}
				return ReturnInfoUtils.successDataInfo(item, totalCount);
			} else {
				return ReturnInfoUtils.errorInfo("暂无数据!");
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	@Override
	public Map<String, Object> setAgentSubMerchant(Map<String, Object> datasMap) {
		if (!datasMap.isEmpty()) {
			Map<String, Object> params = new HashMap<>();
			String agentId = String.valueOf(datasMap.get("agentId"));
			String agentName = String.valueOf(datasMap.get(AGENT_NAME));
			params.put("agentId", agentId);
			params.put(AGENT_NAME, agentName);
			List<AgentBaseContent> reAgentList = agentDao.findByProperty(AgentBaseContent.class, params, 0, 0);
			if (reAgentList != null && !reAgentList.isEmpty()) {
				params.put("merchantId", datasMap.get("merchantId"));
				List<Merchant> reMerchantLis = agentDao.findByProperty(Merchant.class, params, 0, 0);
				if (reMerchantLis != null && !reMerchantLis.isEmpty()) {
					Merchant merchant = reMerchantLis.get(0);
					merchant.setAgentParentId(agentId);
					merchant.setAgentParentName(agentName);
					if (!agentDao.update(merchant)) {
						return ReturnInfoUtils.errorInfo("设置代理商子商户失败,服务器繁忙!");
					}
					return ReturnInfoUtils.successInfo();
				}
				return ReturnInfoUtils.errorInfo("商户信息不存在!");
			}
			return ReturnInfoUtils.errorInfo("代理商信息不存在!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	@Override
	public Map<String, Object> getSubMerchantInfo(String agentId, String agentName) {
		if (StringEmptyUtils.isNotEmpty(agentId)) {
			Map<String, Object> params = new HashMap<>();
			params.put("agentParentId", agentId);
			List<Merchant> reMerchantLis = agentDao.findByProperty(Merchant.class, params, 0, 0);
			if (reMerchantLis != null && !reMerchantLis.isEmpty()) {
				return ReturnInfoUtils.successDataInfo(reMerchantLis, 0);
			}
			return ReturnInfoUtils.errorInfo("商户信息不存在!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	/**
	 * 根据代理商Id获取代理商基本(实体类)信息
	 * 
	 * @param agentId
	 *            代理商Id
	 * @return Map
	 */
	public Map<String, Object> getAgentInfo(String agentId) {
		Map<String, Object> params = new HashMap<>();
		params.put("agentId", agentId);
		List<AgentBaseContent> reLis = agentDao.findByProperty(AgentBaseContent.class, params, 0, 0);
		if (reLis == null) {
			return ReturnInfoUtils.errorInfo("查询代理商信息失败,服务器繁忙!");
		} else if (!reLis.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reLis.get(0));
		} else {
			return ReturnInfoUtils.errorInfo("未找到对应的代理商信息!");
		}
	}

}
