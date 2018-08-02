package org.silver.shop.api.system.manual;

import java.util.List;
import java.util.Map;

import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.organization.Merchant;

import net.sf.json.JSONArray;

public interface MpayService {

	/**
	 * 根据订单Id发起订单备案
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param customsMap
	 *            海关口岸信息
	 * @param orderNoPack
	 *            订单Id信息
	 * @return
	 */
	public Object sendMorderRecord(String merchantId, Map<String, Object> customsMap, String orderNoPack,
			String merchantName);

	/**
	 * 异步回调订单备案信息
	 * 
	 * @param datasMap
	 * @return
	 */
	public Map<String, Object> updateOrderRecordInfo(Map<String, Object> datasMap);

	/**
	 * 根据订单日期与批次号下载订单信息
	 * 
	 * @param merchantId
	 *            商户Id
	 * @param merchantName
	 *            商户名称
	 * @param topPath
	 *            下载路径头部
	 * @param serialNo
	 * @param date
	 * @return Map
	 */
	public Map<String, Object> downOrderExcelByDateSerialNo(String merchantId, String merchantName, String filePath,
			String date, String serialNo);

	/**
	 * 准备开始推送订单备案
	 * 
	 * @param dataList
	 *            订单信息
	 * @param errorList
	 *            错误信息
	 * @param customsMap
	 *            海关信息
	 */
	public void startSendOrderRecord(JSONArray dataList, List<Map<String, Object>> errorList,
			Map<String, Object> customsMap, Map<String, Object> paramsMap);

	/**
	 * 计算商户钱包余额是否足够推送此次手工订单
	 * 
	 * @param jsonList
	 *            订单Id
	 * @param merchant
	 *            商户信息实体类
	 * @param merchantFeeId
	 *            商户口岸费率流水Id
	 * @param errorList
	 *            错误信息集合
	 * @return Map
	 */
	public Map<String, Object> computingCostsManualOrder(JSONArray jsonList, Merchant merchant, String merchantFeeId,
			Map<String, Object> customsMap, List<Map<String, Object>> errorList);

	/**
	 * 发起手工订单备案
	 * 
	 * @param customsMap
	 *            海关信息
	 * @param orderSubList
	 *            备案商品信息List
	 * @param tok
	 *            服务端tok
	 * @param order
	 *            订单信息
	 * @return
	 */
	public Map<String, Object> sendOrder(Map<String, Object> customsMap, List<MorderSub> orderSubList, String tok,
			Morder order);

	/**
	 * 更新订单返回信息、并且将订单状态修改为申报中
	 * 
	 * @param orderNo
	 *            订单编号
	 * @param reOrderMessageID
	 *            网关返回流水Id
	 * @param customsMap
	 *            海关口岸信息包
	 * @return Map
	 */
	public Map<String, Object> updateOrderInfo(String orderNo, String reOrderMessageID, Map<String, Object> customsMap);

	/**
	 * 更新订单推送错误状态
	 * 
	 * @param orderNo
	 *            订单编号
	 * @return Map
	 */
	public Map<String, Object> updateOrderErrorStatus(String orderNo);

	/**
	 * 添加代理商钱包流水日志
	 * 
	 * @param agentWallet
	 *            代理商钱包实体信息类
	 * @param datasMap
	 *            参数:walletId(钱包Id)、balance(余额)、type(类型:1-佣金、2-充值、3-提现、4-缴费、5-购物)、status(
	 *            状态：success-交易成功、fail-交易失败)、amount(金额)、flag(进出帐标识：in-进账,out-出账)、targetWalletId(目标钱包Id)、targetName(目标名称)
	 * 
	 * @return Map
	 */
	public Map<String, Object> addAgentWalletLog(Map<String, Object> datas);

	/**
	 * 针对第三方订单信息，进行回调处理
	 * 
	 * @param order
	 *            订单信息实体类
	 * @param status
	 *            状态码
	 * @param reMsg
	 *            返回信息
	 * @return Map
	 */
	public Map<String, Object> reThirdPartyOrderInfo(Morder order, String status, String reMsg);

}
