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
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = ErrorLogsService.class)
public class ErrorLogsServiceImpl implements ErrorLogsService {

	@Autowired
	private ErrorLogsDao errorLogsDao;

	@Override
	public Map<String, Object> addErrorLogs(List<Map<String, Object>> errorList, int totalCount, String serialNo,
			String merchantId, String merchantName,String action) {
		System.out.println("---------------开始添加日志---------------");
		if (errorList != null && totalCount >= 0 && StringEmptyUtils.isNotEmpty(serialNo)) {
			String[] strArr = serialNo.split("_");
			for (int i = 0; i < errorList.size(); i++) {
				ErrorLogInfo loginfo = new ErrorLogInfo();
				Map<String, Object> msgMap = errorList.get(i);
				loginfo.setSerialNo(strArr[1]);
				loginfo.setAction(action);
				loginfo.setNote(msgMap.get(BaseCode.MSG.toString()) + "");
				loginfo.setOperationTime(new Date());
				loginfo.setOperator(merchantName);
				loginfo.setOperatorId(merchantId);
				if (!errorLogsDao.add(loginfo)) {
					return ReturnInfoUtils.errorInfo(msgMap.get(BaseCode.MSG.toString()) + ";保存失败,服务器繁忙");
				}
			}
			return ReturnInfoUtils.successInfo();
		}
		return ReturnInfoUtils.errorInfo("请求参数错误,请核对信息是否正确!");
	}

}
