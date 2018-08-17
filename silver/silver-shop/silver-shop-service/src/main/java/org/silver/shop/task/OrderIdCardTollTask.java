package org.silver.shop.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silver.common.BaseCode;
import org.silver.shop.dao.system.manual.MorderDao;
import org.silver.shop.impl.system.manual.MpayServiceImpl;
import org.silver.shop.model.common.base.IdCard;
import org.silver.shop.model.system.manual.Morder;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SplitListUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderIdCardTollTask implements Callable<Object> {

	private static Logger logger = LogManager.getLogger(MpayServiceImpl.class);
	/**
	 * 驼峰命名：商户Id
	 */
	private static final String MERCHANT_ID = "merchantId";
	/**
	 * 下划线命名：订单Id
	 */
	private static final String ORDER_ID = "order_id";

	private List datasList;//
	private List<String> idCardList;// 收费的订单id身份证集合
	private List<String> idCardFreeList;// 免费的订单id身份证集合
	private MorderDao morderDao;
	private ConcurrentHashMap<String, Object> cacheIdcardMap;// 身份证是否出现过的缓存

	public OrderIdCardTollTask(List<Map<String, Object>> datasList, List<String> idCardList,
			List<String> idCardFreeList, MorderDao morderDao, ConcurrentHashMap<String, Object> cacheIdcardMap) {
		this.datasList = datasList;
		this.idCardList = idCardList;
		this.idCardFreeList = idCardFreeList;
		this.morderDao = morderDao;
		this.cacheIdcardMap = cacheIdcardMap;
	}

	@Override
	public Object call() {
		try {
			Map<String, Object> params = null;
			for (int i = 0; i < datasList.size(); i++) {
				Map<String, Object> map = (Map<String, Object>) datasList.get(i);
				params = new HashMap<>();
				params.put(ORDER_ID, map.get("orderNo") + "");
				List<Morder> reList = morderDao.findByProperty(Morder.class, params, 1, 1);
				if (reList != null && !reList.isEmpty()) {
					Morder order = reList.get(0);
					if (cacheIdcardMap
							.containsKey(order.getOrderDocName() + "_" + order.getOrderDocId())) {
						// 当同一批订单，相同姓名与身份证号码添加到免费集合中
						idCardFreeList.add(order.getOrder_id());
					} else {
						// 身份证实名认证标识：0-未实名、1-已实名、2-认证失败
						params.clear();
						params.put(MERCHANT_ID, order.getMerchant_no());
						params.put("name", order.getOrderDocName());
						params.put("idNumber", order.getOrderDocId());
						List<IdCard> reIdList = morderDao.findByProperty(IdCard.class, params, 1, 1);
						if (reIdList == null) {
							logger.error(order.getOrder_id() + "获取订单中实名认证不通过的订单时--查询实名库失败--");
						} else if (reIdList != null && !reIdList.isEmpty()) {// 实名库已存在身份证信息
							IdCard idCard = reIdList.get(0);
							// 只要是实名库的认证状态是认证失败,则都需要进行实名计费
							if ("failure".equals(idCard.getStatus())) {
								idCardList.add(order.getOrder_id());
							} else {
								idCardFreeList.add(order.getOrder_id());
							}
						} else {// 当实名库中没有该商户的姓名+身份证号码，则进行计费统计
							idCardList.add(order.getOrder_id());
						}
						// 添加进缓存
						cacheIdcardMap.put(order.getOrderDocName() + "_" + order.getOrderDocId(),
								order.getOrder_id());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
