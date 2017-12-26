package org.silver.shop.impl.system.tenant;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.MerchantWalletService;
import org.silver.shop.dao.system.tenant.MerchantWalletDao;
import org.silver.shop.model.system.organization.Proxy;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletLog;
import org.silver.shop.model.system.tenant.ProxyWalletContent;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MerchantWalletService.class)
public class MerchantWalletServiceImpl implements MerchantWalletService {

	@Autowired
	private MerchantWalletDao merchantWalletDao;

	@Override
	public Map<String, Object> walletRecharge(String merchantId, String merchantName, Double money) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reMap = checkWallet(1, merchantId, merchantName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "创建钱包失败!");
			return statusMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		double oldBalance = wallet.getBalance();
		wallet.setBalance(oldBalance + money);
		wallet.setUpdateDate(date);
		if (!merchantWalletDao.update(wallet)) {
			statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
			statusMap.put(BaseCode.MSG.toString(), "充值失败,服务器繁忙!");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	/**
	 * 检查钱包
	 * 
	 * @param type
	 *            1-商户钱包,2-用户钱包,3-代理商钱包
	 * @param id
	 *            商户Id/用户Id
	 * @param name
	 *            商户名称/用户名称
	 */
	public Map<String, Object> checkWallet(int type, String id, String name) {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> statusMap = new HashMap<>();
		Class entity = null;
		if (type > 0 && StringEmptyUtils.isNotEmpty(id) && StringEmptyUtils.isNotEmpty(name)) {
			switch (type) {
			case 1:
				params.put("merchantId", id);
				entity = MerchantWalletContent.class;
				break;
			case 2:
				params.put("memberId", id);
				entity = MemberWalletContent.class;
				break;
			case 3:
				params.put("proxyId", id);
				entity = ProxyWalletContent.class;
				break;
			default:
				break;
			}
			List<Object> reList = merchantWalletDao.findByProperty(entity, params, 1, 1);
			if (reList == null) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				return statusMap;
			} else if (!reList.isEmpty()) {// 钱包不为空,则直接返回
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.DATAS.toString(), reList.get(0));
				return statusMap;
			} else {
				Object reEntity = createWallet(type, entity, id, name);
				if (reEntity != null) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
					statusMap.put(BaseCode.DATAS.toString(), reEntity);
					return statusMap;
				} else {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
					statusMap.put(BaseCode.MSG.toString(), "创建钱包失败!");
					return statusMap;
				}
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
		statusMap.put(BaseCode.MSG.toString(), "参数不能为空!");
		return statusMap;
	}

	/**
	 * 创建钱包
	 * 
	 * @param type
	 *            1-商户钱包,2-用户钱包
	 * @param entity
	 *            实体类
	 * @param id
	 * @param name
	 * @return
	 */
	private Object createWallet(int type, Class entity, String id, String name) {
		Calendar cal = Calendar.getInstance();
		Date date = new Date();
		int year = cal.get(Calendar.YEAR);
		Object wallet = null;
		long count = merchantWalletDao.findSerialNoCount(entity, "id", 0);
		if (count < 0) {
			return null;
		}
		String serialNo = SerialNoUtils.getSerialNotTimestamp2("walletId", year, count);
		switch (type) {
		case 1:
			wallet = new MerchantWalletContent.Builder(serialNo).merchantId(id).merchantName(name).createBy(name)
					.createDate(date).build();
			break;
		case 2:
			wallet = new MemberWalletContent.Builder(serialNo).memberId(id).memberName(name).createBy(name)
					.createDate(date).build();
			break;
		case 3:
			wallet = new ProxyWalletContent.Builder(serialNo).proxyId(id).proxyName(name).createBy(name)
					.createDate(date).build();
			break;
		default:
			break;
		}
		if (!merchantWalletDao.add(wallet)) {
			return null;
		}
		return wallet;
	}

	@Override
	public Map<String, Object> getMerchantWallet(String merchantId, String merchantName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> reMap = checkWallet(1, merchantId, merchantName);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.FORMAT_ERR.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "创建钱包失败!");
			return statusMap;
		}
		MerchantWalletContent wallet = (MerchantWalletContent) reMap.get(BaseCode.DATAS.toString());
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		statusMap.put(BaseCode.DATAS.toString(), wallet);
		return statusMap;
	}

	@Override
	public Map<String, Object> getMerchantWalletLog(String merchantId, String merchantName, int type, int page,
			int size, int timeLimit) {
		Map<String, Object> statusMap = new HashMap<>();
		if (page >= 0 && size >= 0 && timeLimit >= 0 && type >= 0) {
			Map<String, Object> params = new HashMap<>();
			Date endDate = new Date(); // 当前时间
			Calendar calendar = Calendar.getInstance(); // 得到日历
			calendar.setTime(endDate);// 把当前时间赋给日历
			Date startDate = null;
			// 查询时间范围 1-三个月内,2-一年内,3-今天
			if (timeLimit == 1) {// 查询最近三个月
				calendar.add(Calendar.MONTH, -3); // 设置为前3月
				startDate = calendar.getTime(); // 得到前3月的时间
			} else if (timeLimit == 2) {// 查询最近一年
				calendar.add(Calendar.YEAR, -1); // 设置为前1年
				startDate = calendar.getTime(); // 得到前1年的时间
			} else if (timeLimit == 3) {// 查询今天
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				startDate = calendar.getTime();// 当天零点零分零秒
			}
			params.put("merchantId", merchantId);
			if (type > 0) {
				params.put("type", type);
			}
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			List<Object> reList = merchantWalletDao.findByPropertyLike(MerchantWalletLog.class, params, null, page,
					size);
			long tatolCount = merchantWalletDao.findByPropertyLikeCount(MerchantWalletLog.class, params, null);
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
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.FORMAT_ERR.getMsg());
		return statusMap;
	}

}