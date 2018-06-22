package org.silver.shop.controller.system.cross;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.util.ReturnInfoUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

/**
 * 银盛代付
 */
@Component
public class DaiFuPay {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	/**
	 * 
	 * @param notify_url
	 *            代付异步通知URL
	 * @param out_batch_no
	 *            交易批次号 系统唯一 格式：F+15位唯一流水，建议格式：F+YYYYMMDD+XXXXXXX ,
	 * @param out_trade_no
	 *            订单号 系统唯一 (异步回调时可能不存在)
	 * @param total_amount
	 *            代付总金额
	 * @param subject
	 *            交易描述
	 * @param bank_province
	 *            开户行省份 广东省
	 * @param bank_city
	 *            开户行市 广州市
	 * @param bank_name
	 *            开户行名称 中国建设银行(骏景花园分理处)
	 * @param bank_account_no
	 *            银行卡号 622xxxx9293913
	 * @param bank_account_name
	 *            银行卡账户名称 杨**
	 * @param bank_account_type
	 *            银行卡账户类型 私人(personal) 对公(corporate)
	 * @param bank_card_type
	 *            银行卡类别 借记卡(debit) 信用卡(credit) 单位结算卡(unit)
	 * @return
	 */

	public Map<String, Object> dfTrade(String notify_url, String out_batch_no, String out_trade_no, double total_amount,
			String subject, String bank_province, String bank_city, String bank_name, String bank_account_no,
			String bank_account_name, String bank_account_type, String bank_card_type) {
		Map params = new HashMap<>();
		params.put("method", "ysepay.df.batch.normal.accept");
		params.put("version", "3.0");
		params.put("partner_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		params.put("timestamp", sdf.format(new Date()));
		params.put("charset", "utf-8");
		params.put("sign_type", "RSA");
		params.put("notify_url", notify_url);

		JSONObject biz_content = new JSONObject();
		// String out_batch_no="F"+sdf2.format(new
		// Date())+((System.currentTimeMillis()+"").substring(6, 13));
		biz_content.put("out_batch_no", out_batch_no);// 格式：F+15位唯一流水，建议格式：F+YYYYMMDD+XXXXXXX
														// ,
		biz_content.put("business_code", "01000010");
		biz_content.put("currency", "CNY");
		biz_content.put("total_num", 1);// 总笔数
		biz_content.put("total_amount", total_amount);
		List<Object> list = new ArrayList<>();
		JSONObject detail_data = new JSONObject();
		// String out_trade_no=sdf2.format(new
		// Date())+System.currentTimeMillis();
		detail_data.put("out_trade_no", out_trade_no);// 订单号 yyyyMMdd
		detail_data.put("amount", total_amount);
		detail_data.put("subject", subject);
		detail_data.put("bank_province", bank_province);// 广东省
		detail_data.put("bank_city", bank_city);// 广州市
		detail_data.put("bank_name", bank_name);// 中国建设银行(骏景花园分理处)
		detail_data.put("bank_account_no", bank_account_no);
		detail_data.put("bank_account_name", bank_account_name);
		detail_data.put("bank_account_type", bank_account_type);// personal
		detail_data.put("bank_card_type", bank_card_type);// debit credit

		list.add(detail_data);
		biz_content.put("detail_data", list);
		params.put("biz_content", biz_content.toString());
		String realpath = DaiFuPay.class.getClassLoader().getResource("").getPath() ;
		params.put("sign", ApipaySubmit.signWithFilepath(realpath, params));
		try {
			String result = YmHttpUtil.HttpPost("https://batchdf.ysepay.com/gateway.do", params);
			if (result != null) {
				JSONObject j = JSONObject.fromObject(result);
				String trade_status = JSONObject.fromObject(j.get("ysepay_df_batch_normal_accept_response"))
						.get("trade_status") + "";
				if (trade_status.contains("BATCH_ACCEPT_SUCCESS")) {
					return ReturnInfoUtils.successInfo();
				}
			}
			System.out.println("----result>>>"+result);
			return ReturnInfoUtils.errorInfo("网络异常，代付受理失败!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ReturnInfoUtils.errorInfo("数据异常，代付受理失败!");
	}

	public Map<String, Object> dfCallBack() {
		String ss = "{notify_time=2018-06-14 15:58:21, batch_no=B2021806140496517246, payee_fee=0.0, fee=0.0, sign=rmJYyBFQk7MdtD/6b4KBWZ6EcFWroMXyu5frHT2AnwQEnFr4aA/VrKBNmWUQU0k7xfuZAfOuLarJSj4+icL13yszvXqW1SHFHbopcBSm6GUkOIMXObh2TfhPYljrb6ABhxITszGDJv6cWgxIU42AvEk5X+chXPP+6uV4hnTfItA=, out_batch_no=F201806142833211, batch_account_date=20180614, notify_type=ysepay.df.batch.notify, partner_fee=0.0, total_amount=15.00, trade_status=BATCH_TRADE_FAILURE, total_num=1, sign_type=RSA, trade_status_description=账户：0000400013472536余额不足,出账失败,用户号：yinmeng1116, payer_fee=0.0, success_total_amount=0.00, success_total_num=0}";

		return null;
	}

	public static void main(String[] args) {

		// Logger log = LoggerFactory.getLogger(LogsTest.class);
		// log.error("商户:"+123+"交易金额:"+123 +"一般进出账" + new
		// Date()+"出错"+"【"+123456+"】");
		Map params = new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
		params.put("method", "ysepay.df.batch.normal.accept");
		params.put("version", "3.0");
		params.put("partner_id", DirectPayConfig.PLATFORM_PARTNER_NO);
		params.put("timestamp", sdf.format(new Date()));
		params.put("charset", "utf-8");
		params.put("sign_type", "RSA");
		params.put("notify_url", "http://ym.191ec.com:8280/silver-web-ezpay/ympay/callback");

		JSONObject biz_content = new JSONObject();

		String out_batch_no = "F" + sdf2.format(new Date()) + ((System.currentTimeMillis() + "").substring(6, 13));
		biz_content.put("out_batch_no", out_batch_no);// 格式：F+15位唯一流水，建议格式：F+YYYYMMDD+XXXXXXX
														// ,
		// System.out.println(out_batch_no);
		biz_content.put("business_code", "01000010");
		biz_content.put("currency", "CNY");
		biz_content.put("total_num", 1);
		biz_content.put("total_amount", 15);
		List<Object> list = new ArrayList<>();
		JSONObject detail_data = new JSONObject();

		String out_trade_no = sdf2.format(new Date()) + System.currentTimeMillis();
		detail_data.put("out_trade_no", out_trade_no);// 订单号
		// System.out.println(out_trade_no);
		detail_data.put("amount", 15);
		detail_data.put("subject", "订单说明");
		detail_data.put("bank_name", "中国建设银行(骏景花园分理处)");
		// detail_data.put("bank_name", "中国工商银行股份有限公司广州科技园支行");
		detail_data.put("bank_province", "广东省");
		detail_data.put("bank_city", "广州市");

		detail_data.put("bank_account_no", "6217003320040422662");
		// detail_data.put("bank_account_no", "3602062709200219758");
		detail_data.put("bank_account_name", "杨汕");
		detail_data.put("bank_account_type", "personal");
		// detail_data.put("bank_card_type", "credit");
		detail_data.put("bank_card_type", "debit");

		list.add(detail_data);
		biz_content.put("detail_data", list);
		params.put("biz_content", biz_content.toString());
		String realpath = DaiFuPay.class.getClassLoader().getResource("").getPath() + "pay/";
		// System.out.println(realpath);
		params.put("sign", ApipaySubmit.signWithFilepath(realpath, params));
		try {
			// String result
			// =YmHttpUtil.HttpPost("https://batchdf.ysepay.com/gateway.do",
			// params);
			// System.out.println(result);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
