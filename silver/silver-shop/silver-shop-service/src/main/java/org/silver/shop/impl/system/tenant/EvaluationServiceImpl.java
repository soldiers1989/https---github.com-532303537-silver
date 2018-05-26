package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.tenant.EvaluationService;
import org.silver.shop.dao.system.tenant.EvaluationDao;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.commerce.OrderGoodsContent;
import org.silver.shop.model.system.log.EvaluationLog;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.EvaluationContent;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.wdFilter.WordFilter;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

import net.sf.json.JSONArray;

@Service(interfaceClass = EvaluationService.class)
public class EvaluationServiceImpl implements EvaluationService {

	@Autowired
	private EvaluationDao evaluationDao;

	@Override
	public Map<String, Object> getInfo(String goodsId, int page, int size) {
		if (StringEmptyUtils.isEmpty(goodsId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("goodsId", goodsId);
		params.put("deleteFlag", 0);
		List<EvaluationContent> reList = evaluationDao.findByCreateDate(EvaluationContent.class, params, page, size);
		long count = evaluationDao.findByPropertyCount(EvaluationContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			List<EvaluationContent> list = new ArrayList<>();
			for (EvaluationContent evaluation : reList) {
				String name = evaluation.getMemberName();
				String topStr = name.substring(0, 1);
				String endStr = name.substring(name.length() - 1, name.length());
				evaluation.setMemberName(topStr + "***" + endStr);
				list.add(evaluation);
			}
			return ReturnInfoUtils.successDataInfo(list, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无评论!");
		}
	}

	@Override
	public Map<String, Object> addInfo(String goodsId, String content, double level, String memberId,
			String memberName) {
		if (StringEmptyUtils.isEmpty(goodsId) || StringEmptyUtils.isEmpty(content) || level == 0) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("entGoodsNo", goodsId);
		List<GoodsRecordDetail> reList = evaluationDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			GoodsRecordDetail goods = reList.get(0);
			goods.getGoodsMerchantId();
			EvaluationContent evaluation = new EvaluationContent();
			evaluation.setGoodsId(goodsId);
			evaluation.setGoodsName(goods.getShelfGName());
			evaluation.setMemberId(memberId);
			evaluation.setMemberName(memberName);
			evaluation.setLevel(level);
			evaluation.setContent(content);
			evaluation.setCreateBy(memberName);
			Calendar oldCalendar = Calendar.getInstance();
			oldCalendar.setTime(DateUtil.parseDate("2018-01-01"));
			Date beginDate = oldCalendar.getTime();
			String randomDate = DateUtil.randomDate(beginDate, new Date());
			evaluation.setCreateDate(DateUtil.parseDate(randomDate, "yyyyMMddHHddss"));
			evaluation.setDeleteFlag(0);
			if (evaluationDao.add(evaluation)) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("保存失败,服务器繁忙!");
		} else {
			return ReturnInfoUtils.errorInfo("未找到商品信息,请核对商品Id!");
		}
	}

	@Override
	public Map<String, Object> randomMember() {
		Long count = evaluationDao.findByPropertyCount(Member.class, null);
		Random rand = new Random();
		List<Member> reList = evaluationDao.findByProperty(Member.class, null, rand.nextInt(count.intValue()), 1);
		if (reList != null && !reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList.get(0));
		}
		return ReturnInfoUtils.errorInfo("随机查询用户信息失败,服务器繁忙!");
	}

	@Override
	public Map<String, Object> addEvaluation(String entOrderNo, String goodsInfoPack, String memberId,
			String memberName, String ipAddress) {
		if (StringEmptyUtils.isEmpty(entOrderNo) || StringEmptyUtils.isEmpty(goodsInfoPack)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		JSONArray jsonArr = null;
		try {
			jsonArr = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			return ReturnInfoUtils.errorInfo("订单商品信息格式错误！");
		}
		List<Map<String, Object>> errorList = new ArrayList<>();
		for (int i = 0; i < jsonArr.size(); i++) {
			Map<String, Object> item = new HashMap<>();
			Map<String, Object> datasMap = (Map<String, Object>) jsonArr.get(i);
			Map<String, Object> params = new HashMap<>();
			String goodsId = datasMap.get("goodsId") + "";
			params.put("entGoodsNo", goodsId);
			List<GoodsRecordDetail> reList = evaluationDao.findByProperty(GoodsRecordDetail.class, params, 0, 0);
			if (reList == null) {
				item.put(BaseCode.STATUS.toString(), goodsId + "查询失败,服务器繁忙!");
				errorList.add(item);
			} else if (!reList.isEmpty()) {
				GoodsRecordDetail goods = reList.get(0);
				datasMap.put("merchantId", goods.getGoodsMerchantId());
				datasMap.put("merchantName", goods.getGoodsMerchantName());
				datasMap.put("ipAddress", ipAddress);
				datasMap.put("memberId", memberId);
				datasMap.put("memberName", memberName);
				datasMap.put("goodsName", goods.getShelfGName());
				if (saveEvaluationContent( datasMap)) {
					// 更新订单商品评论标识
					Map<String, Object> reUpdateMap = updateOrderGoodsFlag(entOrderNo, goodsId);
					if (!"1".equals(reUpdateMap.get(BaseCode.STATUS.toString()))) {
						item.put(BaseCode.STATUS.toString(), reUpdateMap.get(BaseCode.MSG.toString()));
						errorList.add(item);
					}
					//日志类型:1-用户商品评论,2-商户回复评论,3-管理员删除
					datasMap.put("type", "1");
					if(!addEvaluationLog( datasMap)){
						item.put(BaseCode.STATUS.toString(), "保存评论日志失败,服务器繁忙!");
						errorList.add(item);
					}
				} else {
					item.put(BaseCode.STATUS.toString(), goodsId + "保存失败,服务器繁忙!");
					errorList.add(item);
				}
			} else {
				item.put(BaseCode.STATUS.toString(), goodsId + "未找到商品信息!");
				errorList.add(item);
			}
		}
		return ReturnInfoUtils.errorInfo(errorList);
	}

	/**
	 * 保存操作日志
	 * @param datasMap
	 * @return
	 */
	private boolean addEvaluationLog(Map<String, Object> datasMap) {
		if(datasMap == null || datasMap.isEmpty()){
			return false;
		}
		EvaluationLog evaluationLog = new EvaluationLog();
		String goodsId = datasMap.get("goodsId") + "";
		evaluationLog.setGoodsId(goodsId);
		evaluationLog.setGoodsName(datasMap.get("goodsName")+"");
		evaluationLog.setMemberId(datasMap.get("memberId")+"");
		evaluationLog.setMemberName(datasMap.get("memberName")+"");
		evaluationLog.setLevel(Double.parseDouble(datasMap.get("level") + ""));
		String content = datasMap.get("content") + "";
		//敏感字眼标识:0-未识别,1-不包含,2-包含
		if(WordFilter.isContains(content)){
			evaluationLog.setSensitiveFlag(2);
			content = WordFilter.doFilter(content);
		}else{
			evaluationLog.setSensitiveFlag(1);
		}
		evaluationLog.setContent(content);
		evaluationLog.setIpAddresses(datasMap.get("ipAddress")+"");
		//日志类型:1-用户商品评论,2-商户回复评论,3-管理员删除
		evaluationLog.setType(datasMap.get("type")+"");
		evaluationLog.setMerchantId(datasMap.get("merchantId")+"");
		evaluationLog.setMerchantName(datasMap.get("merchantName")+"");
		evaluationLog.setCreateBy(datasMap.get("memberName")+"");
		evaluationLog.setCreateDate(new Date());
		// 保存评论信息
		return evaluationDao.add(evaluationLog);
	}

	/**
	 * 保存商品评论信息
	 * @param goods 商品备案信息
	 * @param datasMap 
	 * @return boolean
	 */
	private boolean saveEvaluationContent( Map<String, Object> datasMap) {
		if(datasMap == null || datasMap.isEmpty()){
			return false;
		}
		EvaluationContent evaluation = new EvaluationContent();
		String goodsId = datasMap.get("goodsId") + "";
		evaluation.setGoodsId(goodsId);
		evaluation.setGoodsName(datasMap.get("goodsName")+"");
		evaluation.setMemberId(datasMap.get("memberId")+"");
		evaluation.setMemberName(datasMap.get("memberName")+"");
		evaluation.setLevel(Double.parseDouble(datasMap.get("level") + ""));
		String content = datasMap.get("content") + "";
		//敏感字眼标识:0-未识别,1-不包含,2-包含
		if(WordFilter.isContains(content)){
			evaluation.setSensitiveFlag(2);
			content = WordFilter.doFilter(content);
		}else{
			evaluation.setSensitiveFlag(1);
		}
		evaluation.setContent(content);
		evaluation.setCreateBy(datasMap.get("memberName")+"");
		evaluation.setCreateDate(new Date());
		evaluation.setDeleteFlag(0);
		evaluation.setIpAddresses(datasMap.get("ipAddress")+"");
		evaluation.setMerchantId(datasMap.get("merchantId")+"");
		evaluation.setMerchantName(datasMap.get("merchantName")+"");
		// 保存评论信息
		return evaluationDao.add(evaluation);
	}

	/**
	 * 根据(海关)订单Id与订单(备案商品自编号)商品Id更新订单商品中评论标识
	 * @param entOrderNo 订单Id
	 * @param goodsId 商品自编号
	 * @return Map
	 */
	private Map<String, Object> updateOrderGoodsFlag(String entOrderNo, String goodsId) {
		if (StringEmptyUtils.isEmpty(entOrderNo) || StringEmptyUtils.isEmpty(goodsId)) {
			return ReturnInfoUtils.errorInfo("请求参数不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		params.put("entOrderNo", entOrderNo);
		params.put("entGoodsNo", goodsId);
		List<OrderGoodsContent> reList = evaluationDao.findByProperty(OrderGoodsContent.class, params, 0, 0);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询订单商品信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			OrderGoodsContent goods = reList.get(0);
			// 评价标识0-未评价,1-已评价
			goods.setEvaluationFlag(1);
			if (evaluationDao.update(goods)) {
				return ReturnInfoUtils.successInfo();
			}
			return ReturnInfoUtils.errorInfo("更新订单商品标识错误,服务器繁忙!");
		}
		return ReturnInfoUtils.errorInfo("未找到订单信息,请重试!");
	}

	@Override
	public Map<String, Object> merchantGetInfo(String goodsName, String memberName, String merchantId, int page, int size) {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(goodsName)) {
			blurryMap.put("goodsName", goodsName);
		}
		if (StringEmptyUtils.isNotEmpty(memberName)) {
			params.put("memberName", memberName);
		}
		if (StringEmptyUtils.isNotEmpty(merchantId)) {
			params.put("merchantId", merchantId);
		}
		List<EvaluationContent> reList = evaluationDao.findByPropertyLike(EvaluationContent.class, params, blurryMap, page,
				size);
		long count = evaluationDao.findByPropertyCount(EvaluationContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询评论信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无评论信息!");
		}
	}

	@Override
	public Map<String, Object> managerDeleteInfo(List<String> idList, String managerId, String managerName) {
		if (idList == null || idList.isEmpty()) {
			return ReturnInfoUtils.errorInfo("请求参数不能未空!");
		}
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> errorList = new ArrayList<>();
		for (String id : idList) {
			Map<String, Object> item = new HashMap<>();
			params.clear();
			params.put("id", Long.parseLong(id));
			List<EvaluationContent> reList = evaluationDao.findByProperty(EvaluationContent.class, params, 0, 0);
			if (reList == null) {
				item.put(BaseCode.MSG.toString(), "流水号[" + id + "]查询评论信息失败,服务器繁忙!");
				errorList.add(item);
			} else if (!reList.isEmpty()) {
				EvaluationContent evaluationContent = reList.get(0);
				// 删除标识:0-未删除,1-已删除
				evaluationContent.setDeleteFlag(1);
				if (!evaluationDao.update(evaluationContent)) {
					item.put(BaseCode.MSG.toString(), "流水号[" + id + "]删除失败,服务器繁忙!");
					errorList.add(item);
				}
				Map<String,Object> datasMap = new HashMap<>();
				datasMap.put("goodsId", evaluationContent.getGoodsId());
				datasMap.put("merchantId", evaluationContent.getMerchantId());
				datasMap.put("merchantName", evaluationContent.getMerchantName());
				datasMap.put("ipAddress", evaluationContent.getIpAddresses());
				datasMap.put("memberId", evaluationContent.getMemberId());
				datasMap.put("memberName", evaluationContent.getMemberName());
				datasMap.put("goodsName", evaluationContent.getGoodsName());
				datasMap.put("level", evaluationContent.getLevel());
				datasMap.put("content", evaluationContent.getContent());
				//日志类型:1-用户商品评论,2-商户回复评论,3-管理员删除
				datasMap.put("type", "3");
				addEvaluationLog(datasMap);
			} else {
				item.put(BaseCode.MSG.toString(), "流水号[" + id + "]未找到对应的评论信息!");
				errorList.add(item);
			}
		}
		return ReturnInfoUtils.errorInfo(errorList);
	}
}
