package org.silver.shop.impl.system.log;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.IdCardCertificationlogsService;
import org.silver.shop.api.system.tenant.MerchantIdCardCostService;
import org.silver.shop.dao.system.log.IdCardCertificationlogsDao;
import org.silver.shop.model.common.base.IdCard;
import org.silver.shop.model.system.log.IdCardCertificationLog;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.organization.Merchant;
import org.silver.shop.model.system.tenant.MerchantIdCardCostContent;
import org.silver.shop.util.SearchUtils;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = IdCardCertificationlogsService.class)
public class IdCardCertificationlogsServiceImpl implements IdCardCertificationlogsService {

	@Autowired
	private IdCardCertificationlogsDao ikdCardCertificationlogsDao;
	@Autowired
	private MerchantIdCardCostService merchantIdCardCostService;

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getlogsInfo(Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> reMap = SearchUtils.universalIdCardCertificationlogsSearch(datasMap);
		if (!"1".equals(reMap.get(BaseCode.STATUS.toString()))) {
			return reMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reMap.get("param");
		paramMap.put("deleteFlag", 0);
		List<IdCardCertificationLog> idCardlist = ikdCardCertificationlogsDao
				.findByPropertyLike(IdCardCertificationLog.class, paramMap, null, page, size);
		long count = ikdCardCertificationlogsDao.findByPropertyLikeCount(IdCardCertificationLog.class, paramMap, null);
		if (idCardlist == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!idCardlist.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(idCardlist, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

	@Override
	public Object tempUpdate() {
		Map<String, Object> item = new HashMap<>();
		// MerchantId_00069 一合相
		// MerchantId_00079 海岛号跨境电子商务科技有限公司
		// MerchantId_00078 中山市澳淘商贸有限公司
		// MerchantId_00076 上海锋赞实业有限公司
		// MerchantId_00063 广州颜悦化妆品有限公司
		// MerchantId_00057 深圳市前海爱库
		// MerchantId_00074 深圳市前海九米信息技术有限公司
		// MerchantId_00089 深圳市承和润文化传播股份有限公司
		item.put("merchantId", "MerchantId_00089");
		List<Merchant> merchantlist = ikdCardCertificationlogsDao.findByProperty(Merchant.class, item, 0, 0);
		Map<String, Object> params = null;
		Map<String, Object> cacheMap = null;
		for (Merchant merchant : merchantlist) {
			params = new HashMap<>();
			String startTime = "2018-08-01 00:00:05";
			String endTime = "2018-08-31 23:00:00";
			params.put("startTime", DateUtil.parseDate(startTime, "yyyy-MM-dd HH:mm:ss"));
			params.put("endTime", DateUtil.parseDate(endTime, "yyyy-MM-dd HH:mm:ss"));
			params.put("merchant_no", merchant.getMerchantId());
			cacheMap = new HashMap<>();
			System.out.println(merchant.getMerchantId() + "========================");
			int page = 1;
			int size = 300;
			List<Morder> orderlist = ikdCardCertificationlogsDao.findByPropertyLike(Morder.class, params, null, page,
					size);
			while (orderlist != null && !orderlist.isEmpty()) {
				if (page > 1) {
					orderlist = ikdCardCertificationlogsDao.findByPropertyLike(Morder.class, params, null, page, size);
				}
				System.out.println("++++++++++++++++++++++++++++");
				if (orderlist != null && !orderlist.isEmpty()) {
					Map<String, Object> reCostMap = merchantIdCardCostService
							.getIdCardCostInfo(merchant.getMerchantId());
					if (!"1".equals(reCostMap.get(BaseCode.STATUS.toString()))) {
						return reCostMap;
					}
					MerchantIdCardCostContent merchantCost = (MerchantIdCardCostContent) reCostMap
							.get(BaseCode.DATAS.toString());

					for (Morder order : orderlist) {
						if (cacheMap.containsKey(
								order.getMerchant_no() + "_" + order.getOrderDocName() + "_" + order.getOrderDocId())) {
							addIdCardCertificationLog(order.getMerchant_no(), merchant.getMerchantName(),
									order.getOrder_id(), order.getOrderDocName(), order.getOrderDocId().trim(), 0, 2,
									"一次操作,重复身份证,不计费！", order.getCreate_date());
						} else {
							Map<String, Object> params2 = new HashMap<>();
							params2.put("merchantId", order.getMerchant_no());
							params2.put("name", order.getOrderDocName().trim());
							params2.put("idNumber", order.getOrderDocId().trim());
							List<IdCard> reIdList = ikdCardCertificationlogsDao.findByProperty(IdCard.class, params2, 1,
									1);
							if (reIdList != null && !reIdList.isEmpty()) {
								IdCard idCard = reIdList.get(0);
								// 认证状态:success-成功;failure-失败;wait-待验证
								if ("failure".equals(idCard.getStatus())) {
									addIdCardCertificationLog(order.getMerchant_no(), merchant.getMerchantName(),
											order.getOrder_id(), order.getOrderDocName(), order.getOrderDocId().trim(),
											merchantCost.getPlatformCost(), 1, "实名验证手续费", order.getCreate_date());
								} else {
									String str = idCard.getCreateDate() + "";
									String reCreateDate = str.substring(0, 10);
									if (reCreateDate.equals(startTime.substring(0, 10))) {
										addIdCardCertificationLog(order.getMerchant_no(), merchant.getMerchantName(),
												order.getOrder_id(), order.getOrderDocName(),
												order.getOrderDocId().trim(), merchantCost.getPlatformCost(), 1, "实名验证手续费",
												order.getCreate_date());
									} else {
										addIdCardCertificationLog(order.getMerchant_no(), merchant.getMerchantName(),
												order.getOrder_id(), order.getOrderDocName(),
												order.getOrderDocId().trim(), 0, 2, "姓名与身份证号码已验证通过,不进行二次验证!",
												order.getCreate_date());
									}
								}
							} else {
								IdCard idCard = new IdCard();
								idCard.setMerchantId(merchant.getMerchantId());
								idCard.setMerchantName(merchant.getMerchantName());
								idCard.setName(order.getOrderDocName().trim());
								idCard.setIdNumber(order.getOrderDocId().trim());
								idCard.setType(4);
								idCard.setStatus("wait");
								idCard.setCreateDate(order.getCreate_date());
								idCard.setCreateBy("system");
								idCard.setRemrak("临时补充数据");
								ikdCardCertificationlogsDao.add(idCard);
								addIdCardCertificationLog(order.getMerchant_no(), merchant.getMerchantName(),
										order.getOrder_id(), order.getOrderDocName(), order.getOrderDocId().trim(), merchantCost.getPlatformCost(),
										1, "实名验证手续费", order.getCreate_date());
							}
							cacheMap.put(order.getMerchant_no() + "_" + order.getOrderDocName() + "_"
									+ order.getOrderDocId(), order.getOrder_id());
						}
						System.out.println(order.getOrder_id() + "---order->>>" + order.getCreate_by());
					}
				}
				page++;
			}
		}
		return null;
	}

	private void addIdCardCertificationLog(String merchantId, String merchantName, String orderId, String name,
			String idNumber, double fee, int tollFlag, String note, Date date) {

		IdCardCertificationLog idcardLog = new IdCardCertificationLog();
		Map<String, Object> params2 = new HashMap<>();
		params2.put("orderId", orderId);
		params2.put("merchantId", merchantId);
		params2.put("name", name);
		params2.put("idNumber", idNumber);
		List<IdCardCertificationLog> idlist = ikdCardCertificationlogsDao.findByProperty(IdCardCertificationLog.class,
				params2, 0, 0);
		if (idlist != null && !idlist.isEmpty()) {
			System.out.println("------已存在----");
		} else {
			idcardLog.setOrderId(orderId);
			idcardLog.setMerchantId(merchantId);
			idcardLog.setMerchantName(merchantName);
			idcardLog.setName(name);
			idcardLog.setIdNumber(idNumber);
			idcardLog.setFee(fee);
			idcardLog.setTollFlag(tollFlag);
			idcardLog.setNote(note);
			idcardLog.setCreateBy(merchantName);
			idcardLog.setCreateDate(date);
			if (!ikdCardCertificationlogsDao.add(idcardLog)) {
				System.out.println(orderId + "--添加失败-" + name + "====" + idNumber);
			}
		}
	}
}
