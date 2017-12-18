package org.silver.shop.impl.system.tenant;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.ProxyWalletService;
import org.silver.shop.dao.system.tenant.ProxyWalletDao;
import org.silver.shop.model.system.tenant.ProxyWalletContent;
import org.silver.shop.model.system.tenant.ProxyWalletLog;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = ProxyWalletService.class)
public class ProxyWalletServiceImpl implements ProxyWalletService {

	@Autowired
	private ProxyWalletDao proxyWalletDao;
	@Autowired
	private MerchantWalletServiceImpl merchantWalletServiceImpl;

	@Override
	public Map<String, Object> getProxyWalletInfo(String proxyUUid, String proxyName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reMap = merchantWalletServiceImpl.checkWallet(3, proxyUUid, proxyName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "创建钱包失败!");
			return statusMap;
		}
		ProxyWalletContent wallet = (ProxyWalletContent) reMap.get(BaseCode.DATAS.toString());
		statusMap.put(BaseCode.DATAS.toString(), wallet);
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
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
		List<Object> reList = proxyWalletDao.findByPropertyLike(ProxyWalletLog.class, params, null, page, size);
		long tatolCount = proxyWalletDao.findByPropertyLikeCount(ProxyWalletLog.class, params, null);
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
