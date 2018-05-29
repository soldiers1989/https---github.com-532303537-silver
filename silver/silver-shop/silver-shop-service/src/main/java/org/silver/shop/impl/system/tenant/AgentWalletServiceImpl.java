package org.silver.shop.impl.system.tenant;

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
import org.silver.shop.util.WalletUtils;
import org.silver.util.ReturnInfoUtils;
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

}
