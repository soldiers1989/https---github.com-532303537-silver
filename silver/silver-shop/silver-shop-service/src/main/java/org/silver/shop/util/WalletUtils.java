package org.silver.shop.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.dao.BaseDao;
import org.silver.shop.model.system.organization.AgentBaseContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.AgentWalletContent;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.shop.model.system.tenant.MerchantWalletContent;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.common.json.JSONObject;

/**
 * 钱包操作通用工具类
 */
@Component
public class WalletUtils {

	@Autowired
	private BaseDao baseDao;
	@Autowired
	private IdUtils idUtils;

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
	public Map<String, Object>  checkWallet(int type, String id, String name) {
		Map<String, Object> params = new HashMap<>();
		Class walletEntity = null;
		if (type > 0 && StringEmptyUtils.isNotEmpty(id) ) {
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
				return createWallet(type, walletEntity, id, name);
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
	 * @param name
	 * @return
	 */
	private Map<String, Object> createWallet(int type, Class walletEntity, String id, String name) {
		if (walletEntity == null || StringEmptyUtils.isEmpty(id) ) {
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
			wallet = new MerchantWalletContent.Builder(serialNo).merchantId(id).merchantName(name).createBy(name)
					.createDate(date).build();
			break;
		case 2:
			wallet = new MemberWalletContent.Builder(serialNo).memberId(id).memberName(name).createBy(name)
					.createDate(date).build();
			break;
		case 3:
			wallet = new AgentWalletContent.Builder(serialNo).agentId(id).agentName(name).createBy(name)
					.createDate(date).build();
			break;
		default:
			break;
		}
		if (!baseDao.add(wallet)) {
			return ReturnInfoUtils.errorInfo("保存钱包信息失败,服务器繁忙!");
		}
		return ReturnInfoUtils.successDataInfo(wallet);
	}

	public static Map<String, Object> checkWalletInfo(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return ReturnInfoUtils.errorInfo("校验钱包信息时,请求参数不能为空!");
		}
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			String key = String.valueOf(entry.getKey());
			switch (key) {
			case "walletId":
				if(StringEmptyUtils.isEmpty(entry.getValue())){
					return ReturnInfoUtils.errorInfo("添加钱包日志,钱包Id不能为空!");
				}
				break;
			case "serialName":
				if(StringEmptyUtils.isEmpty(entry.getValue())){
					return ReturnInfoUtils.errorInfo("添加钱包日志,日志名称不能为空!");
				}
				break;
			case "balance":
				if(StringEmptyUtils.isEmpty(entry.getValue())){
					return ReturnInfoUtils.errorInfo("添加钱包日志,余额不能为空!");
				}
				break;
			case "type":
				if(StringEmptyUtils.isEmpty(entry.getValue())){
					return ReturnInfoUtils.errorInfo("添加钱包日志,交易类型不能为空!");
				}
				break;
			case "status":
				if(StringEmptyUtils.isEmpty(entry.getValue())){
					return ReturnInfoUtils.errorInfo("添加钱包日志,交易状态不能为空!");
				}
				break;
			case "targetWalletId":
				if(StringEmptyUtils.isEmpty(entry.getValue())){
					return ReturnInfoUtils.errorInfo("添加钱包日志,目标钱包Id不能为空!");
				}
				break;
			case "targetName":
				if(StringEmptyUtils.isEmpty(entry.getValue())){
					return ReturnInfoUtils.errorInfo("添加钱包日志,目标名称不能为空!");
				}
				break;
			case "amount":
				if(StringEmptyUtils.isEmpty(entry.getValue())){
					return ReturnInfoUtils.errorInfo("添加钱包日志,交易金额不能为空!");
				}
				break;
			case "flag":
				if(StringEmptyUtils.isEmpty(entry.getValue())){
					return ReturnInfoUtils.errorInfo("添加钱包日志,交易进出帐标识不能为空!");
				}
				break;
			default:
				break;
			}
		}
		return ReturnInfoUtils.successInfo();
	}

	public static void main(String[] args) {
		Map<String, Object> item = new HashMap<>();
		item.put("aa", "bb");
		item.put("1", "2");
		for (Map.Entry<String, Object> entry : item.entrySet()) {
			System.out.println(", Value = " + entry.getValue());
		}
	}
}
