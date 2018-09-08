package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.AgentWalletService;
import org.silver.shop.dao.system.tenant.ProxyWalletDao;
import org.silver.shop.model.system.log.AgentWalletLog;
import org.silver.shop.model.system.tenant.AgentWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.util.WalletUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = AgentWalletService.class)
public class AgentWalletServiceImpl implements AgentWalletService {

	@Autowired
	private ProxyWalletDao proxyWalletDao;
	@Autowired
	private WalletUtils walletUtils;
	
	@Override
	public Map<String, Object> getAgentWalletInfo(String agentId, String agentName) {
		return walletUtils.checkWallet(3, agentId, agentName);
	}

	@Override
	public Map<String, Object> getProxyWalletLog(String proxyUUid, String proxyName, int type, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		Date endDate = new Date(); // 当前时间
		Calendar calendar = Calendar.getInstance(); // 得到日历
		calendar.setTime(endDate);// 把当前时间赋给日历
		Date startDate = null;
		// 查询时间范围 1-三个月内,2-一年内
		if (type == 1) {// 查询最近三个月
			calendar.add(Calendar.MONTH, -3); // 设置为前3月
			startDate = calendar.getTime(); // 得到前3月的时间
		} else if (type == 2) {// 查询最近一年
			calendar.add(Calendar.YEAR, -1); // 设置为前1年
			startDate = calendar.getTime(); // 得到前1年的时间
		}
		params.put("proxyId", proxyUUid);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		List<Object> reList = proxyWalletDao.findByPropertyLike(AgentWalletLog.class, params, null, page, size);
		long tatolCount = proxyWalletDao.findByPropertyLikeCount(AgentWalletLog.class, params, null);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), tatolCount);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> generateSign(String agentId) {
		if (StringEmptyUtils.isNotEmpty(agentId)) {
			AgentWalletContent entity = findByIdWallet(agentId);
			if (entity == null) {
				return ReturnInfoUtils.errorInfo("钱包查询失败！");
			}
			entity.setVerifyCode(WalletUtils.generateSign(entity.getWalletId(), entity.getBalance(),
					entity.getReserveAmount(), entity.getFreezingFunds(), entity.getCash()));
			return updateWallet(entity);
		} else {
			List<AgentWalletContent> reList = proxyWalletDao.findByProperty(AgentWalletContent.class, null, 0,
					0);
			if (reList != null && !reList.isEmpty()) {
				for (AgentWalletContent entity : reList) {
					entity.setVerifyCode(WalletUtils.generateSign(entity.getWalletId(), entity.getBalance(),
							entity.getReserveAmount(), entity.getFreezingFunds(), entity.getCash()));
					Map<String, Object> reUpdateMap = updateWallet(entity);
					if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
						return reUpdateMap;
					}
				}
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	private Map<String, Object> updateWallet(AgentWalletContent entity) {
		if (entity == null) {
			return ReturnInfoUtils.errorInfo("更新钱包时，请求参数不能为null");
		}
		entity.setUpdateDate(new Date());
		if (!proxyWalletDao.update(entity)) {
			return ReturnInfoUtils.errorInfo("商户钱包更新失败！");
		}
		return ReturnInfoUtils.successInfo();
	}

	/**
	 * 根据商户id查询、钱包信息
	 * 
	 * @param merchantId
	 *            商户id
	 * @return MerchantWalletContent 商户钱包实体
	 */
	private AgentWalletContent findByIdWallet(String agentId) {
		Map<String, Object> reWalletMap = walletUtils.checkWallet(3, agentId, null);
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return null;
		}
		return (AgentWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
	}
	
}
