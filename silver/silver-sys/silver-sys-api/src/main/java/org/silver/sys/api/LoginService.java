package org.silver.sys.api;

import java.util.Map;

public interface LoginService {
    /**
     * 用户登录验证
     * @param username  用户名
     * @param password  密码
     * @return
     */
	public Map<String,Object> login(String username,String password);
}
