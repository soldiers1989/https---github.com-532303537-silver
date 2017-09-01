package org.silver.shop.impl;

import javax.annotation.Resource;

import org.silver.shop.api.MemberService;
import org.silver.shop.dao.MemberDao;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=MemberService.class)
public class MerberServiceImpl implements MemberService{

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
