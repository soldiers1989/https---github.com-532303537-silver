package org.silver.sys.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.sys.api.LoginService;
import org.silver.sys.dao.UserDao;
import org.silver.sys.model.User;
import org.silver.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;



@Service(interfaceClass=LoginService.class)
public class LoginServiceImpl implements LoginService {

	@Autowired
	private UserDao userDao;
	
	@Override
	public Map<String, Object> login(String username, String password) {
		Map<String,Object>  reqMap = new HashMap<String,Object>();
		Map<String, Object> params = new HashMap<>();
		try {
			password =MD5.getMD5(password.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		params.put("account", username);
		params.put("password", password);
		params.put("del_flag", 0);
		List<User> list=userDao.findByProperty(params, 1, 1);
		if(null!=list&&list.size()>0){
			reqMap.put("status", 1);
			reqMap.put("success", "登录成功");
			return reqMap;
		}
		reqMap.put("status", -1);
		reqMap.put("msg", "账号不存在或密码输入有误");
		return reqMap;
	}

}
