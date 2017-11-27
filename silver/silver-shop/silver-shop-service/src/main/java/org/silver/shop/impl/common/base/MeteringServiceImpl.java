package org.silver.shop.impl.common.base;

import java.util.List;

import org.silver.shop.api.common.base.MeteringService;
import org.silver.shop.dao.common.base.MeteringDao;
import org.silver.shop.model.common.base.Metering;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;


@Service(interfaceClass = MeteringService.class)
public class MeteringServiceImpl implements MeteringService {

	@Autowired
	private MeteringDao meteringDao;

	@Override
	public List<Object> findAllMetering() {
		return meteringDao.findByProperty(Metering.class, null, 0, 0);
	}

}
