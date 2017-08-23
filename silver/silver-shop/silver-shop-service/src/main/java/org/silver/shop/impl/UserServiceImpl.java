package org.silver.shop.impl;

import javax.annotation.Resource;

import org.silver.shop.api.UserService;
import org.silver.shop.dao.UserDao;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=UserService.class)
public class UserServiceImpl implements UserService{

	@Resource
	private UserDao userDao;
	
	@Override
	public Object findAll() {
		return userDao.findAllCount();
	}

	@Override
	public Object pageFind(int page, int size) {
		return userDao.findAll(page, size);
	}

}
