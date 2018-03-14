package org.silver.shop.impl.system.organization;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.loader.custom.Return;
import org.silver.shop.api.system.organization.AgentService;
import org.silver.shop.dao.system.organization.AgentDao;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.util.SearchUtils;
import org.silver.util.MD5;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = AgentService.class)
public class AgentServiceImpl implements AgentService {

	@Autowired
	private AgentDao agentDao;

	@Override
	public Map<String, Object> addAgentBaseInfo(Map<String, Object> datasMap, String managerId, String managerName) {
		if (datasMap != null && !datasMap.isEmpty()) {
			Calendar calendar = Calendar.getInstance();
			AgentBaseContent agentBase = new AgentBaseContent();
			long serialCount = agentDao.findSerialNoCount(AgentBaseContent.class, "id", calendar.get(Calendar.YEAR));
			if (serialCount < 0) {
				return ReturnInfoUtils.errorInfo("查询代理商自增数失败,服务器繁忙!");
			}
			agentBase.setAgentId("agentId_" + serialCount);
			String agentName = (datasMap.get("agentName") + "").trim();
			if (!checkAgentName(agentName)) {
				return ReturnInfoUtils.errorInfo("代理商名称已存在,请重新输入!");
			}
			agentBase.setAgentName(agentName);
			MD5 md5 = new MD5();
			agentBase.setLoginPassword(md5.getMD5ofStr(datasMap.get("loginPassword") + ""));
			agentBase.setAgentStatus("2");
			// 暂设置等级为1
			agentBase.setAgentLevel("1");
			agentBase.setCreateBy(managerName);
			agentBase.setCreateDate(new Date());
			agentBase.setDeleteFlag(0);
			if (agentDao.add(agentBase)) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("添加商户代理商信息失败,请重试!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	private boolean checkAgentName(String agentName) {
		Map<String, Object> params = new HashMap<>();
		params.put("agentName", agentName);
		List<AgentBaseContent> reList = agentDao.findByProperty(AgentBaseContent.class, params, 0, 0);
		return reList != null && reList.isEmpty();
	}

	@Override
	public List<Object> findAngetBy(String account) {
		if (StringEmptyUtils.isNotEmpty(account)) {
			Map<String, Object> params = new HashMap<>();
			params.put("agentName", account);
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
			String agentName = String.valueOf(datasMap.get("agentName"));
			params.put("agentId",agentId);
			params.put("agentName",agentName);
			List<AgentBaseContent> reAgentList = agentDao.findByProperty(AgentBaseContent.class, params, 0, 0);
			if (reAgentList != null && !reAgentList.isEmpty()) {
				params.put("merchantId", datasMap.get("merchantId"));
				List<Merchant> reMerchantLis = agentDao.findByProperty(Merchant.class, params, 0, 0);
				if (reMerchantLis != null && !reMerchantLis.isEmpty()) {
					Merchant merchant  = reMerchantLis.get(0);
					merchant.setAgentParentId(agentId);
					merchant.setAgentParentName(agentName);
					if(!agentDao.update(merchant)){
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
		if(StringEmptyUtils.isNotEmpty(agentId)){
			Map<String,Object> params = new HashMap<>();
			params.put("agentParentId", agentId);
			List<Merchant> reMerchantLis = agentDao.findByProperty(Merchant.class, params, 0, 0);
			if (reMerchantLis != null && !reMerchantLis.isEmpty()) {
				return ReturnInfoUtils.successDataInfo(reMerchantLis, 0);
			}
			return ReturnInfoUtils.errorInfo("商户信息不存在!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}
}
