package org.silver.sys.impl;

import java.util.Map;

import org.silver.sys.api.EBPentRecordService;
import org.silver.sys.dao.EBPentRecordDao;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=EBPentRecordService.class)
public class EBPentRecordImpl implements EBPentRecordService{

	@Autowired
	private EBPentRecordDao eBPentRecordDao;
	
	@Override
	public Object findByProperty(Map<String, Object> params, int page, int size) {
		// TODO Auto-generated method stub
		return eBPentRecordDao.findByProperty(params, page, size);
	}

}
