package org.silver.shop.impl.system.cross;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.cross.PaymentService;
import org.silver.shop.dao.system.cross.PaymentDao;
import org.silver.shop.model.system.cross.PaymentContent;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = PaymentService.class)
public class PaytemServiceImpl implements PaymentService {

	@Autowired
	private PaymentDao paymentDao;

	@Override
	public Map<String, Object> updatePaymentStatus(Map<String, Object> datasMap) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置时间格式
		String defaultDate = sdf.format(date); // 格式化当前时间
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("reSerialNo", datasMap.get("messageID") + "");
		paramMap.put("entPayNo", datasMap.get("entPayNo") + "");
		String reMsg = datasMap.get("errMsg") + "";
		List<Object> reList = paymentDao.findByProperty(PaymentContent.class, paramMap, 1, 1);
		if (reList != null && reList.size() > 0) {
			PaymentContent payment = (PaymentContent) reList.get(0);
			String status = datasMap.get("status") + "";
			String note = payment.getReNote();
			if ("null".equals(note) || note == null) {
				note = "";
			}
			if ("1".equals(status)) {
				// 支付单备案状态修改为成功
				payment.setPayRecord(2);
			} else {
				payment.setPayRecord(3);
			}
			payment.setReNote(note + defaultDate + ":" + reMsg + ";");
			payment.setUpdateDate(date);
			if (!paymentDao.update(payment)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), "异步更新支付单备案信息错误!");
				return paramMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

}
