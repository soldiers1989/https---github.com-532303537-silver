package org.silver.shop.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.organization.AgentService;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.BaseDao;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.AgentWalletContent;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.util.CheckDatasUtil;
import org.silver.util.MD5;
import org.silver.util.MapSortUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 钱包操作通用工具类
 */
@Component
public class WalletUtils {

	@Autowired
	private BaseDao baseDao;
	@Autowired
	private IdUtils idUtils;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private MemberService memberService;
	@Autowired
	private AgentService agentService;

	/**
	 * 检查用户钱包是否存在,如果存在则直接返回,如果不存在则根据类型创建钱包信息,当不传name时,则根据Id寻找对应的商户/用户/代理名称
	 * 
	 * @param type
	 *            1-商户钱包,2-用户钱包,3-代理商钱包
	 * @param id
	 *            商户Id/用户Id/代理商Id
	 * @param name
	 *            商户名称/用户名称/代理商名称
	 * @return Map中datas(Key)-钱包实体
	 */
	public Map<String, Object> checkWallet(int type, String id, String name) {
		Map<String, Object> params = new HashMap<>();
		Class walletEntity = null;
		if (type > 0 && StringEmptyUtils.isNotEmpty(id)) {
			switch (type) {
			case 1:
				params.put("merchantId", id);
				// 商户钱包
				walletEntity = MerchantWalletContent.class;
				break;
			case 2:
				params.put("memberId", id);
				// 用户钱包
				walletEntity = MemberWalletContent.class;
				break;
			case 3:
				params.put("agentId", id);
				// 代理商钱包
				walletEntity = AgentWalletContent.class;
				break;
			default:
				return ReturnInfoUtils.errorInfo("检查钱包类型错误,暂未支持[" + type + "]类型!");
			}
			List<Object> reList = baseDao.findByProperty(walletEntity, params, 1, 1);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询钱包信息失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {// 钱包不为空,则直接返回
				return ReturnInfoUtils.successDataInfo(reList.get(0));
			} else {
				return createWallet(type, walletEntity, id);
			}
		}
		return ReturnInfoUtils.errorInfo("检查钱包,请求参数不能为空!");
	}

	/**
	 * 创建钱包
	 * 
	 * @param type
	 *            1-商户钱包,2-用户钱包
	 * @param walletEntity
	 *            实体类
	 * @param id
	 * @return
	 */
	private Map<String, Object> createWallet(int type, Class walletEntity, String id) {
		if (walletEntity == null || StringEmptyUtils.isEmpty(id)) {
			return ReturnInfoUtils.errorInfo("创建钱包信息时,请求参数不能为空!");
		}
		Object wallet = null;
		Map<String, Object> reIdMap = idUtils.createId(walletEntity, "walletId_");
		if (!"1".equals(reIdMap.get(BaseCode.STATUS.toString()))) {
			return reIdMap;
		}
		String serialNo = reIdMap.get(BaseCode.DATAS.toString()) + "";
		Date date = new Date();
		switch (type) {
		case 1:
			// 根据商户Id获取商户名称
			Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(id);
			if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
				return reMerchantMap;
			}
			Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
			wallet = new MerchantWalletContent.Builder(serialNo).merchantId(id).merchantName(merchant.getMerchantName())
					.createBy(merchant.getMerchantName()).createDate(date).build();
			break;
		case 2:
			// 根据用户Id获取用户信息
			Map<String, Object> reMemberMap = memberService.getMemberInfo(id);
			if (!"1".equals(reMemberMap.get(BaseCode.STATUS.toString()))) {
				return reMemberMap;
			}
			Member member = (Member) reMemberMap.get(BaseCode.DATAS.toString());
			wallet = new MemberWalletContent.Builder(serialNo).memberId(id).memberName(member.getMemberName())
					.createBy(member.getMemberName()).createDate(date).build();
			break;
		case 3:
			// 根据代理商Id获取用户信息
			Map<String, Object> reAgentMap = agentService.getAgentInfo(id);
			if (!"1".equals(reAgentMap.get(BaseCode.STATUS.toString()))) {
				return reAgentMap;
			}
			AgentBaseContent agent = (AgentBaseContent) reAgentMap.get(BaseCode.DATAS.toString());
			wallet = new AgentWalletContent.Builder(serialNo).agentId(id).agentName(agent.getAgentName())
					.createBy(agent.getAgentName()).createDate(date).build();
			break;
		default:
			break;
		}
		if (!baseDao.add(wallet)) {
			return ReturnInfoUtils.errorInfo("保存钱包信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successDataInfo(wallet);
	}

	/**
	 * 通用商户校验钱包日志参数
	 * 
	 * @param datasMap
	 * @return
	 */
	public static Map<String, Object> checkMerchantWalletLogInfo(Map<String, Object> datasMap) {
		List<String> noNullKeys = new ArrayList<>();
		noNullKeys.add("walletId");
		// noNullKeys.add("serialName");
		noNullKeys.add("balance");
		noNullKeys.add("type");
		noNullKeys.add("status");
		noNullKeys.add("amount");
		noNullKeys.add("flag");
		noNullKeys.add("targetWalletId");
		noNullKeys.add("targetName");
		JSONArray jsonArr = new JSONArray();
		jsonArr.add(datasMap);
		return CheckDatasUtil.checkData(jsonArr, noNullKeys);
	}

	public static void main(String[] args) {
		String accessToken = "Ym_tw0j1xxmiYgR8AkKD72841fLV5bcP3Bb0FiYQJKQAwUeXkXVbBfohfoTV8Oe6sbh_lrm3B1KS5eXC1Ay0m8bkrwpcoEI7vin9iOIbD4nV0fYzpQ2Lihx2tt2ZZwiW2riN";
		Map<String, Object> params2 = new HashMap<>();
		params2.put("version", "1.0");
		params2.put("merchantNo", YmMallConfig.ID_CARD_CERTIFICATION_MERCHANT_NO);
		params2.put("businessCode", "PT03");
		JSONObject bizContent = new JSONObject();
		bizContent.put("user_ID", "44098219861015566X");
		bizContent.put("user_name", "林金英");
		bizContent.put("bank_mobile", "13533288817");
		params2.put("bizContent", bizContent);
		params2.put("timestamp", System.currentTimeMillis());
		params2 = new MapSortUtils().sortMap(params2);
		String str2 = YmMallConfig.APPKEY + accessToken + params2;
		String clientSign = null;
		try {
			clientSign = MD5.getMD5(str2.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		params2.put("clientSign", clientSign);
		System.out.println("---->>" + YmHttpUtil.HttpPost(YmMallConfig.REAL_URL, params2));
	}
}
