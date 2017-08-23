package org.silver.shop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.shop.api.UserService;
import org.silver.shop.model.User;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;

@Service("testService")
public class TestService {
@Reference
private	UserService userService;

   public Map<String,Object> test(){
	   Map<String,Object> map = new HashMap<String,Object>();
	  
	   List<User> ul=(List<User>) userService.pageFind(1, 5);
	   if(ul!=null&&ul.size()>0){
		   map.put("status", 1);
		   map.put("datas", ul);
		   return map;
	   }
	   map.put("status", -1);
	   return map;
   }

}
