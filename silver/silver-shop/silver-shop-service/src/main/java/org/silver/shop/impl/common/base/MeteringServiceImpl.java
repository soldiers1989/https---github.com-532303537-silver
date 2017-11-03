package org.silver.shop.impl.common.base;

import java.util.List;

import org.silver.shop.api.common.base.MeteringService;
import org.silver.shop.dao.common.base.MeteringDao;
import org.silver.util.JedisUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;


@Service(interfaceClass = MeteringService.class)
public class MeteringServiceImpl implements MeteringService {

	@Autowired
	private MeteringDao meteringDao;

	@Override
	public List<Object> findAllMetering() {
		List<Object> reList = meteringDao.findMetering();
		if (reList != null && reList.size() > 0) {
			JedisUtil.set("shop_metering_Allmetering", 3600, reList);
			return reList;
		}
		return reList;
	}

}
