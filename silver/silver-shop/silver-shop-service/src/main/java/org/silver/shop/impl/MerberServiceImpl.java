package org.silver.shop.impl;

import javax.annotation.Resource;

import org.silver.shop.api.MerberService;
import org.silver.shop.dao.MemberDao;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=MerberService.class)
public class MerberServiceImpl implements MerberService{

	@Resource
	private MemberDao memberDao;
	
	@Override
	public Object findAll() {
		return memberDao.findAllCount();
	}

	@Override
	public Object pageFind(int page, int size) {
		return memberDao.findAll(page, size);
	}

}
