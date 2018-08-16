package org.silver.shop.impl.system.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.MemberWalletLogService;
import org.silver.shop.dao.system.log.MemberWalletLogDao;
import org.silver.shop.model.system.log.MemberWalletLog;
import org.silver.shop.model.system.log.MerchantWalletLog;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.util.WalletUtils;
import org.silver.util.CheckDatasUtil;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = MemberWalletLogService.class)
public class MemberWalletLogServiceImpl implements MemberWalletLogService {

	@Autowired
	private MemberWalletLogDao memberWalletLogDao;
	@Autowired
	private WalletUtils walletUtils;

	@Override
	public Map<String, Object> addWalletLog(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return ReturnInfoUtils.errorInfo("添加用户钱包流水日志时,请求参数不能为空!");
		}
		Map<String, Object> reCheckMap = checkDatas(params);
		if (!"1".equals(reCheckMap.get(BaseCode.STATUS.toString()))) {
			return reCheckMap;
		}
		MemberWalletLog memberWalletLog = new MemberWalletLog();
		memberWalletLog.setMemberWalletId(params.get("memberWalletId") + "");
		memberWalletLog.setMemberName(params.get("memberName") + "");
		memberWalletLog.setSerialNo(params.get("serialNo") + "");
		memberWalletLog.setSerialName(params.get("serialName") + "");
		memberWalletLog.setBeforeChangingBalance(Double.parseDouble(params.get("beforeChangingBalance") + ""));
		memberWalletLog.setAmount(Double.parseDouble(params.get("amount") + ""));
		memberWalletLog.setAfterChangeBalance(Double.parseDouble(params.get("afterChangeBalance") + ""));
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
		memberWalletLog.setType(Integer.parseInt(params.get("type") + ""));
		memberWalletLog.setStatus(params.get("status") + "");
		memberWalletLog.setFlag(params.get("flag") + "");
		memberWalletLog.setTargetWalletId(params.get("targetWalletId") + "");
		memberWalletLog.setTargetName(params.get("targetName") + "");
		memberWalletLog.setCreateBy("system");
		memberWalletLog.setCreateDate(new Date());
		if (!memberWalletLogDao.add(memberWalletLog)) {
			return ReturnInfoUtils.errorInfo("保存用户钱包流水日志失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successInfo();
	}

	private Map<String, Object> checkDatas(Map<String, Object> params) {
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("memberWalletId");
		noNullKeys.add("memberName");
		noNullKeys.add("serialNo");
		noNullKeys.add("beforeChangingBalance");
		noNullKeys.add("amount");
		noNullKeys.add("afterChangeBalance");
		noNullKeys.add("type");
		noNullKeys.add("status");
		noNullKeys.add("flag");
		noNullKeys.add("targetWalletId");
		noNullKeys.add("targetName");
		JSONArray jsonArr = new JSONArray();
		jsonArr.add(params);
		return CheckDatasUtil.checkData(jsonArr, noNullKeys);
	}

	@Override
	public Map<String, Object> getInfo(Member memberInfo, String startDate, String endDate, int type, int page,
			int size) {
		if (memberInfo == null) {
			return ReturnInfoUtils.errorInfo("请求参数不能为null");
		}
		// 类型:1-佣金、2-充值、3-提现、4-缴费、5-购物
		if (type < 0 || type > 5) {
			return ReturnInfoUtils.errorInfo("类型错误！");
		}
		Map<String, Object> reWalletMap = walletUtils.checkWallet(2, memberInfo.getMemberId(), "");
		if (!"1".equals(reWalletMap.get(BaseCode.STATUS.toString()))) {
			return reWalletMap;
		}
		MemberWalletContent wallet = (MemberWalletContent) reWalletMap.get(BaseCode.DATAS.toString());
		Map<String, Object> params = new HashMap<>();
		params.put("type", type);
		params.put("memberWalletId", wallet.getWalletId());
		if (DateUtil.parseDate(startDate, "yyyy-MM-dd hh:mm:ss") == null
				|| DateUtil.parseDate(endDate, "yyyy-MM-dd hh:mm:ss") == null) {
			return ReturnInfoUtils.errorInfo("日期格式错误,请重新输入!");
		}
		params.put("startDate", DateUtil.parseDate(startDate, "yyyy-MM-dd hh:mm:ss"));
		params.put("endDate", DateUtil.parseDate(endDate, "yyyy-MM-dd hh:mm:ss"));
		List<MemberWalletLog> reList = memberWalletLogDao.findByPropertyLike(MemberWalletLog.class, params, null, page,
				size);
		long tatolCount = memberWalletLogDao.findByPropertyLikeCount(MemberWalletLog.class, params, null);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, tatolCount);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

}
