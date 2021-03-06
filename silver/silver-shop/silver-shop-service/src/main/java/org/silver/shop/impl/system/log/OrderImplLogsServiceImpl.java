package org.silver.shop.impl.system.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.log.OrderImplLogsService;
import org.silver.shop.dao.system.log.ErrorLogsDao;
import org.silver.shop.model.system.log.OrderImplLogs;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.util.SearchUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Transform;

import net.sf.json.JSONArray;

@Service(interfaceClass = OrderImplLogsService.class)
public class OrderImplLogsServiceImpl implements OrderImplLogsService {

	@Autowired
	private ErrorLogsDao errorLogsDao;

	@Override
	public Map<String, Object> addErrorLogs(List<Map<String, Object>> errorList, int totalCount, String serialNo,
			String merchantId, String merchantName, String action) {
		if (errorList != null && totalCount >= 0 && StringEmptyUtils.isNotEmpty(serialNo)) {
			System.out.println("---------------开始添加日志---------------");
			String[] strArr = serialNo.split("_");
			for (int i = 0; i < errorList.size(); i++) {
				OrderImplLogs loginfo = new OrderImplLogs();
				Map<String, Object> errorMap = errorList.get(i);
				String msg = errorMap.get(BaseCode.MSG.toString()) + "";
				String type = errorMap.get("type") + "";
				loginfo.setType(type);
				loginfo.setSerialNo(strArr[1]);
				loginfo.setAction(action);
				loginfo.setNote(msg);
				loginfo.setCreateDate(new Date());
				loginfo.setCreateBy(merchantName);
				loginfo.setOperatorId(merchantId);
				// 阅读标识:1-未阅读,2-已阅读
				loginfo.setReadingSign(1);
				if (!errorLogsDao.add(loginfo)) {
					System.out.println(msg + " 保存失败,服务器繁忙");
				}
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("请求参数错误,请核对信息是否正确!");
	}

	@Override
	public Object merchantGetErrorLogs(Map<String, Object> params, int page, int size) {
		Map<String, Object> reDatasMap = SearchUtils.universalOrderImplLogSearch(params);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		Map<String, Object> blurryMap =  (Map<String, Object>) reDatasMap.get("blurry");
		long count = 0;
		if (page == 0 && size == 0) {
			paramMap.put("readingSign", 1);
			count = errorLogsDao.findByPropertyLikeCount(OrderImplLogs.class, paramMap, blurryMap);
			return ReturnInfoUtils.successDataInfo("", count);
		}
		List<OrderImplLogs> reList = errorLogsDao.findByPropertyLike(OrderImplLogs.class, paramMap, blurryMap, page,
				size);
		count = errorLogsDao.findByPropertyLikeCount(OrderImplLogs.class, paramMap, blurryMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			//增加缓存集合，用于修改状态前传递给前台
			JSONArray cacheList = JSONArray.fromObject(reList);
			for (OrderImplLogs logInfo : reList) {
				// 更新阅读标识
				logInfo.setReadingSign(2);
				if (!errorLogsDao.update(logInfo)) {
					return ReturnInfoUtils.errorInfo("更新阅读标识失败,服务器繁忙!");
				}
			}
			return ReturnInfoUtils.successDataInfo(cacheList, count);
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}
}
