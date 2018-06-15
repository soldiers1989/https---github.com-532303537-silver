package org.silver.shop.quartz;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.functors.SwitchClosure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.api.system.AccessTokenService;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.api.system.cross.YsPayReceiveService;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.config.YmMallConfig;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.impl.system.manual.ManualOrderServiceImpl;
import org.silver.shop.model.system.manual.Appkey;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.model.system.manual.Mpay;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantRecordInfo;
import org.silver.shop.util.MerchantUtils;
import org.silver.shop.util.RedisInfoUtils;
import org.silver.util.DateUtil;
import org.silver.util.RandomUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerialNoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.justep.baas.data.Row;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

/**
 * 定时任务,扫描商户自助申报的订单,进行订单申报
 */
public class PushOrderRecordQtz {
	/**
	 * 口岸
	 */
	private static final String E_PORT = "eport";

	/**
	 * 检验检疫机构代码
	 */
	private static final String CIQ_ORG_CODE = "ciqOrgCode";

	/**
	 * 主管海关代码
	 */
	private static final String CUSTOMS_CODE = "customsCode";
	/**
	 * 下划线命名:商户Id
	 */
	private static final String MERCHANT_NO = "merchant_no";

	/**
	 * 计数器
	 */
	private static AtomicInteger counter = new AtomicInteger(0);

	private static Logger logger = LogManager.getLogger(Object.class);

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private MerchantUtils merchantUtils;
	@Autowired
	private MpayService mpayService;
	@Autowired
	private AccessTokenService accessTokenService;

	public void pushOrderRecordJob() {
		if(counter.get() % 10 == 0){
			System.out.println("---扫描自助申报订单---");
		}
		Map<String, Object> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, -3);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		// 设置为24小时制
		 params.put("startTime", calendar.getTime());
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		params.put("endTime", calendar.getTime());
		// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
		params.put("order_record_status", 10);
		// 网关接收状态： 0-未发起,1-已发起,2-接收成功,3-接收失败
		params.put("status", 1);
		try {
			int page = 1;
			int size = 300;
			List<Morder> reOrderList = orderDao.findByPropertyLike(Morder.class, params, null, page, size);
			while (reOrderList != null && !reOrderList.isEmpty()) {
				if (page > 1) {
					reOrderList = orderDao.findByPropertyLike(Morder.class, params, null, page, size);
				}
				if (reOrderList != null && !reOrderList.isEmpty()) {
					for (Morder order : reOrderList) {
						String merchantId = order.getMerchant_no();
						params.clear();
						params.put(MERCHANT_NO, merchantId);
						params.put("order_id", order.getOrder_id());
						List<MorderSub> reOrderGoodsList = orderDao.findByProperty(MorderSub.class, params, 0, 0);
						if (reOrderGoodsList == null || reOrderGoodsList.isEmpty()) {
							logger.error(order.getOrder_id() + "<-推送失败,订单商品信息不能为空!");
						} else {
							sendOrderRecord(order, reOrderGoodsList);
						}
						//线程休眠0.2秒,防止HTTP请求过快出错
						Thread.sleep(200);
						System.out.println("---发送结束->");
					}
				}
				page++;
			}
			counter.getAndIncrement();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("--定时扫描待发起备案的订单信息错误--", e);
		}
	}

	private void sendOrderRecord(Morder order, List<MorderSub> reOrderGoodsList) {
		String appkey = "";
		String appSecret = "";
		String merchantId = order.getMerchant_no();
		// 获取商户信息
		Map<String, Object> reMerchantMap = merchantUtils.getMerchantInfo(merchantId);
		if (!"1".equals(reMerchantMap.get(BaseCode.STATUS.toString()))) {
			logger.error(reMerchantMap.get(BaseCode.MSG.toString()));
		}
		Merchant merchant = (Merchant) reMerchantMap.get(BaseCode.DATAS.toString());
		int thirdPartyFlag = merchant.getThirdPartyFlag();
		// 当商户标识为第三方平台商户时,则使用第三方appkey
		if (thirdPartyFlag == 2) {
			Map<String, Object> reAppkeyMap = merchantUtils.getMerchantAppkey(merchantId);
			if (!"1".equals(reAppkeyMap.get(BaseCode.STATUS.toString()))) {
				logger.error(reAppkeyMap.get(BaseCode.MSG.toString()));
			} else {
				Appkey appkeyInfo = (Appkey) reAppkeyMap.get(BaseCode.DATAS.toString());
				appkey = appkeyInfo.getApp_key();
				appSecret = appkeyInfo.getApp_secret();
			}
		} else {
			// 当不是第三方时则使用银盟商城appkey
			appkey = YmMallConfig.APPKEY;
			appSecret = YmMallConfig.APPSECRET;
		}
		// 请求获取tok
		Map<String, Object> reTokMap = accessTokenService.getRedisToks(appkey, appSecret);
		if (!"1".equals(reTokMap.get(BaseCode.STATUS.toString()))) {
			logger.error(reTokMap.get(BaseCode.MSG.toString()));
		}
		String tok = reTokMap.get(BaseCode.DATAS.toString()) + "";
		Map<String, Object> customsMap = new HashMap<>();
		customsMap.put(E_PORT, order.getEport());
		customsMap.put(CIQ_ORG_CODE, order.getCiqOrgCode());
		customsMap.put(CUSTOMS_CODE, order.getCustomsCode());
		customsMap.put("appkey", appkey);
		Map<String, Object> reOrderMap = mpayService.sendOrder(customsMap, reOrderGoodsList, tok, order);
		if (!"1".equals(reOrderMap.get(BaseCode.STATUS.toString()) + "")) {
			logger.error(order.getOrder_id() + "--自助申报订单,推送失败-->" + reOrderMap.get(BaseCode.MSG.toString()));
		} else {
			System.out.println("----发送成功>>>");
			String reOrderMessageID = reOrderMap.get("messageID") + "";
			Map<String, Object> reUpdateMap = mpayService.updateOrderInfo(order.getOrder_id(), reOrderMessageID,
					customsMap);
			if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()) + "")) {
				logger.error(
						order.getOrder_id() + "--自助申报订单推送成功后,更新状态失败-->" + reUpdateMap.get(BaseCode.MSG.toString()));
			}
		}
		
	}
}
