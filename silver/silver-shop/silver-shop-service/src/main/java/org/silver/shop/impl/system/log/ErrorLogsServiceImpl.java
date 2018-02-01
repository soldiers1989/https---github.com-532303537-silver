package org.silver.shop.impl.system.log;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.log.ErrorLogsService;
import org.silver.shop.dao.system.log.ErrorLogsDao;
import org.silver.shop.model.system.log.ErrorLogInfo;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.util.SearchUtils;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Transform;

@Service(interfaceClass = ErrorLogsService.class)
public class ErrorLogsServiceImpl implements ErrorLogsService {

	@Autowired
	private ErrorLogsDao errorLogsDao;

	@Override
	public Map<String, Object> addErrorLogs(List<Map<String, Object>> errorList, int totalCount, String serialNo,
			String merchantId, String merchantName, String action) {
		System.out.println("---------------开始添加日志---------------");
		if (errorList != null && totalCount >= 0 && StringEmptyUtils.isNotEmpty(serialNo)) {
			String[] strArr = serialNo.split("_");
			for (int i = 0; i < errorList.size(); i++) {
				ErrorLogInfo loginfo = new ErrorLogInfo();
				Map<String, Object> errorMap = errorList.get(i);
				String msg = errorMap.get(BaseCode.MSG.toString()) + "";
				String type = errorMap.get("type") + "";
				if (StringEmptyUtils.isNotEmpty(type)) {
					// 类型：1-错误,2-警告订单超额,3-详细地址信息错误...待续
					loginfo.setType(Integer.parseInt(type));
				}
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
	public Object merchantGetErrorLogs(Map<String, Object> params, int page, int size, String merchantId,
			String merchantName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> blurryMap = new HashMap<>();
		String blurryStr = "";
		if (StringEmptyUtils.isNotEmpty(params.get("blurryStr") + "")) {
			blurryStr = params.get("blurryStr") + "";
		}
		Map<String, Object> reDatasMap = SearchUtils.universalSearch(params);
		Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
		blurryMap.put("action", blurryStr);
		paramMap.put("operatorId", merchantId);
		long count = 0;
		if (page == 0 && size == 0) {
			paramMap.put("readingSign", 1);
			count = errorLogsDao.findByPropertyLikeCount(ErrorLogInfo.class, paramMap, blurryMap);
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.TOTALCOUNT.toString(), count);
			return statusMap;
		}
		List<ErrorLogInfo> reList = errorLogsDao.findByPropertyLike(ErrorLogInfo.class, paramMap, blurryMap, page,
				size);
		count = errorLogsDao.findByPropertyLikeCount(ErrorLogInfo.class, paramMap, blurryMap);
		if (reList == null) {
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		} else if (!reList.isEmpty()) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), count);
			for (ErrorLogInfo logInfo : reList) {// 更新阅读标识
				logInfo.setReadingSign(2);
				if (!errorLogsDao.update(logInfo)) {
					return ReturnInfoUtils.errorInfo("更新阅读标识失败,服务器繁忙!");
				}
			}
			return statusMap;
		} else {
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}
}
