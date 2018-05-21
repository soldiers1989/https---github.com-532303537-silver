package org.silver.shop.impl.system.tenant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.silver.shop.api.system.tenant.EvaluationService;
import org.silver.shop.dao.system.tenant.EvaluationDao;
import org.silver.shop.model.system.commerce.GoodsRecordDetail;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.EvaluationContent;
import org.silver.util.DateUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

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
	public Map<String, Object> addEvaluation(String goodsId, String content, double level, String memberId,
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
			EvaluationContent evaluation = new EvaluationContent();
			evaluation.setGoodsId(goodsId);
			evaluation.setGoodsName(goods.getShelfGName());
			evaluation.setMemberId(memberId);
			evaluation.setMemberName(memberName);
			evaluation.setLevel(level);
			evaluation.setContent(content);
			evaluation.setCreateBy(memberName);
			evaluation.setCreateDate(new Date());
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
	public Map<String, Object> merchantGetInfo(String goodsName, String memberName, String merchantId) {
		if(StringEmptyUtils.isEmpty(merchantId)){
			return ReturnInfoUtils.errorInfo("商户Id不能为空!");
		}
		Map<String, Object> params = new HashMap<>();
		if (StringEmptyUtils.isNotEmpty(goodsName)) {
			params.put("goodsName", goodsName);
		}
		if (StringEmptyUtils.isNotEmpty(memberName)) {
			params.put("memberName", memberName);
		}
		params.put("merchantId", merchantId);
		List<EvaluationContent> reList = evaluationDao.findByProperty(EvaluationContent.class, params, 0, 0);
		long count = evaluationDao.findByPropertyCount(EvaluationContent.class, params);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("商户查询评论信息失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无评论信息!");
		}
	}
}
