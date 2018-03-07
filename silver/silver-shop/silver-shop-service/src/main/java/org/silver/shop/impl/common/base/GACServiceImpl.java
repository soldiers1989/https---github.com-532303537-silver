package org.silver.shop.impl.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.GACService;
import org.silver.shop.dao.common.base.GACDao;
import org.silver.shop.model.common.base.CustomsPort;
import org.silver.shop.model.common.base.GAC;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = GACService.class)
public class GACServiceImpl implements GACService {

	@Autowired
	private GACDao gacDao;

	@Override
	public Map<String, Object> getGACInfo() {
		Map<String, Object> reMap = new HashMap<>();
		List<GAC> dataList = gacDao.findByProperty(GAC.class, null, 0, 0);
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
