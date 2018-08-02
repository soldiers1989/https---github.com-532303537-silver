package org.silver.shop.quartz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.shop.api.system.manual.MpayService;
import org.silver.shop.component.TimerResendPayment;
import org.silver.shop.dao.system.commerce.OrderDao;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.ThirdPartyOrderCallBack;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 定时任务：第三方订单回传
 */
public class ThirdPartyOrderCallBackQtz {

	@Autowired
	private OrderDao orderDao;
	@Autowired
	private PaymentDao paymentDao;
	@Autowired
	private MpayService mpayService;

	private static Logger logger = LogManager.getLogger(TimerResendPayment.class);

	/**
	 * 扫描第三方电商平台订单返回信息表中的,次数10次以下与返回状态为FALSE的支付单信息
	 */
	private void resendThirdPartyOrderQtzJob() {
		try {
			System.out.println("--扫描订单返回次数10次以下并且状态为failure--");
			Map<String, Object> params = new HashMap<>();
			params.put("resendStatus", "failure");
			List<ThirdPartyOrderCallBack> tpOrderCallBackList = paymentDao
					.getReplyThirdPartyFailInfo(ThirdPartyOrderCallBack.class, params, 1, 200);
			if (tpOrderCallBackList != null && !tpOrderCallBackList.isEmpty()) {
				for (ThirdPartyOrderCallBack entity : tpOrderCallBackList) {
					// 根据交易流水号/订单Id/商户Id查询支付单信息
					params.clear();
					params.put("order_id", entity.getOrderId());
					params.put("merchant_no", entity.getMerchantId());
					List<Morder> reOrderList = orderDao.findByProperty(Morder.class, params, 0, 0);
					if (reOrderList != null && !reOrderList.isEmpty()) {
						String status;
						Morder manualOrder = reOrderList.get(0);
						// 申报状态：1-未申报,2-申报中,3-申报成功、4-申报失败、10-申报中(待系统处理)
						if (manualOrder.getOrder_record_status() == 3) {
							status = "1";
						} else {
							status = "-1";
						}
						String reNote;
						int num = appearNumber(manualOrder.getOrder_re_note(), "#");
						reNote = manualOrder.getOrder_re_note();
						if (StringEmptyUtils.isNotEmpty(reNote)) {
							String[] strArr = reNote.split("#");
							reNote = strArr[(num - 1)];
						}
						mpayService.reThirdPartyOrderInfo(manualOrder, status, reNote);
					}
				}
			}
		} catch (Exception e) {
			logger.error("----扫描回调支付单错误-----", e);
		}
	}

	/**
	 * 获取指定字符串出现的次数
	 * 
	 * @param srcText
	 *            源字符串
	 * @param findText
	 *            要查找的字符串
	 * @return
	 */
	public static int appearNumber(String srcText, String findText) {
		int count = 0;
		Pattern p = Pattern.compile(findText);
		Matcher m = p.matcher(srcText);
		while (m.find()) {
			count++;
		}
		return count;
	}
}
