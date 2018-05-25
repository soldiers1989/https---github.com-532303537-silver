package org.silver.shop.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.dao.BaseDao;
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
	 * 检查用户钱包是否存在,如果存在则直接返回,如果不存在则根据类型创建钱包信息
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
		Class entity = null;
		if (type > 0 && StringEmptyUtils.isNotEmpty(id) && StringEmptyUtils.isNotEmpty(name)) {
			switch (type) {
			case 1:
				params.put("merchantId", id);
				// 商户钱包
				entity = MerchantWalletContent.class;
				break;
			case 2:
				params.put("memberId", id);
				// 用户钱包
				entity = MemberWalletContent.class;
				break;
			case 3:
				params.put("agentId", id);
				// 代理商钱包
				entity = AgentWalletContent.class;
				break;
			default:
				return ReturnInfoUtils.errorInfo("检查钱包类型错误,暂未支持[" + type + "]类型!");
			}
			List<Object> reList = baseDao.findByProperty(entity, params, 1, 1);
			if (reList == null) {
				return ReturnInfoUtils.errorInfo("查询钱包信息失败,服务器繁忙!");
			} else if (!reList.isEmpty()) {// 钱包不为空,则直接返回
				return ReturnInfoUtils.successDataInfo(reList.get(0));
			} else {
				return createWallet(type, entity, id, name);
			}
		}
		return ReturnInfoUtils.errorInfo("检查钱包,请求参数不能为空!");
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
	private Map<String, Object> createWallet(int type, Class entity, String id, String name) {
		if (entity == null || StringEmptyUtils.isEmpty(id) || StringEmptyUtils.isEmpty(name)) {
			return ReturnInfoUtils.errorInfo("创建钱包信息时,请求参数不能为空!");
		}
		Object wallet = null;
		Map<String, Object> reIdMap = idUtils.createId(entity, "walletId_");
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
}
