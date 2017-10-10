package org.silver.sys.impl;

import java.util.HashMap;
import java.util.Map;

import org.silver.sys.api.LoginService;
import org.silver.sys.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=LoginService.class)
public class LoginServiceImpl implements LoginService {

	@Autowired
	private UserDao userDao;
	
	@Override
	public Map<String, Object> login(String username, String password) {
		// TODO Auto-generated method stub
		Map<String, Object> params = new HashMap<>();
		params.put("account", username);
		params.put("password", password);
		params.put("del_flag", 1);
		
		return null;
	}

}
