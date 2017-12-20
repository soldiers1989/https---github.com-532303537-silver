package org.silver.shop.impl.common.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.PostalService;
import org.silver.shop.dao.common.base.PostalDao;
import org.silver.shop.model.common.base.Postal;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = PostalService.class)
public class PostalServiceImpl implements PostalService {

	@Autowired
	private PostalDao postalDao;
	
	@Override
	public Map<String, Object> getPostal() {

		Map<String, Object> reMap = new HashMap<>();
		List<Object> dataList = postalDao.findAll(Postal.class, 0, 0);
		if (dataList != null && dataList.size() > 0) {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			reMap.put(BaseCode.DATAS.toString(), dataList);
			reMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		} else {
			reMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			reMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
		}
		return reMap;
	}

}
