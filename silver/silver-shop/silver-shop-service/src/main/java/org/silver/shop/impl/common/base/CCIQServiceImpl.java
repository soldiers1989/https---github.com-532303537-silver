package org.silver.shop.impl.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.CCIQService;
import org.silver.shop.dao.common.base.CCIQDao;
import org.silver.shop.model.common.base.CCIQ;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = CCIQService.class)
public class CCIQServiceImpl implements CCIQService {

	@Autowired
	private CCIQDao cciqDao;
	
	@Override
	public Map<String, Object> getCCIQInfo() {
		Map<String, Object> reMap = new HashMap<>();
		List<CCIQ> dataList = cciqDao.findByProperty(CCIQ.class, null, 0, 0);
		if (dataList != null && !dataList.isEmpty()) {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reMap.put(BaseCode.DATAS.toString(), dataList);
			reMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			return reMap;
		} else {
			return ReturnInfoUtils.errorInfo("查询海关信息失败,服务器繁忙!");
		}
	}

}
