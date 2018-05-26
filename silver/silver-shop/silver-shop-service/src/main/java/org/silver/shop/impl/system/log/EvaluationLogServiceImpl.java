package org.silver.shop.impl.system.log;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.synth.SynthSpinnerUI;

import org.apache.ibatis.ognl.Evaluation;
import org.silver.shop.api.system.log.EvaluationLogService;
import org.silver.shop.dao.system.log.EvaluationLogDao;
import org.silver.shop.model.system.log.EvaluationLog;
import org.silver.shop.model.system.log.OrderImplLogs;
import org.silver.shop.model.system.tenant.EvaluationContent;
import org.silver.util.IpAddresUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.wdFilter.WordFilter;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = EvaluationLogService.class)
public class EvaluationLogServiceImpl implements EvaluationLogService {

	@Autowired
	private EvaluationLogDao evaluationLogDao;

	@Override
	public Map<String, Object> getlogsInfo(Map<String, Object> datasMap, int page, int size) {
		Map<String, Object> params = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		String type = datasMap.get("type") + "";
		if (StringEmptyUtils.isNotEmpty(type)) {
			params.put("type", type);
		}
		String goodsName = datasMap.get("goodsName") + "";
		if (StringEmptyUtils.isNotEmpty(goodsName)) {
			blurryMap.put("goodsName", goodsName);
		}
		String sensitiveFlag = datasMap.get("sensitiveFlag") + "";
		if (StringEmptyUtils.isNotEmpty(sensitiveFlag)) {
			params.put("sensitiveFlag", Integer.parseInt(sensitiveFlag));
		}
		List<EvaluationLog> reList = evaluationLogDao.findByPropertyLike(EvaluationLog.class, params, blurryMap, page,
				size);
		long count = evaluationLogDao.findByPropertyLikeCount(EvaluationLog.class, params, blurryMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(reList, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无日志信息!");
		}
	}

	@Override
	public Map<String, Object> tempLogs() {
		List<EvaluationContent> reList = evaluationLogDao.findByProperty(EvaluationContent.class, null, 0, 0);
		if (reList != null && !reList.isEmpty()) {
			for (EvaluationContent evaluation : reList) {
				EvaluationLog logs = new EvaluationLog();
				logs.setGoodsId(evaluation.getGoodsId());
				logs.setGoodsName(evaluation.getGoodsName());
				logs.setMemberId(evaluation.getMemberId());
				logs.setMemberName(evaluation.getMemberName());
				logs.setLevel(evaluation.getLevel());
				logs.setSensitiveFlag(evaluation.getSensitiveFlag());
				logs.setContent(evaluation.getContent());
				String ip = evaluation.getIpAddresses();
				if(StringEmptyUtils.isEmpty(ip)){
					ip = IpAddresUtils.getRandomIp();
					logs.setIpAddresses(ip);
					evaluation.setIpAddresses(ip);
					//更新旧日志记录Ip地址
					evaluationLogDao.update(evaluation);
				}else{
					logs.setIpAddresses(ip);
				}
				// 日志类型:1-用户商品评论,2-商户回复评论,3-管理员删除
				logs.setType("1");
				logs.setMerchantId(evaluation.getMemberId());
				logs.setMerchantName(evaluation.getMerchantName());
				logs.setCreateBy(evaluation.getMemberName());
				logs.setCreateDate(evaluation.getCreateDate());
				if(!evaluationLogDao.add(logs)){
					System.out.println("---更新失败-");
				}
				System.out.println("-----更新成功--------");
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
	}
}
